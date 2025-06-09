package com.android.soundrecorder.presentation.list

import java.io.File

data class RenameUiState(
    val showRenameDialog: Boolean = false,
    val recordedFile: File? = null,
    val suggestedName: String = ""
)