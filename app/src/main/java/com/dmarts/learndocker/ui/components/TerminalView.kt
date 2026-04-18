package com.dmarts.learndocker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.domain.model.TerminalLine
import com.dmarts.learndocker.domain.model.TerminalLineType
import com.dmarts.learndocker.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TerminalView(
    lines: List<TerminalLine>,
    input: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<String> = emptyList(),
    onSuggestionSelected: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    var copiedToast by remember { mutableStateOf(false) }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }
    LaunchedEffect(copiedToast) {
        if (copiedToast) { delay(1500); copiedToast = false }
    }

    Column(modifier = modifier.background(NeoTerminalBg)) {

        // ─── macOS-style header bar ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF13162A))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(Color(0xFFFF5F57), Color(0xFFFFBD2E), Color(0xFF28C840)).forEach { c ->
                    Box(Modifier.size(9.dp).background(c, RoundedCornerShape(50)))
                }
            }
            Text(
                "  docker terminal",
                color = Color(0xFF5A6380),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            if (copiedToast) {
                Text("Copied!", color = NeoTerminalSuccess, fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace)
                Spacer(Modifier.width(8.dp))
            }
            IconButton(
                onClick = {
                    clipboard.setText(AnnotatedString(lines.joinToString("\n") { it.text }))
                    copiedToast = true
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ContentCopy, "Copy all",
                    tint = Color(0xFF5A6380), modifier = Modifier.size(15.dp))
            }
        }

        // ─── Output ────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(lines) { line -> TerminalLineItem(line, clipboard) }
        }

        // ─── Autocomplete chips ────────────────────────────────────────────
        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF13162A))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.forEach { s ->
                    SuggestionChip(
                        onClick = { onSuggestionSelected(s) },
                        label = {
                            Text(s, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                                color = NeoTerminalInput)
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF1E2540)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color(0xFF2D3558),
                            borderWidth = 1.dp
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFF232845), thickness = 1.dp)

        // ─── Input row ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", color = NeoTerminalSuccess, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold, fontSize = 15.sp)
            BasicTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = NeoTerminalInput,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(NeoTerminalInput),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (input.isEmpty()) {
                            Text("type a docker command...", color = Color(0xFF3D4566),
                                fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                        }
                        inner()
                    }
                }
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSubmit,
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF1E3A60)
                )
            ) {
                Icon(Icons.Default.Send, "Run",
                    tint = NeoTerminalInput, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TerminalLineItem(line: TerminalLine, clipboard: ClipboardManager) {
    if (line.text.isEmpty()) { Spacer(Modifier.height(4.dp)); return }

    when (line.type) {
        TerminalLineType.INPUT -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onLongClick = {
                    clipboard.setText(AnnotatedString(line.text))
                }) {}
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", color = NeoTerminalSuccess, fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = syntaxHighlight(line.text), fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, lineHeight = 20.sp)
        }

        TerminalLineType.EXPLAIN -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 6.dp)
                .background(Color(0xFF171C34), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("i ", color = NeoTerminalExplain, fontFamily = FontFamily.Monospace,
                fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(line.text, color = Color(0xFF9BAED0), fontFamily = FontFamily.Monospace,
                fontSize = 13.sp, lineHeight = 20.sp, fontStyle = FontStyle.Italic)
        }

        TerminalLineType.ERROR -> Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
            Text("! ", color = NeoTerminalError, fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(line.text, color = NeoTerminalError, fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, lineHeight = 20.sp)
        }

        TerminalLineType.SYSTEM -> Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
        ) {
            Text("# ", color = NeoTerminalSystem, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            Text(line.text, color = NeoTerminalSystem, fontFamily = FontFamily.Monospace,
                fontSize = 13.sp, lineHeight = 19.sp)
        }

        TerminalLineType.SUCCESS -> Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        ) {
            Text("✓ ", color = NeoTerminalSuccess, fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(line.text, color = NeoTerminalSuccess, fontFamily = FontFamily.Monospace,
                fontSize = 14.sp, lineHeight = 20.sp)
        }

        TerminalLineType.TABLE_HEADER -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .height(1.dp)
                    .width(8.dp)
                    .background(Color(0xFF38BDF8))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                line.text,
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(Color(0xFF1E3A5F))
            )
        }

        TerminalLineType.ACTION_OK -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 3.dp, bottom = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "✓ ",
                color = Color(0xFF4ADE80),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                line.text,
                color = Color(0xFFE2E8F0),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )
        }

        TerminalLineType.ID_LINE -> Text(
            text = line.text,
            color = Color(0xFF475569),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 0.dp, bottom = 2.dp)
        )

        TerminalLineType.PULL_LINE -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 1.dp, bottom = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "↓ ",
                color = Color(0xFF22D3EE),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
            Text(
                line.text,
                color = Color(0xFF7DD3FC),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }

        TerminalLineType.OUTPUT -> Text(
            text = line.text,
            color = NeoTerminalText,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp, lineHeight = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 1.dp, bottom = 1.dp)
                .combinedClickable(onLongClick = {
                    clipboard.setText(AnnotatedString(line.text))
                }) {}
        )
    }
}

private fun syntaxHighlight(text: String): AnnotatedString = buildAnnotatedString {
    val tokens = text.trim().split("\\s+".toRegex())
    tokens.forEachIndexed { i, token ->
        val style = when {
            i == 0 && (token == "docker" || token == "docker-compose") ->
                SpanStyle(color = SyntaxCommand, fontWeight = FontWeight.Bold)
            i == 1 && !token.startsWith("-") ->
                SpanStyle(color = SyntaxSubcmd, fontWeight = FontWeight.SemiBold)
            token.startsWith("--") || (token.startsWith("-") && token.length == 2) ->
                SpanStyle(color = SyntaxFlag)
            token.matches(Regex("\\d+:\\d+(/\\w+)?")) ->
                SpanStyle(color = SyntaxPort)
            i > 1 && !token.startsWith("-") && (token.contains(":") || token.contains("/")) ->
                SpanStyle(color = SyntaxImage)
            else -> SpanStyle(color = SyntaxValue)
        }
        withStyle(style) { append(token) }
        if (i < tokens.size - 1) append(" ")
    }
}
