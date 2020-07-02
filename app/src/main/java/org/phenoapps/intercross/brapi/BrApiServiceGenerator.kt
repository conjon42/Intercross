package org.phenoapps.intercross.brapi

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class BrApiServiceGenerator {

    companion object {

        val API_BASE_URL = "https://test-server.brapi.org/brapi/v2/"

        val client = OkHttpClient.Builder()

        val builder = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())

        fun <T> createService(serviceClass: Class<T>, authToken: String): T {

            val retrofit: Retrofit.Builder = Retrofit.Builder()

            if (authToken.isNotEmpty()) {

                val interceptor = AuthInterceptor(authToken)

                if (interceptor !in client.interceptors()) {

                    client.addInterceptor(interceptor)

                    return builder.client(client.build())
                            .baseUrl(API_BASE_URL)
                            .build()
                            .create(serviceClass)

                }
            }

            return retrofit.build().create(serviceClass)

        }
    }
}