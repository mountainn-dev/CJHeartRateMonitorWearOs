package com.san.heartratemonitorwearos.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.san.heartratemonitorwearos.data.source.remote.interceptor.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Utils {
    fun getRetrofit(baseUrl: String) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getClient())
        .build()

    private fun getClient() = OkHttpClient().newBuilder()
        .addInterceptor(HeaderInterceptor("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqZXJlbXkiLCJpc3MiOiJlbGxpb3R0X2tpbSIsImV4cCI6MTc1NDQ3Njg4NSwiaWF0IjoxNzIyOTQwODg1fQ.JIFhCVtYzQzUlCkHUqo7vDT7Yp2Zn1fANqhRm9DX3mQ"))
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    fun checkPermission(
        permission: String, activity: Activity
    ) = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
}