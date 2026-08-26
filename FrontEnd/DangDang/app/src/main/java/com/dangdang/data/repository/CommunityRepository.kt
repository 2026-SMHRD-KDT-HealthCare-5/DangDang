package com.dangdang.data.repository

import android.content.Context
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.deleteSafely
import com.dangdang.common.utils.safeApiCall
import com.dangdang.common.utils.toMultipart
import com.dangdang.common.utils.toRequestBody
import com.dangdang.common.utils.uriToFile
import com.dangdang.data.api.CommunityApiService
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMakeForm
import com.dangdang.data.model.community.TeamMakeResponse
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.data.model.community.TeamRankingStatusModel
import com.dangdang.data.model.community.TeamRankingStatusResponse
import com.dangdang.data.model.community.TeamSearchInfoModel
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class CommunityRepository @Inject constructor(
    private val communityApiService: CommunityApiService
){
    //사용자가 속한 팀 정보 가져오기
    suspend fun getUserTeamInfo(): Response<TeamInfoModel?> = safeApiCall {
        communityApiService.getUserTeamInfo()
    }

    //팀 랭킹 가져오기
    suspend fun getTeamRankingStatusList(): Response<TeamRankingStatusResponse> = safeApiCall {
        communityApiService.getTeamRankingStatusList()
    }

    //팀 나가기
    suspend fun outTeam(teamNo: Int): Response<String> = safeApiCall {
        communityApiService.outTeam(teamNo)
    }

    //팀 리스트 가져오기
    suspend fun getTeamList(keyword:String): Response<List<TeamSearchInfoModel>> = safeApiCall {
        communityApiService.getTeamList(keyword)
    }

    //팀 가입하기
    suspend fun joinTeam(teamId: Long): Response<Unit> = safeApiCall {
        communityApiService.joinTeam(teamId)
    }

    //팀 만들기
    suspend fun makeTeam(context: Context, teamMakeForm: TeamMakeForm): Response<TeamMakeResponse>{
        var uploadFile: File? = null

        try{
            val imagePart = teamMakeForm.uri?.let { uri->
                uploadFile = context.uriToFile(uri)

                uploadFile.toMultipart()
            }

            val response = safeApiCall {
                communityApiService.makeTeam(
                    teamMakeForm = teamMakeForm
                )
            }
            return response
        } finally {
            uploadFile?.deleteSafely()
        }
    }
}