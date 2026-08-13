package com.dangdang.data.repository

import android.content.Context
import com.dangdang.Application.Companion.ExamplePictureUrl
import com.dangdang.common.utils.deleteSafely
import com.dangdang.common.utils.toMultipart
import com.dangdang.common.utils.toRequestBody
import com.dangdang.common.utils.uriToFile
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMakeForm
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.data.model.community.TeamRankingStatusModel
import com.dangdang.data.model.community.TeamSearchInfoModel
import retrofit2.Response
import java.io.File
import javax.inject.Inject

class CommunityRepository @Inject constructor(

){
    //사용자가 속한 팀 정보 가져오기
    suspend fun getUserTeamInfo(): Response<TeamInfoModel?>{
        val response = TeamInfoModel(
            isLeader = false,
            name = "우리팀 5월 걷기 챌린지",
            currentMemberCount = 4,
            maxMemberCount = 5,
            targetDistance = 150f,
            currentDistance = 20f,
            currentTeamDistance = 30f,
            profileImageUrl = ExamplePictureUrl,
            introduction = "하루 7천보 이상 함께 걸어요!"
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

    //팀 랭킹 가져오기
    suspend fun getTeamRankingStatusList(): Response<List<TeamRankingStatusModel>>{
        val response = listOf(
            TeamRankingStatusModel(
                rank = 1,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명",
                monthlyDistance = 32.56f,
            ),
            TeamRankingStatusModel(
                rank = 2,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명2",
                monthlyDistance = 20.56f,
            ),
            TeamRankingStatusModel(
                rank = 3,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명3",
                monthlyDistance = 10.56f,
            ),
            TeamRankingStatusModel(
                rank = 4,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명4",
                monthlyDistance = 5.56f,
            ),
            TeamRankingStatusModel(
                rank = 5,
                profileImageUrl = ExamplePictureUrl,
                teamName = "팀명5",
                monthlyDistance = 3.56f,
            )
        )

        return Response.success(response)
    }

    //팀 나가기
    suspend fun outTeam(): Response<String>{
        return Response.success("success")
    }

    //팀 리스트 가져오기
    suspend fun getTeamList(keyword:String): Response<List<TeamSearchInfoModel>>{
        val response = listOf(
            TeamSearchInfoModel(
                teamNo = 1,
                profileImageUrl = ExamplePictureUrl,
                teamName = "건강한 습관 만들기",
                memberCount = 4,
                capacity = 5,
                currentDistance = 30.2f,
                targetDistance = 150f,
                teamIntro = "하루 7천보 이상 함께 걸어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 2,
                profileImageUrl = ExamplePictureUrl,
                teamName = "매일 만보 걷기",
                memberCount = 3,
                capacity = 5,
                currentDistance = 60.2f,
                targetDistance = 150f,
                teamIntro = "만보 걷기 습관을 만들어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 3,
                profileImageUrl = ExamplePictureUrl,
                teamName = "아침 걷기 챌린지",
                memberCount = 2,
                capacity = 5,
                currentDistance = 40.2f,
                targetDistance = 150f,
                teamIntro = "아침에 함께 걸어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 4,
                profileImageUrl = ExamplePictureUrl,
                teamName = "건강한 습관 만들기",
                memberCount = 1,
                capacity = 5,
                currentDistance = 10.2f,
                targetDistance = 150f,
                teamIntro = "하루 7천보 이상 함께 걸어요!"
            ),
            TeamSearchInfoModel(
                teamNo = 5,
                profileImageUrl = ExamplePictureUrl,
                teamName = "주말 러닝 & 걷기",
                memberCount = 1,
                capacity = 5,
                currentDistance = 50.2f,
                targetDistance = 150f,
                teamIntro = "주말에 함께 러닝과 걷기!"
            )
        ).filter {
            it.teamName.contains(keyword)
        }

        return Response.success(response)
    }

    //팀 가입하기
    suspend fun joinTeam(teamId: Long): Response<String>{
        return Response.success("success")
    }

    //팀 만들기
    suspend fun makeTeam(context: Context, teamMakeForm: TeamMakeForm): Response<String>{
        var uploadFile: File? = null

        try{
            val imagePart = teamMakeForm.uri?.let { uri->
                uploadFile = context.uriToFile(uri)

                uploadFile.toMultipart()
            }
            val namePart = teamMakeForm.name.toRequestBody()
            val introductionPart = teamMakeForm.introduction.toRequestBody()
            val targetDistancePart = teamMakeForm.targetDistance.toRequestBody()

            return Response.success("success")
        } finally {
            uploadFile?.deleteSafely()
        }
    }
}