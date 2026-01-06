
package com.wordflow.domain.import

import com.wordflow.data.database.Word
import com.wordflow.data.network.DictionaryService
import com.wordflow.data.repository.ImportRepository
import javax.inject.Inject

/**
 * 处理从文件导入的单词列表的用例。
 * 此用例负责解析文件内容，进行初步配对，并与网络API进行校对。
 */
class ProcessFileImportUseCase @Inject constructor(
    private val importRepository: ImportRepository,
    private val dictionaryService: DictionaryService
) {
    /**
     * 执行此用例。
     * @param rawText 从文件中读取的原始文本内容。
     * @return 一个包含 ImportSuggestion 的列表，供 UI 层展示和用户确认。
     */
    suspend operator fun invoke(rawText: String): List<ImportSuggestion> {
        // 1. 将文件内容按行分割，并进行初步清洗
        val lines = rawText.lines()

        // 2. 使用 ImportRepository 的算法进行初步配对
        // 这个算法假设文件格式是 "英文
中文
英文
中文" 或类似
        val initialPairs = importRepository.pairTextLines(lines)

        // 3. 批量查询 API 进行校对
        val englishWords = initialPairs.map { it.first }
        val apiResults = dictionaryService.fetchDefinitionsForWords(englishWords)

        // 4. 生成建议列表
        return initialPairs.map { (english, chinese) ->
            val apiResult = apiResults[english]
            if (apiResult != null) {
                // API 有结果，获取建议的释义
                val suggestedMeaning = apiResult.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition
                if (suggestedMeaning != null && isSimilar(chinese, suggestedMeaning)) {
                    // 相似度高，自动通过
                    ImportSuggestion(english, chinese, suggestedMeaning, ConfidenceLevel.HIGH)
                } else {
                    // 不相似，需要用户确认
                    ImportSuggestion(english, chinese, suggestedMeaning, ConfidenceLevel.REVIEW)
                }
            } else {
                // API 未找到该单词
                ImportSuggestion(english, chinese, null, ConfidenceLevel.NOT_FOUND)
            }
        }
    }

    /**
     * 简单的相似度判断算法。
     * 实际项目中可以使用更复杂的算法，如 Jaccard 相似度、编辑距离等。
     */
    private fun isSimilar(text1: String, text2: String): Boolean {
        // 如果两个字符串都包含对方，或者长度相近且重叠度高，则认为相似
        // 这里使用一个非常简单的实现：只要一个包含另一个，就认为相似
        return text1.contains(text2) || text2.contains(text1)
    }
}

