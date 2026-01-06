
package com.wordflow

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wordflow.ui.screens.CurationScreen
import com.wordflow.ui.screens.HomeScreen
import com.wordflow.ui.screens.LibraryScreen
import com.wordflow.ui.screens.SettingsScreen
import com.wordflow.ui.theme.WordFlowTheme
import com.wordflow.ui.viewmodel.ImportViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var tempImageUri: Uri? = null

    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    // 相机启动器
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // 通知 ViewModel 处理结果
            val importViewModel = hiltViewModel<ImportViewModel>()
            importViewModel.handleCameraResult(tempImageUri)
        }
    }

    // 文件选择启动器
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val importViewModel = hiltViewModel<ImportViewModel>()
            importViewModel.handleFileResult(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 获取 ViewModel 实例
                    val importViewModel: ImportViewModel = hiltViewModel()
                    val context = LocalContext.current

                    // 监听 ViewModel 的请求状态
                    val importState by importViewModel.importState.collectAsState()

                    LaunchedEffect(importState) {
                        when (importState) {
                            is ImportViewModel.ImportState.RequestCamera -> {
                                // 请求权限
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                // 创建临时文件并启动相机
                                if (tempImageUri == null) {
                                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                    val fileName = "IMG_${sdf.format(Date())}.jpg"
                                    val tempFile = File(context.filesDir, fileName)
                                    tempImageUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        tempFile
                                    )
                                }
                                cameraLauncher.launch(tempImageUri)
                                // 重置状态，防止重复触发
                                importViewModel.resetState()
                            }
                            is ImportViewModel.ImportState.RequestFile -> {
                                filePickerLauncher.launch("text/*") // 筛选文本文件
                                importViewModel.resetState()
                            }
                            is ImportViewModel.ImportState.Error -> {
                                val message = (importState as ImportViewModel.ImportState.Error).message
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                importViewModel.resetState()
                            }
                            else -> {}
                        }
                    }

                    WordFlowApp()
                }
            }
        }
    }
}

@Composable
fun WordFlowApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("library") {
            LibraryScreen(navController = navController)
        }
        composable("curation") {
            CurationScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}

