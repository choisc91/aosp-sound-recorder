package com.android.soundrecorder.presentation.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.android.soundrecorder.R
import com.android.soundrecorder.presentation.common.Colors
import kotlinx.coroutines.delay
import kotlin.math.max

class MainUi {
    companion object {
        @Composable
        fun VuMeter(
            isRecording: Boolean,
            amplitudeProvider: () -> Int,
            displayTime: String,
            displayLabel: String
        ) {
            var angle by remember { mutableFloatStateOf(0f) }
            val maxAmplitude = 32768f
            val minAngle = -0.75f * Math.PI.toFloat()
            val maxAngle = 0.75f * Math.PI.toFloat()
            val dropOffStep = 0.18f

            val dashLength = 10f
            val gapLength = 10f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength))

            LaunchedEffect(isRecording) {
                while (isRecording) {
                    val amplitude = amplitudeProvider().coerceIn(0, maxAmplitude.toInt())
                    val targetAngle = minAngle + (maxAngle - minAngle) * (amplitude / maxAmplitude)
                    angle = if (targetAngle >= angle) {
                        targetAngle
                    } else {
                        max(targetAngle, angle - dropOffStep)
                    }
                    delay(70)
                }
            }

            Box(
                modifier = Modifier.size(320.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = size.minDimension
                    val center = Offset(size.width / 2, size.height / 2)
                    val baseRadius = canvasSize / 2
                    val progressRadius = baseRadius * 0.70f
                    val borderRadius = baseRadius * 0.90f // 좀 더 바깥으로 띄움
                    val stroke = 12f
                    val borderStroke = 4f

                    val startAngle = 180 + minAngle.toDegrees()
                    val fullSweep = maxAngle.toDegrees() - minAngle.toDegrees()
                    val sweepDegrees = angle.toDegrees() - minAngle.toDegrees()

                    // 1. 외곽선 (더 옅은 색, 더 바깥에 위치)
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        startAngle = startAngle,
                        sweepAngle = fullSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - borderRadius, center.y - borderRadius),
                        size = Size(borderRadius * 2, borderRadius * 2),
                        style = Stroke(width = borderStroke)
                    )

                    // 2. 점선 배경
                    drawArc(
                        color = Color.LightGray,
                        startAngle = startAngle,
                        sweepAngle = fullSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - progressRadius, center.y - progressRadius),
                        size = Size(progressRadius * 2, progressRadius * 2),
                        style = Stroke(width = stroke, pathEffect = dashEffect)
                    )

                    // 3. 점선 프로그레스 바
                    if (isRecording && sweepDegrees > 0) {
                        drawArc(
                            color = Colors.primaryColor,
                            startAngle = startAngle,
                            sweepAngle = sweepDegrees,
                            useCenter = false,
                            topLeft = Offset(center.x - progressRadius, center.y - progressRadius),
                            size = Size(progressRadius * 2, progressRadius * 2),
                            style = Stroke(width = stroke, pathEffect = dashEffect)
                        )
                    }
                }

                // 4. 중앙 텍스트
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        @Composable
        fun RecorderPortraitButtonPanel(
            isRecording: Boolean,
            onListClick: () -> Unit,
            onPause: () -> Unit,
            onStart: () -> Unit,
            onStop: () -> Unit,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onListClick,
                    enabled = !isRecording,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_playlist),
                        contentDescription = "List",
                        tint = if (isRecording) Color(0xFFCCCCCC) else Color.Black // ✅ 회색 처리
                    )
                }

                Spacer(modifier = Modifier.width(40.dp))

                Button(
                    modifier = Modifier.size(64.dp),
                    onClick = { if (isRecording) onPause() else onStart() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        tint = if (isRecording) Color.DarkGray else Color.Red,
                        imageVector = ImageVector.vectorResource(id = if (isRecording) R.drawable.ic_pause else R.drawable.ic_record),
                        contentDescription = if (isRecording) "Pause" else "Record",
                    )
                }

                Spacer(modifier = Modifier.width(40.dp))

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onStop,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_stop),
                        contentDescription = "Stop",
                        tint = Color.DarkGray,
                    )
                }
            }
        }

        @Composable
        fun RecorderLandscapeButtonPanel(
            isRecording: Boolean,
            onListClick: () -> Unit,
            onPause: () -> Unit,
            onStart: () -> Unit,
            onStop: () -> Unit,
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onListClick,
                    enabled = !isRecording,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_playlist),
                        contentDescription = "List",
                        tint = if (isRecording) Color(0xFFCCCCCC) else Color.Black // ✅ 회색 처리
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    modifier = Modifier.size(64.dp),
                    onClick = { if (isRecording) onPause() else onStart() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        tint = if (isRecording) Color.DarkGray else Color.Red,
                        imageVector = ImageVector.vectorResource(id = if (isRecording) R.drawable.ic_pause else R.drawable.ic_record),
                        contentDescription = if (isRecording) "Pause" else "Record",
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = onStop,
                ) {

                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_stop),
                        contentDescription = "Stop",
                        tint = Color.DarkGray,
                    )
                }
            }
        }

        private fun Float.toDegrees(): Float = (this / Math.PI.toFloat()) * 180
    }
}