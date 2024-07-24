package com.san.heartratemonitorwearos.data.source.remote.retrofit

import com.san.heartratemonitorwearos.data.entity.HeartRateEntity
import com.san.heartratemonitorwearos.data.entity.ServiceResponse
import com.san.heartratemonitorwearos.data.entity.UrgentEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface HeartRateService {
    @POST("/setHeartRate")
    suspend fun setHeartRate(
        @Body data: HeartRateEntity
    ): ServiceResponse<String?>

    @POST("/receiveReport")
    suspend fun receiveReport(
        @Body data: UrgentEntity
    )
}