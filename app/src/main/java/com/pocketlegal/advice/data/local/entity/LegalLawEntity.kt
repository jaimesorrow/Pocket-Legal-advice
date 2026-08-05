package com.pocketlegal.advice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legal_laws")
data class LegalLawEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val title: String,
    val code: String,
    val summary: String,
    val fullText: String,
    val tags: String,
    val lastUpdated: Long
)
