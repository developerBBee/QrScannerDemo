package com.example.qrscannerdemo.camerax

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {
    private val cameraStringState = MutableStateFlow("読み取り無し")
    val cameraStringFlow: StateFlow<String> = cameraStringState.asStateFlow()

    fun setCameraString(string: String) {
        cameraStringState.value = string
    }
}