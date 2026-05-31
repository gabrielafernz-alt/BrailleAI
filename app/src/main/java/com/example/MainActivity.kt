package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.ScanEntity
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BrailleVisionViewModel
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.SampleSheet
import com.example.util.BrailleCell
import com.example.util.BrailleTranslator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    BrailleVisionApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BrailleVisionApp(
    modifier: Modifier = Modifier,
    viewModel: BrailleVisionViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified SaaS Header Bar
            SaaSHeader(
                isSoundEnabled = isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onNavigateHome = { viewModel.selectTab("dashboard") }
            )

            // Dynamic Main Tab View Switcher
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(targetState = currentTab, label = "ScreenTransition") { tab ->
                    when (tab) {
                        "dashboard" -> DashboardScreen(viewModel = viewModel)
                        "scanner" -> ScannerScreen(viewModel = viewModel)
                        "translator" -> TranslatorScreen(viewModel = viewModel)
                        "keypad" -> KeypadScreen(viewModel = viewModel)
                        "library" -> LibraryScreen(viewModel = viewModel)
                    }
                }
            }

            // Universal Bottom Navigation Dock
            SaaSBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    }
}

@Composable
fun SaaSHeader(
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        onClick = onNavigateHome,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
                    .testTag("header_logo_row")
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = "Logo icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "BRAILLEVISION AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tactile SaaS OCR Hub",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Speech Narrator Button to control accessibilities
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .background(
                        color = if (isSoundEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .size(40.dp)
                    .testTag("sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (isSoundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                    contentDescription = "Toggle TTS narration",
                    tint = if (isSoundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SaaSBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Hub",
                icon = Icons.Outlined.GridView,
                activeIcon = Icons.Filled.GridView,
                isActive = currentTab == "dashboard",
                onClick = { onTabSelected("dashboard") },
                testTag = "nav_dashboard"
            )
            NavItem(
                label = "Scan",
                icon = Icons.Outlined.DocumentScanner,
                activeIcon = Icons.Filled.DocumentScanner,
                isActive = currentTab == "scanner",
                onClick = { onTabSelected("scanner") },
                testTag = "nav_scanner"
            )
            NavItem(
                label = "Translate",
                icon = Icons.Outlined.Translate,
                activeIcon = Icons.Filled.Translate,
                isActive = currentTab == "translator",
                onClick = { onTabSelected("translator") },
                testTag = "nav_translator"
            )
            NavItem(
                label = "Keypad",
                icon = Icons.Outlined.Keyboard,
                activeIcon = Icons.Filled.Keyboard,
                isActive = currentTab == "keypad",
                onClick = { onTabSelected("keypad") },
                testTag = "nav_keypad"
            )
            NavItem(
                label = "History",
                icon = Icons.Outlined.FolderCopy,
                activeIcon = Icons.Filled.FolderCopy,
                isActive = currentTab == "library",
                onClick = { onTabSelected("library") },
                testTag = "nav_history"
            )
        }
    }
}

@Composable
fun RowScope.NavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isActive) activeIcon else icon,
                    contentDescription = label,
                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== SCREEN 1: DASHBOARD HUB ====================
@Composable
fun DashboardScreen(
    viewModel: BrailleVisionViewModel
) {
    val totalScans by viewModel.statsTotalScans.collectAsStateWithLifecycle()
    val totalBookmarks by viewModel.statsBookmarked.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aesthetic Greeting Card (Slide in look)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Next-Gen Accessibility Platform",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bridging Vision & Touch with AI",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Unified scanner mapping combining OpticalBrailleRecognition algorithms, tactile responsive keypad simulation and multi-modal neural decoding.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Stats Counters Grid (asymmetric custom cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectTab("library") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = "Scans metric",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = totalScans.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = "Saved Records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectTab("library") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Bookmarks",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = totalBookmarks.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = "Bookmarks Saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Feature Navigation Quicklinks Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Quick Launcher Modules",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                QuickLinkRow(
                    title = "Launch OBR Neural Scanner",
                    desc = "Analyze high contrast Braille with Gemini API",
                    icon = Icons.Filled.Camera,
                    onClick = { viewModel.selectTab("scanner") }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                QuickLinkRow(
                    title = "Interactive Tactile Board",
                    desc = "Build words cell-by-cell with TTS voice feedbacks",
                    icon = Icons.Filled.Keyboard,
                    onClick = { viewModel.selectTab("keypad") }
                )
            }
        }

        // Accessibility Speech Control sliders (important architectural customization)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = "Voice settings icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Speech Synthesis Configuration",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "Adjust speech delivery rate to suit comfort level.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.7f, 1.0f, 1.25f, 1.5f).forEach { rate ->
                        val isSelected = speechRate == rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setSpeed(rate) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${rate}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Educative Fact Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Assistive Study Guide",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A standard Braille cell comprises 6 dots aligned in a 2x3 grid. Grade 1 Braille maps letters character-by-character, while Grade 2 incorporates contractions like 'the' or 'and' to increase touch-reading velocities.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        // Open Source Credits acknowledging repositories requested by the user
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Open Source Acknowledgements",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "This applet operates as a synthesizer built on structural paradigms of these great repositories:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                CreditRef(name = "OpticalBrailleRecognition", desc = "Physical dot detection segmentation and threshold alignment workflows.")
                CreditRef(name = "BrailleNext", desc = "Accessibility-first tactile interaction layouts and audio cue synthesis.")
                CreditRef(name = "braille (AaditT)", desc = "High-precision character dot translating, formatting indexes, and mappings.")
                CreditRef(name = "InSight (SatyamSoni23)", desc = "Assistance camera viewfinders UI details and sensor overlays logic.")
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun QuickLinkRow(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun CreditRef(
    name: String,
    desc: String
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
    }
}


// ==================== SCREEN 2: OPTICAL OCR SCANNER ====================
@Composable
fun ScannerScreen(
    viewModel: BrailleVisionViewModel
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val scanLogs by viewModel.scanProgressLogs.collectAsStateWithLifecycle()
    val selectedSample by viewModel.selectedSample.collectAsStateWithLifecycle()
    val customUploadedUri by viewModel.customUploadedUri.collectAsStateWithLifecycle()
    val customUploadedBitmap by viewModel.customUploadedBitmap.collectAsStateWithLifecycle()

    var apiKeyInput by remember { mutableStateOf("") }
    var showKeyDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onCustomImagePicked(context, uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onCustomCameraCaptured(bitmap)
        } else {
            viewModel.announceSpeech("Camera capture cancelled or failed.")
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            viewModel.announceSpeech("Camera permission denied. Cannot capture photo.")
        }
    }

    // Grab secret from Build Context to inform user
    val isSystemKeyAvailable = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    // Set up horizontal laser scan animation!
    val infiniteTransition = rememberInfiniteTransition(label = "LaserAnimation")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScannerLaser"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title
        Column {
            Text(
                text = "Neural OBR Segmentation Lab",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "Simulate real visual signages and decode them via edge rules or Gemini AI.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Active Signage Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Preset Braille Plate",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Grid of 4 samples
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.sampleSheets.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { sheet ->
                                val isSelected = selectedSample.id == sheet.id && customUploadedUri == null
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.background
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { 
                                            viewModel.clearCustomUpload()
                                            viewModel.selectSample(sheet) 
                                        }
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = sheet.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Category: ${sheet.category}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Image / Camera Capture Trigger Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (customUploadedUri != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                                 else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp, 
                color = if (customUploadedUri != null) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Import External Braille Plate Image",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Snap a photo using camera or browse local device files.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = if (customUploadedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (customUploadedUri != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Loaded",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (customUploadedUri?.startsWith("camera_capture_") == true) "Camera Snapshot Active" else "Uploaded Image Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(
                            onClick = { viewModel.clearCustomUpload() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // CAMERA BUTTON
                        Button(
                            onClick = {
                            try {
                                val hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            } catch (e: Exception) {
                                viewModel.announceSpeech("No camera app found on this device or emulator.")
                            }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Click Picture", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // FILE BROWSE BUTTON
                        Button(
                            onClick = {
                                try {
                                    imagePickerLauncher.launch("image/*") 
                                } catch(e: Exception) {
                                    viewModel.announceSpeech("No file manager installed on this device to pick images.")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Browse Files", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Animated Photographic viewfinder rendering the active Braille design
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background simulated camera grid
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (customUploadedUri != null) "CUSTOM OBR IMAGE SOURCE ACTIVE" else "VIRTUAL OBR VIEWFINDER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customUploadedUri != null) MaterialTheme.colorScheme.primary else Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val customBitmap = customUploadedBitmap
                    if (customBitmap != null) {
                        Image(
                            bitmap = customBitmap.asImageBitmap(),
                            contentDescription = "Uploaded custom Braille plate",
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Inside
                        )
                    } else {
                        // Draw the Braille Sheet on Screen
                        Text(
                            text = BrailleTranslator.textToBraille(selectedSample.baseText).map { it.unicode }.joinToString(""),
                            fontSize = 42.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 16.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Text(
                            text = "[ Sample Target: '${selectedSample.baseText}' ]",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Photorefractive laser bar that animates!
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = translationY.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Red, Color.Red, Color.Transparent)
                            )
                        )
                )

                // Overlay grid corners
                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    // Top-Left corner
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawLine(Color.Cyan, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(16.dp.toPx(), 0f), 3.dp.toPx())
                        drawLine(Color.Cyan, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, 16.dp.toPx()), 3.dp.toPx())
                    }
                    // Bottom-Right corner
                    Canvas(modifier = Modifier.size(16.dp).align(Alignment.BottomEnd)) {
                        drawLine(Color.Cyan, androidx.compose.ui.geometry.Offset(16.dp.toPx(), 16.dp.toPx()), androidx.compose.ui.geometry.Offset(0f, 16.dp.toPx()), 3.dp.toPx())
                        drawLine(Color.Cyan, androidx.compose.ui.geometry.Offset(16.dp.toPx(), 16.dp.toPx()), androidx.compose.ui.geometry.Offset(16.dp.toPx(), 0f), 3.dp.toPx())
                    }
                }
            }
        }

        // API Key controller indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isSystemKeyAvailable || apiKeyInput.isNotEmpty()) Icons.Filled.VerifiedUser else Icons.Filled.LockOpen,
                        contentDescription = "Shield",
                        tint = if (isSystemKeyAvailable || apiKeyInput.isNotEmpty()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isSystemKeyAvailable || apiKeyInput.isNotEmpty()) "Neural Connection Shield Active" else "Offline Decryptor Shield Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isSystemKeyAvailable || apiKeyInput.isNotEmpty()) "Using multi-modal Gemini AI parsing." else "Using high contrast local heuristics.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showKeyDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("API Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Trigger Button
        Button(
            onClick = {
                val effectiveKey = if (apiKeyInput.isNotEmpty()) apiKeyInput else BuildConfig.GEMINI_API_KEY
                viewModel.runBrailleVisualScan(effectiveKey)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("initiate_scan_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = "Execute Deep Vector Scan", fontWeight = FontWeight.Black)
            }
        }

        // Live Analyzer Logs Console (InSight overlay feel)
        AnimatedVisibility(
            visible = scanState !is ScanUiState.Idle,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B13)),
                border = BorderStroke(1.dp, Color(0xFF131E2B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "OBR COMPUTER VISION STREAM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Logs Column
                    scanLogs.forEach { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFA1C6E7),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    if (scanState is ScanUiState.RunningAI) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.Green)
                            Text(text = "Running Gemini AI Multi-Modal scan...", fontSize = 11.sp, color = Color.Green, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Scanner Decoded Result Panel
        AnimatedVisibility(
            visible = scanState is ScanUiState.Success,
            enter = expandVertically() + fadeIn()
        ) {
            val state = scanState as? ScanUiState.Success
            if (state != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Green.copy(alpha = 0.15f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "SUCCESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                            }
                            Text(text = "Source: ${state.source}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Decoded Transcription:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.englishText.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                            )
                            IconButton(
                                onClick = { viewModel.announceSpeech("Translation is: ${state.englishText}") },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeUp,
                                    contentDescription = "Speak translation aloud",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(text = "Braille Structure Symbol Sequence:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = state.brailleText,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 6.dp),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tactile Analysis Reports",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = { viewModel.announceSpeech("Transcription reads: ${state.englishText}") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Narrate Result", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = state.details,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Modal dialog to customize API keys inside user space
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = { Text("Local Gemini API Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "To run genuine neural OBR scans, paste your custom Gemini API key. " +
                                "By default, this app will read from project secrets in AI Studio securely.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Paste GEMINI_API_KEY") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isSystemKeyAvailable) {
                        Text(
                            text = "✓ System secret verified. You can leave this field blank to use project credentials.",
                            color = Color.Green,
                            fontSize = 10.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showKeyDialog = false }) {
                    Text("Confirm Shield Configuration")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    apiKeyInput = ""
                    showKeyDialog = false
                }) {
                    Text("Reset Default")
                }
            }
        )
    }
}


// ==================== SCREEN 3: TRANS-TO-BRAILLE TRANSLATOR ====================
@Composable
fun TranslatorScreen(
    viewModel: BrailleVisionViewModel
) {
    val inputVal by viewModel.translatorInput.collectAsStateWithLifecycle()
    val cells by viewModel.translatedCells.collectAsStateWithLifecycle()

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Dual-Synthesizer Board",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "Key in standard English and watch instant conversion to 6-dot tactile grids.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Text input field
        OutlinedTextField(
            value = inputVal,
            onValueChange = { viewModel.updateTranslatorInput(it) },
            label = { Text("English Input Text") },
            placeholder = { Text("Type word here...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("translator_input_text_field"),
            shape = RoundedCornerShape(12.dp)
        )

        // Visual grid renderer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dynamic Tactile Render Sheet",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (cells.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Please enter valid text to synthesize cells", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    // Flowing Row layout for the cells
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cells.forEach { cell ->
                            BrailleCellRenderItem(cell = cell)
                        }
                    }
                }
            }
        }

        // Copy and Listen Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val unicodeLine = cells.map { it.unicode }.joinToString("")
                    clipboardManager.setText(AnnotatedString(unicodeLine))
                    viewModel.announceSpeech("Copied Braille unicodes to copyboard")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.CopyAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Copy Unicode Line")
                }
            }

            Button(
                onClick = { viewModel.announceSpeech("Translating text: $inputVal") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Dictate Speech")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

// FlowRow implementation for compact sizing blocks
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}

@Composable
fun BrailleCellRenderItem(cell: BrailleCell) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .width(58.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label Header character
            Text(
                text = if (cell.char == ' ') "␣" else cell.char.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))

            // 2x3 Grid Visual Matrix representing physical dots
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0..2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0..1) {
                            val dotNum = when {
                                col == 0 && row == 0 -> 1
                                col == 0 && row == 1 -> 2
                                col == 0 && row == 2 -> 3
                                col == 1 && row == 0 -> 4
                                col == 1 && row == 1 -> 5
                                col == 1 && row == 2 -> 6
                                else -> 0
                            }
                            val isFilled = cell.dots.contains(dotNum)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            // Large unicode
            Text(
                text = cell.unicode.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// ==================== SCREEN 4: TACTILE ASSISTIVE KEYPAD ====================
@Composable
fun KeypadScreen(
    viewModel: BrailleVisionViewModel
) {
    val activeDots by viewModel.keypadActiveDots.collectAsStateWithLifecycle()
    val composedBraille by viewModel.keypadComposedBraille.collectAsStateWithLifecycle()
    val composedText by viewModel.keypadComposedText.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "6-Dot Assistive Board",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "Tap dot nodes to formulate cells physically with vocal cues.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Massive visual keyboard layout (The tactile interface from BrailleNext)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tactile Active Cell Builder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 2x3 Massive Touch Nodes Row Column layout
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column (Dots 1, 2, 3)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TouchNodeButton(dot = 1, isActive = activeDots.contains(1), onClick = { viewModel.toggleKeypadDot(1) })
                        TouchNodeButton(dot = 2, isActive = activeDots.contains(2), onClick = { viewModel.toggleKeypadDot(2) })
                        TouchNodeButton(dot = 3, isActive = activeDots.contains(3), onClick = { viewModel.toggleKeypadDot(3) })
                    }

                    // Right Column (Dots 4, 5, 6)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TouchNodeButton(dot = 4, isActive = activeDots.contains(4), onClick = { viewModel.toggleKeypadDot(4) })
                        TouchNodeButton(dot = 5, isActive = activeDots.contains(5), onClick = { viewModel.toggleKeypadDot(5) })
                        TouchNodeButton(dot = 6, isActive = activeDots.contains(6), onClick = { viewModel.toggleKeypadDot(6) })
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                // Text representation
                val currentLetter = BrailleTranslator.dotsToChar(activeDots)
                Text(
                    text = "Live cell shape: [ ${activeDots.sorted().joinToString(", ")} ]" +
                            if (activeDots.isNotEmpty()) " → character '${currentLetter}'" else "",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active control keys: Add cell, space cell, clear cell
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.submitKeypadCell() },
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("keypad_submit_cell_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Text("Add Cell", fontWeight = FontWeight.Black)
                }
            }

            Button(
                onClick = { 
                    viewModel.announceSpeech("Space character")
                    // Submit a space
                    viewModel.toggleKeypadDot(0) // clears active dots or signals zero
                    viewModel.updateTranslatorInput(viewModel.translatorInput.value + " ") // space support
                    // Add space unicode to composed braille
                    viewModel.submitKeypadCell() // will add space
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("keypad_space_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Space ␣")
            }
        }

        // Board Reader Output
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Formulated Sentence String Bar:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (composedText.isNotEmpty()) composedText else "NO CELLS FORMULATED",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (composedText.isNotEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    IconButton(
                        onClick = { viewModel.deleteLastKeypadCell() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Backspace, contentDescription = "delete cell", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Text(
                    text = if (composedBraille.isNotEmpty()) composedBraille else "⠀⠀⠀",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Voice Reader and Storage integration triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.clearKeypad() },
                modifier = Modifier.weight(1.2f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.error)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Clear Board", fontSize = 12.sp)
                }
            }

            Button(
                onClick = { viewModel.announceSpeech("Declaimed readout: $composedText") },
                modifier = Modifier.weight(1.5f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Read Out Loud", fontSize = 12.sp)
                }
            }

            Button(
                onClick = { viewModel.saveManualTranscriptionToHistory() },
                modifier = Modifier.weight(1.5f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Save Log", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun TouchNodeButton(
    dot: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                if (isActive) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 2.dp,
                color = if (isActive) Color.White else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("keypad_dot_${dot}_button"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$dot",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isActive) Color.Black else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "dot",
                fontSize = 10.sp,
                color = if (isActive) Color.Black.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ==================== SCREEN 5: LOGS & LIBRARY ====================
@Composable
fun LibraryScreen(
    viewModel: BrailleVisionViewModel
) {
    val scans by viewModel.filteredScans.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val categories = listOf("All", "Commercial", "Safety", "Tactile", "General")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Historic Braille Library",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "Re-examine saved OBR scans, interactive compositions and bookmarks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Direct search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search Scans") },
            placeholder = { Text("E.g. room four, exit...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        // Horizontal Category Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.updateSelectedCategory(cat) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Danger Action: Clear All logs
        if (scans.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear History Log", fontSize = 12.sp)
                }
            }
        }

        // Lazy historic log rendering
        Box(modifier = Modifier.weight(1f)) {
            if (scans.isEmpty()) {
                // Beautiful empty library state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Empty folder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Library Folder Empty",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Any OBR scanned sheets or tactile board keys saved will appear here.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = scans,
                        key = { it.id }
                    ) { item ->
                        LibraryLogCard(item = item, viewModel = viewModel)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun OriginalImageThumbnail(sourceImageName: String?, brailleCode: String) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (sourceImageName != null && sourceImageName.startsWith("content://")) {
            // It's a custom uploaded image! Show a visual photo metadata layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoLibrary,
                    contentDescription = "Original Uploaded Image",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "UPLOADED",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = "IMG SOURCE",
                    fontSize = 7.sp,
                    color = Color.LightGray,
                    maxLines = 1
                )
            }
        } else {
            // It's a sample preset (s1, s2, s3, s4) or keypad manual entry
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                val icon = when (sourceImageName) {
                    "s1" -> Icons.Filled.Elevator
                    "s2" -> Icons.Filled.Warning
                    "s3" -> Icons.Filled.Star
                    "s4" -> Icons.Filled.Book
                    "keypad_board" -> Icons.Filled.Keyboard
                    else -> Icons.Filled.Fingerprint
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Source preset type",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Small preview of Braille dots
                Text(
                    text = if (brailleCode.isNotEmpty()) brailleCode.take(2) else "⠀",
                    fontSize = 12.sp,
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun LibraryLogCard(
    item: ScanEntity,
    viewModel: BrailleVisionViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_log_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header date detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = item.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val formatedDate = remember(item.timestamp) {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(item.timestamp))
                    }
                    Text(text = formatedDate, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Delete shortcut button
                IconButton(
                    onClick = { viewModel.deleteScan(item.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = "Delete record", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body featuring original image thumbnail next to decoded translation info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left thumbnail representation
                OriginalImageThumbnail(sourceImageName = item.sourceImageName, brailleCode = item.brailleCode)

                // Right text results
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = item.textResult.uppercase(Locale.getDefault()),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.brailleCode,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 10.dp))

            // Action line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmark heartbeat toggle
                IconButton(
                    onClick = { viewModel.toggleBookmark(item) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isBookmarked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Bookmark toggle",
                        tint = if (item.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Narrate Button
                Button(
                    onClick = { viewModel.announceSpeech("Readout from history: ${item.textResult}") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), contentColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(12.dp))
                        Text("Vocalize", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
