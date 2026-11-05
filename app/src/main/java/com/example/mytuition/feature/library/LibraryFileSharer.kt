package com.example.mytuition.feature.library

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.webkit.MimeTypeMap
import com.example.mytuition.core.data.network.PocketBaseApi
import com.example.mytuition.core.domain.model.Resource
import com.example.mytuition.core.domain.model.ResourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URI
import com.example.mytuition.core.data.network.PocketBaseClient

object LibraryFileSharer {

    suspend fun share(
        context: Context,
        api: PocketBaseApi,
        resource: Resource
    ) {
        val fileUrl = resource.url
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("This resource does not have a file URL.")

        ensureTrustedPocketBaseUrl(fileUrl)

        val appContext = context.applicationContext

        val downloadedFile = withContext(Dispatchers.IO) {
            val directory = File(appContext.cacheDir, "shared_resources")
            if (!directory.exists() && !directory.mkdirs()) {
                throw IOException("Could not create the resource cache directory.")
            }

            val extension = getExtension(fileUrl, resource.type)
            val safeTitle = resource.title
                .replace(Regex("[^A-Za-z0-9._-]"), "_")
                .trim('_', '.')
                .take(60)
                .ifBlank { "resource" }

            val destination = File(
                directory,
                "${safeTitle}_${resource.id.take(12)}.$extension"
            )

            try {
                val response = api.downloadResource(fileUrl)

                if (!response.isSuccessful) {
                    throw IOException(
                        "Download failed: HTTP ${response.code()} ${response.message()}"
                    )
                }

                val body = response.body()
                    ?: throw IOException("The server returned an empty file.")

                body.byteStream().use { input ->
                    destination.outputStream().buffered().use { output ->
                        input.copyTo(output)
                    }
                }

                if (!destination.exists() || destination.length() == 0L) {
                    throw IOException("The downloaded file is empty.")
                }

                destination
            } catch (exception: Exception) {
                destination.delete()
                throw exception
            }
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            downloadedFile
        )

        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(downloadedFile.extension.lowercase())
            ?: "application/octet-stream"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            clipData = ClipData.newUri(
                context.contentResolver,
                downloadedFile.name,
                contentUri
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share resource")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    private fun ensureTrustedPocketBaseUrl(fileUrl: String) {
        val requestedUri = try {
            URI(fileUrl)
        } catch (_: Exception) {
            throw IOException("The resource URL is invalid.")
        }

        val trustedUri = URI(PocketBaseClient.DEFAULT_BASE_URL)

        fun effectivePort(uri: URI): Int {
            if (uri.port != -1) return uri.port

            return when (uri.scheme?.lowercase()) {
                "https" -> 443
                "http" -> 80
                else -> -1
            }
        }

        val isTrusted =
            requestedUri.userInfo == null &&
            requestedUri.scheme?.equals(trustedUri.scheme, ignoreCase = true) == true &&
            requestedUri.host?.equals(trustedUri.host, ignoreCase = true) == true &&
            effectivePort(requestedUri) == effectivePort(trustedUri)

        if (!isTrusted) {
            throw IOException("Sharing is blocked for an untrusted resource URL.")
        }
    }

    private fun getExtension(
        fileUrl: String,
        type: ResourceType
    ): String {
        val path = Uri.parse(fileUrl).lastPathSegment.orEmpty()
        val extension = path.substringAfterLast('.', "")
            .lowercase()
            .takeIf { it.matches(Regex("[a-z0-9]{1,8}")) }

        return extension ?: when (type) {
            ResourceType.PDF -> "pdf"
            ResourceType.IMAGE -> "jpg"
            ResourceType.VIDEO -> "mp4"
            ResourceType.DOCUMENT -> "doc"
            else -> "bin"
        }
    }
}
