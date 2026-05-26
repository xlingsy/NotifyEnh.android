package com.example.notifyenh

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.notifyenh.data.AppDatabase
import com.example.notifyenh.data.NotificationEntity
import com.example.notifyenh.data.TaskEntity
import com.example.notifyenh.service.NotifyEnhService
import com.example.notifyenh.ui.theme.NotifyEnhTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotifyEnhTheme {
                NotifyEnhApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun NotifyEnhApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
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
            when (currentDestination) {
                AppDestinations.HOME -> {
                    NotificationListScreen(modifier = Modifier.padding(innerPadding))
                }
                AppDestinations.Tasker -> {
                    TaskerScreen(modifier = Modifier.padding(innerPadding))
                }
                AppDestinations.PROFILE -> {
                    SettingsScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NotificationListScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val notifications by remember(searchQuery) {
        if (searchQuery.isBlank()) {
            database.notificationDao().getAllNotificationsFlow()
        } else {
            database.notificationDao().searchNotifications(searchQuery)
        }
    }.collectAsState(initial = emptyList())

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("搜索通知标题、内容或应用") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(notifications, key = { it.id }) { notification ->
                NotificationItem(notification)
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationEntity) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = timeFormat.format(Date(notification.postTime))

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.packageName.split(".").last(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = notification.title ?: "No Title",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = notification.content ?: "No Content",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TaskerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val tasks by database.taskDao().getAllTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Text(
                text = "任务管理",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加任务")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onEdit = { taskToEdit = it },
                    onToggle = { enabled ->
                        scope.launch {
                            database.taskDao().update(task.copy(isEnabled = enabled))
                        }
                    },
                    onDelete = {
                        scope.launch {
                            database.taskDao().delete(task)
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog || taskToEdit != null) {
        TaskEditDialog(
            task = taskToEdit,
            onDismiss = {
                showAddDialog = false
                taskToEdit = null
            },
            onConfirm = { newTask ->
                scope.launch {
                    if (taskToEdit != null) {
                        database.taskDao().update(newTask)
                    } else {
                        database.taskDao().insert(newTask)
                    }
                    showAddDialog = false
                    taskToEdit = null
                }
            }
        )
    }
}

@Composable
fun TaskItem(
    task: TaskEntity,
    onEdit: (TaskEntity) -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isEnabled) MaterialTheme.colorScheme.surfaceVariant 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.name, style = MaterialTheme.typography.titleMedium)
                if (!task.packageName.isNullOrBlank()) {
                    Text(
                        text = "应用: ${task.packageName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "${if (task.isRegex) "正则" else "关键词"}: ${task.pattern}",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    if (task.actionCancel) {
                        Text(
                            "取消通知 ", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (task.actionTts) {
                        Text(
                            "TTS朗读", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            IconButton(onClick = { onEdit(task) }) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
            Switch(checked = task.isEnabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun TaskEditDialog(
    task: TaskEntity?,
    onDismiss: () -> Unit,
    onConfirm: (TaskEntity) -> Unit
) {
    var name by remember { mutableStateOf(task?.name ?: "") }
    var packageName by remember { mutableStateOf(task?.packageName ?: "") }
    var pattern by remember { mutableStateOf(task?.pattern ?: "") }
    var isRegex by remember { mutableStateOf(task?.isRegex ?: false) }
    var actionCancel by remember { mutableStateOf(task?.actionCancel ?: false) }
    var actionTts by remember { mutableStateOf(task?.actionTts ?: false) }

    var showAppPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "添加任务" else "编辑任务") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("任务名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it },
                        label = { Text("限制应用包名 (可选)") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showAppPicker = true }) {
                                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "选择应用")
                            }
                        }
                    )
                    
                    if (showAppPicker) {
                        AppPickerLoader(
                            onAppSelected = {
                                packageName = it
                                showAppPicker = false
                            },
                            onDismiss = { showAppPicker = false }
                        )
                    }
                }

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text(if (isRegex) "正则表达式" else "关键词") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRegex, onCheckedChange = { isRegex = it })
                    Text("使用正则表达式")
                }
                Text("触发操作:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = actionCancel, onCheckedChange = { actionCancel = it })
                    Text("取消通知")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = actionTts, onCheckedChange = { actionTts = it })
                    Text("TTS朗读")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        TaskEntity(
                            id = task?.id ?: 0,
                            name = name,
                            packageName = if (packageName.isBlank()) null else packageName,
                            pattern = pattern,
                            isRegex = isRegex,
                            actionCancel = actionCancel,
                            actionTts = actionTts,
                            isEnabled = task?.isEnabled ?: true
                        )
                    )
                },
                enabled = name.isNotBlank() && pattern.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun AppPickerLoader(
    onAppSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val apps = remember {
        val pm = context.packageManager
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择应用") },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(apps) { app ->
                    val pm = context.packageManager
                    ListItem(
                        headlineContent = { Text(app.loadLabel(pm).toString()) },
                        supportingContent = { Text(app.packageName) },
                        modifier = Modifier.clickable { onAppSelected(app.packageName) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isPermissionGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }
    var isServiceRunning by remember { mutableStateOf(NotifyEnhService.isServiceRunning) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isPermissionGranted = isNotificationServiceEnabled(context)
                isServiceRunning = NotifyEnhService.isServiceRunning
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        ListItem(
            headlineContent = { Text("通知监听服务") },
            supportingContent = {
                val statusText = when {
                    !isPermissionGranted -> "服务未授权，点击去授权"
                    isServiceRunning -> "服务正在运行"
                    else -> "服务已授权但未启动，点击尝试启动"
                }
                Text(statusText)
            },
            trailingContent = {
                Switch(
                    checked = isPermissionGranted && isServiceRunning,
                    onCheckedChange = { checked ->
                        if (!isPermissionGranted) {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        } else {
                            if (checked) {
                                // 尝试重新绑定服务
                                android.service.notification.NotificationListenerService.requestRebind(
                                    ComponentName(context, NotifyEnhService::class.java)
                                )
                                // 稍微延迟检查状态
                                isServiceRunning = NotifyEnhService.isServiceRunning
                            } else {
                                // 请求解绑并停止服务
                                NotifyEnhService.stopService()
                                isServiceRunning = false
                            }
                        }
                    }
                )
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (!flat.isNullOrEmpty()) {
        val names = flat.split(":")
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null) {
                if (TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
    }
    return false
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("首页", R.drawable.ic_home),
    Tasker("任务", R.drawable.ic_tasks),
    PROFILE("配置", R.drawable.ic_settings),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotifyEnhTheme {
        Greeting("Android")
    }
}
