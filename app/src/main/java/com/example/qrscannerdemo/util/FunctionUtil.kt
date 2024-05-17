package com.example.qrscannerdemo.util

import android.graphics.Rect
import android.util.Log
import android.view.View
import com.google.mlkit.vision.barcode.common.Barcode
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "FunctionUtil"
private const val MARGIN_RATIO = 0.01f // 1% margin

fun List<Barcode>.filterInContainer(container: View): List<Barcode> =
    filter { it.boundingBox?.isInside(container) ?: false }

fun List<Barcode>.pickMinimumDeviationFromCenter(container: View): Barcode =
    minBy { it.boundingBox?.deviationFromCenter(container) ?: Float.MAX_VALUE }

private fun Rect.isInside(v: View): Boolean {
    Log.d(TAG, "Target   : left=$left right=$right top=$top bottom=$bottom")
    Log.d(TAG, "Container: left=${v.left} right=${v.right} top=${v.top} bottom=${v.bottom}")
    val horizontalMargin = v.width * MARGIN_RATIO
    val verticalMargin = v.height * MARGIN_RATIO

    return (left >= v.left + horizontalMargin && right <= v.right - horizontalMargin
            && top >= v.top + verticalMargin && bottom <= v.bottom - verticalMargin)
        .also { Log.d(TAG, "isInside=$it") }
}

private fun Rect.deviationFromCenter(v: View): Float {
    val centerView = Pair((v.left + v.right) / 2f, (v.top + v.bottom) / 2f)
    val centerRect = Pair((left + right) / 2f, (top + bottom) / 2f)
    return sqrt((centerView.first - centerRect.first).pow(2)
            + (centerView.second - centerRect.second).pow(2))
        .also { Log.d(TAG, "deviation=$it") }
}