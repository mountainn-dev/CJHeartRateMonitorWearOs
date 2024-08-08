package com.san.heartratemonitorwearos.data.repositoryimpl

import android.location.Location
import android.util.Log
import com.san.heartratemonitorwearos.data.Result
import com.san.heartratemonitorwearos.data.entity.HeartRateEntity
import com.san.heartratemonitorwearos.data.entity.UrgentEntity
import com.san.heartratemonitorwearos.data.entity.WorkStatusEntity
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService

class HeartRateRepositoryImpl(
    private val service: HeartRateService
) : HeartRateRepository {
    override suspend fun updateWorkNow(userId: String): Result<Boolean> {
        try {
            service.updateWorkNow(WorkStatusEntity(userId))
            return Result.success(true)
        } catch (e: Exception) {
            Log.e("updateWorkNowException", e.toString())
            return Result.error(e)
        }
    }

    override suspend fun urgent(
        location: Location,
        lastHeartRate: Int
    ): Result<Boolean> {
        try {
            service.receiveReport(UrgentEntity(lastHeartRate, location.longitude.toFloat(), location.latitude.toFloat()))
            return Result.success(true)
        } catch (e: Exception) {
            Log.e("urgentException", "error")
            return Result.error(e)
        }
    }
}