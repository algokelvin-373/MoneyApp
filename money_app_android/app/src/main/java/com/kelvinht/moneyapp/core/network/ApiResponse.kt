package com.kelvinht.moneyapp.core.network

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val error_code: String?
)
