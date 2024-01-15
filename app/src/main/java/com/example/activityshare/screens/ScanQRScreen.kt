package com.example.activityshare.screens

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.util.Size
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.activityshare.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer



@Composable
fun ScanQRScreen(navController: NavController) {
    var code by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)
                val preview = Preview.Builder().build()
                val selector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(
                        Size(
                            previewView.width,
                            previewView.height
                        )
                    )
                    .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    QrCodeAnalyzer { result ->
                        code = result
                        checkAndAddUser(result){usernameAdded ->
                            Toast.makeText(context, "User $usernameAdded added to your friends list", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screens.FriendsScreen.name)
                        }
                    }
                )
                try {
                    cameraProviderFuture.get().bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                previewView
            },
            modifier = Modifier.weight(1f).height(100.dp)
        )
        Text(
            text = "Scan QR code to add a friend",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            fontSize = 18.sp
        )
    }
}

fun checkAndAddUser(scannedUserId: String, onUserAdded: (String) -> Unit) {
    val databaseReference = FirebaseDatabase.getInstance().reference
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var usernameAdded: String
    // Step 1: Check if the scanned user ID exists in the users list
    databaseReference.child("users").child(scannedUserId).get().addOnSuccessListener { dataSnapshot ->
        if (dataSnapshot.exists()) {
            // Step 2: Check if the scanned user is not the current user
            if (scannedUserId != currentUserId) {
                // Step 3: Check if the scanned user is not already in the current user's friends list
                if (currentUserId != null) {
                    databaseReference.child("users").child(currentUserId).child("friends")
                        .child(scannedUserId).get().addOnSuccessListener { friendSnapshot ->
                            if (!friendSnapshot.exists()) {
                                // Step 4: Add the scanned user to the current user's friends list
                                databaseReference.child("users").child(currentUserId).child("friends").child(scannedUserId).setValue(true)
                                    .addOnSuccessListener {
                                        println("User $scannedUserId added to $currentUserId's friends list")
                                        // Also, add the current user to the scanned user's friends list
                                        databaseReference.child("users").child(scannedUserId).child("friends").child(currentUserId).setValue(true)
                                            .addOnSuccessListener {
                                                println("User $currentUserId added to $scannedUserId's friends list")
                                                usernameAdded = databaseReference.child("users").child(scannedUserId).child("displayName").toString()
                                                onUserAdded(usernameAdded) // Call this after both updates are successful
                                            }
                                    }
                            } else {
                                println("User $scannedUserId is already in your friends list")
                            }
                    }
                }
            } else {
                println("You cannot add yourself as a friend")
            }
        } else {
            println("User $scannedUserId does not exist")
        }
    }
}

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
): ImageAnalysis.Analyzer {

    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888,
    )

    override fun analyze(image: ImageProxy) {
        if(image.format in supportedImageFormats) {
            val bytes = image.planes.first().buffer.toByteArray()
            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val binaryBmp = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = MultiFormatReader().apply {
                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                BarcodeFormat.QR_CODE
                            )
                        )
                    )
                }.decode(binaryBmp)
                onQrCodeScanned(result.text)
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
        else {
            println("Image format not supported")
            image.close()
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}