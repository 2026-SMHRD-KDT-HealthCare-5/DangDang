package com.dangdang.data.repository

import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.data.model.user.TokenResponse
import com.dangdang.data.model.user.User
import retrofit2.Response
import javax.inject.Inject

class CommunityRepository @Inject constructor(

){
    //사용자가 속한 팀 정보 가져오기
    suspend fun getUserTeamInfo(): Response<TeamInfoModel?>{
        val response = TeamInfoModel(
            isLeader = false,
            name = "우리팀 5월 걷기 챌린지",
            targetDistance = 150f,
            currentDistance = 20f,
            currentTeamDistance = 30f
        )

        return Response.success(response)
    }

    //팀원들 걷기 현황 가져오기
    suspend fun getTeamChallengeStatusList(): Response<List<TeamMemberChallengeStatusModel>>{
        val response = listOf(
            TeamMemberChallengeStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임",
                currentDistance = 32.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 2,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임2",
                currentDistance = 20.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임3",
                currentDistance = 10.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 4,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임4",
                currentDistance = 5.56f,
                targetDistance = 150f
            ),
            TeamMemberChallengeStatusModel(
                rank = 5,
                profileImageUrl = ExamplePictureUrl,
                nickname = "닉네임5",
                currentDistance = 3.56f,
                targetDistance = 150f
            )
        )

        return Response.success(response)
    }

    //팀 나가기
    suspend fun outTeam(): Response<String>{
        return Response.success("success")
    }
}