package com.example.memoir.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.memoir.data.DELETED_RETENTION_MILLIS
import com.example.memoir.data.Memoir
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onOpenMemoir: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val memoirs by viewModel.memoirs.collectAsState()
    RefreshableLibraryList(
        items = memoirs,
        emptyText = "Archive is empty",
        modifier = modifier,
        actions = { memoir ->
            OutlinedButton(onClick = { viewModel.restore(memoir) }) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore")
            }
            TextButton(onClick = { viewModel.delete(memoir) }) {
                Text("Delete")
            }
        },
        onOpenMemoir = onOpenMemoir
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyDeletedScreen(
    viewModel: RecentlyDeletedViewModel,
    modifier: Modifier = Modifier
) {
    val memoirs by viewModel.memoirs.collectAsState()
    Column(modifier = modifier.fillMaxSize()) {
        if (memoirs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = viewModel::emptyAll) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete all")
                }
            }
        }

        RefreshableLibraryList(
            items = memoirs,
            emptyText = "Recently Deleted is empty",
            modifier = Modifier.weight(1f),
            supportingText = { memoir ->
                val deletedAt = memoir.deletedAt ?: System.currentTimeMillis()
                val expiresAt = deletedAt + DELETED_RETENTION_MILLIS
                val daysLeft = ceil(((expiresAt - System.currentTimeMillis()).coerceAtLeast(0L)) / 86_400_000.0).toInt()
                "$daysLeft days left"
            },
            actions = { memoir ->
                OutlinedButton(onClick = { viewModel.restore(memoir) }) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore")
                }
                TextButton(onClick = { viewModel.deletePermanently(memoir) }) {
                    Text("Delete forever")
                }
            },
            onOpenMemoir = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshableLibraryList(
    items: List<Memoir>,
    emptyText: String,
    modifier: Modifier = Modifier,
    supportingText: (Memoir) -> String = { it.folderName },
    actions: @Composable (Memoir) -> Unit,
    onOpenMemoir: (String) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(550)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        modifier = modifier.fillMaxSize()
    ) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { memoir ->
                    LibraryMemoirCard(
                        memoir = memoir,
                        supportingText = supportingText(memoir),
                        actions = { actions(memoir) },
                        onClick = { if (memoir.deletedAt == null) onOpenMemoir(memoir.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryMemoirCard(
    memoir: Memoir,
    supportingText: String,
    actions: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(memoir.createdAt) { sdf.format(Date(memoir.createdAt)) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .padding(end = 10.dp)
                )
                Text(
                    text = memoir.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = memoir.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$formattedDate  •  $supportingText",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                actions()
            }
        }
    }
}
