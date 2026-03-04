package info.sorbon.hskvocabulary.presentation.contacts

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ContactItem(
    val icon: ImageVector,
    val title: String,
    val detail: String,
    val appUrl: String?,
    val webUrl: String
)

private val contacts = listOf(
    ContactItem(Icons.Default.Email, "Email", "sorbonm@gmail.com", null, "mailto:sorbonm@gmail.com"),
    ContactItem(Icons.Default.Language, "Facebook", "sorbonm", "fb://profile/100001280100955", "https://www.facebook.com/sorbonm"),
    ContactItem(Icons.Default.Language, "Instagram", "sorbonm", "instagram://user?username=sorbonm", "https://www.instagram.com/sorbonm"),
    ContactItem(Icons.Default.Language, "Telegram", "sorbonm", "tg://resolve?domain=sorbonm", "https://t.me/sorbonm"),
    ContactItem(Icons.Default.Language, "WeChat", "sorbonm", "weixin://", "https://www.wechat.com")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current

    val appVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (_: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text(
                        "We'd love to hear from you!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Found a bug, have an idea, or want to suggest an improvement? " +
                                "Your feedback helps us make HSK Vocabulary better for everyone. " +
                                "Please don't hesitate to reach out through any channel that's convenient for you — " +
                                "we read every message and appreciate your input!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
            }
            items(contacts) { contact ->
                Surface(
                    onClick = {
                        if (contact.title == "Email") {
                            openEmail(context, contact.detail, appVersion)
                        } else {
                            openContact(context, contact)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(contact.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.title, style = MaterialTheme.typography.bodyLarge)
                            Text(contact.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
            }
            item {
                Text(
                    "Version $appVersion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

private fun openEmail(context: Context, email: String, appVersion: String) {
    val subject = "HSK Vocabulary - Feedback"
    val body = "\n\n\n--- Please don't remove this info ---\n" +
            "App: HSK Vocabulary\n" +
            "Version: $appVersion\n" +
            "Platform: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
            "Device: ${Build.MANUFACTURER} ${Build.MODEL}"

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        // No email client — fall back to mailto link
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$email")))
        } catch (_: Exception) { }
    }
}

private fun openContact(context: Context, contact: ContactItem) {
    val appUrl = contact.appUrl
    if (appUrl != null) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appUrl))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        } catch (_: Exception) { }
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(contact.webUrl)))
    } catch (_: Exception) { }
}
