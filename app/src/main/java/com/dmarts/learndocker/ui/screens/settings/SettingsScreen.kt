package com.dmarts.learndocker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.net.Uri
import com.dmarts.learndocker.AppContainer
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.ui.theme.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface

// ─── ViewModel ───────────────────────────────────────────────────────────────

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    data class UiState(
        val isRunning: Boolean = false,
        val serverUrl: String = "",
        val errorMsg: String = "",
        val port: Int = 8080,
        val userName: String = "CIPHER",
        val userNameSaved: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        if (container.isWebServerRunning) {
            val ip = localIp()
            _state.update { it.copy(isRunning = true, serverUrl = "http://$ip:${it.port}") }
        }
        viewModelScope.launch {
            val name = container.progressRepository.progressFlow.first().userName
            _state.update { it.copy(userName = name.ifBlank { "CIPHER" }) }
        }
    }

    fun onUserNameChange(name: String) = _state.update { it.copy(userName = name, userNameSaved = false) }

    fun saveUserName() {
        val name = _state.value.userName.trim().ifBlank { "CIPHER" }
        viewModelScope.launch {
            container.progressRepository.update { it.copy(userName = name) }
            _state.update { it.copy(userName = name, userNameSaved = true) }
        }
    }

    fun startServer() {
        val port = _state.value.port
        val ok = container.startWebServer(port)
        if (ok) {
            val ip = localIp()
            _state.update { it.copy(isRunning = true, serverUrl = "http://$ip:$port", errorMsg = "") }
        } else {
            _state.update { it.copy(errorMsg = "Failed to start server. Port $port may be in use.") }
        }
    }

    fun stopServer() {
        container.stopWebServer()
        _state.update { it.copy(isRunning = false, serverUrl = "", errorMsg = "") }
    }

    private fun localIp(): String {
        return try {
            NetworkInterface.getNetworkInterfaces()
                ?.asSequence()
                ?.flatMap { it.inetAddresses.asSequence() }
                ?.firstOrNull { addr ->
                    !addr.isLoopbackAddress && addr is Inet4Address &&
                    (addr.hostAddress?.startsWith("192.168") == true ||
                     addr.hostAddress?.startsWith("10.")     == true ||
                     addr.hostAddress?.startsWith("172.")    == true)
                }
                ?.hostAddress ?: "unknown"
        } catch (e: Exception) { "unknown" }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm: SettingsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel((context.applicationContext as LearnDockerApp).container) as T
        }
    })
    val state by vm.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) { kotlinx.coroutines.delay(2000); copied = false }
    }

    Scaffold(
        containerColor = NeoBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeoTextSecondary)
                    }
                },
                title = { Text("Settings", color = NeoTextPrimary, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Profile section ───────────────────────────────────────────
            SectionLabel("Profile")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeoSurface)
                    .border(1.dp, NeoBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = NeoCyan, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Your Name", color = NeoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Shown on the Home screen", color = NeoTextSecondary, fontSize = 12.sp)
                    }
                }
                OutlinedTextField(
                    value = state.userName,
                    onValueChange = vm::onUserNameChange,
                    singleLine = true,
                    placeholder = { Text("e.g. CIPHER", color = NeoTextMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeoCyan,
                        unfocusedBorderColor = NeoBorder,
                        focusedTextColor = NeoTextPrimary,
                        unfocusedTextColor = NeoTextPrimary,
                        cursorColor = NeoCyan,
                        focusedContainerColor = NeoBackground,
                        unfocusedContainerColor = NeoBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = vm::saveUserName,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.userNameSaved) NeoGreen else NeoCyan,
                        contentColor = NeoBackground
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        if (state.userNameSaved) Icons.Default.Check else Icons.Default.Save,
                        null, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.userNameSaved) "Saved!" else "Save Name",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Section header ────────────────────────────────────────────
            SectionLabel("Remote Access")

            // ── Info card ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeoSurface)
                    .border(1.dp, NeoBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Wifi, null,
                        tint = NeoCyan, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Access from Browser", color = NeoTextPrimary, fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold)
                        Text("Same Wi-Fi network required", color = NeoTextSecondary, fontSize = 12.sp)
                    }
                }

                Text(
                    "Start the server on your phone, then open the URL on any laptop browser. " +
                    "You'll get a full Docker terminal with the same simulator.",
                    color = NeoTextSecondary, fontSize = 13.sp, lineHeight = 19.sp
                )

                HorizontalDivider(color = NeoBorder)

                // Toggle button
                if (!state.isRunning) {
                    Button(
                        onClick = vm::startServer,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeoCyan, contentColor = NeoBackground
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Server", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = vm::stopServer,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeoRed, contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Stop, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Server", fontWeight = FontWeight.Bold)
                    }
                }

                if (state.errorMsg.isNotEmpty()) {
                    Text(state.errorMsg, color = NeoRed, fontSize = 12.sp)
                }
            }

            // ── URL card (shown only when running) ────────────────────────
            if (state.isRunning && state.serverUrl.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionLabel("Server Running")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeoGreen.copy(alpha = 0.06f))
                        .border(1.dp, NeoGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NeoGreen, RoundedCornerShape(50))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Server is live", color = NeoGreen, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold)
                    }

                    // URL display + copy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D1117))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.serverUrl,
                            color = NeoCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(state.serverUrl))
                                copied = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                "Copy URL",
                                tint = if (copied) NeoGreen else NeoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = NeoGreen.copy(alpha = 0.15f))

                    // Instructions
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        InstructionStep("1", "Make sure your laptop is on the same Wi-Fi network as this phone.")
                        InstructionStep("2", "Open the URL above in Chrome, Firefox, or Edge.")
                        InstructionStep("3", "You'll see a full Docker terminal. Commands run here — on this device.")
                        InstructionStep("4", "The left panel shows live containers, images, networks and volumes.")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Support section ───────────────────────────────────────────
            SectionLabel("Support")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeoSurface)
                    .border(1.dp, NeoBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Buy me a coffee button
                val ctx = LocalContext.current
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://buymeacoffee.com/jattdjsingh"))
                        ctx.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD00),
                        contentColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("\u2615", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Buy me a coffee", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Footer ───────────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Version 1.0",
                    color = NeoTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Made with ", color = NeoTextMuted, fontSize = 12.sp)
                    Text("\u2764\uFE0F", fontSize = 12.sp)
                    Text(" in India", color = NeoTextMuted, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        color = NeoTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .background(NeoCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = NeoCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = NeoTextSecondary, fontSize = 12.sp, lineHeight = 17.sp,
            modifier = Modifier.weight(1f))
    }
}
