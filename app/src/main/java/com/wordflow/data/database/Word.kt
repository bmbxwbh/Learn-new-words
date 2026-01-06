
package com.wordflow.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val meaning: String,
    val phonetic: String? = null,
    val example: String? = null,
    val reviewCount: Int = 0,
    val nextReviewTime: Long = System.currentTimeMillis()
)

