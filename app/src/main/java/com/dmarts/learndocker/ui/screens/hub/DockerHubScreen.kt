package com.dmarts.learndocker.ui.screens.hub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.ui.theme.*

// ─── Data ────────────────────────────────────────────────────────────────────

data class HubImage(
    val name: String,
    val category: String,
    val categoryColor: Color,
    val description: String,
    val pulls: String,
    val stars: String,
    val isOfficial: Boolean = true,
    val tags: List<String>,
    val usageExample: String,
    val envVars: List<String> = emptyList(),
    val ports: List<String> = emptyList(),
    val volumePaths: List<String> = emptyList()
)

private val HUB_IMAGES = listOf(
    HubImage(
        name = "nginx",
        category = "Web Server",
        categoryColor = Color(0xFF16A34A),
        description = "Official Nginx HTTP server image. High-performance, production-grade web server and reverse proxy used by millions of deployments.",
        pulls = "1B+",
        stars = "19.1k",
        tags = listOf("latest", "alpine", "1.25", "1.24", "1.25-alpine", "stable"),
        usageExample = "docker run -d --name web -p 80:80 nginx",
        ports = listOf("80", "443"),
        volumePaths = listOf("/etc/nginx/conf.d", "/usr/share/nginx/html")
    ),
    HubImage(
        name = "redis",
        category = "Database",
        categoryColor = Color(0xFFDC2626),
        description = "Official Redis image. In-memory data structure store used as database, cache, and message broker. Lightning-fast key-value store.",
        pulls = "1B+",
        stars = "12.3k",
        tags = listOf("latest", "alpine", "7.2", "7.0", "6.2", "7.2-alpine"),
        usageExample = "docker run -d --name redis -p 6379:6379 redis:alpine",
        ports = listOf("6379"),
        volumePaths = listOf("/data")
    ),
    HubImage(
        name = "postgres",
        category = "Database",
        categoryColor = Color(0xFF2563EB),
        description = "Official PostgreSQL image. World's most advanced open source relational database. ACID-compliant, feature-rich SQL database.",
        pulls = "500M+",
        stars = "13.1k",
        tags = listOf("latest", "16", "15", "14", "alpine", "16-alpine"),
        usageExample = "docker run -d --name db -e POSTGRES_PASSWORD=secret -p 5432:5432 postgres",
        envVars = listOf("POSTGRES_PASSWORD", "POSTGRES_USER", "POSTGRES_DB"),
        ports = listOf("5432"),
        volumePaths = listOf("/var/lib/postgresql/data")
    ),
    HubImage(
        name = "mysql",
        category = "Database",
        categoryColor = Color(0xFFEA580C),
        description = "Official MySQL image. World's most popular open source database. Used for web applications, content management, and e-commerce.",
        pulls = "500M+",
        stars = "14.4k",
        tags = listOf("latest", "8.0", "8.3", "5.7", "lts"),
        usageExample = "docker run -d --name mysql -e MYSQL_ROOT_PASSWORD=secret -p 3306:3306 mysql",
        envVars = listOf("MYSQL_ROOT_PASSWORD", "MYSQL_DATABASE", "MYSQL_USER", "MYSQL_PASSWORD"),
        ports = listOf("3306"),
        volumePaths = listOf("/var/lib/mysql")
    ),
    HubImage(
        name = "node",
        category = "Runtime",
        categoryColor = Color(0xFF65A30D),
        description = "Official Node.js image. JavaScript runtime built on Chrome's V8 engine. For building scalable server-side applications.",
        pulls = "1B+",
        stars = "13.8k",
        tags = listOf("latest", "lts", "alpine", "20", "18", "20-alpine", "18-alpine"),
        usageExample = "docker run -d --name app -p 3000:3000 -v \$(pwd):/app node:alpine",
        ports = listOf("3000", "8080"),
        volumePaths = listOf("/app")
    ),
    HubImage(
        name = "python",
        category = "Runtime",
        categoryColor = Color(0xFF0EA5E9),
        description = "Official Python image. Versatile language for web development, data science, automation, AI/ML, and scripting.",
        pulls = "1B+",
        stars = "9.5k",
        tags = listOf("latest", "alpine", "3.12", "3.11", "3.10", "3.12-alpine", "slim"),
        usageExample = "docker run -it --name py -v \$(pwd):/app python:3.12-alpine python /app/main.py",
        ports = listOf("8000"),
        volumePaths = listOf("/app")
    ),
    HubImage(
        name = "mongo",
        category = "Database",
        categoryColor = Color(0xFF16A34A),
        description = "Official MongoDB image. Document-oriented NoSQL database. Flexible schema design, powerful query language, horizontal scaling.",
        pulls = "500M+",
        stars = "10.2k",
        tags = listOf("latest", "7.0", "6.0", "5.0", "7.0-jammy"),
        usageExample = "docker run -d --name mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=admin mongo",
        envVars = listOf("MONGO_INITDB_ROOT_USERNAME", "MONGO_INITDB_ROOT_PASSWORD", "MONGO_INITDB_DATABASE"),
        ports = listOf("27017"),
        volumePaths = listOf("/data/db")
    ),
    HubImage(
        name = "ubuntu",
        category = "OS",
        categoryColor = Color(0xFFE05533),
        description = "Official Ubuntu base image. Most popular Linux distribution for Docker containers. Minimal install ideal for custom images.",
        pulls = "1B+",
        stars = "15.7k",
        tags = listOf("latest", "24.04", "22.04", "20.04", "noble", "jammy", "focal"),
        usageExample = "docker run -it ubuntu:24.04 bash",
        ports = emptyList()
    ),
    HubImage(
        name = "alpine",
        category = "OS",
        categoryColor = Color(0xFF0284C7),
        description = "Official Alpine Linux image. Minimal Docker image — only ~5MB. Based on musl libc and busybox. Perfect for tiny production images.",
        pulls = "1B+",
        stars = "10.6k",
        tags = listOf("latest", "3.19", "3.18", "3.17", "edge"),
        usageExample = "docker run -it alpine sh",
        ports = emptyList()
    ),
    HubImage(
        name = "rabbitmq",
        category = "Messaging",
        categoryColor = Color(0xFFF97316),
        description = "Official RabbitMQ image. Open source message broker. Implements AMQP, STOMP, MQTT protocols. Management UI available.",
        pulls = "100M+",
        stars = "4.6k",
        tags = listOf("latest", "3", "management", "3-management", "3.13", "3.13-management"),
        usageExample = "docker run -d --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management",
        envVars = listOf("RABBITMQ_DEFAULT_USER", "RABBITMQ_DEFAULT_PASS"),
        ports = listOf("5672", "15672")
    ),
    HubImage(
        name = "traefik",
        category = "Proxy",
        categoryColor = Color(0xFF7C3AED),
        description = "Official Traefik reverse proxy image. Cloud-native edge router. Auto-discovers services, manages TLS, integrates with Docker.",
        pulls = "500M+",
        stars = "4.9k",
        tags = listOf("latest", "v3.0", "v2.11", "v3.0-windowsservercore"),
        usageExample = "docker run -d -p 80:80 -p 8080:8080 -v /var/run/docker.sock:/var/run/docker.sock traefik",
        ports = listOf("80", "443", "8080"),
        volumePaths = listOf("/etc/traefik")
    ),
    HubImage(
        name = "grafana/grafana",
        category = "Monitoring",
        categoryColor = Color(0xFFF97316),
        isOfficial = false,
        description = "Grafana observability platform. Create dashboards to visualize metrics, logs, and traces from any data source.",
        pulls = "500M+",
        stars = "3.2k",
        tags = listOf("latest", "10.4.0", "10.3.0", "main"),
        usageExample = "docker run -d --name grafana -p 3000:3000 grafana/grafana",
        envVars = listOf("GF_SECURITY_ADMIN_PASSWORD", "GF_USERS_ALLOW_SIGN_UP"),
        ports = listOf("3000"),
        volumePaths = listOf("/var/lib/grafana")
    ),
)

private val CATEGORIES = listOf("All") + HUB_IMAGES.map { it.category }.distinct()

// ─── Content composable (no Scaffold — used as a tab in MainScreen) ──────────

@Composable
fun DockerHubScreenContent(onTryInSandbox: (String) -> Unit = {}) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedImage by remember { mutableStateOf<String?>(null) }

    val filtered = HUB_IMAGES.filter { img ->
        val matchesSearch = search.isBlank() ||
            img.name.contains(search, ignoreCase = true) ||
            img.description.contains(search, ignoreCase = true) ||
            img.category.contains(search, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || img.category == selectedCategory
        matchesSearch && matchesCategory
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(NeoBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth().background(NeoBackground),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Docker Hub", color = Color(0xFF0EA5E9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Browse official images", color = NeoTextSecondary, fontSize = 11.sp)
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeoSurface)
                    .border(1.dp, NeoBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = NeoTextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = search, onValueChange = { search = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = NeoTextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(NeoCyan), singleLine = true,
                    decorationBox = { inner ->
                        Box {
                            if (search.isEmpty()) Text("Search images...", color = NeoTextSecondary, fontSize = 14.sp)
                            inner()
                        }
                    }
                )
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, null, tint = NeoTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORIES) { cat ->
                    val selected = cat == selectedCategory
                    FilterChip(
                        selected = selected, onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeoCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeoCyan,
                            containerColor = NeoSurface,
                            labelColor = NeoTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selected,
                            borderColor = NeoBorder, selectedBorderColor = NeoCyan.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
        items(filtered) { img ->
            HubImageCard(
                img = img,
                expanded = expandedImage == img.name,
                onToggle = { expandedImage = if (expandedImage == img.name) null else img.name },
                onTryInSandbox = { onTryInSandbox(img.usageExample) }
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

// ─── Screen (with Scaffold + back button) ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockerHubScreen(
    onBack: () -> Unit,
    onTryInSandbox: (String) -> Unit = {}
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedImage by remember { mutableStateOf<String?>(null) }

    val filtered = HUB_IMAGES.filter { img ->
        val matchesSearch = search.isBlank() ||
            img.name.contains(search, ignoreCase = true) ||
            img.description.contains(search, ignoreCase = true) ||
            img.category.contains(search, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || img.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        containerColor = NeoBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeoTextSecondary)
                    }
                },
                title = {
                    Column {
                        Text("Docker Hub", color = Color(0xFF0EA5E9), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Browse & simulate official images", color = NeoTextSecondary, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeoSurface)
                        .border(1.dp, NeoBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = NeoTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = NeoTextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Default),
                        cursorBrush = SolidColor(NeoCyan),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box {
                                if (search.isEmpty()) Text("Search images (nginx, redis, postgres...)", color = NeoTextSecondary, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { search = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, null, tint = NeoTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Category filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CATEGORIES) { cat ->
                        val selected = cat == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeoCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeoCyan,
                                containerColor = NeoSurface,
                                labelColor = NeoTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = NeoBorder,
                                selectedBorderColor = NeoCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Stats row
            item {
                Text(
                    "${filtered.size} image${if (filtered.size != 1) "s" else ""} found",
                    color = NeoTextSecondary, fontSize = 12.sp
                )
            }

            // Image cards
            items(filtered) { img ->
                HubImageCard(
                    img = img,
                    expanded = expandedImage == img.name,
                    onToggle = { expandedImage = if (expandedImage == img.name) null else img.name },
                    onTryInSandbox = { onTryInSandbox(img.usageExample) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HubImageCard(
    img: HubImage,
    expanded: Boolean,
    onToggle: () -> Unit,
    onTryInSandbox: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeoSurface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column {
            // Header row — always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(img.categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        img.name.take(2).uppercase(),
                        color = img.categoryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            img.name,
                            color = NeoTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (img.isOfficial) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0EA5E9).copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("OFFICIAL", color = Color(0xFF0EA5E9), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(img.categoryColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(img.category, color = img.categoryColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("↓ ${img.pulls}", color = NeoTextSecondary, fontSize = 11.sp)
                        Text("★ ${img.stars}", color = Color(0xFFF59E0B), fontSize = 11.sp)
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = NeoTextSecondary, modifier = Modifier.size(20.dp)
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Description
                    Text(img.description, color = Color(0xFF94A3B8), fontSize = 13.sp, lineHeight = 19.sp)

                    // Tags
                    if (img.tags.isNotEmpty()) {
                        InfoSection("Tags") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(img.tags) { tag ->
                                    Text(
                                        tag,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = Color(0xFF7DD3FC),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // Ports / Volumes / Env
                    if (img.ports.isNotEmpty()) {
                        InfoSection("Exposed Ports") {
                            Text(img.ports.joinToString("  "), color = Color(0xFF4ADE80), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    if (img.envVars.isNotEmpty()) {
                        InfoSection("Environment Variables") {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                img.envVars.forEach { env ->
                                    Text("-e $env=...", color = Color(0xFFFBBF24), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                    if (img.volumePaths.isNotEmpty()) {
                        InfoSection("Volume Paths") {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                img.volumePaths.forEach { path ->
                                    Text("-v mydata:$path", color = Color(0xFFC084FC), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Usage example
                    InfoSection("Run Command") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0A0E1A))
                                .padding(12.dp)
                        ) {
                            Text(img.usageExample, color = Color(0xFFE2E8F0), fontSize = 12.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp)
                        }
                    }

                    // Try in Sandbox button
                    Button(
                        onClick = onTryInSandbox,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0EA5E9).copy(alpha = 0.15f),
                            contentColor = Color(0xFF0EA5E9)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Terminal, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Try in Sandbox", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), color = Color(0xFF475569), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        content()
    }
}
