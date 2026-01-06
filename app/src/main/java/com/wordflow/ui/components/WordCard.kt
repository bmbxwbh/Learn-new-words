
package com.wordflow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wordflow.data.database.Word

@Composable
fun WordCard(
    word: Word,
    isFlipped: Boolean = false,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!isFlipped) {
                Text(word.word, style = MaterialTheme.typography.displayLarge)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(word.meaning, style = MaterialTheme.typography.headlineMedium)
                    word.example?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

