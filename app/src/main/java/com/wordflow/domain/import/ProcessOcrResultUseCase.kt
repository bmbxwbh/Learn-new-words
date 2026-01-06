
package com.wordflow.domain.import

import com.wordflow.data.network.DictionaryService
import com.wordflow.data.repository.ImportRepository
import javax.inject.Inject

class ProcessOcrResultUseCase @Inject constructor(
    private val importRepository: ImportRepository,
    private val dictionaryService: DictionaryService
) {
    suspend operator fun invoke(rawTextLines: List<String>): List<ImportSuggestion> {
        val initialPairs = importRepository.pairTextLines(rawTextLines)
        val englishWords = initialPairs.map { it.first }
        val apiResults = dictionaryService.fetchDefinitionsForWords(englishWords)

        return initialPairs.map { (english, chinese) ->
            val apiResult = apiResults[english]
            if (apiResult != null) {
                val suggestedMeaning = apiResult.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition
                if (suggestedMeaning != null && isSimilar(chinese, suggestedMeaning)) {
                    ImportSuggestion(english, chinese, suggestedMeaning, ConfidenceLevel.HIGH)
                } else {
                    ImportSuggestion(english, chinese, suggestedMeaning, ConfidenceLevel.REVIEW)
                }
            } else {
                ImportSuggestion(english, chinese, null, ConfidenceLevel.NOT_FOUND)
            }
        }
    }

    private fun isSimilar(text1: String, text2: String): Boolean {
        return text1.contains(text2) || text2.contains(text1)
    }
}

enum class ConfidenceLevel { HIGH, REVIEW, NOT_FOUND }

data class ImportSuggestion(
    val english: String,
    val originalChinese: String,
    val suggestedChinese: String?,
    val confidence: ConfidenceLevel
)

