package com.dangdang.di

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.dangdang.Application.Companion.API_BASE_URL
import com.dangdang.common.utils.AppPrefs
import com.dangdang.data.api.UserApiService
import com.dangdang.data.repository.UserRepository
import com.dangdang.data.repository.WalkRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        userApiService: Lazy<UserApiService>,
        sessionManager: SessionManager
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(ApiInterceptor(userApiService, sessionManager))
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
        appPrefs: AppPrefs
    ): SessionManager {
        return SessionManager(context, appPrefs)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userApiService: UserApiService): UserRepository{
        return UserRepository(userApiService)
    }

    @Provides
    @Singleton
    fun providerWalkRepository(): WalkRepository{
        return WalkRepository()
    }
}