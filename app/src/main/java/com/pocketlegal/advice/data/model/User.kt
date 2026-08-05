package com.pocketlegal.advice.data.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
