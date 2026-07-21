package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.AddEditScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OrangeAccent
import com.example.viewmodel.WorkViewModel

sealed class Screen {
    object Home : Screen()
    object List : Screen()
    data class Detail(val entryId: Int) : Screen()
    data class AddEdit(val entryId: Int? = null) : Screen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: WorkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                Scaffold(
                    bottomBar = {
                        // Only show bottom navigation on main tabs (Home & List)
                        if (currentScreen is Screen.Home || currentScreen is Screen.List) {
                            NavigationBar(
                                containerColor = NavyPrimary,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen is Screen.Home,
                                    onClick = { currentScreen = Screen.Home },
                                    label = { Text("Home", color = if (currentScreen is Screen.Home) OrangeAccent else Color.White.copy(alpha = 0.7f)) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen is Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home Dashboard"
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangeAccent,
                                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                        selectedTextColor = OrangeAccent,
                                        unselectedTextColor = Color.White.copy(alpha = 0.7f),
                                        indicatorColor = NavyDark
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )
                                NavigationBarItem(
                                    selected = currentScreen is Screen.List,
                                    onClick = { currentScreen = Screen.List },
                                    label = { Text("Work Orders", color = if (currentScreen is Screen.List) OrangeAccent else Color.White.copy(alpha = 0.7f)) },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentScreen is Screen.List) Icons.Filled.List else Icons.Outlined.List,
                                            contentDescription = "Work Orders"
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangeAccent,
                                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                                        selectedTextColor = OrangeAccent,
                                        unselectedTextColor = Color.White.copy(alpha = 0.7f),
                                        indicatorColor = NavyDark
                                    ),
                                    modifier = Modifier.testTag("nav_work_orders")
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (currentScreen is Screen.Home || currentScreen is Screen.List) {
                                    innerPadding.calculateBottomPadding()
                                } else {
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                                }
                            )
                    ) {
                        when (val screen = currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is Screen.List -> {
                                ListScreen(
                                    viewModel = viewModel,
                                    onEntryClick = { entryId ->
                                        currentScreen = Screen.Detail(entryId)
                                    },
                                    onAddClick = {
                                        currentScreen = Screen.AddEdit(null)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is Screen.Detail -> {
                                DetailScreen(
                                    viewModel = viewModel,
                                    entryId = screen.entryId,
                                    onBack = { currentScreen = Screen.List },
                                    onEdit = { entryId ->
                                        currentScreen = Screen.AddEdit(entryId)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            is Screen.AddEdit -> {
                                AddEditScreen(
                                    viewModel = viewModel,
                                    entryId = screen.entryId,
                                    onBack = {
                                        currentScreen = if (screen.entryId != null) {
                                            Screen.Detail(screen.entryId)
                                        } else {
                                            Screen.List
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
