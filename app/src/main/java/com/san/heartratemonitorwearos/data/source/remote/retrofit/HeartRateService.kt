package com.san.heartratemonitorwearos.data.source.remote.retrofit

import com.san.heartratemonitorwearos.data.entity.HeartRateEntity
import com.san.heartratemonitorwearos.data.entity.ServiceResponse
import com.san.heartratemonitorwearos.data.entity.UpdateWorkEntity
import com.san.heartratemonitorwearos.data.entity.UrgentEntity
import retrofit2.http.Body
import retrofit2.http.POST

interface HeartRateService {
    @POST("/updateWorkNow")
    suspend fun updateWorkNow(
        @Body data: UpdateWorkEntity
    )

    @POST("/receiveReport")
    suspend fun receiveReport(
        @Body data: UrgentEntity
    )
}