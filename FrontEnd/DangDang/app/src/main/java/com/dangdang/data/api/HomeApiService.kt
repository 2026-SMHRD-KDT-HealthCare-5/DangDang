package com.dangdang.data.api

import com.dangdang.Application.Companion.HomePath
import com.dangdang.data.model.home.HomeDataResponse
import retrofit2.Response
import retrofit2.http.GET

interface HomeApiService {
    @GET(HomePath)
    suspend fun getHomeData(): Response<HomeDataResponse>
}