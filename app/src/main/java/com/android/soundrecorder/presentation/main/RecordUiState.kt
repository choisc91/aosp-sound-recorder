package com.android.soundrecorder.presentation.main

import java.io.File
import java.util.Locale

data class RecordUiState(
    val recordTime: String,
    val isRecording: Boolean,
    val recordState: String,
    val dummyAmplitude: Int,
    //
    val showRenameDialog: Boolean = false,
    val recordedFile: File? = null,
    val suggestedName: String = "",
) {
    companion object {
        fun default(): RecordUiState = RecordUiState(
            recordState = "Press Record",
            isRecording = false,
            recordTime = "00:00",
            dummyAmplitude = 0,
        )
    }

    fun start(): RecordUiState = this.copy(
        isRecording = true,
        recordState = "Recording...",
        dummyAmplitude = 0
    )

    fun pause(): RecordUiState = this.copy(
        isRecording = false,
        recordState = "Paused"
    )

    fun stop(file: File): RecordUiState = this.copy(
        isRecording = false,
        recordState = "Stopped",
        dummyAmplitude = 0,
        recordTime = "00:00",
        showRenameDialog = true,
        recordedFile = file,
        suggestedName = file.nameWithoutExtension
    )

    fun incrementRecordTime(): RecordUiState {
        val (minutes, seconds) = recordTime.split(":").map { it.toInt() }
        val totalSeconds = minutes * 60 + seconds + 1
        val newTime = String.format(Locale.getDefault(), "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
        return this.copy(recordTime = newTime)
    }

    fun clearFile(): RecordUiState = this.copy(
        showRenameDialog = false,
        recordedFile = null,
        suggestedName = ""
    )
}