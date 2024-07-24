package com.san.heartratemonitorwearos.data.entity

import com.google.gson.annotations.SerializedName

data class UrgentEntity(
    @SerializedName("reportHeartRate") val lastHeartRate: Int,
    @SerializedName("locationXPos") val longitude: Float,
    @SerializedName("locationYPos") val latitude: Float
)
