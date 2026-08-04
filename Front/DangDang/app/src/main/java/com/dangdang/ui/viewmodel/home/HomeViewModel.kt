package com.dangdang.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.community.TeamMemberChallengeStatusModel
import com.dangdang.data.model.home.AfterMealGlucoseStatusModel
import com.dangdang.data.model.home.WeeklyGlucoseCheckModel
import com.dangdang.data.repository.CommunityRepository
import com.dangdang.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository
) : ViewModel() {
    private val _weeklyGlucoseCheckList = MutableStateFlow<List<WeeklyGlucoseCheckModel>>(emptyList())
    val weeklyGlucoseCheckList: StateFlow<List<WeeklyGlucoseCheckModel>> = _weeklyGlucoseCheckList.asStateFlow()

    private val _afterMealGlucoseStatus = MutableStateFlow<AfterMealGlucoseStatusModel?>(null)
    val afterMealGlucoseStatus: StateFlow<AfterMealGlucoseStatusModel?> = _afterMealGlucoseStatus.asStateFlow()

    private val _teamInfo = MutableStateFlow<TeamInfoModel?>(null)
    val teamInfo: StateFlow<TeamInfoModel?> = _teamInfo.asStateFlow()

    private val _teamChallengeStatusList =
        MutableStateFlow<List<TeamMemberChallengeStatusModel>?>(null)
    val teamChallengeStatusList: StateFlow<List<TeamMemberChallengeStatusModel>?> =
        _teamChallengeStatusList.asStateFlow()

    init {
        getWeeklyGlucoseCheckList()
        getAfterMealGlucoseStatus()
        getUserTeamInfo()
        getTeamChallengeStatusList()
    }

    fun getWeeklyGlucoseCheckList(){
        viewModelScope.launch {
            val response = userRepository.getWeeklyGlucoseCheckList()
            if(response.isSuccessful){
                val responseBody = response.body()
                _weeklyGlucoseCheckList.value = responseBody ?: emptyList()
            }
        }
    }

    fun getAfterMealGlucoseStatus(){
        viewModelScope.launch {
            val response = userRepository.getAfterMealGlucoseStatus()
            if(response.isSuccessful){
                val responseBody = response.body()
                _afterMealGlucoseStatus.value = responseBody
            }
        }
    }

    //사용자가 속한 팀 정보 가져오기
    fun getUserTeamInfo(){
        viewModelScope.launch {
            val response = communityRepository.getUserTeamInfo()
            if(response.isSuccessful){
                val responseBody = response.body()
                _teamInfo.value = responseBody
            }
        }
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