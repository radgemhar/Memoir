package com.example.memoir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.example.memoir.ui.editor.DeskScreen
import com.example.memoir.ui.home.ChronicleScreen
import com.example.memoir.ui.library.ArchiveScreen
import com.example.memoir.ui.library.RecentlyDeletedScreen
import com.example.memoir.ui.navigation.Route
import com.example.memoir.ui.settings.SettingsScreen
import com.example.memoir.ui.folders.FoldersScreen
import com.example.memoir.ui.tasks.MilestonesScreen
import com.example.memoir.ui.theme.MemoirTheme
import com.example.memoir.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()
            val fontSizeOption by themeViewModel.fontSizeOption.collectAsStateWithLifecycle()

            MemoirTheme(
                darkTheme = isDarkMode,
                fontScale = fontSizeOption.scale
            ) {
                var isLoading by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(1500)
                    isLoading = false
                }

                if (isLoading) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkMode) R.drawable.logo_dark else R.drawable.logo_light
                            ),
                            contentDescription = "Logo",
                            modifier = Modifier.size(120.dp),
                            tint = Color.Unspecified
                        )
                    }
                }

                val backStack = remember { mutableStateListOf<Route>(Route.Memoirs) }
                val currentRoute = backStack.lastOrNull()
                var navigationMotion by remember { mutableStateOf(NavigationMotion.Instant) }

                val popBack = {
                    if (backStack.size > 1) {
                        navigationMotion = NavigationMotion.Slide
                        backStack.removeAt(backStack.size - 1)
                    }
                    Unit
                }
                val navigateTopLevel = { route: Route, motion: NavigationMotion ->
                    if (backStack.lastOrNull() != route) {
                        navigationMotion = motion
                        backStack.clear()
                        backStack.add(route)
                    }
                    Unit
                }

                if (!isLoading) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    onBack = popBack,
                    transitionSpec = {
                        if (navigationMotion == NavigationMotion.Instant) {
                            memoirInstantTransform()
                        } else {
                            memoirSlideTransform(
                                if (targetState.motionOrder >= initialState.motionOrder) {
                                    AnimatedContentTransitionScope.SlideDirection.Left
                                } else {
                                    AnimatedContentTransitionScope.SlideDirection.Right
                                }
                            )
                        }
                    },
                    popTransitionSpec = {
                        if (navigationMotion == NavigationMotion.Instant) {
                            memoirInstantTransform()
                        } else {
                            memoirSlideTransform(AnimatedContentTransitionScope.SlideDirection.Right)
                        }
                    },
                    predictivePopTransitionSpec = {
                        memoirSlideTransform(AnimatedContentTransitionScope.SlideDirection.Right)
                    },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = { key ->
                        when (key) {
                            is Route.Memoirs -> NavEntry(key, metadata = key.motionMetadata()) {
                                Scaffold(
                                    modifier = Modifier.topLevelSwipeNavigation(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            navigateTopLevel(route, NavigationMotion.Slide)
                                        }
                                    ),
                                    topBar = {
                                        MemoirTopBar(
                                            title = "Memoir",
                                            isDarkMode = isDarkMode,
                                            showLogo = true,
                                            actions = {
                                                IconButton(
                                                    onClick = {
                                                        navigationMotion = NavigationMotion.Slide
                                                        backStack.add(Route.Folders)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Folder,
                                                        contentDescription = "Manage Folders",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    bottomBar = {
                                        MemoirBottomBar(
                                            currentRoute = currentRoute,
                                            onNavigate = { route ->
                                                navigateTopLevel(route, NavigationMotion.Instant)
                                            }
                                        )
                                    },
                                    floatingActionButton = {
                                        FloatingActionButton(
                                            onClick = {
                                                navigationMotion = NavigationMotion.Slide
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
                                            navigationMotion = NavigationMotion.Slide
                                            backStack.add(Route.Desk(id = id, isMilestone = false))
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            is Route.Milestones -> NavEntry(key, metadata = key.motionMetadata()) {
                                Scaffold(
                                    modifier = Modifier.topLevelSwipeNavigation(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            navigateTopLevel(route, NavigationMotion.Slide)
                                        }
                                    ),
                                    topBar = {
                                        MemoirTopBar(
                                            title = "Memoir",
                                            isDarkMode = isDarkMode,
                                            showLogo = true
                                        )
                                    },
                                    bottomBar = {
                                        MemoirBottomBar(
                                            currentRoute = currentRoute,
                                            onNavigate = { route ->
                                                navigateTopLevel(route, NavigationMotion.Instant)
                                            }
                                        )
                                    },
                                    floatingActionButton = {
                                        FloatingActionButton(
                                            onClick = {
                                                navigationMotion = NavigationMotion.Slide
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
                                            navigationMotion = NavigationMotion.Slide
                                            backStack.add(Route.Desk(id = id, isMilestone = isMilestone))
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            is Route.Settings -> NavEntry(key, metadata = key.motionMetadata()) {
                                Scaffold(
                                    modifier = Modifier.topLevelSwipeNavigation(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            navigateTopLevel(route, NavigationMotion.Slide)
                                        }
                                    ),
                                    topBar = {
                                        MemoirTopBar(
                                            title = "Settings",
                                            isDarkMode = isDarkMode,
                                            navigationIcon = {
                                                if (backStack.size > 1) {
                                                    IconButton(onClick = popBack) {
                                                        Icon(
                                                            Icons.AutoMirrored.Filled.ArrowBack,
                                                            contentDescription = "Back"
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    },
                                    bottomBar = {
                                        MemoirBottomBar(
                                            currentRoute = currentRoute,
                                            onNavigate = { route ->
                                                navigateTopLevel(route, NavigationMotion.Instant)
                                            }
                                        )
                                    }
                                ) { innerPadding ->
                                    SettingsScreen(
                                        isDarkMode = isDarkMode,
                                        fontSizeOption = fontSizeOption,
                                        onDarkModeChange = themeViewModel::setDarkMode,
                                        onFontSizeChange = themeViewModel::setFontSizeOption,
                                        onArchiveClick = {
                                            navigationMotion = NavigationMotion.Slide
                                            backStack.add(Route.Archive)
                                        },
                                        onRecentlyDeletedClick = {
                                            navigationMotion = NavigationMotion.Slide
                                            backStack.add(Route.RecentlyDeleted)
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            is Route.Archive -> NavEntry(key, metadata = key.motionMetadata()) {
                                Scaffold(
                                    topBar = {
                                        MemoirTopBar(
                                            title = "Archive",
                                            isDarkMode = isDarkMode,
                                            navigationIcon = {
                                                IconButton(onClick = popBack) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                ) { innerPadding ->
                                    ArchiveScreen(
                                        viewModel = viewModel(),
                                        onOpenMemoir = { id ->
                                            navigationMotion = NavigationMotion.Slide
                                            backStack.add(Route.Desk(id = id, isMilestone = false))
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            is Route.RecentlyDeleted -> NavEntry(key, metadata = key.motionMetadata()) {
                                Scaffold(
                                    topBar = {
                                        MemoirTopBar(
                                            title = "Recently Deleted",
                                            isDarkMode = isDarkMode,
                                            navigationIcon = {
                                                IconButton(onClick = popBack) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                ) { innerPadding ->
                                    RecentlyDeletedScreen(
                                        viewModel = viewModel(),
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            is Route.Folders -> NavEntry(key, metadata = key.motionMetadata()) {
                                Scaffold(
                                    topBar = {
                                        MemoirTopBar(
                                            title = "Folders",
                                            isDarkMode = isDarkMode,
                                            navigationIcon = {
                                                IconButton(onClick = popBack) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                ) { innerPadding ->
                                    FoldersScreen(
                                        viewModel = viewModel(),
                                        onFolderClick = { _ ->
                                            popBack()
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            is Route.Desk -> NavEntry(key, metadata = key.motionMetadata()) {
                                DeskScreen(
                                    id = key.id,
                                    isMilestone = key.isMilestone,
                                    viewModel = viewModel(),
                                    onBack = popBack
                                )
                            }
                        }
                    }
                )
                }
            }
        }
    }
}

private const val MotionOrderKey = "motionOrder"
private const val MotionDurationMillis = 340
private val TopLevelRoutes = listOf(Route.Memoirs, Route.Milestones, Route.Settings)

private enum class NavigationMotion {
    Instant,
    Slide
}

private val Scene<Route>.motionOrder: Int
    get() = metadata[MotionOrderKey] as? Int ?: 0

private fun Route.motionMetadata(): Map<String, Any> = mapOf(MotionOrderKey to motionOrder)

private val Route.motionOrder: Int
    get() = when (this) {
        is Route.Memoirs -> 0
        is Route.Milestones -> 1
        is Route.Settings -> 2
        is Route.Folders -> 3
        is Route.Archive -> 3
        is Route.RecentlyDeleted -> 3
        is Route.Desk -> 4
    }

private fun AnimatedContentTransitionScope<Scene<Route>>.memoirSlideTransform(
    direction: AnimatedContentTransitionScope.SlideDirection
): ContentTransform {
    val animationSpec = tween<IntOffset>(
        durationMillis = MotionDurationMillis,
        easing = FastOutSlowInEasing
    )
    return slideIntoContainer(
        towards = direction,
        animationSpec = animationSpec
    ) togetherWith slideOutOfContainer(
        towards = direction,
        animationSpec = animationSpec
    )
}

private fun memoirInstantTransform(): ContentTransform {
    return EnterTransition.None togetherWith ExitTransition.None
}

private fun Modifier.topLevelSwipeNavigation(
    currentRoute: Route?,
    onNavigate: (Route) -> Unit
): Modifier {
    val currentIndex = TopLevelRoutes.indexOf(currentRoute).takeIf { it >= 0 } ?: return this
    return pointerInput(currentRoute) {
        val threshold = 112.dp.toPx()
        var totalDrag = 0f

        detectHorizontalDragGestures(
            onDragStart = {
                totalDrag = 0f
            },
            onHorizontalDrag = { _, dragAmount ->
                totalDrag += dragAmount
            },
            onDragEnd = {
                if (abs(totalDrag) >= threshold) {
                    val destinationIndex = if (totalDrag < 0f) currentIndex + 1 else currentIndex - 1
                    TopLevelRoutes.getOrNull(destinationIndex)?.let(onNavigate)
                }
            },
            onDragCancel = {
                totalDrag = 0f
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoirTopBar(
    title: String,
    isDarkMode: Boolean,
    showLogo: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showLogo) {
                    Icon(
                        painter = painterResource(
                            id = if (isDarkMode) R.drawable.logo_dark else R.drawable.logo_light
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun MemoirBottomBar(
    currentRoute: Route?,
    onNavigate: (Route) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        NavigationItem(
            selected = currentRoute is Route.Memoirs,
            icon = { Icon(Icons.Default.AutoStories, contentDescription = null) },
            label = "Chronicle",
            onClick = { onNavigate(Route.Memoirs) }
        )
        NavigationItem(
            selected = currentRoute is Route.Milestones,
            icon = { Icon(Icons.Default.Flag, contentDescription = null) },
            label = "Milestones",
            onClick = { onNavigate(Route.Milestones) }
        )
        NavigationItem(
            selected = currentRoute is Route.Settings,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = "Settings",
            onClick = { onNavigate(Route.Settings) }
        )
    }
}

@Composable
private fun RowScope.NavigationItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
