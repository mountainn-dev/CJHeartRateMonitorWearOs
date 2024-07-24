package com.san.heartratemonitorwearos.domain.utils

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
        .addInterceptor(HeaderInterceptor("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MTIxMiIsImlzcyI6ImVsbGlvdHRfa2ltIiwiZXhwIjoxNzUzMjg4MDA5LCJpYXQiOjE3MjE3NTIwMDl9.8t8FqLRkeUO1fWTLO9Ucbc20GwusxEhx7CmOWYgTktg"))
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    fun checkPermission(
        permission: String, activity: Activity
    ) = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
}