package com.pocketlegal.advice.core.di

import com.pocketlegal.advice.data.repository.AiRepositoryImpl
import com.pocketlegal.advice.data.repository.AuthRepositoryImpl
import com.pocketlegal.advice.data.repository.LegalRepositoryImpl
import com.pocketlegal.advice.domain.repository.IAiRepository
import com.pocketlegal.advice.domain.repository.IAuthRepository
import com.pocketlegal.advice.domain.repository.ILegalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindLegalRepository(impl: LegalRepositoryImpl): ILegalRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): IAiRepository
}
