package com.pocketlegal.advice.presentation.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pocketlegal.advice.presentation.ai.AiChatScreen
import com.pocketlegal.advice.presentation.legal.directory.LegalDirectoryScreen
import com.pocketlegal.advice.presentation.navigation.Screen
import com.pocketlegal.advice.presentation.profile.ProfileScreen

private val bottomNavItems = listOf(
    Triple(Screen.Directory, "Directory", Icons.Default.Gavel),
    Triple(Screen.AiChat, "AI Chat", Icons.Default.SmartToy),
    Triple(Screen.Profile, "Profile", Icons.Default.Person)
)

@Composable
fun HomeScreen(
    onNavigateToCategory: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { (screen, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Directory.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Directory.route) {
                LegalDirectoryScreen(
                    onCategoryClick = onNavigateToCategory,
                    onSearchClick = onNavigateToSearch
                )
            }
            composable(Screen.AiChat.route) {
                AiChatScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
