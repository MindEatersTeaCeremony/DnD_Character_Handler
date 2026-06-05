package com.dndcharacterhandler.presentation.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.dndcharacterhandler.presentation.AppScreen
import com.dndcharacterhandler.presentation.localization.text

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    screens: List<AppScreen>,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = text(screen.titleKey)
                    )
                },
                label = { Text(text(screen.titleKey)) }
            )
        }
    }
}
