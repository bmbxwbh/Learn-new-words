
package com.wordflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBottomSheet(
    onManualAdd: () -> Unit,
    onPhotoImport: () -> Unit,
    onFileImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("添加单词", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onManualAdd, modifier = Modifier.fillMaxWidth()) {
            Text("手动输入")
        }
        Button(onClick = onPhotoImport, modifier = Modifier.fillMaxWidth()) {
            Text("拍照/扫描导入")
        }
        Button(onClick = onFileImport, modifier = Modifier.fillMaxWidth()) {
            Text("文件/网络导入")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

