package com.san.heartratemonitorwearos.data.repository

import android.location.Location
import com.san.heartratemonitorwearos.data.Result
import com.san.heartratemonitorwearos.data.entity.HeartRateEntity
import com.san.heartratemonitorwearos.data.entity.UrgentEntity

interface HeartRateRepository {
    suspend fun updateWorkNow(): Result<Boolean>
    suspend fun urgent(location: Location, lastHeartRate: Int): Result<Boolean>
}