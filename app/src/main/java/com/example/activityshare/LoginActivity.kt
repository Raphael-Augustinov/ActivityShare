package com.example.activityshare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.activityshare.ui.theme.ActivityShareTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


class LoginActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            println("LoginActivity onCreate called")
        // Initialize Firebase Auth
        auth = Firebase.auth
            println("auth: $auth")
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
                println("gso: $gso")
        googleSignInClient = GoogleSignIn.getClient(this, gso)
            println("googleSignInClient: $googleSignInClient")
        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("googleSignInLauncher result: $result")
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    println("account: $account")
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Handle exception
                        println("exception: $e")
                }
            }
            else {
                println("googleSignInLauncher result code not OK result: $result")
            }
        }

        setContent {
            ActivityShareTheme {
                LoginScreen(onLoginClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    println("button clicked, signInIntent: $signInIntent")
                    googleSignInLauncher.launch(signInIntent)
                })
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
            println("auth credential: $credential")
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                        println("signInWithCredential:success")
                    val user = auth.currentUser
                    Log.d("LoginActivity", "User Name: ${user?.displayName}")
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    // Handle sign in failure
                        println("signInWithCredential:failure")
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }
}

@Composable
fun LoginScreen(onLoginClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onLoginClick) {
            Text(text = "Sign in with Google", fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
        }
    }
}
