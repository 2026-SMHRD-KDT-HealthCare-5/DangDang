package com.dangdang.data.model.chat

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FoodInputDirectlyForm (
    var name: String, //이름
    var intake: String, //섭취량
    var kcal: String, //칼로리
    var carbohydrate: String, //탄수화물
    var sugar: String, // 당류
    var dietaryFiber: String, //식이섬유
    var protein: String, //단백질
    var fat: String//지방
) : Parcelable