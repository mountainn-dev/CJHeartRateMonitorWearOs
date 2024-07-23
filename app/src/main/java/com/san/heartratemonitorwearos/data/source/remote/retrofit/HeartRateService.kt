package com.san.heartratemonitorwearos.data.source.remote.retrofit

import com.san.heartratemonitorwearos.data.entity.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface HeartRateService {
    @POST("/setHeartRate")
    suspend fun setHeartRate(
        @Body data: Int
    ): ServiceResponse<String?>
}