package com.example.qrscannerdemo.camerax

import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {
    private val cameraStringState = MutableStateFlow("読み取り無し")
    val cameraStringFlow: StateFlow<String> = cameraStringState.asStateFlow()

    private var conter = 0
    private var textBuffer = "読み取り無し"

    fun setCode(code: Barcode) {
        (code.rawValue ?: "").let { text ->
            when (code.format) {
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39 -> setCode39(text)
                else -> {
                    conter = 0
                    textBuffer = text
                    cameraStringState.value = text
                }
            }
        }
    }

    private fun setCode39(text: String) {
        if (textBuffer == text) {
            conter++
        } else {
            conter = 0
        }

        textBuffer = text
        if (conter >= 3) {
            cameraStringState.value = text
            conter = 0
        }
    }

    fun erase() {
        conter = 0
        textBuffer = "読み取り無し"
        cameraStringState.value = textBuffer
    }
}