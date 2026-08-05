package com.pocketlegal.advice.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pocketlegal.advice.core.util.Constants
import com.pocketlegal.advice.data.local.dao.ChatMessageDao
import com.pocketlegal.advice.data.local.dao.LegalLawDao
import com.pocketlegal.advice.data.local.entity.ChatMessageEntity
import com.pocketlegal.advice.data.local.entity.LegalLawEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [LegalLawEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun legalLawDao(): LegalLawDao
    abstract fun chatMessageDao(): ChatMessageDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLegalLawDao(db: AppDatabase): LegalLawDao = db.legalLawDao()

    @Provides
    fun provideChatMessageDao(db: AppDatabase): ChatMessageDao = db.chatMessageDao()
}
