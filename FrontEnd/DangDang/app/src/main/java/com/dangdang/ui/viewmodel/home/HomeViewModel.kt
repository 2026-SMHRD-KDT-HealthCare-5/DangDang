package com.dangdang.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.applyResponse
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
    private val _weeklyGlucoseCheckList = MutableStateFlow<PendingModel<List<WeeklyGlucoseCheckModel>>>(
        PendingModel(emptyList(), LoadingState.Loading)
    )
    val weeklyGlucoseCheckList: StateFlow<PendingModel<List<WeeklyGlucoseCheckModel>>> = _weeklyGlucoseCheckList.asStateFlow()

    private val _afterMealGlucoseStatus = MutableStateFlow<PendingModel<AfterMealGlucoseStatusModel>>(
        PendingModel(null, LoadingState.Loading)
    )
    val afterMealGlucoseStatus: StateFlow<PendingModel<AfterMealGlucoseStatusModel>> =
        _afterMealGlucoseStatus.asStateFlow()

    private val _teamInfo = MutableStateFlow<PendingModel<TeamInfoModel?>>(
        PendingModel(null, LoadingState.Loading)
    )
    val teamInfo: StateFlow<PendingModel<TeamInfoModel?>> = _teamInfo.asStateFlow()

    init {
        getWeeklyGlucoseCheckList()
        getAfterMealGlucoseStatus()
        getUserTeamInfo()
    }

    fun getWeeklyGlucoseCheckList(){
        viewModelScope.launch {
            _weeklyGlucoseCheckList.applyResponse(userRepository.getWeeklyGlucoseCheckList())
        }
    }

    fun getAfterMealGlucoseStatus(){
        viewModelScope.launch {
            _afterMealGlucoseStatus.applyResponse(userRepository.getAfterMealGlucoseStatus())
        }
    }

    //사용자가 속한 팀 정보 가져오기
    fun getUserTeamInfo(){
        viewModelScope.launch {
            _teamInfo.applyResponse(communityRepository.getUserTeamInfo())
        }
    }
}