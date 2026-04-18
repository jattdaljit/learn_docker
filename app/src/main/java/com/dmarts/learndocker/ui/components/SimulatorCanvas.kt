package com.dmarts.learndocker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.domain.model.*
import com.dmarts.learndocker.ui.theme.*
import kotlinx.coroutines.delay

private val CanvasBg      = Color(0xFF0F1117)
private val CardBg        = Color(0xFF1A1D2E)
private val CardBgAlt     = Color(0xFF161928)
private val SectionLabelC = Color(0xFF3D4566)
private val TextPrimary   = Color(0xFFE2E8F0)
private val TextSecondary = Color(0xFF8892AA)
private val TextMuted     = Color(0xFF4A5270)
private val BorderColor   = Color(0xFF232845)
private val CopiedGreen   = Color(0xFF22C55E)

// ─── Copy helper ─────────────────────────────────────────────────────────────

@Composable
private fun rememberCopyState(): Pair<Boolean, (String) -> Unit> {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val copy: (String) -> Unit = { text ->
        clipboard.setText(AnnotatedString(text))
        copied = true
    }
    LaunchedEffect(copied) {
        if (copied) { delay(1400); copied = false }
    }
    return copied to copy
}

/** A clickable value pill. Flashes green with "✓ Copied" for 1.4 s after tap. */
@Composable
private fun CopyableText(
    value: String,
    displayValue: String = value,
    color: Color = TextSecondary,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier
) {
    val (copied, copy) = rememberCopyState()
    val textColor by animateColorAsState(
        if (copied) CopiedGreen else color,
        animationSpec = tween(200),
        label = "copy_color"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { copy(value) }
            .background(if (copied) CopiedGreen.copy(alpha = 0.12f) else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            if (copied) "✓ Copied" else displayValue,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─── Main composable ─────────────────────────────────────────────────────────

@Composable
fun SimulatorCanvas(
    state: SimulatorState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "p"
    )

    val isEmpty = state.containers.isEmpty() && state.images.isEmpty() &&
                  state.volumes.isEmpty() &&
                  state.networks.none { it.name !in listOf("bridge", "host", "none") }

    Box(modifier = modifier.background(CanvasBg)) {
        // Dot-grid background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 28f
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(Color(0xFF1E2238), radius = 1f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }

        if (isEmpty) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Containers ───────────────────────────────────────────
                if (state.containers.isNotEmpty()) {
                    item {
                        SectionHeader(
                            label = "CONTAINERS",
                            count = state.containers.size,
                            icon = Icons.Default.Inbox,
                            tint = Color(0xFF60A5FA)
                        )
                    }
                    items(state.containers) { container ->
                        ContainerCard(container = container, pulse = pulse)
                    }
                }

                // ── Images ───────────────────────────────────────────────
                if (state.images.isNotEmpty()) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item {
                        SectionHeader(
                            label = "IMAGES",
                            count = state.images.size,
                            icon = Icons.Default.Photo,
                            tint = Color(0xFFA78BFA)
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.images) { img -> ImageCard(img) }
                        }
                    }
                }

                // ── Volumes ──────────────────────────────────────────────
                if (state.volumes.isNotEmpty()) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item {
                        SectionHeader(
                            label = "VOLUMES",
                            count = state.volumes.size,
                            icon = Icons.Default.Save,
                            tint = Color(0xFFFBBF24)
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.volumes) { vol -> VolumeCard(vol) }
                        }
                    }
                }

                // ── Networks ─────────────────────────────────────────────
                val customNets = state.networks.filter { it.name !in listOf("bridge", "host", "none") }
                if (customNets.isNotEmpty()) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item {
                        SectionHeader(
                            label = "NETWORKS",
                            count = customNets.size,
                            icon = Icons.Default.Share,
                            tint = Color(0xFF34D399)
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(customNets) { net ->
                                NetworkCard(net, state.containers)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ─── Section header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Row(
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Text(label, color = tint, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp)
        Text("($count)", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

// ─── Container card ───────────────────────────────────────────────────────────

@Composable
private fun ContainerCard(container: DockerContainer, pulse: Float) {
    val statusColor = when (container.status) {
        ContainerStatus.RUNNING -> Color(0xFF22C55E)
        ContainerStatus.STOPPED -> Color(0xFF6B7280)
        ContainerStatus.PAUSED  -> Color(0xFFF59E0B)
        ContainerStatus.CREATED -> Color(0xFF60A5FA)
    }
    val statusLabel = container.status.name

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
    ) {
        // ── Header strip — tap to copy container name ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusColor.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(
                        if (container.status == ContainerStatus.RUNNING)
                            statusColor.copy(alpha = pulse)
                        else
                            statusColor.copy(alpha = 0.7f)
                    )
            )
            Spacer(Modifier.width(10.dp))
            // Container name — tap to copy
            CopyableText(
                value = container.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.18f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(statusLabel, color = statusColor, fontSize = 9.sp,
                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp)
            }
        }

        // ── Body ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Image — tap to copy `docker run <imageRef>`
            CopyableInfoRow(
                icon = Icons.Default.Photo,
                iconTint = Color(0xFFA78BFA),
                label = "IMAGE",
                value = container.imageRef,
                copyValue = "docker run -d ${container.imageRef}",
                valueColor = Color(0xFFA78BFA)
            )

            // Short container ID — tap to copy full ID
            CopyableInfoRow(
                icon = Icons.Default.Fingerprint,
                iconTint = TextMuted,
                label = "ID",
                value = container.id.take(12),
                copyValue = container.id
            )

            // Ports — each pill taps to copy `-p host:container` flag
            if (container.ports.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.SettingsEthernet, null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(13.dp).padding(top = 1.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("PORTS", color = TextMuted, fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(56.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        container.ports.forEach { p ->
                            val portStr = "${p.hostPort}:${p.containerPort}/${p.protocol}"
                            CopyableText(
                                value = "-p $portStr",
                                displayValue = portStr,
                                color = Color(0xFF60A5FA),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF60A5FA).copy(alpha = 0.12f))
                                    .padding(horizontal = 3.dp)
                            )
                        }
                    }
                }
            }

            // Network — tap to copy `--network <name>`
            CopyableInfoRow(
                icon = Icons.Default.Share,
                iconTint = Color(0xFF34D399),
                label = "NETWORK",
                value = container.networkName,
                copyValue = "--network ${container.networkName}",
                valueColor = Color(0xFF34D399)
            )

            // IP address — tap to copy
            if (container.ipAddress.isNotEmpty()) {
                CopyableInfoRow(
                    icon = Icons.Default.Router,
                    iconTint = Color(0xFF34D399),
                    label = "IP",
                    value = container.ipAddress,
                    valueColor = Color(0xFF34D399)
                )
            }

            // Volumes — tap each to copy `-v volumeName:mountPath`
            if (container.volumeMounts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Save, null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(13.dp).padding(top = 1.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("VOLUMES", color = TextMuted, fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(56.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        container.volumeMounts.forEach { v ->
                            CopyableText(
                                value = "-v ${v.volumeName}:${v.mountPath}",
                                displayValue = "${v.volumeName} → ${v.mountPath}",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Env vars — tap each to copy `-e KEY=VALUE`
            if (container.envVars.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Code, null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(13.dp).padding(top = 1.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ENV", color = TextMuted, fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(56.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        container.envVars.entries.take(4).forEach { (k, v) ->
                            CopyableText(
                                value = "-e $k=$v",
                                displayValue = "$k=$v",
                                color = Color(0xFFF87171),
                                fontSize = 11.sp
                            )
                        }
                        if (container.envVars.size > 4) {
                            Text(
                                "+ ${container.envVars.size - 4} more",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Copyable info row ────────────────────────────────────────────────────────

@Composable
private fun CopyableInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    copyValue: String = value,
    valueColor: Color = TextSecondary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextMuted, fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(56.dp))
        CopyableText(
            value = copyValue,
            displayValue = value,
            color = valueColor,
            fontSize = 12.sp
        )
    }
}

// ─── Image card ───────────────────────────────────────────────────────────────

@Composable
private fun ImageCard(img: DockerImage) {
    val (copied, copy) = rememberCopyState()
    val bgColor by animateColorAsState(
        if (copied) CopiedGreen.copy(alpha = 0.10f) else CardBg,
        label = "img_bg"
    )

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            // Tap anywhere on the card to copy `docker pull repo:tag`
            .clickable { copy("docker pull ${img.repository}:${img.tag}") }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFA78BFA).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (copied) Icons.Default.Check else Icons.Default.Photo,
                    null,
                    tint = if (copied) CopiedGreen else Color(0xFFA78BFA),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    img.repository,
                    color = if (copied) CopiedGreen else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Text(
                    ":${img.tag}",
                    color = if (copied) CopiedGreen else Color(0xFFA78BFA),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatSize(img.sizeBytes),
                color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace
            )
            Text(
                if (copied) "✓ Copied!" else img.id.take(8),
                color = if (copied) CopiedGreen else TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ─── Volume card ─────────────────────────────────────────────────────────────

@Composable
private fun VolumeCard(vol: DockerVolume) {
    val (copied, copy) = rememberCopyState()
    val bgColor by animateColorAsState(
        if (copied) CopiedGreen.copy(alpha = 0.08f) else CardBg,
        label = "vol_bg"
    )

    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { copy(vol.name) }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFBBF24).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (copied) Icons.Default.Check else Icons.Default.Save,
                    null,
                    tint = if (copied) CopiedGreen else Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                if (copied) "✓ Copied!" else vol.name,
                color = if (copied) CopiedGreen else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
        Text("driver: ${vol.driver}", color = TextMuted, fontSize = 10.sp,
            fontFamily = FontFamily.Monospace)
        Text(vol.mountpoint, color = TextMuted, fontSize = 9.sp,
            fontFamily = FontFamily.Monospace, maxLines = 2, lineHeight = 13.sp)
    }
}

// ─── Network card ─────────────────────────────────────────────────────────────

@Composable
private fun NetworkCard(net: DockerNetwork, containers: List<DockerContainer>) {
    val connected = containers.filter {
        it.networkName == net.name || it.id in net.connectedContainerIds
    }
    val (copied, copy) = rememberCopyState()
    val bgColor by animateColorAsState(
        if (copied) CopiedGreen.copy(alpha = 0.08f) else CardBg,
        label = "net_bg"
    )

    Column(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { copy(net.name) }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF34D399).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (copied) Icons.Default.Check else Icons.Default.Share,
                    null,
                    tint = if (copied) CopiedGreen else Color(0xFF34D399),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                if (copied) "✓ Copied!" else net.name,
                color = if (copied) CopiedGreen else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
        Text("driver: ${net.driver}", color = TextMuted, fontSize = 10.sp,
            fontFamily = FontFamily.Monospace)
        if (net.subnet.isNotEmpty()) {
            Text("subnet: ${net.subnet}", color = TextMuted, fontSize = 10.sp,
                fontFamily = FontFamily.Monospace)
        }
        if (connected.isNotEmpty()) {
            Text("containers: ${connected.size}", color = Color(0xFF34D399),
                fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1D2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inbox, null,
                    tint = Color(0xFF3D4566),
                    modifier = Modifier.size(36.dp))
            }
            Text("No resources yet", color = Color(0xFF3D4566),
                fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            Text("Run docker commands to see\ncontainers, images & volumes here",
                color = Color(0xFF2A2F4A), fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_000_000_000 -> "${"%.1f".format(bytes / 1_000_000_000.0)} GB"
    bytes >= 1_000_000     -> "${"%.0f".format(bytes / 1_000_000.0)} MB"
    bytes >= 1_000         -> "${"%.0f".format(bytes / 1_000.0)} KB"
    else                   -> "$bytes B"
}
