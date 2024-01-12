package com.example.activityshare

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context) : ViewModel() {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    val permissionsGranted = MutableLiveData<Boolean>(false)

    fun checkAndRequestHealthConnectPermissions() {
        viewModelScope.launch {
            val permissions = setOf(
                HealthPermission.getReadPermission(StepsRecord::class),
                HealthPermission.getReadPermission(DistanceRecord::class)
            )

            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            permissionsGranted.value = granted.containsAll(permissions)

            if (!granted.containsAll(permissions)) {
                // Trigger permission request in MainActivity
                // This can be done using a LiveData event or a callback
            }
        }
    }

    // Add methods for loading and managing Health Connect data
}
