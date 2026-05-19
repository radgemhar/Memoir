package com.example.memoir.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import java.util.Locale

fun parseHexColor(colorStr: String): Color {
    return try {
        val hex = colorStr.removePrefix("#")
        val colorLong = hex.toLong(16)
        if (hex.length == 6) {
            Color(colorLong or 0xFF000000)
        } else {
            Color(colorLong)
        }
    } catch (e: Exception) {
        Color.Transparent
    }
}

fun parseRichText(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var index = 0
    val boldStack = mutableListOf<Int>()
    val italicStack = mutableListOf<Int>()
    val highlightStack = mutableListOf<Pair<Int, Color>>()
    val fontSizeStack = mutableListOf<Pair<Int, Int>>()
    
    val cleanText = StringBuilder()
    
    while (index < text.length) {
        if (text.startsWith("<b>", index)) {
            boldStack.add(cleanText.length)
            index += 3
        } else if (text.startsWith("</b>", index)) {
            if (boldStack.isNotEmpty()) {
                val start = boldStack.removeAt(boldStack.size - 1)
                builder.addStyle(SpanStyle(fontWeight = FontWeight.Black), start, cleanText.length)
            }
            index += 4
        } else if (text.startsWith("<i>", index)) {
            italicStack.add(cleanText.length)
            index += 3
        } else if (text.startsWith("</i>", index)) {
            if (italicStack.isNotEmpty()) {
                val start = italicStack.removeAt(italicStack.size - 1)
                builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, cleanText.length)
            }
            index += 4
        } else if (text.startsWith("<mark color=\"", index)) {
            val endQuote = text.indexOf("\"", index + 13)
            if (endQuote != -1 && text.startsWith("\">", endQuote)) {
                val colorStr = text.substring(index + 13, endQuote)
                val color = parseHexColor(colorStr)
                highlightStack.add(cleanText.length to color)
                index = endQuote + 2
            } else {
                cleanText.append(text[index])
                index++
            }
        } else if (text.startsWith("</mark>", index)) {
            if (highlightStack.isNotEmpty()) {
                val (start, color) = highlightStack.removeAt(highlightStack.size - 1)
                builder.addStyle(SpanStyle(background = color), start, cleanText.length)
            }
            index += 7
        } else if (text.startsWith("<font size=\"", index)) {
            val endQuote = text.indexOf("\"", index + 12)
            if (endQuote != -1 && text.startsWith("\">", endQuote)) {
                val sizeStr = text.substring(index + 12, endQuote)
                val size = sizeStr.toIntOrNull() ?: 16
                fontSizeStack.add(cleanText.length to size)
                index = endQuote + 2
            } else {
                cleanText.append(text[index])
                index++
            }
        } else if (text.startsWith("</font>", index)) {
            if (fontSizeStack.isNotEmpty()) {
                val (start, size) = fontSizeStack.removeAt(fontSizeStack.size - 1)
                builder.addStyle(SpanStyle(fontSize = size.sp), start, cleanText.length)
            }
            index += 7
        } else {
            cleanText.append(text[index])
            index++
        }
    }
    
    builder.append(cleanText.toString())
    
    // Close remaining open stacks
    val length = cleanText.length
    boldStack.forEach { start -> builder.addStyle(SpanStyle(fontWeight = FontWeight.Black), start, length) }
    italicStack.forEach { start -> builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length) }
    highlightStack.forEach { (start, color) -> builder.addStyle(SpanStyle(background = color), start, length) }
    fontSizeStack.forEach { (start, size) -> builder.addStyle(SpanStyle(fontSize = size.sp), start, length) }
    
    return builder.toAnnotatedString()
}

fun stripImageTags(text: String): String {
    return text.replace(Regex("""!\[(?:image|sketch)\]\((.*?)\)"""), "")
}

fun applyTag(value: TextFieldValue, openTag: String, closeTag: String): TextFieldValue {
    val text = value.text
    val selection = value.selection
    val start = selection.min
    val end = selection.max
    
    val selectedText = text.substring(start, end)
    val newText = text.substring(0, start) + openTag + selectedText + closeTag + text.substring(end)
    val newSelectionStart = start + openTag.length
    val newSelectionEnd = newSelectionStart + selectedText.length
    
    return TextFieldValue(
        text = newText,
        selection = TextRange(newSelectionStart, newSelectionEnd)
    )
}

fun toggleTag(value: TextFieldValue, openTag: String, closeTag: String): TextFieldValue {
    val text = value.text
    val selection = value.selection
    val start = selection.min
    val end = selection.max
    
    if (start >= openTag.length && end <= text.length - closeTag.length) {
        val checkOpen = text.substring(start - openTag.length, start)
        val checkClose = text.substring(end, end + closeTag.length)
        if (checkOpen == openTag && checkClose == closeTag) {
            val newText = text.substring(0, start - openTag.length) +
                    text.substring(start, end) +
                    text.substring(end + closeTag.length)
            return TextFieldValue(
                text = newText,
                selection = TextRange(start - openTag.length, end - openTag.length)
            )
        }
    }
    
    val selectedText = text.substring(start, end)
    if (selectedText.startsWith(openTag) && selectedText.endsWith(closeTag)) {
        val stripped = selectedText.substring(openTag.length, selectedText.length - closeTag.length)
        val newText = text.substring(0, start) + stripped + text.substring(end)
        return TextFieldValue(
            text = newText,
            selection = TextRange(start, start + stripped.length)
        )
    }
    
    return applyTag(value, openTag, closeTag)
}

fun changeFontSize(value: TextFieldValue, delta: Int): TextFieldValue {
    val text = value.text
    val selection = value.selection
    val start = selection.min
    val end = selection.max
    val selectedText = text.substring(start, end)
    
    val fontRegex = Regex("""^<font size="(\d+)">([\s\S]*?)</font>$""")
    val match = fontRegex.matchEntire(selectedText)
    if (match != null) {
        val currentSize = match.groupValues[1].toIntOrNull() ?: 16
        val newSize = (currentSize + delta).coerceIn(10, 48)
        val innerText = match.groupValues[2]
        val newSelected = """<font size="$newSize">$innerText</font>"""
        val newText = text.substring(0, start) + newSelected + text.substring(end)
        return TextFieldValue(
            text = newText,
            selection = TextRange(start, start + newSelected.length)
        )
    } else {
        val newSize = if (delta > 0) 20 else 12
        val newSelected = """<font size="$newSize">$selectedText</font>"""
        val newText = text.substring(0, start) + newSelected + text.substring(end)
        return TextFieldValue(
            text = newText,
            selection = TextRange(start, start + newSelected.length)
        )
    }
}

fun insertImageTag(value: TextFieldValue, uri: String, isSketch: Boolean): TextFieldValue {
    val tagType = if (isSketch) "sketch" else "image"
    val insertText = "\n![$tagType]($uri)\n"
    val text = value.text
    val selection = value.selection
    val start = selection.min
    
    val newText = text.substring(0, start) + insertText + text.substring(start)
    val newSelection = start + insertText.length
    
    return TextFieldValue(
        text = newText,
        selection = TextRange(newSelection)
    )
}

fun highlightText(value: TextFieldValue, colorHex: String): TextFieldValue {
    return toggleTag(value, "<mark color=\"$colorHex\">", "</mark>")
}

fun parseQueryParam(uri: String, key: String): String? {
    if (!uri.contains("?")) return null
    val queryString = uri.substringAfter("?")
    val pairs = queryString.split("&")
    for (pair in pairs) {
        val parts = pair.split("=")
        if (parts.size == 2 && parts[0] == key) {
            return parts[1]
        }
    }
    return null
}

data class ImagePlacement(
    val rawUri: String,
    val cleanUri: String,
    val isSketch: Boolean,
    val visualStart: Int,
    val visualEnd: Int,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val flipped: Boolean = false,
    val zIndex: Float = 0f,
    val width: Float = 180f,
    val height: Float = 180f
)

class HTMLVisualTransformation(
    private val onPlacementsCalculated: (List<ImagePlacement>) -> Unit = {}
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val rawToVisual = IntArray(rawText.length + 1)
        val visualToRawList = mutableListOf<Int>()
        val visualSb = StringBuilder()

        val boldSpans = mutableListOf<Pair<Int, Int>>()
        val italicSpans = mutableListOf<Pair<Int, Int>>()
        val highlightSpans = mutableListOf<Triple<Int, Int, Color>>()
        val fontSizeSpans = mutableListOf<Triple<Int, Int, Int>>()

        val boldStack = mutableListOf<Int>()
        val italicStack = mutableListOf<Int>()
        val highlightStack = mutableListOf<Pair<Int, Color>>()
        val fontSizeStack = mutableListOf<Pair<Int, Int>>()

        val imagePlacements = mutableListOf<ImagePlacement>()

        var rawIdx = 0
        while (rawIdx < rawText.length) {
            if (rawText.startsWith("<b>", rawIdx)) {
                boldStack.add(visualSb.length)
                for (i in 0 until 3) {
                    rawToVisual[rawIdx + i] = visualSb.length
                }
                rawIdx += 3
            } else if (rawText.startsWith("</b>", rawIdx)) {
                if (boldStack.isNotEmpty()) {
                    val start = boldStack.removeAt(boldStack.size - 1)
                    boldSpans.add(start to visualSb.length)
                }
                for (i in 0 until 4) {
                    rawToVisual[rawIdx + i] = visualSb.length
                }
                rawIdx += 4
            } else if (rawText.startsWith("<i>", rawIdx)) {
                italicStack.add(visualSb.length)
                for (i in 0 until 3) {
                    rawToVisual[rawIdx + i] = visualSb.length
                }
                rawIdx += 3
            } else if (rawText.startsWith("</i>", rawIdx)) {
                if (italicStack.isNotEmpty()) {
                    val start = italicStack.removeAt(italicStack.size - 1)
                    italicSpans.add(start to visualSb.length)
                }
                for (i in 0 until 4) {
                    rawToVisual[rawIdx + i] = visualSb.length
                }
                rawIdx += 4
            } else if (rawText.startsWith("<mark color=\"", rawIdx)) {
                val endQuote = rawText.indexOf("\"", rawIdx + 13)
                if (endQuote != -1 && rawText.startsWith("\">", endQuote)) {
                    val colorStr = rawText.substring(rawIdx + 13, endQuote)
                    val color = parseHexColor(colorStr)
                    highlightStack.add(visualSb.length to color)
                    val tagLen = (endQuote + 2) - rawIdx
                    for (i in 0 until tagLen) {
                        rawToVisual[rawIdx + i] = visualSb.length
                    }
                    rawIdx = endQuote + 2
                } else {
                    rawToVisual[rawIdx] = visualSb.length
                    visualToRawList.add(rawIdx)
                    visualSb.append(rawText[rawIdx])
                    rawIdx++
                }
            } else if (rawText.startsWith("</mark>", rawIdx)) {
                if (highlightStack.isNotEmpty()) {
                    val (start, color) = highlightStack.removeAt(highlightStack.size - 1)
                    highlightSpans.add(Triple(start, visualSb.length, color))
                }
                for (i in 0 until 7) {
                    rawToVisual[rawIdx + i] = visualSb.length
                }
                rawIdx += 7
            } else if (rawText.startsWith("<font size=\"", rawIdx)) {
                val endQuote = rawText.indexOf("\"", rawIdx + 12)
                if (endQuote != -1 && rawText.startsWith("\">", endQuote)) {
                    val sizeStr = rawText.substring(rawIdx + 12, endQuote)
                    val size = sizeStr.toIntOrNull() ?: 16
                    fontSizeStack.add(visualSb.length to size)
                    val tagLen = (endQuote + 2) - rawIdx
                    for (i in 0 until tagLen) {
                        rawToVisual[rawIdx + i] = visualSb.length
                    }
                    rawIdx = endQuote + 2
                } else {
                    rawToVisual[rawIdx] = visualSb.length
                    visualToRawList.add(rawIdx)
                    visualSb.append(rawText[rawIdx])
                    rawIdx++
                }
            } else if (rawText.startsWith("</font>", rawIdx)) {
                if (fontSizeStack.isNotEmpty()) {
                    val (start, size) = fontSizeStack.removeAt(fontSizeStack.size - 1)
                    fontSizeSpans.add(Triple(start, visualSb.length, size))
                }
                for (i in 0 until 7) {
                    rawToVisual[rawIdx + i] = visualSb.length
                }
                rawIdx += 7
            } else if (rawText.startsWith("![image](", rawIdx) || rawText.startsWith("![sketch](", rawIdx)) {
                val isSketch = rawText.startsWith("![sketch](", rawIdx)
                val prefixLen = if (isSketch) 10 else 9
                val endParen = rawText.indexOf(")", rawIdx + prefixLen)
                if (endParen != -1) {
                    val uri = rawText.substring(rawIdx + prefixLen, endParen)
                    val tagLen = (endParen + 1) - rawIdx
                    val visualStart = visualSb.length
                    val w = parseQueryParam(uri, "w")?.toFloatOrNull() ?: 180f
                    val h = parseQueryParam(uri, "h")?.toFloatOrNull() ?: 180f
                    val numNewlines = (h / 20f).toInt().coerceIn(4, 25)
                    val placeholder = "\n".repeat(numNewlines)
                    for (char in placeholder) {
                        visualToRawList.add(rawIdx)
                        visualSb.append(char)
                    }
                    val visualEnd = visualSb.length
                    val rawUri = uri
                    val cleanUri = if (uri.contains("?")) uri.substringBefore("?") else uri
                    val offsetX = parseQueryParam(uri, "x")?.toFloatOrNull() ?: 0f
                    val offsetY = parseQueryParam(uri, "y")?.toFloatOrNull() ?: 0f
                    val scale = parseQueryParam(uri, "scale")?.toFloatOrNull() ?: 1f
                    val rotation = parseQueryParam(uri, "rotation")?.toFloatOrNull() ?: 0f
                    val flipped = parseQueryParam(uri, "flipped")?.toBoolean() ?: false
                    val zIndex = parseQueryParam(uri, "z")?.toFloatOrNull() ?: 0f
                    imagePlacements.add(
                        ImagePlacement(
                            rawUri = rawUri,
                            cleanUri = cleanUri,
                            isSketch = isSketch,
                            visualStart = visualStart,
                            visualEnd = visualEnd,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            scale = scale,
                            rotation = rotation,
                            flipped = flipped,
                            zIndex = zIndex,
                            width = w,
                            height = h
                        )
                    )
                    for (i in 0 until tagLen) {
                        rawToVisual[rawIdx + i] = visualStart
                    }
                    rawIdx = endParen + 1
                } else {
                    rawToVisual[rawIdx] = visualSb.length
                    visualToRawList.add(rawIdx)
                    visualSb.append(rawText[rawIdx])
                    rawIdx++
                }
            } else {
                rawToVisual[rawIdx] = visualSb.length
                visualToRawList.add(rawIdx)
                visualSb.append(rawText[rawIdx])
                rawIdx++
            }
        }
        rawToVisual[rawText.length] = visualSb.length
        visualToRawList.add(rawText.length)

        val visualLength = visualSb.length
        boldStack.forEach { start -> boldSpans.add(start to visualLength) }
        italicStack.forEach { start -> italicSpans.add(start to visualLength) }
        highlightStack.forEach { (start, color) -> highlightSpans.add(Triple(start, visualLength, color)) }
        fontSizeStack.forEach { (start, size) -> fontSizeSpans.add(Triple(start, visualLength, size)) }

        val builder = AnnotatedString.Builder()
        builder.append(visualSb.toString())

        boldSpans.forEach { (start, end) ->
            builder.addStyle(SpanStyle(fontWeight = FontWeight.Black), start, end)
        }
        italicSpans.forEach { (start, end) ->
            builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
        }
        highlightSpans.forEach { (start, end, color) ->
            builder.addStyle(SpanStyle(background = color), start, end)
        }
        fontSizeSpans.forEach { (start, end, size) ->
            builder.addStyle(SpanStyle(fontSize = size.sp), start, end)
        }

        onPlacementsCalculated(imagePlacements)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val bounded = offset.coerceIn(0, rawText.length)
                return rawToVisual[bounded]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val bounded = offset.coerceIn(0, visualToRawList.size - 1)
                return visualToRawList[bounded]
            }
        }

        return TransformedText(builder.toAnnotatedString(), offsetMapping)
    }
}

data class MarkSpan(
    val tagStart: Int,
    val contentStart: Int,
    val contentEnd: Int,
    val tagEnd: Int,
    val color: String
)

fun findMarkSpans(text: String): List<MarkSpan> {
    val spans = mutableListOf<MarkSpan>()
    var idx = 0
    val markStartTag = "<mark color=\""
    val markEndTag = "</mark>"
    
    val stack = mutableListOf<Pair<Int, String>>()
    val contentStartStack = mutableListOf<Int>()
    
    while (idx < text.length) {
        if (text.startsWith(markStartTag, idx)) {
            val endQuote = text.indexOf("\"", idx + markStartTag.length)
            if (endQuote != -1 && text.startsWith("\">", endQuote)) {
                val color = text.substring(idx + markStartTag.length, endQuote)
                stack.add(idx to color)
                contentStartStack.add(endQuote + 2)
                idx = endQuote + 2
            } else {
                idx++
            }
        } else if (text.startsWith(markEndTag, idx)) {
            if (stack.isNotEmpty()) {
                val (tagStart, color) = stack.removeAt(stack.size - 1)
                val contentStart = contentStartStack.removeAt(contentStartStack.size - 1)
                val contentEnd = idx
                val tagEnd = idx + markEndTag.length
                spans.add(MarkSpan(tagStart, contentStart, contentEnd, tagEnd, color))
            }
            idx += markEndTag.length
        } else {
            idx++
        }
    }
    return spans.sortedBy { it.tagStart }
}

fun clearHighlightInSelection(text: String, selectionStart: Int, selectionEnd: Int): String {
    val spans = findMarkSpans(text)
    if (spans.isEmpty()) return text
    
    val sb = java.lang.StringBuilder()
    var lastIdx = 0
    
    for (span in spans) {
        if (span.tagStart > lastIdx) {
            sb.append(text.substring(lastIdx, span.tagStart))
        }
        
        val spanContent = text.substring(span.contentStart, span.contentEnd)
        val overlapStart = maxOf(span.contentStart, selectionStart)
        val overlapEnd = minOf(span.contentEnd, selectionEnd)
        
        if (overlapStart < overlapEnd) {
            val leftLen = overlapStart - span.contentStart
            val rightLen = span.contentEnd - overlapEnd
            
            if (leftLen > 0) {
                val leftContent = spanContent.substring(0, leftLen)
                sb.append("<mark color=\"${span.color}\">$leftContent</mark>")
            }
            
            val middleContent = spanContent.substring(leftLen, spanContent.length - rightLen)
            sb.append(middleContent)
            
            if (rightLen > 0) {
                val rightContent = spanContent.substring(spanContent.length - rightLen)
                sb.append("<mark color=\"${span.color}\">$rightContent</mark>")
            }
        } else {
            sb.append(text.substring(span.tagStart, span.tagEnd))
        }
        
        lastIdx = span.tagEnd
    }
    
    if (lastIdx < text.length) {
        sb.append(text.substring(lastIdx))
    }
    
    return sb.toString()
}

fun getCleanCharacterCount(text: String): Int {
    val withoutImages = text.replace(Regex("!\\[(image|sketch)\\]\\([^)]*\\)"), "")
    val withoutHtml = withoutImages.replace(Regex("<[^>]*>"), "")
    return withoutHtml.length
}
