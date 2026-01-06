
package com.wordflow.data.repository

import com.wordflow.data.database.Word
import com.wordflow.data.database.WordDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WordRepository @Inject constructor(
    private val wordDao: WordDao
) {
    // 获取下一个需要复习的单词
    fun getNextWordToReview(currentTime: Long): Flow<Word?> {
        return wordDao.getNextWordToReview(currentTime)
    }

    // 获取所有单词
    fun getAllWords(): Flow<List<Word>> {
        return wordDao.getAllWords()
    }

    // 插入单词
    suspend fun insertWord(word: Word) {
        wordDao.insertWord(word)
    }

    // 批量插入单词
    suspend fun insertAll(words: List<Word>) {
        wordDao.insertAll(words)
    }

    // 更新单词（例如，更新复习次数和下次复习时间）
    suspend fun updateWord(word: Word) {
        wordDao.updateWord(word)
    }
}

