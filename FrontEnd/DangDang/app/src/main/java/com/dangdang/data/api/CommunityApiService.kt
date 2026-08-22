package com.dangdang.data.api

import com.dangdang.Application.Companion.RankingPath
import com.dangdang.Application.Companion.TeamPath
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMakeForm
import com.dangdang.data.model.community.TeamMakeResponse
import com.dangdang.data.model.community.TeamRankingStatusResponse
import com.dangdang.data.model.community.TeamSearchInfoModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApiService {
    //사용자 팀 정보 가져오기
    @GET("${TeamPath}/me")
    suspend fun getUserTeamInfo(): Response<TeamInfoModel?>

    //팀 목록 가져오기
    @GET(TeamPath)
    suspend fun getTeamList(
        @Query("keyword") keyword: String
    ): Response<List<TeamSearchInfoModel>>

    //팀 만들기
    @POST(TeamPath)
    suspend fun makeTeam(
        @Body teamMakeForm: TeamMakeForm
    ): Response<TeamMakeResponse>

    //팀 나가기
    @DELETE("${TeamPath}/{teamNo}/members/me")
    suspend fun outTeam(
        @Path("teamNo") teamNo: Int
    ): Response<String>

    //팀 가입하기
    @POST("${TeamPath}/{teamNo}/join")
    suspend fun joinTeam(
        @Path("teamNo") teamId: Long
    ): Response<Unit>

    //팀 랭킹 가져오기
    @GET("${RankingPath}/teams")
    suspend fun getTeamRankingStatusList(

    ): Response<TeamRankingStatusResponse>
}