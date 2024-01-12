package com.example.activityshare.viewModels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class UserHealthData(
    var steps: Int = 0,
    var distance: Int = 0,
    var calories: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    //implement live data for the step count and distance, use the readStepsToday and readDistanceToday functions to retrieve the data
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    private val _distance = MutableStateFlow(0)
    val distance: StateFlow<Int> = _distance

    private val _calories = MutableStateFlow(0)
    val calories: StateFlow<Int> = _calories

    private val _sleep = MutableStateFlow(0)
    val sleep: StateFlow<Int> = _sleep
    private lateinit var healthConnectClient: HealthConnectClient

    init {
        initializeHealthData(getApplication<Application>().applicationContext)
    }

    private fun initializeHealthData(appContext: Context) {
        // Initialize HealthConnectClient and fetch data
        viewModelScope.launch {
            healthConnectClient = HealthConnectClient.getOrCreate(appContext)

            fetchHealthData(healthConnectClient)
            saveOrUpdateHealthData()
        }
    }

    fun refreshHealthData(appContext: Context) {
        // Initialize HealthConnectClient and fetch data
        viewModelScope.launch {
            healthConnectClient = HealthConnectClient.getOrCreate(appContext)

            fetchHealthData(healthConnectClient)
            saveOrUpdateHealthData()
        }
    }

    private suspend fun fetchHealthData(healthConnectClient: HealthConnectClient) {
        // Fetch steps and distance data here
        // Update _steps and _distance accordingly
        _steps.value = readStepsToday(healthConnectClient)
        _distance.value = readDistanceToday(healthConnectClient)
        _calories.value = readCaloriesToday(healthConnectClient)
    }

    fun saveOrUpdateHealthData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            println("currentDate: $currentDate")
            val databaseRef = FirebaseDatabase.getInstance().getReference("healthData/$uid/$currentDate")
            println("databaseRef: $databaseRef")

            databaseRef.get().addOnSuccessListener { dataSnapshot ->
                val healthData = dataSnapshot.getValue(UserHealthData::class.java) ?: UserHealthData()
                healthData.steps = _steps.value
                healthData.distance = _distance.value
                healthData.calories = _calories.value

                databaseRef.setValue(healthData)
                    .addOnSuccessListener {
                        // Handle success, e.g., show a toast
                    }
                    .addOnFailureListener {
                        // Handle failure, e.g., show an error message
                    }
            }.addOnFailureListener {
                // Handle failure in getting the data
            }
        }
    }
}

suspend fun readCaloriesToday(healthConnectClient: HealthConnectClient): Int {
    val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
    return try {
        val response = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, Instant.now())
            )
        )
        // The result may be null if no data is available in the time range
        val calories = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories ?: 0.0
        println("calories: $calories")
        calories.toInt()
    } catch (e: Exception) {
        println("Error retrieving calories data: ${e.message}")
        0
    }
}



@SuppressLint("SetTextI18n")
suspend fun readStepsToday(
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

        return (stepsByOrigin.values.max() ?: 0L).toInt()
    } catch (e: Exception) {
        // Run error handling here.
//        val statusTextView3 = findViewById<TextView>(R.id.textView3)
//        statusTextView3.text = "Error retrieving data"
        println("Error retrieving steps data")
        return 0
    }
}

@SuppressLint("SetTextI18n")
suspend fun readDistanceToday(
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