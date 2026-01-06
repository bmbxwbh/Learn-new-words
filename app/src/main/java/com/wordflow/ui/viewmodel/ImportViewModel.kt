
package com.wordflow.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    // UI层调用此函数来启动相机流程
    fun startCameraFlow() {
        _importState.value = ImportState.RequestCamera
    }

    // UI层调用此函数来启动文件选择流程
    fun startFilePickerFlow() {
        _importState.value = ImportState.RequestFile
    }

    // 接收相机返回的图片URI并进行OCR处理
    fun handleCameraResult(imageUri: Uri?) {
        if (imageUri == null) {
            _importState.value = ImportState.Error("拍照失败")
            return
        }
        viewModelScope.launch {
            try {
                _importState.value = ImportState.Processing
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image = android.graphics.BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
                val visionText = recognizer.process(image).await()
                
                val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text }
                _importState.value = ImportState.OcrReady(lines)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("OCR识别失败: ${e.message}")
            }
        }
    }

    // 接收文件选择返回的URI并读取内容
    fun handleFileResult(fileUri: Uri?) {
        if (fileUri == null) {
            _importState.value = ImportState.Error("文件选择失败")
            return
        }
        viewModelScope.launch {
            try {
                _importState.value = ImportState.Processing
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()
                _importState.value = ImportState.FileReady(content)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("文件读取失败: ${e.message}")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }

    sealed class ImportState {
        object Idle : ImportState()
        object RequestCamera : ImportState()
        object RequestFile : ImportState()
        object Processing : ImportState()
        data class OcrReady(val lines: List<String>) : ImportState()
        data class FileReady(val content: String) : ImportState()
        data class Error(val message: String) : ImportState()
    }
}

