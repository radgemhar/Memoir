package com.example.memoir.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.Palette
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
import java.text.SimpleDateFormat
import java.util.*

data class Mood(val emoji: String, val label: String)

val DefaultMoods = listOf(
    Mood("\uD83D\uDE0A", "Happy"),
    Mood("\uD83D\uDE14", "Sad"),
    Mood("\uD83E\uDD14", "Thoughtful"),
    Mood("\uD83D\uDE24", "Angry"),
    Mood("\uD83D\uDE0C", "Calm"),
    Mood("\u2728", "Inspired")
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

                EntryHighlightPicker(
                    selectedColor = viewModel.highlightColor,
                    onColorSelected = viewModel::onHighlightColorChange,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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

private val HighlightPresets = listOf(
    0xFFEF4444,
    0xFFF97316,
    0xFFEAB308,
    0xFF22C55E,
    0xFF06B6D4,
    0xFF6366F1,
    0xFFD946EF,
    0xFF9CA3AF
)

private val PickerGrayColumn = listOf(
    0xFF111111,
    0xFF3F3F46,
    0xFF71717A,
    0xFFA1A1AA,
    0xFFD4D4D8,
    0xFFFFFFFF
)

private val PickerRows = listOf(
    listOf(0xFF660000, 0xFF663300, 0xFF666600, 0xFF336600, 0xFF006600, 0xFF006633, 0xFF006666, 0xFF003366, 0xFF000066),
    listOf(0xFF990000, 0xFF994C00, 0xFF999900, 0xFF4C9900, 0xFF009900, 0xFF00994C, 0xFF009999, 0xFF004C99, 0xFF000099),
    listOf(0xFFCC0000, 0xFFCC6600, 0xFFCCCC00, 0xFF66CC00, 0xFF00CC00, 0xFF00CC66, 0xFF00CCCC, 0xFF0066CC, 0xFF0000CC),
    listOf(0xFFFF0000, 0xFFFF8000, 0xFFFFFF00, 0xFF80FF00, 0xFF00FF00, 0xFF00FF80, 0xFF00FFFF, 0xFF0080FF, 0xFF0000FF),
    listOf(0xFFFF6666, 0xFFFFB266, 0xFFFFFF66, 0xFFB2FF66, 0xFF66FF66, 0xFF66FFB2, 0xFF66FFFF, 0xFF66B2FF, 0xFF6666FF),
    listOf(0xFFFFB3B3, 0xFFFFD9B3, 0xFFFFFFB3, 0xFFD9FFB3, 0xFFB3FFB3, 0xFFB3FFD9, 0xFFB3FFFF, 0xFFB3D9FF, 0xFFB3B3FF)
)

@Composable
fun EntryHighlightPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomPicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Highlight",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HighlightPresets.forEach { colorValue ->
                HighlightSwatch(
                    color = colorValue,
                    selected = selectedColor == colorValue,
                    onClick = {
                        if (selectedColor == colorValue) {
                            onColorSelected(0L)
                        } else {
                            onColorSelected(colorValue)
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { showCustomPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Custom color",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showCustomPicker) {
        CustomHighlightDialog(
            selectedColor = selectedColor,
            onDismiss = { showCustomPicker = false },
            onConfirm = { color ->
                onColorSelected(color)
                showCustomPicker = false
            }
        )
    }
}

@Composable
private fun HighlightSwatch(
    color: Long,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(color))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun CustomHighlightDialog(
    selectedColor: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var previewColor by remember(selectedColor) { mutableStateOf(selectedColor) }
    var hexValue by remember(selectedColor) { mutableStateOf(selectedColor.toHexColor()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom highlight") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(width = 72.dp, height = 36.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(previewColor)
                    ) {}
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = hexValue,
                        onValueChange = { value ->
                            hexValue = value.toHexInput()
                            hexValue.parseHexColor()?.let { color ->
                                previewColor = color
                            }
                        },
                        label = { Text("Hex") },
                        placeholder = { Text("#0066CC") },
                        singleLine = true,
                        isError = hexValue.isNotBlank() && hexValue.parseHexColor() == null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PickerGrayColumn.forEach { color ->
                            PaletteCell(
                                color = color,
                                selected = previewColor == color,
                                onClick = {
                                    previewColor = color
                                    hexValue = color.toHexColor()
                                }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PickerRows.forEach { rowColors ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                rowColors.forEach { color ->
                                    PaletteCell(
                                        color = color,
                                        selected = previewColor == color,
                                        onClick = {
                                            previewColor = color
                                            hexValue = color.toHexColor()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(previewColor) }) {
                Text("Apply")
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
private fun PaletteCell(
    color: Long,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(Color(color))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            .clickable(onClick = onClick)
    )
}

private fun Long.toHexColor(): String {
    return String.format(Locale.ROOT, "#%06X", this and 0xFFFFFF)
}

private fun String.toHexInput(): String {
    val trimmed = trim().removePrefix("#")
    val value = trimmed
        .filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        .take(6)
        .uppercase(Locale.ROOT)
    return if (value.isEmpty()) "" else "#$value"
}

private fun String.parseHexColor(): Long? {
    val value = trim().removePrefix("#")
    if (value.length != 6 || value.any { !it.isDigit() && it !in 'a'..'f' && it !in 'A'..'F' }) {
        return null
    }
    return 0xFF000000 or value.toLong(16)
}
