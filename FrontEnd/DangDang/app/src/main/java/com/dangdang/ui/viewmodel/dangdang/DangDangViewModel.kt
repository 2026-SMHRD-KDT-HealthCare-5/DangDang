package com.dangdang.ui.viewmodel.dangdang

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangdang.common.utils.AnalysisFoodType
import com.dangdang.common.utils.BeforeMealTipType
import com.dangdang.common.utils.TodayWalkTargetType
import com.dangdang.common.utils.applyResponse
import com.dangdang.data.enums.LoadingState
import com.dangdang.data.model.PendingModel
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
    private val _chattingList = MutableStateFlow<PendingModel<List<ChatModel>>>(
        PendingModel(emptyList(), LoadingState.Loading)
    )
    val chattingList: StateFlow<PendingModel<List<ChatModel>>> = _chattingList.asStateFlow()

    private val _recommendQuestionList = MutableStateFlow<PendingModel<List<ChatRecommendQuestionModel>>>(
        PendingModel(emptyList(), LoadingState.Loading)
    )
    val recommendQuestionList: StateFlow<PendingModel<List<ChatRecommendQuestionModel>>> =
        _recommendQuestionList.asStateFlow()

    init {
        getRecommendQuestion()
    }

    fun getRecommendQuestion(){
        viewModelScope.launch {
            _recommendQuestionList.applyResponse(dangDangRepository.getRecommendQuestion())
        }
    }

    fun getChattingList(){
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.getChattingList())
        }
    }

    //채팅 전송
    fun chatSend(message: String) {
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.chatSend(message))
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
            _chattingList.applyResponse(dangDangRepository.startAnalysisFood())
        }
    }

    //식전 혈당 전송
    fun sendBeforeMealGlucose(glucoseValue: String?) {
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.sendBeforeMealGlucose(glucoseValue))
        }
    }

    //음식 입력 전송
    fun ateFoodSend(context: Context, ateFoodValue: String, ateFoodImageUri: Uri?) {
        viewModelScope.launch {
            _chattingList.applyResponse(
                dangDangRepository.ateFoodSend(
                    context = context,
                    ateFoodValue = ateFoodValue,
                    ateFoodImageUri = ateFoodImageUri
                )
            )
        }
    }

    //음식 먹은 양 전송
    fun ateWeightSend(weightValue: String) {
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.ateWeightSend(weightValue))
        }
    }

    //음식 직접 입력 전송
    fun sendFoodInputDirectly(foodInputDirectlyForm: FoodInputDirectlyForm) {
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.sendFoodInputDirectly(foodInputDirectlyForm))
        }
    }

    //검색어 다시 입력 선택 시
    fun ateFoodReSearch() {
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.ateFoodReSearch())
        }
    }

    //음식 확정 선택 시
    fun foodCheck(){
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.foodCheck())
        }
    }

    //오늘 걷기 목표 불러오기 선택 시
    fun getRecommendWalkChallenge(){
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.getRecommendWalkChallenge())
        }
    }

    //걷기 완료 미션 전송
    fun completeWalkMission() {
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.completeWalkMission())
        }
    }

    fun afterWalkGlucoseSend(glucose: Int){
        viewModelScope.launch {
            _chattingList.applyResponse(dangDangRepository.afterWalkGlucoseSend(glucose))
        }
    }
}