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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.memoir.ui.editor.parseRichText
import com.example.memoir.ui.editor.stripImageTags
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
    var memoirToDelete by remember { mutableStateOf<Memoir?>(null) }

    RefreshableLibraryList(
        items = memoirs,
        emptyText = "Archive is empty",
        modifier = modifier,
        actions = { memoir ->
            Button(
                onClick = { viewModel.restore(memoir) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore", style = MaterialTheme.typography.labelLarge)
            }
            
            OutlinedButton(
                onClick = { memoirToDelete = memoir },
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.5.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete", style = MaterialTheme.typography.labelLarge)
            }
        },
        onOpenMemoir = onOpenMemoir
    )

    if (memoirToDelete != null) {
        AlertDialog(
            onDismissRequest = { memoirToDelete = null },
            title = { Text("Discard Memoir") },
            text = { Text("Are you sure you want to move this memoir to recently deleted?") },
            confirmButton = {
                Button(
                    onClick = {
                        memoirToDelete?.let { viewModel.delete(it) }
                        memoirToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { memoirToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyDeletedScreen(
    viewModel: RecentlyDeletedViewModel,
    modifier: Modifier = Modifier
) {
    val memoirs by viewModel.memoirs.collectAsState()
    var memoirToDeletePermanently by remember { mutableStateOf<Memoir?>(null) }
    var showEmptyAllDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        if (memoirs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showEmptyAllDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Empty all")
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
                Button(
                    onClick = { viewModel.restore(memoir) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore", style = MaterialTheme.typography.labelLarge)
                }
                
                OutlinedButton(
                    onClick = { memoirToDeletePermanently = memoir },
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.5.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete forever", style = MaterialTheme.typography.labelLarge)
                }
            },
            onOpenMemoir = {}
        )
    }

    if (memoirToDeletePermanently != null) {
        AlertDialog(
            onDismissRequest = { memoirToDeletePermanently = null },
            title = { Text("Delete Permanently") },
            text = { Text("This memoir will be gone forever. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        memoirToDeletePermanently?.let { viewModel.deletePermanently(it) }
                        memoirToDeletePermanently = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { memoirToDeletePermanently = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEmptyAllDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyAllDialog = false },
            title = { Text("Empty Recently Deleted") },
            text = { Text("Clear all ${memoirs.size} items? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyAll()
                        showEmptyAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Empty All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyAllDialog = false }) {
                    Text("Cancel")
                }
            }
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

    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        modifier = modifier.fillMaxSize()
    ) {
        if (items.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()), 
                contentAlignment = Alignment.Center
            ) {
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
                text = parseRichText(stripImageTags(memoir.body)),
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
