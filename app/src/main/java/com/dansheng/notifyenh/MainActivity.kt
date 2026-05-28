package com.dansheng.notifyenh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.dansheng.notifyenh.data.prefs.AppPreferences
import com.dansheng.notifyenh.data.prefs.ThemeMode
import com.dansheng.notifyenh.ui.screens.NotificationListScreen
import com.dansheng.notifyenh.ui.screens.SettingsScreen
import com.dansheng.notifyenh.ui.screens.TaskerScreen
import com.dansheng.notifyenh.ui.theme.NotifyEnhTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appPreferences = AppPreferences(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by appPreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            NotifyEnhTheme(darkTheme = darkTheme) {
                NotifyEnhApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun NotifyEnhApp() {
    val destinations = AppDestinations.entries
    var currentDestination by remember { mutableStateOf(AppDestinations.HOME) }
    val pagerState = rememberPagerState(pageCount = { destinations.size })
    val scope = rememberCoroutineScope()

    // 当 Pager 页面滑动时，同步更新导航栏选中状态
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentDestination = destinations[page]
        }
    }

    // 当导航栏点击时，同步更新 Pager 页面
    LaunchedEffect(currentDestination) {
        val targetPage = destinations.indexOf(currentDestination)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            destinations.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                beyondViewportPageCount = 1
            ) { page ->
                when (destinations[page]) {
                    AppDestinations.HOME -> NotificationListScreen()
                    AppDestinations.Tasker -> TaskerScreen()
                    AppDestinations.PROFILE -> SettingsScreen()
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("首页", R.drawable.ic_home),
    Tasker("任务", R.drawable.ic_tasks),
    PROFILE("配置", R.drawable.ic_settings),
}
