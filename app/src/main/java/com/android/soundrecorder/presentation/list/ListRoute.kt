@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.soundrecorder.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import com.android.soundrecorder.presentation.common.CommonUi.Companion.defaultComposable
import com.android.soundrecorder.entities.Route
import com.android.soundrecorder.presentation.list.ListUi.Companion.AudioControllerBar
import com.android.soundrecorder.presentation.list.ListUi.Companion.RecordingItem

fun NavGraphBuilder.routeList(
    viewModel: ListViewModel,
) {
    defaultComposable(route = Route.LIST) {
        RecordingListWithController(viewModel)
    }
}

@Composable
fun RecordingListWithController(
    viewModel: ListViewModel,
) {
    val context = LocalContext.current
    val playerUiState by viewModel.playerUiState.collectAsState()
    val files by viewModel.fileList.collectAsState()
    val renameUiState by viewModel.renameUiState.collectAsState()
    val selectionState by viewModel.selectionState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.loadFiles()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            ListUi.SelectionTopBar(
                selectionCount = selectionState.selectedFiles.size,
                onDeleteClick = { viewModel.deleteSelectedFiles() },
                onEditClick = {
                    val file = selectionState.selectedFiles.singleOrNull()
                    if (file != null)
                        viewModel.requestRename(file)
                },
                onCloseClick = { viewModel.clearSelection() }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            items(files, key = { it.absolutePath }) { file ->
                RecordingItem(
                    fileName = file.name,
                    isSelected = selectionState.selectedFiles.contains(file),
                    isInSelectionMode = selectionState.isInSelectionMode,
                    onClick = {
                        if (selectionState.isInSelectionMode) {
                            viewModel.toggleFileSelection(file)
                        } else {
                            viewModel.playOrToggle(file)
                        }
                    },
                    onLongClick = { viewModel.toggleFileSelection(file) },
                    onCheckChanged = { checked -> viewModel.toggleFileSelection(file, checked) } // ✅ 선택 제어용
                )
            }
        }

        // 하단 컨트롤러
        val showBottomSheet = !selectionState.isInSelectionMode && playerUiState.currentFile != null && playerUiState.duration > 0
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.resetAllState() },
                containerColor = Color(0xFFF8F9FA),
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                AudioControllerBar(
                    fileName = playerUiState.currentFile!!.name,
                    currentPosition = playerUiState.currentPosition,
                    totalDuration = playerUiState.duration,
                    isPlaying = playerUiState.isPlaying,
                    onTogglePlay = { viewModel.togglePlayPause() }
                )
            }
        }
    }

    if (renameUiState.showRenameDialog && renameUiState.recordedFile != null) {
        var text by remember { mutableStateOf(renameUiState.suggestedName) }
        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
            onDismissRequest = { viewModel.dismissRenameDialog() },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(size = 12.dp),
                        )
                        .padding(all = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Rename",
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("File name") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = { viewModel.dismissRenameDialog() },
                            content = { Text("DISCARD", color = Color(0xFFA5D8FF)) },
                        )

                        TextButton(
                            onClick = { viewModel.renameRecordingFile(renameUiState.recordedFile!!, text) },
                            content = { Text("SAVE", color = Color(0xFFFFA8A8)) },
                        )
                    }
                }
            },
        )
    }
}