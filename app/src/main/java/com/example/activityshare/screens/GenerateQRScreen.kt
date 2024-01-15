package com.example.activityshare.screens

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.activityshare.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun GenerateQRScreen(navController: NavController) {
    val qrCodeBitmap = FirebaseAuth.getInstance().currentUser?.uid?.let { generateQRCodeBitmap(it) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission Granted: Continue with camera operation
                navController.navigate(Screens.ScanQRScreen.name)
            } else {
                // Permission Denied: Handle the denial
                Toast.makeText(
                    navController.context,
                    "Camera is required, try again or enable in settings.",
                    Toast.LENGTH_SHORT
                ).show()
                println("Permission Denied")
            }
        }
    )
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            qrCodeBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }) {
                Text("Scan QR Code")
            }
        }
    }
}

fun generateQRCodeBitmap(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 256, 256)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}