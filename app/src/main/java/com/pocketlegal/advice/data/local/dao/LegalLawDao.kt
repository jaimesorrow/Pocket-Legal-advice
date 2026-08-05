package com.pocketlegal.advice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pocketlegal.advice.data.local.entity.LegalLawEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LegalLawDao {

    @Query("SELECT * FROM legal_laws WHERE categoryId = :categoryId ORDER BY title ASC")
    fun getLawsByCategory(categoryId: String): Flow<List<LegalLawEntity>>

    @Query("SELECT * FROM legal_laws WHERE title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    fun searchLaws(query: String): Flow<List<LegalLawEntity>>

    @Query("SELECT * FROM legal_laws WHERE id = :id")
    suspend fun getLawById(id: String): LegalLawEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(laws: List<LegalLawEntity>)

    @Query("DELETE FROM legal_laws WHERE categoryId = :categoryId")
    suspend fun deleteByCategoryId(categoryId: String)
}
