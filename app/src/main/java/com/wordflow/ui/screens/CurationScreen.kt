
package com.wordflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wordflow.domain.import.ConfidenceLevel
import com.wordflow.domain.import.ImportSuggestion
import com.wordflow.ui.viewmodel.CurationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurationScreen(
    navController: NavController,
    viewModel: CurationViewModel = hiltViewModel()
) {
    val suggestions by viewModel.suggestions.collectAsState()
    val groupedSuggestions = suggestions.groupBy { it.confidence }

    Scaffold(
        topBar = { TopAppBar(title = { Text("智能校对") }) },
        bottomBar = {
            if (suggestions.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.saveToLibrary()
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("保存到词库")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 需确认项 (优先展示)
            groupedSuggestions[ConfidenceLevel.REVIEW]?.let { list ->
                item {
                    Text("需确认项 (${list.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                }
                items(list) { suggestion ->
                    ReviewSuggestionCard(
                        suggestion = suggestion,
                        onAccept = { viewModel.acceptSuggestion(suggestion) },
                        onReject = { viewModel.rejectSuggestion(suggestion) }
                    )
                }
            }

            // 高置信度项
            groupedSuggestions[ConfidenceLevel.HIGH]?.let { list ->
                item {
                    Text("自动通过项 (${list.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                items(list) { suggestion ->
                    HighConfidenceCard(suggestion)
                }
            }

            // 未找到项
            groupedSuggestions[ConfidenceLevel.NOT_FOUND]?.let { list ->
                item {
                    Text("未找到项 (${list.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                }
                items(list) { suggestion ->
                    NotFoundCard(suggestion)
                }
            }
        }
    }
}

@Composable
fun ReviewSuggestionCard(suggestion: ImportSuggestion, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(suggestion.english, style = MaterialTheme.typography.titleLarge)
            Text("原文: ${suggestion.originalChinese}", style = MaterialTheme.typography.bodyMedium)
            Text("建议: ${suggestion.suggestedChinese ?: "无"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onReject) { Text("保留原文") }
                Button(onClick = onAccept) { Text("采纳建议") }
            }
        }
    }
}

@Composable
fun HighConfidenceCard(suggestion: ImportSuggestion) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(suggestion.english, style = MaterialTheme.typography.titleLarge)
            Text(suggestion.suggestedChinese ?: suggestion.originalChinese, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun NotFoundCard(suggestion: ImportSuggestion) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(suggestion.english, style = MaterialTheme.typography.titleLarge)
            Text("原文: ${suggestion.originalChinese}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

