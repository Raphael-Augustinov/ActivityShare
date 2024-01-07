package com.example.activityshare

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.activityshare.navigation.AppNavigation
import com.example.activityshare.ui.theme.ActivityShareTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth

        setContent {
            ActivityShareTheme {
                AppNavigation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        println("MainActivity onStart called")
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        //print in the log the current user
        if (currentUser != null) {
            println("Current user: ${currentUser.email}")
        }
        if (currentUser == null) {
            // User not signed in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            //delay 2 seconds
            Thread.sleep(2000)
            startActivity(intent)
            finish()
        }
    }
}

