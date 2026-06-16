package com.dansheng.notifyenh.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dansheng.notifyenh.R
import com.dansheng.notifyenh.ui.theme.NotifyEnhTheme
import kotlin.system.exitProcess

class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashLog = intent.getStringExtra(EXTRA_CRASH_LOG) ?: "No crash log found"

        setContent {
            NotifyEnhTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrashScreen(
                        crashLog = crashLog,
                        onCopy = {
                            copyToClipboard(crashLog)
                        },
                        onExit = {
                            finishAffinity()
                            exitProcess(0)
                        }
                    )
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Crash Log", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_CRASH_LOG = "extra_crash_log"
    }
}

@Composable
fun CrashScreen(
    crashLog: String,
    onCopy: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_crashed),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.crash_details),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = crashLog,
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCopy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.copy_to_clipboard))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.close))
        }
    }
}
