package com.dangdang.ui.viewmodel.community

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.applyResponse
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
            _teamInfo.applyResponse(communityRepository.getUserTeamInfo())
        }
    }

    //팀 나가기
    fun outTeam(context: Context){
        viewModelScope.launch {
            val response = communityRepository.outTeam()
            if(response.isSuccessful){
                _teamInfo.value = _teamInfo.value.copy(
                    data = null,
                    loadingState = LoadingState.Success
                )
            }else{
                Toast.makeText(context, "팀 나가기 요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}