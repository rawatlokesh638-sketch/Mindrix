package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Cyan400
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.Slate100

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = com.example.ui.theme.DarkSlate.copy(alpha = 0.9f),
        contentColor = Slate100,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("HOME", Icons.Default.Home, "com.example.ui.navigation.Home"),
            Triple("BATTLE", Icons.Default.SportsEsports, "com.example.ui.navigation.ModeSelection"),
            Triple("RANK", Icons.Default.EmojiEvents, "com.example.ui.navigation.Leaderboard"),
            Triple("SHOP", Icons.Default.ShoppingCart, "com.example.ui.navigation.Shop"),
            Triple("PROFILE", Icons.Default.Person, "com.example.ui.navigation.Profile")
        )

        items.forEach { (title, icon, route) ->
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Cyan400,
                    unselectedIconColor = Slate100.copy(alpha = 0.4f),
                    selectedTextColor = Cyan400,
                    unselectedTextColor = Slate100.copy(alpha = 0.4f),
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    }
}
