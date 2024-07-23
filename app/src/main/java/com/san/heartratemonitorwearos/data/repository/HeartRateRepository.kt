package com.san.heartratemonitorwearos.data.repository

import android.location.Location
import com.san.heartratemonitorwearos.data.Result
import com.san.heartratemonitorwearos.data.entity.HeartRateEntity

interface HeartRateRepository {
    suspend fun urgent(location: Location, lastHeartRate: HeartRateEntity): Result<Boolean>
}