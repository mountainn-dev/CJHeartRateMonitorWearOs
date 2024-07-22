package com.san.heartratemonitorwearos.data.repositoryimpl

import android.util.Log
import com.san.heartratemonitorwearos.data.Result
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService

class HeartRateRepositoryImpl(
    private val service: HeartRateService
) : HeartRateRepository {
    override suspend fun setHeartRate(heartRate: Int): Result<Boolean> {
        try {
            service.setHeartRate(heartRate)
            return Result.success(true)
        } catch (e: Exception) {
            Log.d("setHeartRateException", e.toString())
            return Result.error(e)
        }
    }
}