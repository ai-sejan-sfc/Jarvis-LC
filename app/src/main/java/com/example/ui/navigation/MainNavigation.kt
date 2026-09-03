package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JarvisHeader
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.screens.SecurityPluginsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartHomeScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.viewmodel.JarvisViewModel

enum class JarvisDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    ASSISTANT("Jarvis", Icons.Filled.Mic, Icons.Outlined.Mic),
    SMART_HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    CALENDAR("Tasks", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    ROUTINES("Routines", Icons.Filled.Repeat, Icons.Outlined.Repeat),
    HEALTH("Health", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SECURITY("Vault", Icons.Filled.Security, Icons.Outlined.Security),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainNavigation(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(JarvisDestination.ASSISTANT) }
    val syncState by viewModel.syncState.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        topBar = {
            JarvisHeader(
                isOnline = syncState.isOnline,
                isSpeaking = isSpeaking,
                onToggleOnline = { viewModel.toggleOnlineSync() },
                onStopSpeaking = { viewModel.stopSpeaking() },
                onOpenSettings = { currentDestination = JarvisDestination.SETTINGS }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_bottom_nav"),
                containerColor = JarvisSurface,
                tonalElevation = 6.dp
            ) {
                JarvisDestination.values().forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = dest.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JarvisBackground,
                            selectedTextColor = JarvisCyan,
                            indicatorColor = JarvisCyan,
                            unselectedIconColor = JarvisTextMuted,
                            unselectedTextColor = JarvisTextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { dest ->
                when (dest) {
                    JarvisDestination.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                    JarvisDestination.SMART_HOME -> SmartHomeScreen(viewModel = viewModel)
                    JarvisDestination.CALENDAR -> CalendarScreen(viewModel = viewModel)
                    JarvisDestination.ROUTINES -> RoutinesScreen(viewModel = viewModel)
                    JarvisDestination.HEALTH -> DashboardScreen(viewModel = viewModel)
                    JarvisDestination.SECURITY -> SecurityPluginsScreen(viewModel = viewModel)
                    JarvisDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
