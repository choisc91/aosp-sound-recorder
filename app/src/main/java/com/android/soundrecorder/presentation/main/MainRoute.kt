package com.android.soundrecorder.presentation.main

import android.Manifest
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.android.soundrecorder.presentation.common.CommonUi.Companion.defaultComposable
import com.android.soundrecorder.entities.Route

fun NavGraphBuilder.routeMain(
    controller: NavController,
    viewModel: MainViewModel,
) {
    defaultComposable(route = Route.MAIN) {
        RouteMain(
            controller = controller,
            viewModel = viewModel,
        )
    }
}

@Composable
fun RouteMain(
    controller: NavController,
    viewModel: MainViewModel,
) {
    val context = LocalContext.current
    val uiState by viewModel.recordUiStateFlow.collectAsState()

    // 퍼미션 요청 런처 선언
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecord()
        } else {
            Toast.makeText(context, "android.permission.RECORD_AUDIO Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { innerPadding ->

        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MainUi.VuMeter(
                    isRecording = uiState.isRecording,
                    amplitudeProvider = { uiState.dummyAmplitude },
                    displayTime = uiState.recordTime,
                    displayLabel = uiState.recordState,
                )

                Spacer(modifier = Modifier.height(16.dp))

                MainUi.RecorderPortraitButtonPanel(
                    isRecording = uiState.isRecording,
                    onStart = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onPause = { viewModel.pauseRecord() },
                    onStop = { viewModel.stopRecord() },
                    onListClick = { controller.navigate(route = Route.LIST) },
                )
            }

        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(32.dp))

                MainUi.VuMeter(
                    isRecording = uiState.isRecording,
                    amplitudeProvider = { uiState.dummyAmplitude },
                    displayTime = uiState.recordTime,
                    displayLabel = uiState.recordState,
                )

                Spacer(modifier = Modifier.weight(1f))

                MainUi.RecorderLandscapeButtonPanel(
                    isRecording = uiState.isRecording,
                    onStart = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onPause = { viewModel.pauseRecord() },
                    onStop = { viewModel.stopRecord() },
                    onListClick = { controller.navigate(route = Route.LIST) },
                )

                Spacer(modifier = Modifier.width(32.dp))
            }
        }
    }

    if (uiState.showRenameDialog && uiState.recordedFile != null) {
        var text by remember { mutableStateOf(uiState.suggestedName) }
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
                        text = "Save recording?",
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(color = 0xFF333333),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("File name") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Spacer(modifier = Modifier.weight(1f))

                            TextButton(
                                onClick = { viewModel.dismissRenameDialog() },
                                content = { Text("DISCARD", color = Color(0xffA5D8FF)) },
                            )

                            TextButton(
                                onClick = { viewModel.renameRecordingFile(uiState.recordedFile!!, text) },
                                content = { Text("SAVE", color = Color(0xffFFA8A8)) },
                            )
                        },
                    )
                }
            },
        )
    }
}