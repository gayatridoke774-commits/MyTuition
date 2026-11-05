package com.example.mytuition.core.data.network

import com.example.mytuition.core.data.local.TokenManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

object SessionEvents {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun emitSessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}

class AuthInterceptor(
    private val tokenManager: TokenManager,
    baseUrl: String = PocketBaseClient.DEFAULT_BASE_URL
) : Interceptor {

    private val trustedBaseUrl = okhttp3.HttpUrl.get(baseUrl)

    private fun isTrustedRequest(url: HttpUrl): Boolean {
        return url.scheme == trustedBaseUrl.scheme &&
            url.host == trustedBaseUrl.host &&
            url.port == trustedBaseUrl.port
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val isTrusted = isTrustedRequest(originalRequest.url)
        val requestBuilder = originalRequest.newBuilder()

        if (isTrusted) {
            val token = runBlocking {
                try {
                    tokenManager.getTokenSync()
                } catch (_: Exception) {
                    null
                }
            }

            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        val response = chain.proceed(requestBuilder.build())

        if (
            isTrusted &&
            response.code == 401 &&
            !originalRequest.url.encodedPath.contains("auth-with-password")
        ) {
            runBlocking {
                try {
                    tokenManager.clearSession()
                } catch (_: Exception) {}
            }
            SessionEvents.emitSessionExpired()
        }

        return response
    }
}
