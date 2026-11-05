package com.example.mytuition.core.data.repository

import com.example.mytuition.core.data.local.AppDatabase
import com.example.mytuition.core.data.local.FeatureCacheEntity
import com.example.mytuition.core.data.network.PocketBaseApi
import com.example.mytuition.core.data.network.PocketBaseClient
import com.example.mytuition.core.domain.model.Resource
import com.example.mytuition.core.domain.model.ResourceType
import com.example.mytuition.core.domain.repository.LibraryRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class PocketBaseLibraryRepository(
    private val api: PocketBaseApi,
    private val database: AppDatabase? = null
) : LibraryRepository {

    private val cacheKey = "library_resources"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(
        List::class.java,
        Resource::class.java
    )

    private val adapter = moshi.adapter<List<Resource>>(listType)

    override suspend fun getResources(): Result<List<Resource>> {
        return try {
            val response = api.getResources(
                sort = "-created",
                perPage = 50
            )

            if (response.isSuccessful) {
                val records = response.body()?.items

                if (records != null) {
                    val resources = records.map { record ->
                        val resourceType = when (record.type?.uppercase()) {
                            "PDF" -> ResourceType.PDF
                            "IMAGE" -> ResourceType.IMAGE
                            "VIDEO" -> ResourceType.VIDEO
                            "LINK" -> ResourceType.LINK
                            "DOCUMENT" -> ResourceType.DOCUMENT
                            else -> ResourceType.OTHER
                        }

                        val fileUrl = record.file
                            ?.takeIf { it.isNotBlank() }
                            ?.let {
                                "${PocketBaseClient.DEFAULT_BASE_URL}files/resources/${record.id}/$it"
                            }

                        Resource(
                            id = record.id,
                            title = record.title
                                ?.takeIf { it.isNotBlank() }
                                ?: "Untitled resource",
                            type = resourceType,
                            url = fileUrl,
                            sizeBytes = record.size?.takeIf { it > 0L },
                            subject = record.subject,
                            batch = record.batch
                        )
                    }

                    // Cache the latest successful response.
                    try {
                        val json = adapter.toJson(resources)
                        database?.featureCacheDao()?.saveCache(
                            FeatureCacheEntity(
                                key = cacheKey,
                                dataJson = json
                            )
                        )
                    } catch (exception: kotlinx.coroutines.CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        // A cache failure must not discard valid network data.
                    }

                    Result.success(resources)
                } else {
                    getCachedResources()
                        ?: Result.failure(
                            Exception("The resources response was empty.")
                        )
                }
            } else {
                getCachedResources()
                    ?: Result.failure(
                        Exception(
                            "Failed to fetch resources: HTTP ${response.code()} ${response.message()}"
                        )
                    )
            }
        } catch (exception: kotlinx.coroutines.CancellationException) {
            throw exception
        } catch (exception: Exception) {
            getCachedResources() ?: Result.failure(exception)
        }
    }

    private suspend fun getCachedResources(): Result<List<Resource>>? {
        return try {
            val cached = database
                ?.featureCacheDao()
                ?.getCache(cacheKey)

            if (cached == null || cached.dataJson.isBlank()) {
                null
            } else {
                val resources = adapter.fromJson(cached.dataJson)
                    ?: return null

                Result.success(
                    resources.map { it.copy(isFromCache = true) }
                )
            }
        } catch (exception: kotlinx.coroutines.CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }
    }
}
