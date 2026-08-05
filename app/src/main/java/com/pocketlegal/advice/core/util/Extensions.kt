package com.pocketlegal.advice.core.util

import com.pocketlegal.advice.core.network.NetworkResult
import retrofit2.Response

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> =
    try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) NetworkResult.Success(body)
            else NetworkResult.Error("Empty response body", response.code())
        } else {
            NetworkResult.Error(
                response.errorBody()?.string() ?: "Unknown error",
                response.code()
            )
        }
    } catch (e: Exception) {
        NetworkResult.Error(e.localizedMessage ?: "Network error")
    }
