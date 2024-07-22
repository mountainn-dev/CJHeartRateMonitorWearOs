package com.san.heartratemonitorwearos.data.entity

data class ServiceResponse<T>(
    val message: String,
    val data: T
)
