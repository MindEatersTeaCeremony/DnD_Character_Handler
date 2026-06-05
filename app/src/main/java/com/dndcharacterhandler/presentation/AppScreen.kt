package com.dndcharacterhandler.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppScreen(
    val route: String,
    val titleKey: String,
    val icon: ImageVector
) {
    data object Overview : AppScreen("overview", "nav_overview", Icons.Outlined.Badge)
    data object Attributes : AppScreen("attributes", "nav_attributes", Icons.Outlined.FitnessCenter)
    data object Combat : AppScreen("combat", "nav_combat", Icons.Outlined.Shield)
    data object Inventory : AppScreen("inventory", "nav_inventory", Icons.Outlined.Inventory2)
    data object Spells : AppScreen("spells", "nav_spells", Icons.Outlined.Bolt)
    data object Features : AppScreen("features", "nav_features", Icons.Outlined.AutoStories)
    data object Biography : AppScreen("biography", "nav_biography", Icons.Outlined.Description)
    data object Notes : AppScreen("notes", "nav_notes", Icons.Outlined.Notes)
}

val bottomNavigationScreens = listOf(
    AppScreen.Overview,
    AppScreen.Attributes,
    AppScreen.Combat,
    AppScreen.Inventory,
    AppScreen.Spells,
    AppScreen.Features,
    AppScreen.Biography,
    AppScreen.Notes
)
