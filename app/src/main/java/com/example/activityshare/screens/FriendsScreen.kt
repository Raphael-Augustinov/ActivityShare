package com.example.activityshare.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.activityshare.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController) {
    var friendsList by remember { mutableStateOf(listOf<String>()) }

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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn {
                items(friendsList) { friendName ->
                    FriendItem(friendName) // Define a FriendItem Composable for list item UI
                }
            }
        }
    }
}

@Composable
fun FriendItem(friendName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Person, contentDescription = null) // Replace with friend's avatar if available
            Spacer(Modifier.width(8.dp))
            Text(friendName, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

fun fetchFriendsList(onResult: (List<String>) -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    FirebaseDatabase.getInstance().getReference("users/$currentUserId/friends")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendUids = snapshot.children.mapNotNull { it.key }
                val friendsNames = mutableListOf<String>()
                var fetchCount = 0
                friendUids.forEach { friendUid ->
                    FirebaseDatabase.getInstance().getReference("users/$friendUid/displayName")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val friendName = snapshot.value as? String
                                if (friendName != null) {
                                    friendsNames.add(friendName)
                                }
                                fetchCount++
                                if (fetchCount == friendUids.size) {
                                    onResult(friendsNames)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                                if (fetchCount == friendUids.size) {
                                    onResult(friendsNames)
                                }
                                Log.e("FriendsScreen", "Error fetching friend name", error.toException())
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