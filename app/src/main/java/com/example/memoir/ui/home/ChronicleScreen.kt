package com.example.memoir.ui.home

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.memoir.ui.editor.parseRichText
import com.example.memoir.ui.editor.stripImageTags
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.example.memoir.data.Memoir
import com.example.memoir.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicleScreen(
    viewModel: ChronicleViewModel,
    onNavigateToDesk: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val memoirs by viewModel.memoirs.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(550)
            isRefreshing = false
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refresh()
            isRefreshing = true
        },
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
        Column(modifier = Modifier.fillMaxSize()) {
        val focusRequester = remember { FocusRequester() }
        
        TextField(
            value = searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = { Text("Recall...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .focusRequester(focusRequester),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear recall")
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        FolderSelector(
            folders = folders,
            selectedFolder = selectedFolder,
            onFolderSelected = viewModel::setSelectedFolder,
            onCreateFolder = { showNewFolderDialog = true },
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            if (memoirs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyChronicle(
                        isRecall = searchQuery.isNotBlank(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(memoirs, key = { it.id }) { memoir ->
                        val dismissState = rememberSwipeToDismissBoxState()
                        
                        LaunchedEffect(dismissState.currentValue) {
                            when (dismissState.currentValue) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    viewModel.preserveMemoir(memoir)
                                    Toast.makeText(context, "Memoir archived", Toast.LENGTH_SHORT).show()
                                    dismissState.reset()
                                }
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    viewModel.discardMemoir(memoir) { deletedMemoir ->
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Memoir deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.restoreMemoir(deletedMemoir)
                                            }
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = { SwipeBackground(dismissState) },
                            content = {
                                MemoirCard(
                                    memoir = memoir,
                                    folders = folders,
                                    onClick = { onNavigateToDesk(memoir.id) },
                                    onLongClick = {
                                        viewModel.togglePin(memoir) {
                                            Toast.makeText(context, "Max 5 memoirs pinned", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onMoveToFolder = { folderName ->
                                        viewModel.moveMemoirToFolder(memoir, folderName)
                                    },
                                    onPreserve = { viewModel.preserveMemoir(memoir) },
                                    onDiscard = {
                                         viewModel.discardMemoir(memoir) { }
                                         Toast.makeText(context, "Memoir deleted", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        }
        }
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { name ->
                viewModel.createFolder(name)
                showNewFolderDialog = false
            }
        )
    }
}

@Composable
fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF525252)
            SwipeToDismissBoxValue.EndToStart -> Color(0xFF27272A)
            else -> Color.Transparent
        }, label = "swipe_color"
    )
    val alignment = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    val icon = when (dismissState.targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Archive
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        icon?.let {
            Icon(it, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun FolderSelector(
    folders: List<String>,
    selectedFolder: String,
    onFolderSelected: (String) -> Unit,
    onCreateFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(folders, key = { it }) { folder ->
            FilterChip(
                selected = selectedFolder == folder,
                onClick = { onFolderSelected(folder) },
                label = { Text(folder) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        item {
            AssistChip(
                onClick = onCreateFolder,
                label = { Text("New folder") },
                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) }
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MemoirCard(
    memoir: Memoir,
    folders: List<String> = emptyList(),
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveToFolder: (String) -> Unit = {},
    onPreserve: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightColor = Color(memoir.highlightColor)
    val sdf = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }
    val formattedDate = remember(memoir.createdAt) { sdf.format(Date(memoir.createdAt)) }

    var showMenu by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .then(
                if (memoir.isPinned) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (memoir.isArchived) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (memoir.isArchived) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .then(
                    if (memoir.isArchived) {
                        Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    } else Modifier
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (memoir.highlightColor != 0L) 6.dp else 0.dp)
                    .background(highlightColor)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = memoir.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (memoir.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                if (folders.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Move to folder") }, 
                                        colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSurface, leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                        onClick = {
                                            showMenu = false
                                            showFolderDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Archive") }, 
                                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSurface, leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    onClick = {
                                        showMenu = false
                                        onPreserve()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") }, 
                                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error, leadingIconColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        showMenu = false
                                        onDiscard()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = parseRichText(stripImageTags(memoir.body)),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (memoir.moodEmoji != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = if (!memoir.moodLabel.isNullOrBlank()) "${memoir.moodEmoji}  ${memoir.moodLabel}" else memoir.moodEmoji!!,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFolderDialog) {
        MoveToFolderDialog(
            folders = folders,
            currentFolder = memoir.folderName,
            onDismiss = { showFolderDialog = false },
            onSelect = { folderName ->
                onMoveToFolder(folderName)
                showFolderDialog = false
            }
        )
    }
}

@Composable
private fun NewFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("New folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(32) },
                label = { Text("Folder name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.trim().isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MoveToFolderDialog(
    folders: List<String>,
    currentFolder: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text("Move to folder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                folders.forEach { folder ->
                    ListItem(
                        headlineContent = { Text(folder) },
                        trailingContent = {
                            if (folder == currentFolder) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(folder) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyChronicle(
    isRecall: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isRecall) "No matching reflections found" else "Your chronicle is empty",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
