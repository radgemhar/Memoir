package com.example.memoir.ui.editor

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Mood(val emoji: String, val label: String)

val DefaultMoods = listOf(
    Mood("\uD83D\uDE0A", "Happy"),
    Mood("\uD83D\uDE14", "Sad"),
    Mood("\uD83E\uDD14", "Thoughtful"),
    Mood("\uD83D\uDE24", "Angry"),
    Mood("\uD83D\uDE0C", "Calm"),
    Mood("\u2728", "Inspired")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeskScreen(
    id: String?,
    isMilestone: Boolean,
    viewModel: DeskViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(id, isMilestone) {
        viewModel.load(id, isMilestone)
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isUserTyping by remember { mutableStateOf(false) }

    var titleValue by remember { mutableStateOf(TextFieldValue(viewModel.title)) }
    var bodyValue by remember { mutableStateOf(TextFieldValue(viewModel.body)) }
    val isKeyboardVisible = WindowInsets.isImeVisible
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    var prevImeBottom by remember { mutableStateOf(0) }
    var isKeyboardClosing by remember { mutableStateOf(false) }
    LaunchedEffect(imeBottom) {
        if (imeBottom < prevImeBottom && imeBottom > 0) {
            isKeyboardClosing = true
        }
        if (imeBottom == 0) {
            isKeyboardClosing = false
        }
        prevImeBottom = imeBottom
    }
    var imagePlacements by remember { mutableStateOf<List<ImagePlacement>>(emptyList()) }

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

    var selectedPlacementRawUri by remember { mutableStateOf<String?>(null) }
    var activeMediaCommand by remember { mutableStateOf<Pair<String, String>?>(null) }

    val handleBack = {
        viewModel.save()
        selectedPlacementRawUri = null
        onBack()
    }

    val handleSaveState = {
        viewModel.save()
        isUserTyping = false
        selectedPlacementRawUri = null
        viewModel.clearHistory()
        focusManager.clearFocus()
        keyboardController?.hide()
        Unit
    }

    val showActions = isUserTyping || viewModel.canUndo() || viewModel.canRedo() || viewModel.isDirty

    val sdf = remember { SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault()) }
    val formattedDate = remember(viewModel.createdAt) { sdf.format(Date(viewModel.createdAt)) }

    // Media states and Launchers
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showSketchDialog by remember { mutableStateOf(false) }
    var showHighlightPickerDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = java.io.File(context.filesDir, "camera_${System.currentTimeMillis()}.png")
                java.io.FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                }
                val fileUri = "file://" + file.absolutePath
                bodyValue = insertImageTag(bodyValue, fileUri, false)
                viewModel.onBodyChange(bodyValue.text)
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving camera photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val file = java.io.File(context.filesDir, "gallery_${System.currentTimeMillis()}.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                val fileUri = "file://" + file.absolutePath
                bodyValue = insertImageTag(bodyValue, fileUri, false)
                viewModel.onBodyChange(bodyValue.text)
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    BackHandler(onBack = handleBack)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
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
        },
        bottomBar = {
            val selectedPlacement = imagePlacements.firstOrNull { it.rawUri == selectedPlacementRawUri }
            if (selectedPlacement != null) {
                MediaToolbar(
                    onDeselect = { selectedPlacementRawUri = null },
                    onAlignLeft = {
                        activeMediaCommand = "align_left" to selectedPlacement.rawUri
                    },
                    onAlignCenter = {
                        activeMediaCommand = "align_center" to selectedPlacement.rawUri
                    },
                    onAlignRight = {
                        activeMediaCommand = "align_right" to selectedPlacement.rawUri
                    },
                    onBringToFront = {
                        activeMediaCommand = "layer_forward" to selectedPlacement.rawUri
                    },
                    onSendToBack = {
                        activeMediaCommand = "layer_backward" to selectedPlacement.rawUri
                    }
                )
            } else if (!viewModel.isMilestoneMode && isKeyboardVisible && !isKeyboardClosing) {
                EditorToolbar(
                    onUploadImage = { showImageSourceDialog = true },
                    onSketch = { showSketchDialog = true },
                    onHighlight = { showHighlightPickerDialog = true },
                    onIncreaseFontSize = {
                        bodyValue = changeFontSize(bodyValue, 4)
                        viewModel.onBodyChange(bodyValue.text)
                    },
                    onDecreaseFontSize = {
                        bodyValue = changeFontSize(bodyValue, -4)
                        viewModel.onBodyChange(bodyValue.text)
                    },
                    onBold = {
                        bodyValue = toggleTag(bodyValue, "<b>", "</b>")
                        viewModel.onBodyChange(bodyValue.text)
                    },
                    onItalic = {
                        bodyValue = toggleTag(bodyValue, "<i>", "</i>")
                        viewModel.onBodyChange(bodyValue.text)
                    }
                )
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
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
                        text = "${getCleanCharacterCount(viewModel.body)} characters",
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
                }

                BodyEditorWithInlineImages(
                    bodyValue = bodyValue,
                    onBodyValueChange = {
                        bodyValue = it
                        if (viewModel.body != it.text) {
                            viewModel.onBodyChange(it.text)
                            isUserTyping = true
                        }
                    },
                    isMilestoneMode = viewModel.isMilestoneMode,
                    onFocusChanged = { if (it.isFocused) isUserTyping = true },
                    imagePlacements = imagePlacements,
                    onPlacementsCalculated = { imagePlacements = it },
                    selectedPlacementRawUri = selectedPlacementRawUri,
                    onSelectedPlacementRawUriChange = { selectedPlacementRawUri = it },
                    activeMediaCommand = activeMediaCommand,
                    onMediaCommandProcessed = { activeMediaCommand = null }
                )
            }
        }
    }

    // Media and Format Pickers Dialogs
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add Image") },
            text = { Text("Choose a photo source:") },
            confirmButton = {
                Button(onClick = {
                    showImageSourceDialog = false
                    cameraLauncher.launch()
                }) {
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Gallery")
                }
            }
        )
    }

    if (showSketchDialog) {
        SketchDialog(
            onDismiss = { showSketchDialog = false },
            onSave = { fileUri ->
                showSketchDialog = false
                bodyValue = insertImageTag(bodyValue, fileUri, true)
                viewModel.onBodyChange(bodyValue.text)
            }
        )
    }

    if (showHighlightPickerDialog) {
        TextHighlightDialog(
            onDismiss = { showHighlightPickerDialog = false },
            onConfirm = { colorValue ->
                if (colorValue == null) {
                    val start = bodyValue.selection.min
                    val end = bodyValue.selection.max
                    val newText = clearHighlightInSelection(bodyValue.text, start, end)
                    bodyValue = TextFieldValue(newText, TextRange(start, start))
                    viewModel.onBodyChange(newText)
                } else {
                    val hexStr = String.format(java.util.Locale.ROOT, "#%06X", colorValue and 0xFFFFFF)
                    bodyValue = highlightText(bodyValue, hexStr)
                    viewModel.onBodyChange(bodyValue.text)
                }
                showHighlightPickerDialog = false
            }
        )
    }
}

@Composable
fun MediaToolbar(
    onDeselect: () -> Unit,
    onAlignLeft: () -> Unit,
    onAlignCenter: () -> Unit,
    onAlignRight: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        tonalElevation = 2.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDeselect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Deselect Media",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            IconButton(onClick = onAlignLeft) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FormatAlignLeft,
                    contentDescription = "Align Left",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAlignCenter) {
                Icon(
                    imageVector = Icons.Default.FormatAlignCenter,
                    contentDescription = "Align Center",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAlignRight) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FormatAlignRight,
                    contentDescription = "Align Right",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            VerticalDivider(modifier = Modifier.height(24.dp))

            IconButton(onClick = onBringToFront) {
                Icon(
                    imageVector = Icons.Default.FlipToFront,
                    contentDescription = "Bring to Front",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onSendToBack) {
                Icon(
                    imageVector = Icons.Default.FlipToBack,
                    contentDescription = "Send to Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EditorToolbar(
    onUploadImage: () -> Unit,
    onSketch: () -> Unit,
    onHighlight: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        tonalElevation = 2.dp,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onUploadImage) {
                Icon(Icons.Default.Image, contentDescription = "Add Image", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onSketch) {
                Icon(Icons.Default.Gesture, contentDescription = "Sketch", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onHighlight) {
                Icon(Icons.Default.BorderColor, contentDescription = "Highlight", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onIncreaseFontSize) {
                Text("A+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDecreaseFontSize) {
                Text("A-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onBold) {
                Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onItalic) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun rememberLocalImagePainter(filePath: String): ImageBitmap? {
    return remember(filePath) {
        try {
            val cleanPath = filePath.removePrefix("file://")
            val file = java.io.File(cleanPath)
            if (file.exists()) {
                val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }
}


data class ColoredPath(
    val points: List<Offset>,
    val color: Color
)

@Composable
fun SketchDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val paths = remember { mutableStateListOf<ColoredPath>() }
    val currentPath = remember { mutableStateOf<List<Offset>>(emptyList()) }
    var currentDrawColor by remember { mutableStateOf(Color.Black) }
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sketch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Color selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val drawingColors = listOf(
                        Color.Black,
                        Color.White,
                        Color.Red,
                        Color(0xFF3B82F6),
                        Color(0xFF10B981),
                        Color(0xFFF59E0B),
                        Color(0xFF8B5CF6)
                    )
                    drawingColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (currentDrawColor == color) 2.dp else 0.5.dp,
                                    color = if (currentDrawColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { currentDrawColor = color }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .onGloballyPositioned { canvasSize = it.size }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath.value = listOf(offset)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    currentPath.value = currentPath.value + change.position
                                },
                                onDragEnd = {
                                    if (currentPath.value.isNotEmpty()) {
                                        paths.add(ColoredPath(currentPath.value, currentDrawColor))
                                        currentPath.value = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().clipToBounds()) {
                        // Draw checkered background pattern
                        val cellSize = 15.dp.toPx()
                        val cols = (size.width / cellSize).toInt() + 1
                        val rows = (size.height / cellSize).toInt() + 1
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val cellColor = if ((r + c) % 2 == 0) Color.White else Color(0xFFE0E0E0)
                                drawRect(
                                    color = cellColor,
                                    topLeft = Offset(c * cellSize, r * cellSize),
                                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                )
                            }
                        }

                        paths.forEach { path ->
                            for (i in 0 until path.points.size - 1) {
                                drawLine(
                                    color = path.color,
                                    start = path.points[i],
                                    end = path.points[i + 1],
                                    strokeWidth = 5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        val current = currentPath.value
                        if (current.isNotEmpty()) {
                            for (i in 0 until current.size - 1) {
                                drawLine(
                                    color = currentDrawColor,
                                    start = current[i],
                                    end = current[i + 1],
                                    strokeWidth = 5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPath.value.isNotEmpty()) {
                        paths.add(ColoredPath(currentPath.value, currentDrawColor))
                        currentPath.value = emptyList()
                    }
                    val w = if (canvasSize.width > 0) canvasSize.width else 600
                    val h = if (canvasSize.height > 0) canvasSize.height else 600
                    val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
                    
                    paths.forEach { path ->
                        val androidPath = android.graphics.Path()
                        if (path.points.isNotEmpty()) {
                            androidPath.moveTo(path.points[0].x, path.points[0].y)
                            for (i in 1 until path.points.size) {
                                androidPath.lineTo(path.points[i].x, path.points[i].y)
                            }
                            
                            val paint = android.graphics.Paint().apply {
                                color = path.color.toArgb()
                                strokeWidth = 10f
                                style = android.graphics.Paint.Style.STROKE
                                strokeCap = android.graphics.Paint.Cap.ROUND
                                strokeJoin = android.graphics.Paint.Join.ROUND
                                isAntiAlias = true
                            }
                            canvas.drawPath(androidPath, paint)
                        }
                    }
                    
                    val file = java.io.File(context.filesDir, "sketch_${System.currentTimeMillis()}.png")
                    java.io.FileOutputStream(file).use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                    
                    onSave("file://" + file.absolutePath)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    paths.clear()
                }) {
                    Text("Clear")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { showCustomDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Custom Mood",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { if (it.length <= 2) emoji = it },
                    label = { Text("Emoji") },
                    placeholder = { Text("\uD83D\uDCDD") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
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
    0xFFEF4444, // Red
    0xFFF97316, // Orange
    0xFFEAB308, // Yellow
    0xFF22C55E, // Green
    0xFF3B82F6, // Blue
    0xFF6366F1, // Indigo
    0xFFA855F7, // Violet
    0xFFEC4899  // Pink
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
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HighlightPresets.forEach { colorValue ->
                HighlightSwatch(
                    color = colorValue,
                    selected = selectedColor == colorValue,
                    size = 34.dp,
                    onClick = {
                        if (selectedColor == colorValue) {
                            onColorSelected(0L)
                        } else {
                            onColorSelected(colorValue)
                        }
                    }
                )
            }

            val isCustomSelected = selectedColor != 0L && !HighlightPresets.contains(selectedColor)

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCustomSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        width = if (isCustomSelected) 2.5.dp else 1.dp,
                        color = if (isCustomSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { showCustomPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Custom color",
                    modifier = Modifier.size(18.dp),
                    tint = if (isCustomSelected) MaterialTheme.colorScheme.onPrimary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
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
    size: androidx.compose.ui.unit.Dp = 24.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(color))
            .border(
                width = if (selected) 2.5.dp else 1.dp,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = hexValue,
                    onValueChange = { value ->
                        hexValue = value.toHexInput()
                        hexValue.parseHexColor()?.let { color ->
                            previewColor = color
                        }
                    },
                    label = { Text("Hex Color") },
                    placeholder = { Text("#0066CC") },
                    singleLine = true,
                    isError = hexValue.isNotBlank() && hexValue.parseHexColor() == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp),
                            shape = CircleShape,
                            color = Color(previewColor),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PickerGrayColumn.forEach { color ->
                            PaletteCell(
                                color = color,
                                selected = previewColor == color,
                                size = 20.dp,
                                onClick = {
                                    previewColor = color
                                    hexValue = color.toHexColor()
                                }
                            )
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .height(140.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PickerRows.forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowColors.forEach { color ->
                                    PaletteCell(
                                        color = color,
                                        selected = previewColor == color,
                                        size = 20.dp,
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
private fun TextHighlightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    var previewColor by remember { mutableStateOf(0xFFFFFF00L) }
    var hexValue by remember { mutableStateOf("#FFFF00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Highlight Text") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = hexValue,
                    onValueChange = { value ->
                        hexValue = value.toHexInput()
                        hexValue.parseHexColor()?.let { color ->
                            previewColor = color
                        }
                    },
                    label = { Text("Hex Color") },
                    placeholder = { Text("#FFFF00") },
                    singleLine = true,
                    isError = hexValue.isNotBlank() && hexValue.parseHexColor() == null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Surface(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp),
                            shape = CircleShape,
                            color = Color(previewColor),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PickerGrayColumn.forEach { color ->
                            PaletteCell(
                                color = color,
                                selected = previewColor == color,
                                size = 20.dp,
                                onClick = {
                                    previewColor = color
                                    hexValue = color.toHexColor()
                                }
                            )
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .height(140.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PickerRows.forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowColors.forEach { color ->
                                    PaletteCell(
                                        color = color,
                                        selected = previewColor == color,
                                        size = 20.dp,
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onConfirm(null) }) {
                    Text("Clear")
                }
                Button(onClick = { onConfirm(previewColor) }) {
                    Text("Apply")
                }
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
    size: androidx.compose.ui.unit.Dp = 24.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(color))
            .border(
                width = if (selected) 2.2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
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

@Composable
fun EditHandle(
    imageVector: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .size(28.dp)
            .background(backgroundColor, CircleShape)
            .border(
                1.dp,
                if (isDark) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (isDark) Color.Black else Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun BodyEditorWithInlineImages(
    bodyValue: TextFieldValue,
    onBodyValueChange: (TextFieldValue) -> Unit,
    isMilestoneMode: Boolean,
    onFocusChanged: (FocusState) -> Unit,
    imagePlacements: List<ImagePlacement>,
    onPlacementsCalculated: (List<ImagePlacement>) -> Unit,
    selectedPlacementRawUri: String?,
    onSelectedPlacementRawUriChange: (String?) -> Unit,
    activeMediaCommand: Pair<String, String>?,
    onMediaCommandProcessed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current
    var isFocused by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        if (bodyValue.text.isEmpty()) {
            Text(
                text = if (isMilestoneMode) "Describe this milestone..." else "Write your thoughts...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.fillMaxWidth()
            )
        }
        BasicTextField(
            value = bodyValue,
            onValueChange = onBodyValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isFocused = it.isFocused
                    onFocusChanged(it)
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            visualTransformation = HTMLVisualTransformation(onPlacementsCalculated),
            onTextLayout = { textLayoutResult = it },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )

        // Draw overlay images sorted by zIndex so higher values draw on top!
        textLayoutResult?.let { layout ->
            val sortedPlacements = imagePlacements.sortedBy { it.zIndex }
            sortedPlacements.forEach { placement ->
                val start = placement.visualStart
                val end = placement.visualEnd
                if (start < layout.layoutInput.text.length && end <= layout.layoutInput.text.length) {
                    val rect = try {
                        val startTop = layout.getCursorRect(start).top
                        val endBottom = layout.getCursorRect(end).bottom
                        val widthPx = layout.size.width
                        androidx.compose.ui.geometry.Rect(0f, startTop, widthPx.toFloat(), endBottom)
                    } catch (e: Exception) {
                        null
                    }

                    rect?.let { r ->
                        val topDp = with(density) { r.top.toDp() }
                        val heightDp = with(density) { r.height.toDp() }

                        val tagType = if (placement.isSketch) "sketch" else "image"
                        var localOffsetX by remember(placement.rawUri) { mutableStateOf(placement.offsetX) }
                        var localOffsetY by remember(placement.rawUri) { mutableStateOf(placement.offsetY) }
                        var localScale by remember(placement.rawUri) { mutableStateOf(placement.scale) }
                        var localRotation by remember(placement.rawUri) { mutableStateOf(placement.rotation) }
                        var localFlipped by remember(placement.rawUri) { mutableStateOf(placement.flipped) }
                        var localZIndex by remember(placement.rawUri) { mutableStateOf(placement.zIndex) }

                        val bitmap = rememberLocalImagePainter(placement.cleanUri)
                        val aspectRatio = if (bitmap != null) bitmap.width.toFloat() / bitmap.height.toFloat() else 1f

                        val maxWidthDp = with(density) { r.width.toDp() }

                        var baseWidthDp: androidx.compose.ui.unit.Dp
                        var baseHeightDp: androidx.compose.ui.unit.Dp

                        if (placement.isSketch) {
                            baseWidthDp = 180.dp
                            baseHeightDp = 180.dp
                        } else {
                            if (bitmap != null) {
                                val naturalWidthDp = with(density) { bitmap.width.toDp() }
                                val naturalHeightDp = with(density) { bitmap.height.toDp() }
                                
                                val maxPortraitHeightDp = 280.dp
                                if (naturalWidthDp >= naturalHeightDp) {
                                    // Landscape/Square uses natural size
                                    baseWidthDp = naturalWidthDp
                                    baseHeightDp = naturalHeightDp
                                } else {
                                    // Portrait capped to 280.dp height
                                    if (naturalHeightDp > maxPortraitHeightDp) {
                                        baseHeightDp = maxPortraitHeightDp
                                        baseWidthDp = maxPortraitHeightDp * aspectRatio
                                    } else {
                                        baseWidthDp = naturalWidthDp
                                        baseHeightDp = naturalHeightDp
                                    }
                                }

                                if (maxWidthDp > 0.dp && baseWidthDp > maxWidthDp) {
                                    baseWidthDp = maxWidthDp
                                    baseHeightDp = maxWidthDp / aspectRatio
                                }
                            } else {
                                baseWidthDp = 180.dp
                                baseHeightDp = 180.dp
                            }
                        }

                        val persistMediaParams = { newZ: Float? ->
                            val cleanUri = placement.cleanUri
                            val zVal = newZ ?: localZIndex
                            val wVal = baseWidthDp.value
                            val hVal = baseHeightDp.value
                            val newRawUri = "$cleanUri?x=$localOffsetX&y=$localOffsetY&scale=$localScale&rotation=$localRotation&flipped=$localFlipped&z=$zVal&w=$wVal&h=$hVal"
                            val oldTag = "![$tagType](${placement.rawUri})"
                            val newTag = "![$tagType]($newRawUri)"
                            val currentText = bodyValue.text
                            if (currentText.contains(oldTag)) {
                                val newText = currentText.replace(oldTag, newTag)
                                onBodyValueChange(bodyValue.copy(text = newText))
                            }
                        }

                        LaunchedEffect(bitmap, baseWidthDp, baseHeightDp) {
                            if (bitmap != null) {
                                val wVal = baseWidthDp.value
                                val hVal = baseHeightDp.value
                                if (kotlin.math.abs(placement.width - wVal) > 0.5f || kotlin.math.abs(placement.height - hVal) > 0.5f) {
                                    val cleanUri = placement.cleanUri
                                    val zVal = localZIndex
                                    val newRawUri = "$cleanUri?x=$localOffsetX&y=$localOffsetY&scale=$localScale&rotation=$localRotation&flipped=$localFlipped&z=$zVal&w=$wVal&h=$hVal"
                                    val oldTag = "![$tagType](${placement.rawUri})"
                                    val newTag = "![$tagType]($newRawUri)"
                                    val currentText = bodyValue.text
                                    if (currentText.contains(oldTag)) {
                                        val newText = currentText.replace(oldTag, newTag)
                                        onBodyValueChange(bodyValue.copy(text = newText))
                                    }
                                }
                            }
                        }

                        val currentWidthDp = baseWidthDp * localScale
                        val currentHeightDp = baseHeightDp * localScale

                        val xPos = (maxWidthDp - currentWidthDp) / 2 + localOffsetX.dp
                        val yPos = topDp + (heightDp - currentHeightDp) / 2 + localOffsetY.dp

                        val isMediaFocused = selectedPlacementRawUri == placement.rawUri

                        // Process alignment/layering commands if targeted at this placement
                        LaunchedEffect(activeMediaCommand) {
                            if (activeMediaCommand != null && activeMediaCommand.second == placement.rawUri) {
                                val cmd = activeMediaCommand.first
                                when (cmd) {
                                    "align_left" -> {
                                        localOffsetX = -((maxWidthDp - currentWidthDp) / 2f).value
                                        persistMediaParams(null)
                                    }
                                    "align_center" -> {
                                        localOffsetX = 0f
                                        persistMediaParams(null)
                                    }
                                    "align_right" -> {
                                        localOffsetX = ((maxWidthDp - currentWidthDp) / 2f).value
                                        persistMediaParams(null)
                                    }
                                    "layer_forward" -> {
                                        val maxZ = imagePlacements.map { it.zIndex }.maxOrNull() ?: 0f
                                        val newZ = maxZ + 1f
                                        localZIndex = newZ
                                        persistMediaParams(newZ)
                                    }
                                    "layer_backward" -> {
                                        val minZ = imagePlacements.map { it.zIndex }.minOrNull() ?: 0f
                                        val newZ = minZ - 1f
                                        localZIndex = newZ
                                        persistMediaParams(newZ)
                                    }
                                }
                                onMediaCommandProcessed()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .absoluteOffset(x = xPos, y = yPos)
                                .width(currentWidthDp)
                                .height(currentHeightDp)
                                .pointerInput(placement.rawUri) {
                                    detectTapGestures(
                                        onTap = {
                                            onSelectedPlacementRawUriChange(placement.rawUri)
                                        }
                                    )
                                }
                                .pointerInput(placement.rawUri) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            onSelectedPlacementRawUriChange(placement.rawUri)
                                            localOffsetX += dragAmount.x / density.density
                                            localOffsetY += dragAmount.y / density.density
                                        },
                                        onDragEnd = {
                                            persistMediaParams(null)
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // 1. Rotated & Flipped Content Box (Image & Border)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        rotationZ = localRotation,
                                        scaleX = if (localFlipped) -1f else 1f
                                    )
                                    .then(
                                        if (isMediaFocused) Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            RoundedCornerShape(4.dp)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = null,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (placement.isSketch) Icons.Default.Gesture else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // 2. Interactive Controls Overlay (aligned to rotated corners manually)
                            if (isMediaFocused) {
                                val halfWidthDp = currentWidthDp / 2f
                                val halfHeightDp = currentHeightDp / 2f
                                val rad = Math.toRadians(localRotation.toDouble())
                                val cos = Math.cos(rad).toFloat()
                                val sin = Math.sin(rad).toFloat()

                                // Rotate Corner points:
                                // Top-Left: (-halfWidthDp, -halfHeightDp)
                                val tlOffsetX = (-halfWidthDp) * cos - (-halfHeightDp) * sin
                                val tlOffsetY = (-halfWidthDp) * sin + (-halfHeightDp) * cos

                                // Top-Right: (halfWidthDp, -halfHeightDp)
                                val trOffsetX = halfWidthDp * cos - (-halfHeightDp) * sin
                                val trOffsetY = halfWidthDp * sin + (-halfHeightDp) * cos

                                // Bottom-Right: (halfWidthDp, halfHeightDp)
                                val brOffsetX = halfWidthDp * cos - halfHeightDp * sin
                                val brOffsetY = halfWidthDp * sin + halfHeightDp * cos

                                // Bottom-Left: (-halfWidthDp, halfHeightDp)
                                val blOffsetX = (-halfWidthDp) * cos - halfHeightDp * sin
                                val blOffsetY = (-halfWidthDp) * sin + halfHeightDp * cos

                                // Top-Left: Flip button
                                IconButton(
                                    onClick = {
                                        val nextFlipped = !localFlipped
                                        localFlipped = nextFlipped
                                        persistMediaParams(null)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .absoluteOffset(x = tlOffsetX, y = tlOffsetY)
                                        .size(28.dp)
                                ) {
                                    EditHandle(
                                        imageVector = Icons.Default.Flip,
                                        contentDescription = "Flip Media",
                                        backgroundColor = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Top-Right: Delete button
                                IconButton(
                                    onClick = {
                                        val oldTag = "![$tagType](${placement.rawUri})"
                                        val currentText = bodyValue.text
                                        if (currentText.contains(oldTag)) {
                                            val newText = currentText.replace(oldTag, "")
                                            onBodyValueChange(bodyValue.copy(text = newText))
                                            onSelectedPlacementRawUriChange(null)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .absoluteOffset(x = trOffsetX, y = trOffsetY)
                                        .size(28.dp)
                                ) {
                                    EditHandle(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Media",
                                        backgroundColor = Color(0xFFE53935)
                                    )
                                }

                                // Bottom-Right: Resize (Scale) Handle
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .absoluteOffset(x = brOffsetX, y = brOffsetY)
                                        .size(28.dp)
                                        .pointerInput(placement.rawUri) {
                                            detectDragGestures(
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val angleRad = Math.toRadians((localRotation + 45f).toDouble())
                                                    val outwardX = Math.cos(angleRad).toFloat()
                                                    val outwardY = Math.sin(angleRad).toFloat()
                                                    val projection = (dragAmount.x * outwardX + dragAmount.y * outwardY) / density.density
                                                    val deltaScale = projection / 90f
                                                    localScale = (localScale + deltaScale).coerceIn(0.5f, 3.0f)
                                                },
                                                onDragEnd = {
                                                    persistMediaParams(null)
                                                }
                                            )
                                        }
                                ) {
                                    EditHandle(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Resize Media",
                                        backgroundColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.rotate(90f)
                                    )
                                }

                                // Bottom-Left: Rotate Handle
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .absoluteOffset(x = blOffsetX, y = blOffsetY)
                                        .size(28.dp)
                                        .pointerInput(placement.rawUri) {
                                            detectDragGestures(
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val halfWidthPx = halfWidthDp.toPx()
                                                    val halfHeightPx = halfHeightDp.toPx()
                                                    val blPxX = -halfWidthPx * cos - halfHeightPx * sin
                                                    val blPxY = -halfWidthPx * sin + halfHeightPx * cos
                                                    val nextPxX = blPxX + dragAmount.x
                                                    val nextPxY = blPxY + dragAmount.y
                                                    val prevAngle = Math.toDegrees(Math.atan2(blPxY.toDouble(), blPxX.toDouble())).toFloat()
                                                    val nextAngle = Math.toDegrees(Math.atan2(nextPxY.toDouble(), nextPxX.toDouble())).toFloat()
                                                    val diff = nextAngle - prevAngle
                                                    localRotation = (localRotation + diff + 360f) % 360f
                                                },
                                                onDragEnd = {
                                                    persistMediaParams(null)
                                                }
                                            )
                                        }
                                ) {
                                    EditHandle(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Rotate Media",
                                        backgroundColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
