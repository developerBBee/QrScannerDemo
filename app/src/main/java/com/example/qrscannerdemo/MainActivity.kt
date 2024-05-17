package com.example.qrscannerdemo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.qrscannerdemo.camerax.CameraScreen
import com.example.qrscannerdemo.ui.theme.QrScannerDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrScannerDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isCameraPermissionGranted by remember { mutableStateOf(false) }
                    val isRequestCameraPermission = remember { mutableStateOf(false) }

                    RequestCameraPermission(isRequestCameraPermission) {
                        isCameraPermissionGranted = true
                        Log.d("OnPermissionGranted", "Camera permission granted")
                    }
                    if (isCameraPermissionGranted) {
                        CompositionLocalProvider(androidx.lifecycle.compose.LocalLifecycleOwner provides LocalLifecycleOwner.current) {
                            CameraScreen()
                        }
                    } else if (isRequestCameraPermission.value) {
                        OpenSettingDialog()
                    }
                }
            }
        }
    }
}

@Composable
private fun Activity.RequestCameraPermission(
    state: MutableState<Boolean>,
    onPermissionGranted: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (!isGranted) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                state.value = true
            } else {
                finish()
            }
        } else {
            onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        Log.d("RequestCameraPermission", "Requesting camera permission")
        launcher.launch(Manifest.permission.CAMERA)
    }
}

@Composable
private fun Activity.OpenSettingDialog() {
    val launchSetting = {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
        finish()
    }

    AlertDialog(
        title = { Text("Camera permission required.") },
        text = {
            Text("This app requires access to your device's camera to scan QR codes.")
        },
        onDismissRequest = { launchSetting() },
        confirmButton = {
            Button(onClick = { launchSetting() }) {
                Text(text = "OK")
            }
        }
    )
}