package com.pocketlawbook.alaska.data.local.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pocketlawbook.alaska.data.local.VerifiedContentSeed

/**
 * The app's local database. Its only table today is [ActionStepRow] — the
 * verified-content store the zero-hallucination pipeline reads from.
 *
 * Seeded once, on first creation of the database file, from
 * [VerifiedContentSeed]. That seed is NOT yet attorney-reviewed content (see
 * the warning on [VerifiedContentSeed]); this callback exists to make the
 * Home → Analysis → Action steps slice runnable end to end today, and is the
 * exact place a real content-publishing pipeline would replace with rows from
 * a reviewed, versioned dataset instead of the in-source seed.
 */
@Database(entities = [ActionStepRow::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PocketLawbookDatabase : RoomDatabase() {

    internal abstract fun actionStepDao(): ActionStepRoomDao

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            VerifiedContentSeed.entries.values.forEach { entry ->
                val values = ContentValues().apply {
                    put("violation_key", entry.violationKey)
                    put("action_steps", Converters.fromActionStepsList(entry.actionSteps))
                    put("description", entry.description)
                    put("jurisdiction", Converters.fromJurisdiction(entry.jurisdiction))
                }
                db.insert("action_steps", SQLiteDatabase.CONFLICT_REPLACE, values)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: PocketLawbookDatabase? = null

        fun getInstance(context: Context): PocketLawbookDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PocketLawbookDatabase::class.java,
                    "pocket_lawbook.db"
                )
                    .addCallback(SeedCallback())
                    .build()
                    .also { instance = it }
            }
    }
}
