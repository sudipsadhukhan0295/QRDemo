package com.practice.qrdemo

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.its52.pushnotifications.model.ImageCropSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QrAnalyser(private val listener: (value: String?) -> Unit) :
    ImageAnalysis.Analyzer {
    fun updateAnalysis(value: Boolean){
        this.isAnalyse = value
    }
    var isAnalyse = true
    var currentTimestamp = 0L
    override fun analyze(imageProxy: ImageProxy) {
        if (isAnalyse) {
            currentTimestamp = System.currentTimeMillis()
            val bitmap =
                Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)

            imageProxy.use { bitmap.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            if (bitmap != null) {
                val image = InputImage.fromBitmap(bitmap, 0)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC
                    )
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                val result = scanner.process(image)
                    .addOnSuccessListener { list ->
                        if (list.isNotEmpty()) {
                            listener(list[0].rawValue)
                        }

                    }
                    .addOnFailureListener {

                    }
                    .addOnCompleteListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)
                            imageProxy.close()
                        }
                    }
            }
        }
    }

}


fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}