package com.android.soundrecorder.presentation.list

import android.content.Context
import android.media.MediaPlayer
import android.os.Environment
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ListViewModel : ViewModel() {
    private val _selectionState = MutableStateFlow(value = FileSelectionUiState())
    val selectionState = _selectionState.asStateFlow()

    private val _fileList = MutableStateFlow(value = emptyList<File>())
    val fileList = _fileList.asStateFlow()

    private val _renameUiState = MutableStateFlow(value = RenameUiState())
    val renameUiState = _renameUiState.asStateFlow()

    private val _playerUiState = MutableStateFlow(value = PlayerUiState())
    val playerUiState = _playerUiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    private var progressJob: Job? = null

    fun setPlayerUiState(newState: PlayerUiState) {
        _playerUiState.value = newState
    }

    fun requestRename(file: File) {
        _renameUiState.value = RenameUiState(
            showRenameDialog = true,
            recordedFile = file,
            suggestedName = file.nameWithoutExtension
        )
    }

    fun dismissRenameDialog() {
        _renameUiState.value = RenameUiState()
    }

    fun renameRecordingFile(file: File, newName: String) {
        val ext = file.extension
        val newFile = File(file.parent, "$newName.$ext")

        if (newFile.exists())
            return

        if (file.renameTo(newFile)) {
            loadFiles()
            _selectionState.value = FileSelectionUiState()
            dismissRenameDialog()
        }
    }

    fun loadFiles() {
        val dir = File(Environment.getExternalStorageDirectory(), "SoundRecorder")
        if (!dir.exists())
            dir.mkdirs()

        val supportedExtensions = listOf("m4a", "aac", "amr", "3gpp")
        val files = dir.listFiles { file ->
            file.extension.lowercase() in supportedExtensions
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        _fileList.value = files
    }

    fun resetAllState() {
        stopPlayback()
        _playerUiState.value = PlayerUiState()
        _selectionState.value = FileSelectionUiState()
        _renameUiState.value = RenameUiState()
    }

    fun toggleFileSelection(file: File, checked: Boolean? = null) {
        val current = _selectionState.value.selectedFiles.toMutableSet()
        val shouldSelect = checked ?: !current.contains(file)

        if (shouldSelect) current.add(file) else current.remove(file)

        val newState = FileSelectionUiState(selectedFiles = current)

        // ✅ 선택 모드 진입 시 재생 중지
        if (newState.isInSelectionMode && !_selectionState.value.isInSelectionMode) {
            stopPlayback()
        }

        _selectionState.value = newState
    }

    fun clearSelection() {
        _selectionState.value = _selectionState.value.clear()
    }

    fun deleteSelectedFiles() {
        val selected = _selectionState.value.selectedFiles
        selected.forEach { it.delete() }
        _selectionState.value = FileSelectionUiState()
        loadFiles()
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopProgressJob()
        _playerUiState.value = PlayerUiState() // 초기화
    }

    fun playOrToggle(file: File) {
        val isSameFile = _playerUiState.value.currentFile?.absolutePath == file.absolutePath
        val isEnded = _playerUiState.value.currentPosition >= _playerUiState.value.duration

        if (isSameFile && !isEnded) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    stopProgressJob()
                    _playerUiState.value = _playerUiState.value.copy(isPlaying = false)
                } else {
                    it.start()
                    startProgressJob()
                    _playerUiState.value = _playerUiState.value.copy(isPlaying = true)
                }
            }
            return
        }

        // 새로 재생 시작
        mediaPlayer?.stop()
        mediaPlayer?.release()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            seekTo(0) // ✅ 새로 재생할 때 무조건 처음부터 시작
            start()

            setOnCompletionListener {
                stopProgressJob()
                mediaPlayer?.seekTo(0)
                _playerUiState.value = _playerUiState.value.copy(
                    isPlaying = false,
                    currentPosition = 0 // ✅ 반드시 0으로 리셋
                )
            }
        }

        _playerUiState.value = PlayerUiState(
            currentFile = file,
            currentPosition = 0,
            duration = mediaPlayer?.duration ?: 0,
            isPlaying = true
        )

        startProgressJob()
    }

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                stopProgressJob()
                _playerUiState.value = _playerUiState.value.copy(isPlaying = false)
            } else {
                it.start()
                startProgressJob()
                _playerUiState.value = _playerUiState.value.copy(isPlaying = true)
            }
        }
    }

    private fun startProgressJob() {
        stopProgressJob()
        progressJob = viewModelScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                val pos = mediaPlayer?.currentPosition ?: 0
                _playerUiState.value = _playerUiState.value.copy(currentPosition = pos)
                delay(500)
            }
        }
    }

    private fun stopProgressJob() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        stopProgressJob()
    }
}