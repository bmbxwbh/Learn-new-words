
package com.wordflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordflow.data.database.Word
import com.wordflow.data.repository.WordRepository
import com.wordflow.domain.study.GetNextWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNextWordUseCase: GetNextWordUseCase,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _currentWord = MutableStateFlow<Word?>(null)
    val currentWord: StateFlow<Word?> = _currentWord.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    // 假设每日目标是20个新词
    private val dailyGoal = 20
    private var learnedCount = 0

    init {
        loadNextWord()
    }

    fun loadNextWord() {
        viewModelScope.launch {
            getNextWordUseCase().collect { word ->
                _currentWord.value = word
            }
        }
    }

    fun recordReview(isRemembered: Boolean) {
        val word = _currentWord.value ?: return
        viewModelScope.launch {
            // 简单的艾宾浩斯算法实现
            val newReviewCount = word.reviewCount + 1
            val interval = when {
                newReviewCount == 1 -> 1 * 60 * 60 * 1000L // 1小时
                newReviewCount == 2 -> 24 * 60 * 60 * 1000L // 1天
                else -> 3 * 24 * 60 * 60 * 1000L // 3天
            }
            val updatedWord = word.copy(
                reviewCount = newReviewCount,
                nextReviewTime = System.currentTimeMillis() + interval
            )
            wordRepository.updateWord(updatedWord)

            // 更新进度
            if (isRemembered) {
                learnedCount++
                _progress.value = (learnedCount.toFloat() / dailyGoal).coerceAtMost(1.0f)
            }

            // 加载下一个
            loadNextWord()
        }
    }
}

