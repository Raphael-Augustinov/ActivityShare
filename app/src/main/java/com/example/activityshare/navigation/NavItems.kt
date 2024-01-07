package com.example.activityshare.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

val listOfNavItems = listOf(
    NavItem(
        route = Screens.Mainscreen.name,
        label = "Home",
        filledIcon = Icons.Default.Home,
        outlinedIcon = Icons.Outlined.Home
    ),
    NavItem(
        route = Screens.FriendsScreen.name,
        label = "Friends",
        filledIcon = Icons.Default.Person,
        outlinedIcon = Icons.Outlined.Person
    ),
    NavItem(
        route = Screens.SettingsScreen.name,
        label = "Settings",
        filledIcon = Icons.Default.Settings,
        outlinedIcon = Icons.Outlined.Settings
    )
)