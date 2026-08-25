package com.dangdang.data.model.chat

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FoodInputDirectlyForm (
    var foodName: String, //이름
    var servingSize: String, //섭취량
    var calorie: String, //칼로리
    var carb: String, //탄수화물
    var sugar: String, // 당류
    var fiber: String, //식이섬유
    var protein: String, //단백질
    var fat: String,//지방
    val source: String = "사용자입력"
) : Parcelable