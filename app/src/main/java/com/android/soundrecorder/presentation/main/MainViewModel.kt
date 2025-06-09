package com.android.soundrecorder.presentation.main

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext val context: Context,
) : ViewModel() {

    private val _recordUiStateFLow = MutableStateFlow(value = RecordUiState.default())
    val recordUiStateFlow = _recordUiStateFLow.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime = 0L
    private var recordingJob: Job? = null
    private var elapsedBeforePause: Long = 0L

    private fun setUiState(uiState: RecordUiState) {
        _recordUiStateFLow.value = uiState
    }

    fun startRecord() {
        try {
            stopMediaRecorderSafely()

            val fileName = "recording_${System.currentTimeMillis()}.m4a"
            val file = File(context.getExternalFilesDir(null), fileName)

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            outputFile = file
            startTime = System.currentTimeMillis()

            setUiState(_recordUiStateFLow.value.start())
            startMonitoring()

        } catch (e: Exception) {
            Log.e("MainViewModel", "startRecording failed: ${e.message}", e)
        }
    }

    fun pauseRecord() {
        try {
            mediaRecorder?.pause()
            elapsedBeforePause += System.currentTimeMillis() - startTime
            stopMonitoring()
            setUiState(_recordUiStateFLow.value.pause())

        } catch (e: Exception) {
            Log.e("MainViewModel", "pauseRecording failed: ${e.message}", e)
        }
    }

    fun stopRecord() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            stopMonitoring()

            val savedFile = outputFile?.let { moveFileToPermanentLocation(it) }

            savedFile?.let {
                setUiState(_recordUiStateFLow.value.stop(it))
            }

            elapsedBeforePause = 0L

        } catch (e: Exception) {
            Log.e("MainViewModel", "stopRecording failed: ${e.message}", e)
        }
    }

    fun dismissRenameDialog() {
        setUiState(uiState = _recordUiStateFLow.value.clearFile())
    }

    fun renameRecordingFile(file: File, newName: String) {
        val dir = file.parentFile ?: return
        val sanitized = newName.trim().replace(Regex("[^a-zA-Z0-9-_ ]"), "_")
        val newFile = File(dir, "$sanitized.m4a")
        try {
            if (file.renameTo(newFile)) {
                Toast.makeText(context, "Saved as \"$sanitized.m4a\"", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        dismissRenameDialog()
    }

    private fun startMonitoring() {
        recordingJob = viewModelScope.launch {
            var lastSecond = _recordUiStateFLow.value.recordTime.toSeconds()

            while (isActive && mediaRecorder != null) {
                delay(250)

                val amplitude = mediaRecorder?.maxAmplitude ?: 0
                val currentMillis = System.currentTimeMillis()
                val totalElapsedSeconds =
                    ((elapsedBeforePause + (currentMillis - startTime)) / 1000).toInt()

                if (totalElapsedSeconds > lastSecond) {
                    lastSecond = totalElapsedSeconds
                    setUiState(_recordUiStateFLow.value.incrementRecordTime().copy(dummyAmplitude = amplitude))
                } else {
                    setUiState(_recordUiStateFLow.value.copy(dummyAmplitude = amplitude))
                }
            }
        }
    }

    private fun stopMonitoring() {
        recordingJob?.cancel()
        recordingJob = null
    }

    private fun stopMediaRecorderSafely() {
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun generateFileName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        return "record_${sdf.format(System.currentTimeMillis())}.m4a"
    }

    private fun moveFileToPermanentLocation(tempFile: File): File? {
        return try {
            val dir = File(Environment.getExternalStorageDirectory(), "SoundRecorder")
            if (!dir.exists())
                dir.mkdirs()

            val newFile = File(dir, generateFileName())
            tempFile.copyTo(newFile, overwrite = true)
            tempFile.delete()
            newFile
        } catch (e: Exception) {
            Log.e("MainViewModel", "moveFile failed: ${e.message}", e)
            null
        }
    }

    private fun String.toSeconds(): Int {
        return split(":").let {
            val min = it.getOrNull(0)?.toIntOrNull() ?: 0
            val sec = it.getOrNull(1)?.toIntOrNull() ?: 0
            min * 60 + sec
        }
    }

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
}