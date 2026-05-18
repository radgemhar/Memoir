package com.example.memoir.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.memoir.data.MemoirTag
import com.example.memoir.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class Mood(val emoji: String, val label: String)

val DefaultMoods = listOf(
    Mood("😊", "Happy"),
    Mood("😔", "Sad"),
    Mood("🤔", "Thoughtful"),
    Mood("😤", "Angry"),
    Mood("😌", "Calm"),
    Mood("✨", "Inspired")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeskScreen(
    id: String?,
    isMilestone: Boolean,
    viewModel: DeskViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(id, isMilestone) {
        viewModel.load(id, isMilestone)
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isUserTyping by remember { mutableStateOf(false) }

    var titleValue by remember { mutableStateOf(TextFieldValue(viewModel.title)) }
    var bodyValue by remember { mutableStateOf(TextFieldValue(viewModel.body)) }

    LaunchedEffect(viewModel.title) {
        if (titleValue.text != viewModel.title) {
            titleValue = titleValue.copy(text = viewModel.title, selection = TextRange(viewModel.title.length))
        }
    }

    LaunchedEffect(viewModel.body) {
        if (bodyValue.text != viewModel.body) {
            bodyValue = bodyValue.copy(text = viewModel.body, selection = TextRange(viewModel.body.length))
        }
    }

    LaunchedEffect(id) {
        if (id == null) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val handleBack = {
        viewModel.save()
        onBack()
    }

    val handleSaveState = {
        viewModel.save()
        isUserTyping = false
        // Clear history so buttons stay hidden until new edits
        viewModel.clearHistory()
        focusManager.clearFocus()
        keyboardController?.hide()
        Unit
    }

    val showActions = isUserTyping || viewModel.canUndo() || viewModel.canRedo() || viewModel.isDirty

    val sdf = remember { SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault()) }
    val formattedDate = remember(viewModel.createdAt) { sdf.format(Date(viewModel.createdAt)) }

    BackHandler(onBack = handleBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (showActions) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = viewModel.canUndo()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (viewModel.canUndo()) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = viewModel.canRedo()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (viewModel.canRedo()) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                            )
                        }
                        IconButton(
                            onClick = handleSaveState,
                            enabled = viewModel.isDirty
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save State",
                                tint = if (viewModel.isDirty) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(
                value = titleValue,
                onValueChange = {
                    titleValue = it
                    if (viewModel.title != it.text) {
                        viewModel.onTitleChange(it.text)
                        isUserTyping = true
                    }
                },
                placeholder = { Text("Title", style = TextStyle(fontSize = 24.sp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (it.isFocused) isUserTyping = true },
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Text(
                    text = "${viewModel.body.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (!viewModel.isMilestoneMode) {
                MoodSelector(
                    selectedEmoji = viewModel.moodEmoji,
                    selectedLabel = viewModel.moodLabel,
                    onMoodSelected = { emoji, label ->
                        viewModel.moodEmoji = emoji
                        viewModel.moodLabel = label
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                MemoirTagPicker(
                    selectedTag = viewModel.tag,
                    onTagSelected = { viewModel.tag = it },
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                TextField(
                    value = bodyValue,
                    onValueChange = {
                        bodyValue = it
                        if (viewModel.body != it.text) {
                            viewModel.onBodyChange(it.text)
                            isUserTyping = true
                        }
                    },
                    placeholder = { Text("Write your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) isUserTyping = true },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Text(
                    text = "${viewModel.body.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MoodSelector(
    selectedEmoji: String?,
    selectedLabel: String?,
    onMoodSelected: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Mood",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(DefaultMoods) { mood ->
                MoodChip(
                    emoji = mood.emoji,
                    label = mood.label,
                    isSelected = selectedEmoji == mood.emoji,
                    onClick = {
                        if (selectedEmoji == mood.emoji) onMoodSelected(null, null)
                        else onMoodSelected(mood.emoji, mood.label)
                    }
                )
            }
            
            item {
                IconButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Custom Mood", modifier = Modifier.size(20.dp))
                }
            }

            if (selectedEmoji != null && DefaultMoods.none { it.emoji == selectedEmoji }) {
                item {
                    MoodChip(
                        emoji = selectedEmoji,
                        label = selectedLabel ?: "Custom",
                        isSelected = true,
                        onClick = { onMoodSelected(null, null) }
                    )
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomMoodDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { emoji, label ->
                onMoodSelected(emoji, label)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun MoodChip(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun CustomMoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var emoji by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Mood") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { if (it.length <= 2) emoji = it },
                    label = { Text("Emoji") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    placeholder = { Text("Inspired") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (emoji.isNotBlank() && label.isNotBlank()) onConfirm(emoji, label) },
                enabled = emoji.isNotBlank() && label.isNotBlank()
            ) {
                Text("Add")
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
fun MemoirTagPicker(
    selectedTag: MemoirTag,
    onTagSelected: (MemoirTag) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemoirTag.entries.forEach { tag ->
            val color = when (tag) {
                MemoirTag.PLAIN -> TagPlain
                MemoirTag.IDEA -> TagIdea
                MemoirTag.TASK -> TagTask
                MemoirTag.MEMORY -> TagMemory
                MemoirTag.URGENT -> TagUrgent
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selectedTag == tag) 3.dp else 0.dp,
                        color = if (selectedTag == tag) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onTagSelected(tag) }
            )
        }
    }
}
