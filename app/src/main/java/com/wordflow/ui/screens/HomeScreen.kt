
package com.wordflow.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wordflow.ui.components.ImportBottomSheet
import com.wordflow.ui.components.WordCard
import com.wordflow.ui.viewmodel.HomeViewModel
import com.wordflow.ui.viewmodel.ImportViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    importViewModel: ImportViewModel = hiltViewModel() // 共享 ImportViewModel
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val progress by viewModel.progress.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAnswer by remember { mutableStateOf(false) }

    // 监听来自 ImportViewModel 的状态，决定是否跳转
    val importState by importViewModel.importState.collectAsState()
    LaunchedEffect(importState) {
        when (importState) {
            is ImportViewModel.ImportState.OcrReady -> {
                val lines = (importState as ImportViewModel.ImportState.OcrReady).lines
                navController.navigate("curation") {
                    // 清理栈，避免返回时卡在奇怪的页面
                    popUpTo("home") { inclusive = false }
                }
                // 将数据传递给 CurationViewModel
                hiltViewModel<CurationViewModel>().processRawText(lines)
                importViewModel.resetState()
            }
            is ImportViewModel.ImportState.FileReady -> {
                val content = (importState as ImportViewModel.ImportState.FileReady).content
                navController.navigate("curation") {
                    popUpTo("home") { inclusive = false }
                }
                // 将数据传递给 CurationViewModel
                hiltViewModel<CurationViewModel>().processFileContent(content)
                importViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WordFlow") },
                actions = {
                    IconButton(onClick = { navController.navigate("library") }) {
                        Icon(Icons.Default.List, contentDescription = "Library")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Word")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            Text(text = "今日进度: ${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (currentWord == null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "恭喜！今日任务完成",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    WordCard(
                        word = currentWord!!,
                        isFlipped = showAnswer,
                        onCardClick = { showAnswer = !showAnswer }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showAnswer && currentWord != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            viewModel.recordReview(false)
                            showAnswer = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("忘记")
                    }
                    Button(
                        onClick = {
                            viewModel.recordReview(true)
                            showAnswer = false
                        }
                    ) {
                        Text("记得")
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            ImportBottomSheet(
                onManualAdd = {
                    // TODO: 实现手动添加对话框
                    showBottomSheet = false
                },
                onPhotoImport = {
                    importViewModel.startCameraFlow()
                    showBottomSheet = false
                },
                onFileImport = {
                    importViewModel.startFilePickerFlow()
                    showBottomSheet = false
                }
            )
        }
    }
}

