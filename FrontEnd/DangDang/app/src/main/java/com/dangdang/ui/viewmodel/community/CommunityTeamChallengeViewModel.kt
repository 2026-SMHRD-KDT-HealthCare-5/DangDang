package com.dangdang.ui.viewmodel.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityTeamChallengeViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
): ViewModel(){
    private val _teamChallengeStatusList =
        MutableStateFlow<List<TeamMemberChallengeStatusModel>?>(null)
    val teamChallengeStatusList: StateFlow<List<TeamMemberChallengeStatusModel>?> =
        _teamChallengeStatusList.asStateFlow()

    init {
        getTeamChallengeStatusList()
    }

    //팀원들 걷기 현황 가져오기
    fun getTeamChallengeStatusList(){
        viewModelScope.launch {
            val response = communityRepository.getTeamChallengeStatusList()
            if(response.isSuccessful){
                val responseBody = response.body()
                _teamChallengeStatusList.value = responseBody
            }
        }
    }
}