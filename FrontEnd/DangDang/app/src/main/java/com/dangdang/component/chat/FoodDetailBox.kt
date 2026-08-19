package com.dangdang.component.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.R
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.component.button.outlined.PrimaryOutlinedButton
import com.dangdang.component.divider.Divider
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.enums.LayoutSize
import com.dangdang.data.model.chat.FoodInfoModel
import com.dangdang.data.model.chat.FoodNutritionModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.DarkGray
import com.dangdang.ui.theme.DarkGreenOpacity15
import com.dangdang.ui.theme.DeepGreen
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.LavenderPurple
import com.dangdang.ui.theme.LightPurple
import com.dangdang.ui.theme.LightSlateGray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.PrimaryBlueOpacity13
import com.dangdang.ui.theme.SlateGray
import com.dangdang.ui.theme.SlateGrayOpacity13
import com.dangdang.ui.theme.StoneGray
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White

@Preview
@Composable
fun FoodDetailBoxPreview() {
    FoodDetailBox(
        predictedGlucoseRise = 35.0,
        beginGlucose = 140.0,
        foodInfo = FoodInfoModel(
            name = "비빔밥",
            nutritionInfo = "총 내용량 550g 1인분(1개)  / 650kcal",
            nutritionList = listOf(
                FoodNutritionModel(
                    name = "탄수화물",
                    unit = "g",
                    value = 15.0
                ),
                FoodNutritionModel(
                    name = "식이섬유",
                    unit = "g",
                    value = 2.0
                ),
                FoodNutritionModel(
                    name = "단백질",
                    unit = "g",
                    value = 20.0
                ),
                FoodNutritionModel(
                    name = "지방",
                    unit = "g",
                    value = 10.0
                ),
                FoodNutritionModel(
                    name = "칼로리",
                    unit = "kcal",
                    value = 250.0
                )
            )
        ),
        onCheckClick = {},
        onAIAnalysisClick = {},
        onKeywordInputClick = {},
        onInputDirectlyClick = {}
    )
}

@Composable
fun FoodDetailBox(
    predictedGlucoseRise: Double, //예상 혈당 상승량
    beginGlucose: Double, //식전 혈당
    foodInfo: FoodInfoModel,
    isMenuShow: Boolean = true,
    onCheckClick: () -> Unit = {},
    onAIAnalysisClick: () -> Unit = {},
    onKeywordInputClick: () -> Unit = {},
    onInputDirectlyClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = White,
                shape = MediumRoundShape
            )
            .border(
                width = ThinLineDp,
                color = SlateGray,
                shape = MediumRoundShape
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp
                ),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = foodInfo.name,
                style = AppTypography.titleLarge.regular,
                color = Black,
            )

            Row(
                modifier = Modifier
                    .padding(
                        vertical = 3.dp
                    ),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "식약청& 공공포털 검색 결과예요 ",
                    style = AppTypography.caption.medium,
                    color = LightSlateGray,
                )

                Icon(
                    painter = painterResource(R.drawable.important_icon),
                    contentDescription = "중요 아이콘",
                    modifier = Modifier
                        .size(8.dp),
                    tint = LightSlateGray
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = White,
                    shape = MediumRoundShape
                )
                .border(
                    width = ThinLineDp,
                    color = Gray,
                    shape = MediumRoundShape
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "영양정보",
                    style = AppTypography.bodyLarge.regular,
                    color = Black,
                )

                Text(
                    text = foodInfo.nutritionInfo,
                    style = AppTypography.labelSmall.regular,
                    color = DarkGray,
                )
            }

            Divider(
                position = DividerPosition.Horizontal,
                color = Gray
            )

            foodInfo.nutritionList.forEachIndexed { index, nutrition ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = nutrition.name,
                        style = AppTypography.labelSmall.regular,
                        color = Black,
                    )

                    Text(
                        text = "${nutrition.value} ${nutrition.unit}",
                        style = AppTypography.labelSmall.regular,
                        color = DarkGray,
                    )
                }

                //마지막 아이템이 아닐 경우 구분선
                if(index != foodInfo.nutritionList.lastIndex){
                    Divider(
                        position = DividerPosition.Horizontal,
                        color = Gray
                    )
                }
            }
        }

        PredictedGlucoseRiseBox(
            predictedGlucoseRise = predictedGlucoseRise,
            beginGlucose = beginGlucose
        )

        if(isMenuShow){
            PrimaryOutlinedButton(
                text = "맞아요!",
                color = DeepGreen,
                backgroundColor = DarkGreenOpacity15,
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.green_check),
                        contentDescription = "green check",
                        modifier = Modifier
                            .size(24.dp),
                        tint = DeepGreen
                    )
                },
                sizeType = LayoutSize.FillMaxSize,
                onClick = onCheckClick
            )

            PrimaryOutlinedButton(
                text = "틀려요,AI으로 분석하기",
                color = LightPurple,
                backgroundColor = LavenderPurple,
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ai_chip),
                        contentDescription = "ai chip",
                        modifier = Modifier
                            .size(24.dp),
                        tint = LightPurple
                    )
                },
                sizeType = LayoutSize.FillMaxSize,
                onClick = onAIAnalysisClick
            )

            PrimaryOutlinedButton(
                text = "검색어 다시 입력",
                color = PrimaryBlue,
                backgroundColor = PrimaryBlueOpacity13,
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "search",
                        modifier = Modifier
                            .size(24.dp),
                        tint = PrimaryBlue
                    )
                },
                sizeType = LayoutSize.FillMaxSize,
                onClick = onKeywordInputClick
            )

            PrimaryOutlinedButton(
                text = "직접 입력하기 (이름, 영양성분)",
                color = StoneGray,
                backgroundColor = SlateGrayOpacity13,
                leftIcon = {
                    Icon(
                        painter = painterResource(R.drawable.pencil_icon),
                        contentDescription = "pencil",
                        modifier = Modifier
                            .size(24.dp),
                        tint = StoneGray
                    )
                },
                sizeType = LayoutSize.FillMaxSize,
                onClick = onInputDirectlyClick
            )
        }
    }
}