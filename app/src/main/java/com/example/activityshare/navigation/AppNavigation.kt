package com.example.activityshare.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
//import rememberNavController from androidx.navigation.compose
import androidx.navigation.compose.rememberNavController
import com.example.activityshare.screens.MainScreen
import com.example.activityshare.screens.FriendsScreen
import com.example.activityshare.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                listOfNavItems.forEach() {navItem ->
                    NavigationBarItem(
                        icon = {
                            if (currentDestination?.hierarchy?.any { it.route == navItem.route } == true) {
                            Icon(imageVector = navItem.filledIcon, contentDescription = null)
                            } else {
                            Icon(imageVector = navItem.outlinedIcon, contentDescription = null)
                            } },
                        label = { Text(text = navItem.label) },
                        selected = currentDestination?.hierarchy?.any{ it.route == navItem.route} == true,
                        onClick = {
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screens.Mainscreen.name,
                modifier = Modifier.padding(paddingValues)
            ){
                composable(route = Screens.Mainscreen.name){
                    MainScreen()
                }
                composable(route = Screens.FriendsScreen.name){
                    FriendsScreen()
                }
                composable(route = Screens.SettingsScreen.name){
                    SettingsScreen()
                }
            }
    }
}