package com.example.activityshare.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.activityshare.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HealthData(
    val calories: Int = 0,
    val distance: Int = 0,
    val steps: Int = 0
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController) {
    var friendsList by remember { mutableStateOf(listOf<Pair<String, HealthData>>()) }

    // Fetch friends list
    LaunchedEffect(Unit) {
        fetchFriendsList { friends ->
            friendsList = friends
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screens.GenerateQRScreen.name) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Friend")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn {
                items(friendsList) { (friendName, healthData) ->
                    FriendItem(friendName, healthData)
                }
            }
        }
    }
}

@Composable
fun FriendItem(friendName: String, healthData: HealthData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(friendName, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Steps: ${healthData.steps}")
            Text("Distance: ${healthData.distance}")
            Text("Calories: ${healthData.calories}")
        }
    }
}


fun fetchFriendsList(onResult: (List<Pair<String, HealthData>>) -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    FirebaseDatabase.getInstance().getReference("users/$currentUserId/friends")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendUids = snapshot.children.mapNotNull { it.key }
                var fetchCount = 0
                val friendsData = mutableListOf<Pair<String, HealthData>>()

                friendUids.forEach { friendUid ->
                    val userRef = FirebaseDatabase.getInstance().getReference("users/$friendUid/displayName")
                    val healthRef = FirebaseDatabase.getInstance().getReference("healthData/$friendUid/$currentDate")

                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val friendName = userSnapshot.value as? String ?: return

                            healthRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(healthSnapshot: DataSnapshot) {
                                    val healthData = healthSnapshot.getValue(HealthData::class.java) ?: HealthData(0, 0, 0)
                                    friendsData.add(friendName to healthData)
                                    fetchCount++
                                    if (fetchCount == friendUids.size) {
                                        onResult(friendsData)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    fetchCount++
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            fetchCount++
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("FriendsScreen", "Error fetching friends list", error.toException())
            }
        })
}