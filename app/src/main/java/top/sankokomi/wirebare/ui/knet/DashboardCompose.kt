package top.sankokomi.wirebare.ui.knet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.auto
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.flow.SharedFlow
import top.sankokomi.wirebare.kernel.dashboard.Bandwidth
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.KnetPolicyDataStore
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.RealRow
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.util.Global

@Composable
fun BandwidthChart(
    icon: Any,
    itemName: String,
    subName: String,
    bandwidthFlow: SharedFlow<Bandwidth>
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val bandwidthHistory = remember { mutableStateListOf<Bandwidth>() }
    var bandwidth by remember { mutableStateOf(Bandwidth(0.0, -1L)) }

    LaunchedEffect(Unit) {
        repeat(61) {
            bandwidthHistory.add(bandwidth)
        }
        modelProducer.runTransaction {
            lineSeries { series(bandwidthHistory.map { it.value }) }
        }
        bandwidthFlow.collect {
            bandwidth = it
            if (bandwidthHistory.size >= 61) {
                bandwidthHistory.removeAt(0)
            }
            bandwidthHistory.add(it)
            modelProducer.runTransaction {
                lineSeries { series(bandwidthHistory.map { bandwidth -> bandwidth.value }) }
            }
        }
    }

    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .padding(16.dp)
    ) {
        RealRow(
            modifier = Modifier
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = icon,
                modifier = Modifier
                    .size(28.dp),
                colorFilter = Colors.primary.let { ColorFilter.tint(it) },
                contentDescription = null
            )
            RealColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = itemName,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    style = Typographies.titleSmall
                )
                AnimatedVisibility(subName.isNotEmpty()) {
                    AnimatedContent(
                        targetState = subName,
                        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                        label = "BandwidthChart"
                    ) { name ->
                        Text(
                            text = name,
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            style = Typographies.bodySmall
                        )
                    }
                }
            }
            Text(
                text = remember(bandwidth.value) {
                    BandwidthFormatter.format(
                        bandwidth.value
                    ).string(2, true) + "/" + Global.appContext.getString(
                        R.string.unit_second
                    )
                },
                style = Typographies.titleSmall
            )
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    // 轴线的绘制风格
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(fill = fill(Colors.primaryContainer)),
                            stroke = LineCartesianLayer.LineStroke.continuous(
                                thickness = 1.8.dp
                            )
                        )
                    ),
                    // 图表最大最小值计算
                    rangeProvider = remember { BandwidthCartesianLayerRangeProvider() },
                    verticalAxisPosition = Axis.Position.Vertical.Start,
                    // 表示尽可能地小，以让整个表格不需要滚动也能看到全部
                    pointSpacing = 0.1.dp
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberAxisLabelComponent(
                        textSize = 12.sp
                    ),
                    itemPlacer = remember {
                        BandwidthVerticalAxisItemPlacer()
                    },
                    valueFormatter = remember { BandwidthCartesianValueFormatter() },
                    size = BaseAxis.Size.auto()
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberAxisLabelComponent(
                        textSize = 12.sp
                    ),
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.aligned(
                            spacing = { 5 },
                            offset = { 0 },
                            shiftExtremeLines = false,
                            addExtremeLabelPadding = false,
                        )
                    },
                    size = BaseAxis.Size.auto()
                )
            ),
            modelProducer = modelProducer,
            animationSpec = tween(durationMillis = 0),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun MiniBandwidthChart(
    itemName: String,
    bandwidthFlow: SharedFlow<Bandwidth>
) {
    val enableMiniMode by KnetPolicyDataStore.enableMiniFloatingWindow.collectAsState()

    val modelProducer = remember { CartesianChartModelProducer() }
    val bandwidthHistory = remember { mutableStateListOf<Bandwidth>() }
    var bandwidth by remember { mutableStateOf(Bandwidth(0.0, -1L)) }

    LaunchedEffect(Unit) {
        repeat(61) {
            bandwidthHistory.add(bandwidth)
        }
        modelProducer.runTransaction {
            lineSeries { series(bandwidthHistory.map { it.value }) }
        }
        bandwidthFlow.collect {
            bandwidth = it
            if (bandwidthHistory.size >= 61) {
                bandwidthHistory.removeAt(0)
            }
            bandwidthHistory.add(it)
            modelProducer.runTransaction {
                lineSeries { series(bandwidthHistory.map { bandwidth -> bandwidth.value }) }
            }
        }
    }


    if (enableMiniMode) {
        RealBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = remember(bandwidth.value) {
                    BandwidthFormatter.format(
                        bandwidth.value
                    ).string(2, true) + "/" + Global.appContext.getString(
                        R.string.unit_second
                    )
                },
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = 12.sp
                ),
                style = Typographies.titleSmall
            )
        }
    } else {
        RealColumn(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 2f)
                .padding(8.dp)
        ) {
            RealRow(
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                RealColumn(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = itemName,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        style = Typographies.titleSmall.copy(
                            fontSize = 12.sp
                        )
                    )
                }
                Text(
                    text = remember(bandwidth.value) {
                        BandwidthFormatter.format(
                            bandwidth.value
                        ).string(2, true) + "/" + Global.appContext.getString(
                            R.string.unit_second
                        )
                    },
                    style = Typographies.titleSmall.copy(
                        fontSize = 12.sp
                    )
                )
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        // 轴线的绘制风格
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill = fill(Colors.primaryContainer)),
                                stroke = LineCartesianLayer.LineStroke.continuous(
                                    thickness = 1.4.dp
                                )
                            )
                        ),
                        // 图表最大最小值计算
                        rangeProvider = remember { BandwidthCartesianLayerRangeProvider() },
                        verticalAxisPosition = Axis.Position.Vertical.Start,
                        // 表示尽可能地小，以让整个表格不需要滚动也能看到全部
                        pointSpacing = 0.1.dp
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        label = rememberAxisLabelComponent(
                            textSize = 8.sp
                        ),
                        itemPlacer = remember {
                            BandwidthVerticalAxisItemPlacer()
                        },
                        valueFormatter = remember { BandwidthCartesianValueFormatter() },
                        size = BaseAxis.Size.auto()
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = rememberAxisLabelComponent(
                            textSize = 8.sp
                        ),
                        itemPlacer = remember {
                            HorizontalAxis.ItemPlacer.aligned(
                                spacing = { 10 },
                                offset = { 0 },
                                shiftExtremeLines = false,
                                addExtremeLabelPadding = false,
                            )
                        },
                        size = BaseAxis.Size.auto()
                    )
                ),
                modelProducer = modelProducer,
                animationSpec = tween(durationMillis = 0)
            )
        }
    }
}
