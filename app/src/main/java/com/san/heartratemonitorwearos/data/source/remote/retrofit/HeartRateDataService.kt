package com.san.heartratemonitorwearos.data.source.remote.retrofit

import com.san.heartratemonitorwearos.data.entity.HeartRateEntity
import com.san.heartratemonitorwearos.data.entity.ServiceResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface HeartRateDataService {
    @POST("/setHeartRate")
    suspend fun setHeartRate(
        @Body entity: HeartRateEntity
    )
}