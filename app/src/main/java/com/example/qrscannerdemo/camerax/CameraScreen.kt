package com.example.qrscannerdemo.camerax

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qrscannerdemo.util.filterInContainer
import com.example.qrscannerdemo.util.pickMinimumDeviationFromCenter
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning

@Composable
fun CameraScreen() {
    val viewModel = remember { CameraViewModel() }
    val text by viewModel.cameraStringFlow.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }

    val barcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .build()
    )

    val cameraController = LifecycleCameraController(context)
        .apply {
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                MlKitAnalyzer(
                    listOf(barcodeScanner),
                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED ,
                    ContextCompat.getMainExecutor(context)
                ) { result ->
                    result?.getValue(barcodeScanner)
                        ?.filterInContainer(previewView)
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { barcodes ->
                            barcodes.pickMinimumDeviationFromCenter(previewView).let { barcode ->
                                // QRコードを読み取った時の処理
                                viewModel.setCode(barcode)
                            }
                    }
                }
            )

            bindToLifecycle(lifecycleOwner)
        }

    previewView.controller = cameraController

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { viewModel.erase() }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Erase")
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Box (
                modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds()
            ){
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = Color.Red,
                    fontSize = 24.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}