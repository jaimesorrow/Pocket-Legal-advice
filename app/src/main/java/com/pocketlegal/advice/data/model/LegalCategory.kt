package com.pocketlegal.advice.data.model

data class LegalCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val lawCount: Int = 0
)
