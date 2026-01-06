
package com.wordflow.data.repository

import com.wordflow.data.database.Word
import javax.inject.Inject

// 处理导入相关的数据逻辑
class ImportRepository @Inject constructor(
    private val wordRepository: WordRepository
) {
    // 保存经过校对的单词列表
    suspend fun saveWords(words: List<Word>) {
        wordRepository.insertAll(words)
    }

    // 核心算法：对OCR识别出的文本行进行初步配对
    // 假设格式为：一行英文，下一行中文
    fun pairTextLines(lines: List<String>): List<Pair<String, String>> {
        val pairs = mutableListOf<Pair<String, String>>()
        val cleanedLines = lines.map { it.trim() }.filter { it.isNotEmpty() }

        var i = 0
        while (i < cleanedLines.size - 1) {
            // 简单的启发式规则：如果下一行包含较多中文字符，则配对
            val currentLine = cleanedLines[i]
            val nextLine = cleanedLines[i+1]

            if (isLikelyEnglish(currentLine) && isLikelyChinese(nextLine)) {
                pairs.add(currentLine to nextLine)
                i += 2 // 跳过这两行
            } else {
                i += 1 // 无法配对，继续检查下一行
            }
        }
        return pairs
    }

    // 判断是否为英文（简单判断：包含字母）
    private fun isLikelyEnglish(text: String): Boolean {
        return text.matches(Regex(".*[a-zA-Z].*"))
    }

    // 判断是否为中文（简单判断：包含中文字符）
    private fun isLikelyChinese(text: String): Boolean {
        return text.matches(Regex(".*[\u4e00-\u9fa5].*"))
    }
}

