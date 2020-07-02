package org.phenoapps.intercross.brapi

import okhttp3.Interceptor
import okhttp3.Response

/***
 * Token authentication using okhttp3/retrofit libraries
 * Interceptor is a wrapper that adds the authentication token to response chains.
 */
data class AuthInterceptor(private val token: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        return chain.proceed(chain
                .request()
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build())

    }

}