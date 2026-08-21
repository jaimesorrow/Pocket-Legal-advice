package com.pocketlegal.advice.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pocketlegal.advice.data.local.dao.ActionStepDao
import com.pocketlegal.advice.data.local.entity.ActionStepEntity
import com.pocketlegal.advice.data.local.entity.ActionStepsConverter

@Database(
    entities = [ActionStepEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ActionStepsConverter::class)
abstract class LegalAdviceDatabase : RoomDatabase() {
    abstract fun actionStepDao(): ActionStepDao

    companion object {
        @Volatile
        private var INSTANCE: LegalAdviceDatabase? = null

        fun getDatabase(context: Context): LegalAdviceDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, LegalAdviceDatabase::class.java, "legal_advice_db")
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
