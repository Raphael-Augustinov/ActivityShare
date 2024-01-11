package com.example.activityshare.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun SettingsScreen(
    userProfilePicUrl: String,
    userEmail: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // User's Profile Picture
        val image: Painter = rememberAsyncImagePainter(userProfilePicUrl)
        Image(
            painter = image,
            contentDescription = "User Profile Picture",
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User's Email
        Text(text = userEmail, fontFamily = FontFamily.SansSerif, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}