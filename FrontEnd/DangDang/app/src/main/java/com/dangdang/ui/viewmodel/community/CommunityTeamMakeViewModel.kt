package com.dangdang.ui.viewmodel.community

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.data.model.community.TeamMakeForm
import com.dangdang.data.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityTeamMakeViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
): ViewModel(){

    fun makeTeam(context: Context, teamMakeForm: TeamMakeForm, onMakeSuccess: () -> Unit){
        viewModelScope.launch {
            val response = communityRepository.makeTeam(context, teamMakeForm)
            if(response.isSuccessful){
                onMakeSuccess()
            }else{
                Toast.makeText(context, "팀 만들기 요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}