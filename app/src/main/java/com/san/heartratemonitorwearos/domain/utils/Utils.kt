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
    fun getRetrofit(baseUrl: String, idToken: String?) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(getClientWithOrWithoutIdToken(idToken))
        .build()

    private fun getClientWithOrWithoutIdToken(token: String?): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)

        if (token == null) return builder.build()

        builder.addInterceptor(HeaderInterceptor(token))
        return builder.build()
    }

    fun checkPermission(
        permission: String, activity: Activity
    ) = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
}