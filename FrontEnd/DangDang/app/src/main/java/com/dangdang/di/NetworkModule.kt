package com.dangdang.di

import android.content.Context
import com.dangdang.Application.Companion.API_BASE_URL
import com.dangdang.BuildConfig
import com.dangdang.common.utils.AppPrefs
import com.dangdang.common.utils.LoginRetrofit
import com.dangdang.common.utils.RefreshRetrofit
import com.dangdang.data.api.ChatApiService
import com.dangdang.data.api.CommunityApiService
import com.dangdang.data.api.HomeApiService
import com.dangdang.data.api.LoginApiService
import com.dangdang.data.api.RefreshApiService
import com.dangdang.data.api.UserApiService
import com.dangdang.data.api.WalkApiService
import com.dangdang.data.manager.SessionManager
import com.dangdang.data.network.ApiAuthenticator
import com.dangdang.data.network.ApiInterceptor
import com.dangdang.data.repository.CommunityRepository
import com.dangdang.data.repository.DangDangRepository
import com.dangdang.data.repository.UserRepository
import com.dangdang.data.repository.WalkRepository
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
        sessionManager: SessionManager,
        apiAuthenticator: ApiAuthenticator
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE

        return OkHttpClient.Builder()
            .addInterceptor(ApiInterceptor(sessionManager))
            .addInterceptor(httpLoggingInterceptor)
            .authenticator(apiAuthenticator)
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
    @RefreshRetrofit
    fun provideRefreshRetrofit(): Retrofit {

        val client =
            OkHttpClient.Builder()
                .build()

        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    @Provides
    @Singleton
    @LoginRetrofit
    fun provideLoginRetrofit(): Retrofit {

        val client =
            OkHttpClient.Builder()
                .build()

        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRefreshApiService(
        @RefreshRetrofit retrofit: Retrofit
    ): RefreshApiService {

        return retrofit.create(
            RefreshApiService::class.java
        )
    }

    @Provides
    @Singleton
    fun provideLoginApiService(
        @LoginRetrofit retrofit: Retrofit
    ): LoginApiService {

        return retrofit.create(
            LoginApiService::class.java
        )
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
    fun provideWalkApiService(retrofit: Retrofit): WalkApiService {
        return retrofit.create(WalkApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommunityApiService(retrofit: Retrofit): CommunityApiService {
        return retrofit.create(CommunityApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideHomeApiService(retrofit: Retrofit): HomeApiService {
        return retrofit.create(HomeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userApiService: UserApiService,
        loginApiService: LoginApiService,
        homeApiService: HomeApiService
    ): UserRepository{
        return UserRepository(userApiService, loginApiService, homeApiService)
    }

    @Provides
    @Singleton
    fun providerWalkRepository(walkApiService: WalkApiService): WalkRepository{
        return WalkRepository(walkApiService)
    }

    @Provides
    @Singleton
    fun providerCommunityRepository(communityApiService: CommunityApiService): CommunityRepository{
        return CommunityRepository(communityApiService)
    }

    @Provides
    @Singleton
    fun providerDangDangRepository(
        chatApiService: ChatApiService,
        walkApiService: WalkApiService
    ): DangDangRepository {
        return DangDangRepository(chatApiService, walkApiService)
    }
}