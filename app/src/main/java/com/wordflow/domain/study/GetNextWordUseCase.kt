
package com.wordflow.domain.study

import com.wordflow.data.database.Word
import com.wordflow.data.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 获取下一个需要复习的单词的用例。
 * 这个用例封装了从仓库中获取单词的业务逻辑。
 */
class GetNextWordUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    /**
     * 执行此用例，返回一个 Flow<Word?>。
     * 调用方可以收集这个 Flow 来获取单词。
     */
    operator fun invoke(): Flow<Word?> {
        val currentTime = System.currentTimeMillis()
        return wordRepository.getNextWordToReview(currentTime)
    }
}

