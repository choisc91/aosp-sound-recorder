package com.android.soundrecorder.presentation.list

import java.io.File

data class PlayerUiState(
    val currentFile: File? = null,
    val currentPosition: Int = 0,
    val duration: Int = 0, // ✅ 기본값 0
    val isPlaying: Boolean = false
)