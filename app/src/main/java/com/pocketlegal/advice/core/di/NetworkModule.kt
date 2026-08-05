package com.pocketlegal.advice.core.di

import com.pocketlegal.advice.core.network.ApiClient
import com.pocketlegal.advice.core.util.Constants
import com.pocketlegal.advice.data.remote.AiApiService
import com.pocketlegal.advice.data.remote.AuthApiService
import com.pocketlegal.advice.data.remote.LegalApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("main")
    fun provideMainOkHttpClient(): OkHttpClient = ApiClient.buildOkHttpClient()

    @Provides
    @Singleton
    @Named("ai")
    fun provideAiOkHttpClient(): OkHttpClient = ApiClient.buildOkHttpClient()

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("main") client: OkHttpClient): AuthApiService =
        ApiClient.buildService(Constants.BASE_URL, client)

    @Provides
    @Singleton
    fun provideLegalApiService(@Named("main") client: OkHttpClient): LegalApiService =
        ApiClient.buildService(Constants.BASE_URL, client)

    @Provides
    @Singleton
    fun provideAiApiService(@Named("ai") client: OkHttpClient): AiApiService =
        ApiClient.buildService(Constants.AI_BASE_URL, client)
}
