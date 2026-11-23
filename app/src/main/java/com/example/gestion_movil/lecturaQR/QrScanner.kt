package com.example.gestion_movil.lecturaQR

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.gestion_movil.R
import com.example.gestion_movil.ui.oportunidad.MensajeError
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun QrScanner(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    linterna:Boolean = false,
    zoom: Float = 0f
) {
    var camara by remember { mutableStateOf<Camera?>(null) }
    LaunchedEffect(camara, linterna) { camara?.cameraControl?.enableTorch(linterna) }
    LaunchedEffect(camara, zoom) { camara?.cameraControl?.setLinearZoom(zoom) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // mantiene sin reiniciar camarax
    val currentCallback = rememberUpdatedState(onQrCodeScanned)

    // crao una vista previa
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER

            // compatibilidad con emulador
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // mostrar camara
    AndroidView(
        factory = { previewView },
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures { toque ->
                    val punto = previewView.meteringPointFactory.createPoint(toque.x, toque.y)
                    camara?.cameraControl?.startFocusAndMetering(
                        FocusMeteringAction.Builder(punto).build()
                    )
                }
            }
    )

    val resolucion  = ResolutionSelector.Builder()
        .setResolutionStrategy(
            ResolutionStrategy(
                Size(1280, 720),
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
            )
        ).build()
    val imageAnalysis = ImageAnalysis.Builder()
        .setResolutionSelector(resolucion)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    // gestiona camara
    DisposableEffect(context, lifecycleOwner, previewView) {

        val cameraExecutor = Executors.newSingleThreadExecutor()

        // reconose unicamente el QR
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val barcodeScanner = BarcodeScanning.getClient(options)

        val disposed = AtomicBoolean(false)
        val detected = AtomicBoolean(false)

        // vista previa
        val preview = Preview.Builder()
            .build()
            .apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

        // analisis de imagenes
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(
                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
            )
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

            if (disposed.get() || detected.get()) {
                imageProxy.close()
            } else {

                val mediaImage = imageProxy.image

                if (mediaImage == null) {
                    imageProxy.close()
                } else {

                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    try {
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->

                                val qr = barcodes.firstOrNull {
                                    it.format == Barcode.FORMAT_QR_CODE &&
                                            it.rawValue != null
                                }

                                if (
                                    qr != null &&
                                    !disposed.get() &&
                                    detected.compareAndSet(false, true)
                                ) {
                                    imageAnalysis.clearAnalyzer()

                                    currentCallback.value(
                                        qr.rawValue!!
                                    )

                                    Log.d(
                                        "QrScanner",
                                        "Código QR detectado"
                                    )
                                }
                            }
                            .addOnFailureListener { error ->
                                Log.e(
                                    "QrScanner",
                                    "Error al analizar código QR",
                                    error
                                )
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }

                    } catch (error: Exception) {
                        Log.e(
                            "QrScanner",
                            "Error al procesar imagen",
                            error
                        )
                        imageProxy.close()
                    }
                }
            }
        }

        // camara
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        var boundProvider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({

            if (!disposed.get()) {

                try {
                    val cameraProvider =
                        cameraProviderFuture.get()

                    if (!disposed.get()) {

                        // camara tracera
                        val cameraSelector =
                            CameraSelector.DEFAULT_BACK_CAMERA

                        camara = cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageAnalysis
                        )

                        boundProvider = cameraProvider

                        Log.d(
                            "QrScanner",
                            "CameraX vinculado correctamente"
                        )
                    }

                } catch (error: Exception) {
                    Log.e(
                        "QrScanner",
                        "Error al iniciar la cámara",
                        error
                    )
                }
            }

        }, ContextCompat.getMainExecutor(context))

        // salir de pantalla
        onDispose {

            disposed.set(true)

            imageAnalysis.clearAnalyzer()

            boundProvider?.unbind(
                preview,
                imageAnalysis
            )

            barcodeScanner.close()

            cameraExecutor.shutdown()

            Log.d(
                "QrScanner",
                "Recursos de cámara liberados"
            )
        }
    }
}