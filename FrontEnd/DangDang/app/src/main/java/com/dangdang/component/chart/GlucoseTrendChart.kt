package com.dangdang.component.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dangdang.common.utils.medium
import com.dangdang.common.utils.regular
import com.dangdang.data.model.home.GlucoseChartPointModel
import com.dangdang.ui.theme.AppTypography
import com.dangdang.ui.theme.Black
import com.dangdang.ui.theme.Gray
import com.dangdang.ui.theme.MediumRoundShape
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.White
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

@Preview
@Composable
fun GlucoseTrendChartPreview(){
    GlucoseTrendChart(
        values = listOf(
            GlucoseChartPointModel(
                time = "12:00",
                glucose = 180
            ),
            GlucoseChartPointModel(
                time = "13:00",
                glucose = 170
            ),
        ),
        goal = 180f
    )
}

@Composable
fun GlucoseTrendChart(
    values: List<GlucoseChartPointModel>,
    goal: Float
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(values) {
        if (values.isNotEmpty()) {
            modelProducer.runTransaction {
                lineModel {
                    series(values.map {
                        it.glucose.toFloat()
                    })
                }
            }
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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "식후 혈당 추이",
                style = AppTypography.bodyLarge.medium,
                color = Black,
            )

            Text(
                text = "(mg/dL)",
                style = AppTypography.labelMedium.regular,
                color = Gray,
            )
        }

        if (values.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "혈당 데이터가 없습니다.",
                    style = AppTypography.bodyLarge.regular,
                    color = Black,
                )
            }
        } else {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(Color(0xFF4C6EF5)))
                            )
                        ),
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        itemPlacer = VerticalAxis.ItemPlacer.step({ 20.0 })
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, x, _ ->
                            values.map{
                                it.time
                            }.getOrElse(x.toInt()) { "" }
                        },
                    ),
                    decorations = listOf(
                        HorizontalLine(
                            y = { goal.toDouble() },
                            line = LineComponent(fill = Fill(Color.Red), thickness = 1.dp),
                            labelComponent = rememberTextComponent(style = TextStyle(color = Color.Red)),
                            label = { "$goal (목표)" }
                        )
                    )
                ),
                modelProducer = modelProducer,
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                zoomState = rememberVicoZoomState(initialZoom = Zoom.Content, zoomEnabled = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}