package com.example.memoir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.memoir.ui.editor.DeskScreen
import com.example.memoir.ui.home.ChronicleScreen
import com.example.memoir.ui.navigation.Route
import com.example.memoir.ui.tasks.MilestonesScreen
import com.example.memoir.ui.theme.MemoirTheme
import com.example.memoir.ui.theme.ThemeViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()

            MemoirTheme(darkTheme = isDarkMode) {
                val backStack = remember { mutableStateListOf<Route>(Route.Memoirs) }
                val currentRoute = backStack.lastOrNull()
                var showSettings by remember { mutableStateOf(false) }

                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = { key ->
                        when (key) {
                            is Route.Memoirs -> NavEntry(key) {
                                Scaffold(
                                    topBar = {
                                        TopAppBar(
                                            title = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        painter = painterResource(
                                                            id = if (isDarkMode) R.drawable.logo_dark else R.drawable.logo_light
                                                        ),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(32.dp),
                                                        tint = Color.Unspecified
                                                    )
                                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                                    Text(
                                                        text = "Memoir",
                                                        style = MaterialTheme.typography.headlineLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            },
                                            actions = {
                                                androidx.compose.foundation.layout.Box {
                                                    IconButton(onClick = { showSettings = true }) {
                                                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                                                    }
                                                    DropdownMenu(
                                                        expanded = showSettings,
                                                        onDismissRequest = { showSettings = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text(if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode") },
                                                            onClick = {
                                                                themeViewModel.setDarkMode(!isDarkMode)
                                                                showSettings = false
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                            },
                                            colors = TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.background
                                            )
                                        )
                                    },
                                    bottomBar = {
                                        NavigationBar {
                                            NavigationBarItem(
                                                selected = true,
                                                onClick = { },
                                                icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
                                                label = { Text("Chronicle") }
                                            )
                                            NavigationBarItem(
                                                selected = false,
                                                onClick = { 
                                                    backStack.clear()
                                                    backStack.add(Route.Milestones)
                                                },
                                                icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                                label = { Text("Milestones") }
                                            )
                                        }
                                    },
                                    floatingActionButton = {
                                        FloatingActionButton(
                                            onClick = {
                                                backStack.add(Route.Desk(id = null, isMilestone = false))
                                            },
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add to Chronicle")
                                        }
                                    }
                                ) { innerPadding ->
                                    ChronicleScreen(
                                        viewModel = viewModel(),
                                        onNavigateToDesk = { id ->
                                            backStack.add(Route.Desk(id = id, isMilestone = false))
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                            is Route.Milestones -> NavEntry(key) {
                                Scaffold(
                                    topBar = {
                                        TopAppBar(
                                            title = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        painter = painterResource(
                                                            id = if (isDarkMode) R.drawable.logo_dark else R.drawable.logo_light
                                                        ),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(32.dp),
                                                        tint = Color.Unspecified
                                                    )
                                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                                    Text(
                                                        text = "Memoir",
                                                        style = MaterialTheme.typography.headlineLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            },
                                            actions = {
                                                androidx.compose.foundation.layout.Box {
                                                    IconButton(onClick = { showSettings = true }) {
                                                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                                                    }
                                                    DropdownMenu(
                                                        expanded = showSettings,
                                                        onDismissRequest = { showSettings = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text(if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode") },
                                                            onClick = {
                                                                themeViewModel.setDarkMode(!isDarkMode)
                                                                showSettings = false
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                            },
                                            colors = TopAppBarDefaults.topAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.background
                                            )
                                        )
                                    },
                                    bottomBar = {
                                        NavigationBar {
                                            NavigationBarItem(
                                                selected = false,
                                                onClick = { 
                                                    backStack.clear()
                                                    backStack.add(Route.Memoirs)
                                                },
                                                icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
                                                label = { Text("Chronicle") }
                                            )
                                            NavigationBarItem(
                                                selected = true,
                                                onClick = { },
                                                icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                                label = { Text("Milestones") }
                                            )
                                        }
                                    },
                                    floatingActionButton = {
                                        FloatingActionButton(
                                            onClick = {
                                                backStack.add(Route.Desk(id = null, isMilestone = true))
                                            },
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add Milestone")
                                        }
                                    }
                                ) { innerPadding ->
                                    MilestonesScreen(
                                        viewModel = viewModel(),
                                        onNavigateToDesk = { id, isMilestone ->
                                            backStack.add(Route.Desk(id = id, isMilestone = isMilestone))
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                            is Route.Desk -> NavEntry(key) {
                                DeskScreen(
                                    id = key.id,
                                    isMilestone = key.isMilestone,
                                    viewModel = viewModel(),
                                    onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
