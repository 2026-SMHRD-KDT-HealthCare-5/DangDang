package com.dangdang.ui.viewmodel.dangdang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.AnalysisFoodType
import com.dangdang.common.utils.BeforeMealTipType
import com.dangdang.common.utils.TodayWalkTargetType
import com.dangdang.data.model.chat.ChatModel
import com.dangdang.data.model.chat.ChatRecommendQuestionModel
import com.dangdang.data.model.chat.FoodInputDirectlyForm
import com.dangdang.data.repository.DangDangRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DangDangViewModel @Inject constructor(
    private val dangDangRepository: DangDangRepository
): ViewModel(){
    private val _chattingList = MutableStateFlow<List<ChatModel>>(emptyList())
    val chattingList: StateFlow<List<ChatModel>> = _chattingList.asStateFlow()

    private val _recommendQuestionList = MutableStateFlow<List<ChatRecommendQuestionModel>>(emptyList())
    val recommendQuestionList: StateFlow<List<ChatRecommendQuestionModel>> = _recommendQuestionList.asStateFlow()

    init {
        getRecommendQuestion()
    }

    fun getRecommendQuestion(){
        viewModelScope.launch {
            val response = dangDangRepository.getRecommendQuestion()
            if(response.isSuccessful){
                val responseBody = response.body()
                _recommendQuestionList.value = responseBody ?: emptyList()
            }
        }
    }

    fun getChattingList(){
        viewModelScope.launch {
            val response = dangDangRepository.getChattingList()
            if(response.isSuccessful){
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //채팅 전송
    fun chatSend(message: String) {
        viewModelScope.launch {
            val response = dangDangRepository.chatSend(message)
            if (response.isSuccessful) {
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    fun onRecommendQuestionClick(chatRecommendQuestionModel:ChatRecommendQuestionModel){
        when(chatRecommendQuestionModel.type){
            AnalysisFoodType -> {
                startAnalysisFood()
            }
            BeforeMealTipType -> {
                chatSend(chatRecommendQuestionModel.question)
            }
            TodayWalkTargetType -> {
                getRecommendWalkChallenge()
            }
        }
    }

    //음식 분석&걷기 시작
    fun startAnalysisFood(){
        viewModelScope.launch {
            val response = dangDangRepository.startAnalysisFood()
            if(response.isSuccessful){
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //식전 혈당 전송
    fun sendBeforeMealGlucose(glucoseValue: String) {
        viewModelScope.launch {
            val response = dangDangRepository.sendBeforeMealGlucose(glucoseValue)
            if (response.isSuccessful) {
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //음식 입력 전송
    fun ateFoodSend(ateFoodValue: String) {
        viewModelScope.launch {
            val response = dangDangRepository.ateFoodSend(ateFoodValue)
            if (response.isSuccessful) {
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //음식 직접 입력 전송
    fun sendFoodInputDirectly(foodInputDirectlyForm: FoodInputDirectlyForm) {
        viewModelScope.launch {
            val response = dangDangRepository.sendFoodInputDirectly(foodInputDirectlyForm)
            if (response.isSuccessful) {
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //검색어 다시 입력 선택 시
    fun ateFoodReSearch() {
        viewModelScope.launch {
            val response = dangDangRepository.ateFoodReSearch()
            if (response.isSuccessful) {
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //음식 확정 선택 시
    fun foodCheck(){
        viewModelScope.launch {
            val response = dangDangRepository.foodCheck()
            if(response.isSuccessful){
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //오늘 걷기 목표 불러오기 선택 시
    fun getRecommendWalkChallenge(){
        viewModelScope.launch {
            val response = dangDangRepository.getRecommendWalkChallenge()
            if(response.isSuccessful){
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    //걷기 완료 미션 전송
    fun completeWalkMission() {
        viewModelScope.launch {
            val response = dangDangRepository.completeWalkMission()
            if (response.isSuccessful) {
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }

    fun afterWalkGlucoseSend(glucose: Int){
        viewModelScope.launch {
            val response = dangDangRepository.afterWalkGlucoseSend(glucose)
            if(response.isSuccessful){
                val responseBody = response.body()
                _chattingList.value = responseBody ?: emptyList()
            }
        }
    }
}