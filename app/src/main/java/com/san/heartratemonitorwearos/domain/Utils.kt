package com.san.heartratemonitorwearos.domain

import com.san.heartratemonitorwearos.data.source.remote.interceptor.HeaderInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Utils {
    fun getRetrofit(token: String) = Retrofit.Builder()
        .baseUrl("http://43.203.200.27:8081")
        .addConverterFactory(GsonConverterFactory.create())
        .client(getClient(token))
        .build()

    private fun getClient(token: String) = OkHttpClient().newBuilder()
        .addInterceptor(HeaderInterceptor(token))
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()
}