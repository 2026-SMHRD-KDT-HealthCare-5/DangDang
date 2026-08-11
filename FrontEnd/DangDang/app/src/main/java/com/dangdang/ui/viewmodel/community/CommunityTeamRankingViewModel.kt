package com.dangdang.ui.viewmodel.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.data.model.community.TeamRankingStatusModel
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
    private val _teamRankingStatusList = MutableStateFlow<List<TeamRankingStatusModel>>(emptyList())
    val teamRankingStatusList: StateFlow<List<TeamRankingStatusModel>> = _teamRankingStatusList.asStateFlow()

    init {
        getTeamRankingStatusList()
    }

    fun getTeamRankingStatusList(){
        viewModelScope.launch {
            val response = communityRepository.getTeamRankingStatusList()
            if(response.isSuccessful){
                val responseBody = response.body()
                _teamRankingStatusList.value = responseBody ?: emptyList()
            }
        }
    }
}