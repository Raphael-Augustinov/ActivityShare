package com.example.activityshare.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.activityshare.viewModels.MainViewModel

@Composable
fun MainScreen(mainViewModel: MainViewModel) {
    //retrieve the steps and distance from the view model using CollectAsStateWithLifecycle
    val steps by mainViewModel.steps.collectAsState()
    val distance by mainViewModel.distance.collectAsState()
    val calories by mainViewModel.calories.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(text = "Steps: $steps", fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Distance: $distance meters", fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Calories: $calories kcal", fontFamily = FontFamily.SansSerif, fontSize = 18.sp)
        }
    }
}