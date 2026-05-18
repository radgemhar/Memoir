package com.example.memoir.ui.home

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.memoir.data.MemoirTag
import com.example.memoir.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicleScreen(
    viewModel: ChronicleViewModel,
    onNavigateToDesk: (String?) -> Unit
) {
    val memoirs by viewModel.memoirs.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

        ChronicleFilterChips(
            selectedFilter = currentFilter,
            onFilterSelected = viewModel::setFilter,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            if (memoirs.isEmpty()) {
                EmptyChronicle(
                    isRecall = searchQuery.isNotBlank(),
                    modifier = Modifier.fillMaxSize()
                )
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
                                    Toast.makeText(context, "Memoir preserved", Toast.LENGTH_SHORT).show()
                                    dismissState.reset()
                                }
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    viewModel.discardMemoir(memoir) { deletedMemoir ->
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Memoir discarded",
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
                                    onClick = { onNavigateToDesk(memoir.id) },
                                    onLongClick = {
                                        viewModel.togglePin(memoir) {
                                            Toast.makeText(context, "Max 5 memoirs pinned", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onPreserve = { viewModel.preserveMemoir(memoir) },
                                    onDiscard = {
                                         viewModel.discardMemoir(memoir) { }
                                         Toast.makeText(context, "Memoir discarded", Toast.LENGTH_SHORT).show()
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

@Composable
fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFB7185)
            SwipeToDismissBoxValue.EndToStart -> Color(0xFF1E293B)
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
fun ChronicleFilterChips(
    selectedFilter: ChronicleFilter,
    onFilterSelected: (ChronicleFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChronicleFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { 
                    val label = when(filter) {
                        ChronicleFilter.ALL -> "All"
                        ChronicleFilter.ACTIVE -> "Active"
                        ChronicleFilter.ARCHIVED -> "Preserved"
                    }
                    Text(label) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MemoirCard(
    memoir: Memoir,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPreserve: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = when (memoir.tag) {
        MemoirTag.PLAIN -> TagPlain
        MemoirTag.IDEA -> TagIdea
        MemoirTag.TASK -> TagTask
        MemoirTag.MEMORY -> TagMemory
        MemoirTag.URGENT -> TagUrgent
    }

    val isNew = System.currentTimeMillis() - memoir.createdAt < 24 * 60 * 60 * 1000
    val sdf = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(memoir.createdAt) { sdf.format(Date(memoir.createdAt)) }

    var showMenu by remember { mutableStateOf(false) }

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
                        color = if (isSystemInDarkTheme()) DarkPinnedGlow else LightPinnedGlow,
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
                    .width(6.dp)
                    .background(tagColor)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
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
                        if (isNew) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LightPrimary)
                            )
                        }
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
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Preserve") },
                                    onClick = { 
                                        onPreserve()
                                        showMenu = false 
                                    },
                                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Discard") },
                                    onClick = { 
                                        onDiscard()
                                        showMenu = false 
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = memoir.body,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    if (memoir.moodEmoji != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = memoir.moodEmoji,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
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
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
