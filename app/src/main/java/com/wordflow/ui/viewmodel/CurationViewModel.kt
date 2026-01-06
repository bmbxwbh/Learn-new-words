
package com.wordflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordflow.data.database.Word
import com.wordflow.data.repository.ImportRepository
import com.wordflow.domain.import.ImportSuggestion
import com.wordflow.domain.import.ProcessFileImportUseCase
import com.wordflow.domain.import.ProcessOcrResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurationViewModel @Inject constructor(
    private val processOcrResultUseCase: ProcessOcrResultUseCase,
    private val processFileImportUseCase: ProcessFileImportUseCase,
    private val importRepository: ImportRepository
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<ImportSuggestion>>(emptyList())
    val suggestions: StateFlow<List<ImportSuggestion>> = _suggestions

    private val confirmedWords = mutableListOf<Word>()

    // 从相机/OCR来的数据
    fun processRawText(lines: List<String>) {
        viewModelScope.launch {
            _suggestions.value = processOcrResultUseCase(lines)
        }
    }

    // 从文件导入来的数据
    fun processFileContent(content: String) {
        viewModelScope.launch {
            _suggestions.value = processFileImportUseCase(content)
        }
    }

    fun acceptSuggestion(suggestion: ImportSuggestion) {
        val newWord = Word(
            word = suggestion.english,
            meaning = suggestion.suggestedChinese ?: suggestion.originalChinese
        )
        confirmedWords.add(newWord)
        _suggestions.value = _suggestions.value.filter { it != suggestion }
    }

    fun rejectSuggestion(suggestion: ImportSuggestion) {
        val newWord = Word(
            word = suggestion.english,
            meaning = suggestion.originalChinese
        )
        confirmedWords.add(newWord)
        _suggestions.value = _suggestions.value.filter { it != suggestion }
    }

    fun saveToLibrary() {
        viewModelScope.launch {
            importRepository.saveWords(confirmedWords)
            confirmedWords.clear()
        }
    }
}

