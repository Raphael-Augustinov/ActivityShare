package com.example.activityshare

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.activityshare.navigation.AppNavigation
import com.example.activityshare.ui.theme.ActivityShareTheme
import com.example.activityshare.viewModels.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )
    private lateinit var mainViewModel: MainViewModel
    private var isViewModelInitialized = false
    private val requestPermission =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { grantedPermissions ->
            println("grantedPermissions: $grantedPermissions")
            if (grantedPermissions.containsAll(permissions)) {
                // Read or process steps related health records.
                println("Permission granted")
            } else {
                // user denied permission
                println("Permission denied")
                return@registerForActivityResult
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]


        setContent {
            ActivityShareTheme {
                // Check if the user is null and redirect if necessary
                if (currentUser != null) {
                    CheckHealthConnectAvailability()
                    // Check permissions and run
                    println("health connect available")
                    checkPermissionsAndRun()
                    println("permissions checked")
                    AppNavigation(
                        mainViewModel = mainViewModel,
                        userProfilePicUrl = currentUser.photoUrl.toString(),
                        userEmail = currentUser.email ?: "",
                        onLogout = {
                            // Implement the logout logic
//                            revokeHealthConnectPermissionsForDebug()
                            auth.signOut()
                            googleSignInClient.signOut().addOnCompleteListener {
                                // After signing out, redirect to LoginActivity
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()

                            }
                        }
                    )
                    isViewModelInitialized = true
                } else {
                    // Redirect to login
                    Intent(this@MainActivity, LoginActivity::class.java).also {
                        startActivity(it)
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isViewModelInitialized)
            mainViewModel.refreshHealthData(this)
    }

    @Composable
    private fun CheckHealthConnectAvailability() {
        val providerPackageName = "com.google.android.apps.healthdata"
        val availabilityStatus = HealthConnectClient.getSdkStatus(this, providerPackageName)

        println("availabilityStatus: $availabilityStatus")
        when (availabilityStatus) {
            HealthConnectClient.SDK_AVAILABLE -> {
                // Health connect app is installed and available
                println("Health connect app is installed and available")
            }
            HealthConnectClient.SDK_UNAVAILABLE,
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                // Health connect app is not installed or needs an update
                HealthConnectDialog(providerPackageName = providerPackageName, showDialog = true)
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun HealthConnectDialog(providerPackageName: String, showDialog: Boolean) {
        val context = LocalContext.current
        var showDialogState by remember { mutableStateOf(showDialog) }

        if (showDialogState) {
            AlertDialog(
                onDismissRequest = { showDialogState = false },
                title = { Text(text = "Health Connect Required") },
                text = {
                    Text(text = "Health Connect app is required for this app to function properly. " +
                            "Please install or update it from the Play Store.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialogState = false
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$providerPackageName"))
                            context.startActivity(intent)
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }

    private fun checkPermissionsAndRun() {
        val healthConnectClient by lazy { HealthConnectClient.getOrCreate(this) }
        println("healthConnectClient: $healthConnectClient")
        CoroutineScope(Dispatchers.Main).launch {
            println("checking permissions")
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            println("granted: $granted")
            if (granted.containsAll(permissions)) {
                // Permissions already granted; proceed with inserting or reading data
//                val startOfDay =
//                    ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(
//                        ZoneId.systemDefault()
//                    ).toInstant()
//                val now = Instant.now()
                println("Permissions already granted")
            } else {
                println("requesting permissions")
                requestPermission.launch(permissions)
            }
        }
    }

    private fun revokeHealthConnectPermissionsForDebug() {
        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                healthConnectClient.permissionController.revokeAllPermissions()
                println("Debug: Health Connect permissions revoked.")
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                println("granted: $granted")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

