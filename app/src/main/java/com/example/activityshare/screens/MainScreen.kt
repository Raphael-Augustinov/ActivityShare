package com.example.activityshare.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun MainScreen(healthConnectClient: HealthConnectClient) {
    var steps by remember { mutableStateOf(0) }
    var distance by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        steps = readStepsToday(healthConnectClient)
        distance = readDistanceToday(healthConnectClient)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(text = "Steps: $steps", fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Distance: $distance meters", fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
        }
    }
}


@SuppressLint("SetTextI18n")
private suspend fun readStepsToday(
    healthConnectClient: HealthConnectClient
): Int {
    try {
        val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay,  Instant.now())
                )
            )
        val stepsByOrigin = mutableMapOf<String, Long>()
        for (stepRecord in response.records) {
            val origin: String = stepRecord.metadata.dataOrigin.toString()
            stepsByOrigin[origin] =
                stepsByOrigin.getOrDefault(origin, 0L) + stepRecord.count
        }

        return stepsByOrigin.values.max().toInt()
    } catch (e: Exception) {
        // Run error handling here.
//        val statusTextView3 = findViewById<TextView>(R.id.textView3)
//        statusTextView3.text = "Error retrieving data"
        println("Error retrieving steps data")
        return 0
    }
}

@SuppressLint("SetTextI18n")
private suspend fun readDistanceToday(
    healthConnectClient: HealthConnectClient
): Int {
    try {
        val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()

        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    DistanceRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay,  Instant.now())
                )
            )
        //return the sum of all distance entries in an int data type
        val distanceByOrigin = mutableMapOf<String, Double>()
        for (distanceRecord in response.records) {
            val origin: String = distanceRecord.metadata.dataOrigin.toString()
            distanceByOrigin[origin] =
                distanceByOrigin.getOrDefault(origin, 0.0) + distanceRecord.distance.inMeters
        }
        return distanceByOrigin.values.max().toInt()
    } catch (e: Exception) {
        // Run error handling here.
//        val statusTextView3 = findViewById<TextView>(R.id.textView3)
//        statusTextView3.text = "Error retrieving distance data"
        println("Error retrieving distance data")
        return 0
    }
}