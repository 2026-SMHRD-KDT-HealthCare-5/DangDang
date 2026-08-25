package com.dangdang.ui.viewmodel.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.applyResponse
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.community.TeamRankingStatusModel
import com.dangdang.data.model.community.TeamRankingStatusResponse
import com.dangdang.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityTeamRankingViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
): ViewModel(){
    private val _teamRankingStatusList = MutableStateFlow<PendingModel<TeamRankingStatusResponse>>(
        PendingModel(null, LoadingState.Loading)
    )
    val teamRankingStatusList: StateFlow<PendingModel<TeamRankingStatusResponse>> =
        _teamRankingStatusList.asStateFlow()

    init {
        getTeamRankingStatusList()
    }

    fun getTeamRankingStatusList(){
        viewModelScope.launch {
            _teamRankingStatusList.applyResponse(communityRepository.getTeamRankingStatusList())
        }
    }
}