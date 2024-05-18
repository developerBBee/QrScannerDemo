package com.example.qrscannerdemo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.qrscannerdemo.camerax.CameraScreen
import com.example.qrscannerdemo.ui.theme.QrScannerDemoTheme

private data class PermissionState(
    val request: RequestState? = RequestState.CAMERA,
    val granted: Map<RequestState, Boolean> =
        mapOf(RequestState.CAMERA to false, RequestState.READ_PHONE_STATE to false),
) {
    fun update(request: RequestState, granted: Boolean): PermissionState =
        copy(
            request = request.next(),
            granted = this.granted.toMutableMap().apply { put(request, granted) }.toMap()
        ).also { Log.d("PermissionState", it.toString()) }

    fun acceptedAll(): Boolean = granted.values.all { it }

    fun needAllowOnSettings(): Boolean = request == null && !acceptedAll()

    override fun toString(): String =
        "PermissionState(request=$request, granted=${
            granted.map { "[${it.key}:${it.value}]" }.joinToString(separator = ",") { it }
        })"
}

enum class RequestState(val permission: String) {
    CAMERA(Manifest.permission.CAMERA),
    READ_PHONE_STATE(Manifest.permission.READ_PHONE_STATE),
    ;
    fun next(): RequestState? = runCatching { entries[ordinal + 1] }.getOrNull()
}

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
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun Activity.MainContent() {
    var permissionState by remember { mutableStateOf(PermissionState()) }

    when (val request = permissionState.request) {
        RequestState.CAMERA,
        RequestState.READ_PHONE_STATE -> {
            RequestPermission(
                requestPermission = request.permission,
                onDenied = {
                    Log.d("RequestPermission", "$request permission denied")
                    permissionState = permissionState.update(request, false)
                },
                onPermissionGranted = {
                    Log.d("RequestPermission", "$request permission granted")
                    permissionState = permissionState.update(request, true)
                }
            )
        }
        else -> {
            if (permissionState.needAllowOnSettings()) {
                OpenSettingDialog()
            } else if (permissionState.acceptedAll()) {
                CompositionLocalProvider(
                    androidx.lifecycle.compose.LocalLifecycleOwner provides
                            androidx.compose.ui.platform.LocalLifecycleOwner.current
                ) {
                    CameraScreen()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        var showSerial by remember { mutableStateOf(true) }
                        if (showSerial) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Snackbar(
                                    modifier = Modifier.fillMaxWidth(),
                                    dismissAction = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            TextButton(onClick = { showSerial = false }) {
                                                Text(text = "Dismiss")
                                            }
                                        }
                                    }
                                ) {
                                    Text(text = getSerial())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Activity.RequestPermission(
    requestPermission: String,
    onDenied: () -> Unit = {},
    onPermissionGranted: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            if (shouldShowRequestPermissionRationale(requestPermission)) {
                // denied permission once
                finish()
            } else {
                // denied permission with "Don't ask again"
                onDenied()
            }
        }
    }

    LaunchedEffect(requestPermission) {
        Log.d("RequestPermission", "Requesting permission $requestPermission")
        launcher.launch(requestPermission)
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
        title = { Text("Camera and read phone state permission required.") },
        text = {
            Text("This app requires access to your device's camera to scan codes.")
        },
        onDismissRequest = { launchSetting() },
        confirmButton = {
            Button(onClick = { launchSetting() }) {
                Text(text = "OK")
            }
        }
    )
}

private fun getSerial(): String = runCatching {
    Build.getSerial()
}.getOrDefault("Unknown")