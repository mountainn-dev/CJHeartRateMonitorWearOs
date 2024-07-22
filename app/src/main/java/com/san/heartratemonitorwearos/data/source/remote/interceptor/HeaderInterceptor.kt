package com.san.heartratemonitorwearos.data.source.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
    private val token: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val currentRequest = chain.request().newBuilder()
        currentRequest.addHeader(ID_TOKEN, token)

        val newRequest = currentRequest.build()
        return chain.proceed(newRequest)
    }

    companion object {
        private const val ID_TOKEN = "id token"
    }
}