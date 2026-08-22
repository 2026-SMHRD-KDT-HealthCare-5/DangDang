package com.dangdang.data.model.chat

import androidx.annotation.Keep

@Keep
data class FoodConfirmInputForm(
    val foodNo: Int?,
    val customFood: FoodInputDirectlyForm?,
    val preGlucose: Float?,
    val portion: Float?,
)
