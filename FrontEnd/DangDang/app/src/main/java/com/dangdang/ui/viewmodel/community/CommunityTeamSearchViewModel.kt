package com.dangdang.ui.viewmodel.community

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.applyResponse
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.community.TeamSearchInfoModel
import com.dangdang.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityTeamSearchViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
): ViewModel(){
    private val _teamList = MutableStateFlow<PendingModel<List<TeamSearchInfoModel>>>(
        PendingModel(emptyList(), LoadingState.Loading)
    )
    val teamList: StateFlow<PendingModel<List<TeamSearchInfoModel>>> = _teamList

    init {
        getTeamList("")
    }

    fun getTeamList(keyword: String){
        viewModelScope.launch {
            _teamList.applyResponse(communityRepository.getTeamList(keyword))
        }
    }

    fun joinTeam(context: Context, teamId: Long, onJoinSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = communityRepository.joinTeam(teamId)
            if (response.isSuccessful) {
                onJoinSuccess()
            }else{
                Toast.makeText(context, "팀 가입 요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}