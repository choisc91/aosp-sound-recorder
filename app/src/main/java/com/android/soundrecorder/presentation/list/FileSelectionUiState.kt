package com.android.soundrecorder.presentation.list

import java.io.File

data class FileSelectionUiState(
    val selectedFiles: Set<File> = emptySet()
) {
    fun toggle(file: File): FileSelectionUiState {
        return if (selectedFiles.contains(file)) {
            copy(selectedFiles = selectedFiles - file)
        } else {
            copy(selectedFiles = selectedFiles + file)
        }
    }

    fun setSelection(file: File, selected: Boolean): FileSelectionUiState {
        return if (selected) {
            copy(selectedFiles = selectedFiles + file)
        } else {
            copy(selectedFiles = selectedFiles - file)
        }
    }

    fun isSelected(file: File): Boolean {
        return selectedFiles.contains(file)
    }

    fun clear(): FileSelectionUiState = copy(selectedFiles = emptySet())

    val isInSelectionMode: Boolean
        get() = selectedFiles.isNotEmpty()

    val isSingleSelection: Boolean
        get() = selectedFiles.size == 1
}