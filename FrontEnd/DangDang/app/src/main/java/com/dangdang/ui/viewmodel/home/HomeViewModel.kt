package com.dangdang.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.applyResponse
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
import com.dangdang.data.model.community.TeamInfoModel
import com.dangdang.data.model.home.AfterMealGlucoseStatusModel
import com.dangdang.data.model.home.HomeDataResponse
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
    private val userRepository: UserRepository
) : ViewModel() {
    private val _homeData = MutableStateFlow<PendingModel<HomeDataResponse>>(
        PendingModel(null, LoadingState.Loading)
    )
    val homeData: StateFlow<PendingModel<HomeDataResponse>> = _homeData.asStateFlow()

    fun getHomeData(){
        viewModelScope.launch {
            _homeData.applyResponse(userRepository.getHomeData())
        }
    }
}