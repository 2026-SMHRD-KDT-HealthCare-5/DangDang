package com.dangdang.ui.viewmodel.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.user.SignUpForm
import com.dangdang.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
): ViewModel(){
    private val _teamInfo = MutableStateFlow<PendingModel<TeamInfoModel?>>(
        PendingModel(null, LoadingState.Loading)
    )
    val teamInfo: StateFlow<PendingModel<TeamInfoModel?>> = _teamInfo.asStateFlow()

    init {
        getUserTeamInfo()
    }

    //사용자가 속한 팀 정보 가져오기
    fun getUserTeamInfo(){
        viewModelScope.launch {
            val response = communityRepository.getUserTeamInfo()
            if(response.isSuccessful){
                val responseBody = response.body()
                _teamInfo.value = _teamInfo.value.copy(
                    data = responseBody,
                    loadingState = LoadingState.Success
                )
            }else{
                _teamInfo.value = _teamInfo.value.copy(
                    loadingState = LoadingState.Error
                )
            }
        }
    }

    //팀 나가기
    fun outTeam(){
        viewModelScope.launch {
            val response = communityRepository.outTeam()
            if(response.isSuccessful){
                _teamInfo.value = _teamInfo.value.copy(
                    data = null,
                    loadingState = LoadingState.Success
                )
            }
        }
    }
}