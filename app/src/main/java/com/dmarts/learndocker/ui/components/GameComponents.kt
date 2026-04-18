package com.dmarts.learndocker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.domain.model.AchievementDef
import com.dmarts.learndocker.domain.model.Objective
import com.dmarts.learndocker.domain.engine.ValidationResult
import com.dmarts.learndocker.ui.theme.*

@Composable
fun XpBar(
    currentXp: Int,
    modifier: Modifier = Modifier
) {
    val levelThresholds = listOf(0, 500, 1000, 2000, 5000, 10000)
    val currentLevel = levelThresholds.indexOfLast { currentXp >= it }.coerceAtLeast(0)
    val nextThreshold = levelThresholds.getOrElse(currentLevel + 1) { levelThresholds.last() }
    val prevThreshold = levelThresholds.getOrElse(currentLevel) { 0 }
    val progress = if (nextThreshold > prevThreshold)
        (currentXp - prevThreshold).toFloat() / (nextThreshold - prevThreshold)
    else 1f

    val animProgress by animateFloatAsState(progress.coerceIn(0f, 1f), tween(800), label = "xp")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${currentXp} XP", color = XpGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Lv.${currentLevel + 1}", color = NeoTextSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(NeoSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(XpGold.copy(alpha = 0.8f), XpGold)))
            )
        }
    }
}

@Composable
fun ObjectivePanel(
    objectives: List<Objective>,
    validationResult: ValidationResult,
    hints: List<String>,
    hintsUsed: Int,
    onRequestHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Objectives",
            color = NeoCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        objectives.forEach { obj ->
            val done = obj.id in validationResult.completedObjectiveIds
            ObjectiveItem(description = obj.description, completed = done)
        }
        Spacer(Modifier.height(12.dp))
        if (hints.isNotEmpty()) {
            OutlinedButton(
                onClick = onRequestHint,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeoAmber),
                border = ButtonDefaults.outlinedButtonBorder.copy(),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Show Hint", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ObjectiveItem(description: String, completed: Boolean) {
    val color = if (completed) NeoGreen else NeoTextSecondary
    val icon = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(description, color = color, fontSize = 14.sp,
            style = if (completed) LocalTextStyle.current.copy(color = color) else LocalTextStyle.current)
    }
}

@Composable
fun StoryDialogBox(
    speaker: String,
    text: String,
    speakerColor: Color,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedText by remember(text) { mutableStateOf("") }
    var finished by remember(text) { mutableStateOf(false) }

    LaunchedEffect(text) {
        displayedText = ""
        finished = false
        for (i in text.indices) {
            displayedText = text.substring(0, i + 1)
            kotlinx.coroutines.delay(18L)
        }
        finished = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeoSurface),
        border = CardDefaults.outlinedCardBorder().copy()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(speakerColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    speaker,
                    color = speakerColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                displayedText,
                color = NeoTextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
            if (finished) {
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(contentColor = NeoCyan)
                ) {
                    Text("Continue ▶", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AchievementToast(
    achievement: AchievementDef,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(achievement.id) {
        kotlinx.coroutines.delay(4000)
        onDismiss()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, XpGold.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(XpGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = XpGold, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("ACHIEVEMENT UNLOCKED", color = XpGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(2.dp))
                Text(achievement.title, color = NeoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(achievement.description, color = NeoTextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.weight(1f))
            Text("+${achievement.xpReward} XP", color = XpGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeoBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeoSurface),
        content = content
    )
}

@Composable
fun ChapterStatusBadge(unlocked: Boolean, completed: Boolean) {
    val (color, icon, label) = when {
        completed -> Triple(NeoGreen, Icons.Default.CheckCircle, "DONE")
        unlocked  -> Triple(NeoCyan, Icons.Default.PlayArrow, "PLAY")
        else      -> Triple(NeoTextMuted, Icons.Default.Lock, "LOCKED")
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}
