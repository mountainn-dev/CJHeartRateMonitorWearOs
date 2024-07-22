package com.san.heartratemonitorwearos.data.repository

import com.san.heartratemonitorwearos.data.Result

interface HeartRateRepository {
    suspend fun setHeartRate(heartRate: Int): Result<Boolean>
}