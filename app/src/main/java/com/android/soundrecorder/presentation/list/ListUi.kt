@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.android.soundrecorder.presentation.list

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.android.soundrecorder.R
import com.android.soundrecorder.presentation.common.Colors


class ListUi {
    companion object {
        @Composable
        fun SelectionTopBar(
            selectionCount: Int,
            onDeleteClick: () -> Unit,
            onEditClick: () -> Unit,
            onCloseClick: () -> Unit
        ) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Colors.primaryColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
                title = {
                    if (selectionCount == 0)
                        Text(text = "Recording list")
                    else
                        Text(text = "$selectionCount selected")
                },
                navigationIcon = {
                    if (selectionCount != 0)
                        IconButton(onClick = onCloseClick) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                                contentDescription = "선택 종료"
                            )
                        }
                    else
                        Spacer(modifier = Modifier.size(24.dp))
                },
                actions = {
                    if (selectionCount == 0)
                        return@TopAppBar

                    // 이름 편집: 하나만 선택했을 때만 보이기
                    if (selectionCount == 1) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
                                contentDescription = "이름 편집"
                            )
                        }
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                            contentDescription = "삭제"
                        )
                    }
                }
            )
        }

        @Composable
        fun AudioControllerBar(
            modifier: Modifier = Modifier,
            fileName: String,
            currentPosition: Int,
            totalDuration: Int,
            isPlaying: Boolean,
            onTogglePlay: () -> Unit,
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatMillis(currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    LinearProgressIndicator(
                        progress = { (currentPosition / totalDuration.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp)),
                        color = Colors.primaryColor,
                        trackColor = Color(0xFFE0E0E0),
                        strokeCap = StrokeCap.Butt,
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = formatMillis(totalDuration),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = "Play/Pause",
                    )
                }
            }
        }

        fun formatMillis(millis: Int): String {
            val seconds = millis / 1000
            val m = seconds / 60
            val s = seconds % 60
            return "%02d:%02d".format(m, s)
        }

        @Composable
        fun RecordingItem(
            fileName: String,
            isSelected: Boolean,
            isInSelectionMode: Boolean,
            onClick: () -> Unit,
            onLongClick: () -> Unit,
            onCheckChanged: (Boolean) -> Unit
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                    ),
                headlineContent = {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingContent = {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xffAD4646),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_play_circle),
                        contentDescription = "Play/Pause",
                    )
                },
                trailingContent = {
                    if (isInSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = onCheckChanged,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Colors.primaryColor,
                            ),
                        )
                    }
                }
            )
        }
    }
}