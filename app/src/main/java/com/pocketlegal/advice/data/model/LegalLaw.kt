package com.pocketlegal.advice.data.model

data class LegalLaw(
    val id: String,
    val categoryId: String,
    val title: String,
    val code: String,
    val summary: String,
    val fullText: String,
    val tags: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
