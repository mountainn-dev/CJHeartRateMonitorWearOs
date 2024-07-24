package com.san.heartratemonitorwearos.data

abstract class Result<T> {
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun <T> error(error: Exception): Result<T> = Error(error)
    }
}

class Success <T> (val data: T) : Result<T>()

class Error <T> (private val error: Exception) : Result<T>() {
    fun message(): String {
        return error.message ?: error.toString()
    }
}