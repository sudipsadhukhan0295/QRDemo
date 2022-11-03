package com.practice.qrdemo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.practice.qrdemo.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val resultCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    initUI()
                }
                else -> {
                    val builder = AlertDialog.Builder(this)
                        .setTitle("ITS - Permission Alert")
                        .setMessage("You need to go settings and turned on camera permission manually.")
                        .setPositiveButton("Go") { dialog, _ ->
                            dialog.dismiss()
                            openSettings()
                            finish()
                        }

                    val dialog = builder.create()
                    dialog.show()
                    dialog.setOnCancelListener {
                        finish()
                    }
                }
            }
        }
    private lateinit var cameraExecutor: ExecutorService
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        if (!hasPermission(CAMERA_PERMISSION)) {
            resultCameraPermission.launch(CAMERA_PERMISSION)
        } else {
            initUI()
        }
    }

    private fun initUI() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({

            // CameraProvider
            cameraProvider = cameraProviderFuture.get()


            // Build and bind the camera use cases
            binding.previewView.post {

                bindCameraUseCases()


            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun bindCameraUseCases() {

        binding.apply {
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()

            val rotation = previewView.display.rotation

            // CameraProvider
            val cameraProvider = cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

            // CameraSelector
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            // Preview
            preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                // Set initial target rotation
                .setTargetRotation(rotation)
                .build()
            var showNoInternetMessage = false
            // ImageAnalysis
            val analyser = QrAnalyser { value ->


            }

            imageAnalyzer = ImageAnalysis.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor, analyser)
                }


            imageAnalyzer!!.targetRotation = previewView.display.rotation
            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(
                    this@MainActivity,
                    cameraSelector, preview, imageCapture, imageAnalyzer
                )

                // Attach the viewfinder's surface provider to preview use case
                preview?.setSurfaceProvider(previewView.surfaceProvider)
                //observeCameraState(camera?.cameraInfo!!)
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }
    }

}