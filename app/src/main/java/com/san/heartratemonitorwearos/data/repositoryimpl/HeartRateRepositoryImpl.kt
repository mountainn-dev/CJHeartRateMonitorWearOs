package com.san.heartratemonitorwearos.data.repositoryimpl

import android.location.Location
import android.util.Log
import com.san.heartratemonitorwearos.data.Result
import com.san.heartratemonitorwearos.data.entity.HeartRateEntity
import com.san.heartratemonitorwearos.data.repository.HeartRateRepository
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService

class HeartRateRepositoryImpl(
    private val service: HeartRateService
) : HeartRateRepository {
    override suspend fun urgent(
        location: Location,
        lastHeartRate: HeartRateEntity
    ): Result<Boolean> {
        // TODO("Not yet implemented")
        return Result.success(true)
    }
}