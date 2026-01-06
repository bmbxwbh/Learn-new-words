
package com.wordflow.data.network

import com.wordflow.data.model.DictionaryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 封装 Retrofit API，处理网络请求和错误
class DictionaryService @Inject constructor(
    private val api: DictionaryApi
) {
    // 获取单个单词的定义
    suspend fun getWordDefinition(word: String): DictionaryResponse? {
        return try {
            withContext(Dispatchers.IO) {
                val response = api.getWordDefinition(word)
                // API 返回的是一个列表，我们取第一个
                response.firstOrNull()
            }
        } catch (e: Exception) {
            // 网络错误或API异常，返回null
            e.printStackTrace()
            null
        }
    }

    // 批量获取单词定义，返回一个 Map<单词, 定义>
    suspend fun fetchDefinitionsForWords(words: List<String>): Map<String, DictionaryResponse> {
        val resultMap = mutableMapOf<String, DictionaryResponse>()
        // 为了避免请求过多导致被封，可以进行分批处理
        words.chunked(10).forEach { batch ->
            batch.forEach { word ->
                val definition = getWordDefinition(word)
                if (definition != null) {
                    resultMap[word] = definition
                }
            }
            // 可以在这里添加小的延迟，避免请求过快
            kotlinx.coroutines.delay(200)
        }
        return resultMap
    }
}

