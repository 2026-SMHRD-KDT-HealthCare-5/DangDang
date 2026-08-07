package com.dangdang.ui.viewmodel.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _teamList = MutableStateFlow<List<TeamSearchInfoModel>>(emptyList())
    val teamList: StateFlow<List<TeamSearchInfoModel>> = _teamList

    init {
        getTeamList("")
    }

    fun getTeamList(keyword: String){
        viewModelScope.launch {
            val response = communityRepository.getTeamList()
            if(response.isSuccessful){
                val responseBody = response.body()
                _teamList.value = (responseBody ?: emptyList()).filter {
                    it.name.contains(keyword)
                }
            }
        }
    }

    fun joinTeam(teamId: Long, onJoinSuccess: () -> Unit) {
        viewModelScope.launch {
            val response = communityRepository.joinTeam(teamId)
            if (response.isSuccessful) {
                onJoinSuccess()
            }
        }
    }
}