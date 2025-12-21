package top.sankokomi.wirebare.ui.knet

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import top.sankokomi.wirebare.kernel.dashboard.Bandwidth
import top.sankokomi.wirebare.kernel.dashboard.WireBareDashboard
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.Typographies

@Composable
fun BandwidthDashboardChart() {
    val modelProducer = remember { CartesianChartModelProducer() }
    val bandwidthHistory = remember { mutableStateListOf<Bandwidth>() }
    var bandwidth by remember { mutableStateOf(Bandwidth(0.0, -1L)) }

    LaunchedEffect(Unit) {
        repeat(100) {
            bandwidthHistory.add(bandwidth)
        }
        modelProducer.runTransaction {
            lineSeries { series(bandwidthHistory.map { it.value }) }
        }
        WireBareDashboard.bandwidthFlow.collect {
            bandwidth = it
            if (bandwidthHistory.size >= 100) {
                bandwidthHistory.removeAt(0)
            }
            bandwidthHistory.add(it)
            modelProducer.runTransaction {
                lineSeries { series(bandwidthHistory.map { it.value }) }
            }
        }
    }

    RealColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = bandwidth.value.convertBandwidth(),
            style = Typographies.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            LineCartesianLayer.LineFill.single(fill = fill(Colors.primaryContainer)),
                            stroke = LineCartesianLayer.LineStroke.continuous(
                                thickness = 1.8.dp
                            )
                        )
                    ),
                    pointSpacing = 0.1.dp
                ),
                startAxis = VerticalAxis.rememberStart(
                    title = "速度 (KB/s)"
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    title = "时间"
                )
            ),
            modelProducer = modelProducer,
            animationSpec = tween(durationMillis = 0)
        )
    }
}

private fun Double.convertBandwidth(): String {
    val value = this
    return if (value <= 2 * 1024.0) {
        "${"%.2f".format(value)} B/s"
    } else if (value <= 2 * 1024.0 * 1024.0) {
        "${"%.2f".format(value / 1024.0)} KB/s"
    } else {
        "${"%.2f".format(value / 1024.0 / 1024.0)} MB/s"
    }
}
