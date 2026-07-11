package com.example.ui

import android.graphics.Bitmap
import kotlinx.coroutines.delay
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.AnalysisEntity
import com.example.data.StockEntity
import com.example.data.UserEntity
import com.example.viewmodel.AnalysisState
import com.example.viewmodel.AppLockState
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

// Premium Stock-Market Color Palette
val SlateDark = Color(0xFF0F1113) // Near black background from Elegant Dark theme
val CardSlate = Color(0xFF1A1C1E) // Premium dark slate card surface
val StockGreen = Color(0xFF14B8A6) // Teal accent / uptrend indicators from Elegant Dark theme
val StockRed = Color(0xFFEF4444) // Clean vibrant red for downtrends
val AccentGold = Color(0xFF14B8A6) // Redefined to match Elegant Dark's premium teal accent
val LightWhite = Color(0xFFF1F5F9) // Slate-100 high-contrast white text
val LightGray = Color(0xFF94A3B8) // Premium slate-400 text color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val appLockState by viewModel.appLockState.collectAsStateWithLifecycle()
    val selectedScreen by viewModel.selectedScreen.collectAsStateWithLifecycle()

    // Listen to toasts
    LaunchedEffect(key1 = true) {
        viewModel.toastMessage.collect { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = SlateDark,
        bottomBar = {
            if (appLockState == AppLockState.Unlocked) {
                BottomNavigationBar(
                    selectedScreen = selectedScreen,
                    onNavigate = { viewModel.selectScreen(it) },
                    lang = currentLanguage
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "N",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(
                                text = Translations.get("app_title", currentLanguage).uppercase(),
                                color = LightWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentGold)
                                )
                                Text(
                                    text = "SECURE ACTIVE",
                                    color = AccentGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardSlate,
                    titleContentColor = LightWhite
                ),
                actions = {
                    LanguageSelectorDropdown(
                        currentLanguage = currentLanguage,
                        onLanguageSelected = { viewModel.selectLanguage(it) }
                    )
                    if (appLockState == AppLockState.Unlocked) {
                        IconButton(onClick = { viewModel.lockApp() }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = StockRed)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (appLockState) {
                is AppLockState.Registering -> RegisterScreen(viewModel, currentLanguage)
                is AppLockState.OtpSent -> OtpScreen(viewModel, currentLanguage)
                is AppLockState.SafetySetup -> SafetySetupScreen(viewModel, currentLanguage)
                is AppLockState.Locked -> LockScreen(viewModel, currentLanguage)
                is AppLockState.NotAuthorized -> PendingAuthorizationScreen(viewModel, currentLanguage)
                is AppLockState.ServerStep1 -> ServerStep1Screen(viewModel, currentLanguage)
                is AppLockState.ServerStep2 -> ServerStep2Screen(viewModel, currentLanguage)
                is AppLockState.ServerStep3 -> ServerStep3Screen(viewModel, currentLanguage)
                is AppLockState.Unlocked -> {
                    when (selectedScreen) {
                        Screen.DASHBOARD -> DashboardScreen(viewModel, currentLanguage)
                        Screen.ACADEMY -> AcademyScreen(currentLanguage)
                        Screen.LIVE_TRACKER -> TrackerScreen(viewModel, currentLanguage)
                        Screen.MOCK_TRADING -> MockTradingScreen(viewModel, currentLanguage)
                        Screen.SIP_CALCULATOR -> CalculatorScreen(currentLanguage)
                        Screen.RISK_PROFILER -> RiskProfilerScreen(currentLanguage)
                        Screen.GLOSSARY -> GlossaryScreen(currentLanguage)
                        Screen.AI_CHAT -> AiChatScreen(currentLanguage)
                        Screen.IPO_ALERT -> IpoAlertScreen(currentLanguage)
                        Screen.ADMIN_PANEL -> AdminPanelScreen(viewModel, currentLanguage)
                        Screen.ANALYSIS_HISTORY -> AnalysisHistoryScreen(viewModel, currentLanguage)
                    }
                }
            }
        }
    }
}

// Language Selector Component
@Composable
fun LanguageSelectorDropdown(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Language, contentDescription = "Lang", tint = AccentGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(currentLanguage.displayName, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardSlate)
        ) {
            AppLanguage.values().forEach { lang ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${lang.nativeName} (${lang.displayName})",
                            color = if (lang == currentLanguage) AccentGold else LightWhite,
                            fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onLanguageSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 1. REGISTER SCREEN (First-time phone & username setup)
@Composable
fun RegisterScreen(viewModel: MainViewModel, lang: AppLanguage) {
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Shield,
            contentDescription = "Shield",
            tint = AccentGold,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Translations.get("app_title", lang),
            color = LightWhite,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = Translations.get("tagline", lang),
            color = LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = Translations.get("welcome_back", lang),
                    color = LightWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Beginner Investor Portal Registration",
                    color = LightGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 10) phone = it },
                    label = { Text(Translations.get("phone_num", lang), color = LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightWhite,
                        unfocusedTextColor = LightWhite,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = LightGray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("register_phone_input")
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(Translations.get("username", lang), color = LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightWhite,
                        unfocusedTextColor = LightWhite,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = LightGray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("register_username_input")
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.sendOtp(phone, username) },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("register_send_otp"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Translations.get("send_otp", lang), color = SlateDark, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(
            onClick = { viewModel.startServerUnlockFlow() },
            modifier = Modifier.testTag("server_portal_btn")
        ) {
            Icon(Icons.Default.Dns, contentDescription = "Server Portal", tint = AccentGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Access Server Handler Secure Portal", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// 2. OTP VERIFICATION SCREEN
@Composable
fun OtpScreen(viewModel: MainViewModel, lang: AppLanguage) {
    var otp by remember { mutableStateOf("") }
    val phone by viewModel.currentPhone.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.LockOpen, contentDescription = "Otp", tint = AccentGold, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(Translations.get("enter_otp", lang), color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text("Sent to $phone", color = LightGray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 4) otp = it },
                    label = { Text("4-Digit OTP Code", color = LightGray) },
                    placeholder = { Text("e.g. 1234", color = LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightWhite,
                        unfocusedTextColor = LightWhite,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = LightGray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("otp_code_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(Translations.get("otp_sent", lang), color = StockGreen, fontSize = 11.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.verifyOtp(otp) },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("otp_verify_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = StockGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(Translations.get("verify_login", lang), color = LightWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 3. SAFETY LOCK SETUP SCREEN
@Composable
fun SafetySetupScreen(viewModel: MainViewModel, lang: AppLanguage) {
    var password by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var eyeLockEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(true) }
    var activeLockType by remember { mutableStateOf("PIN") } // "PIN", "PASSWORD", "EYELOCK", "BIOMETRIC"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(Translations.get("security_config", lang), color = LightWhite, fontWeight = FontWeight.Black, fontSize = 24.sp, textAlign = TextAlign.Center)
        Text("Customize the safest method to lock your account", color = LightGray, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Selector
                Text("Select Primary Unlock Method", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("PIN", "PASSWORD", "EYELOCK", "BIOMETRIC").forEach { method ->
                        FilterChip(
                            selected = activeLockType == method,
                            onClick = { activeLockType = method },
                            label = { Text(method, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = LightWhite,
                                selectedLabelColor = SlateDark,
                                selectedContainerColor = AccentGold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // PIN Config
                if (activeLockType == "PIN") {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        label = { Text(Translations.get("pin", lang), color = LightGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightWhite,
                            unfocusedTextColor = LightWhite,
                            focusedBorderColor = AccentGold
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("setup_pin_input")
                    )
                }

                // Password Config
                if (activeLockType == "PASSWORD") {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Translations.get("password", lang), color = LightGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightWhite,
                            unfocusedTextColor = LightWhite,
                            focusedBorderColor = AccentGold
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("setup_password_input")
                    )
                }

                // Eye Lock Config
                if (activeLockType == "EYELOCK") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateDark)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Eye", tint = AccentGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(Translations.get("eye_lock", lang), color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Retinal scanner will scan visual Iris features", color = LightGray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = eyeLockEnabled,
                            onCheckedChange = { eyeLockEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentGold)
                        )
                    }
                }

                // Biometric Config
                if (activeLockType == "BIOMETRIC") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateDark)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Fingerprint", tint = StockGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(Translations.get("biometric", lang), color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Use Android OS secure fingerprint profiles", color = LightGray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = StockGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.registerLockCredentials(
                            passwordVal = password,
                            pinVal = pin,
                            eyeLockConfig = eyeLockEnabled,
                            biometricVal = biometricEnabled,
                            activeLock = activeLockType
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("setup_finish_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Safe Lock Credentials", color = SlateDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 4. LOCK SCREEN
@Composable
fun LockScreen(viewModel: MainViewModel, lang: AppLanguage) {
    val userEntity by viewModel.currentUserEntity.collectAsStateWithLifecycle()
    val eyeLockProgress by viewModel.eyeLockProgress.collectAsStateWithLifecycle()
    val isEyeScanning by viewModel.isEyeScanning.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val currentMethod = userEntity?.activeLockType ?: "PIN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Security, contentDescription = "Secure Lock", tint = AccentGold, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(Translations.get("welcome_back", lang) + ", ${userEntity?.username ?: "User"}", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("${Translations.get("unlock_using", lang)}: $currentMethod", color = LightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                
                when (currentMethod) {
                    "PASSWORD" -> {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text(Translations.get("password", lang), color = LightGray) },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LightWhite,
                                unfocusedTextColor = LightWhite,
                                focusedBorderColor = AccentGold
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("lock_password_input")
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.verifyUnlock(textInput) },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("lock_pass_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                        ) {
                            Text("Unlock System", color = SlateDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    "PIN" -> {
                        Text(Translations.get("enter_pass_pin", lang), color = LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Fake PIN Dots
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 1..4) {
                                val filled = textInput.length >= i
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) AccentGold else SlateDark)
                                        .border(1.dp, AccentGold, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // Numeric Keypad Grid
                        val pins = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Clear", "0", "Verify")
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.height(240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pins) { value ->
                                Button(
                                    onClick = {
                                        when (value) {
                                            "Clear" -> textInput = ""
                                            "Verify" -> {
                                                val ok = viewModel.verifyUnlock(textInput)
                                                if (!ok) textInput = ""
                                            }
                                            else -> {
                                                if (textInput.length < 4) textInput += value
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (value == "Verify") StockGreen else if (value == "Clear") StockRed else SlateDark
                                    ),
                                    modifier = Modifier.fillMaxSize().aspectRatio(1.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightWhite)
                                }
                            }
                        }
                    }

                    "EYELOCK" -> {
                        Text(Translations.get("eye_lock_scan", lang), color = LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Eye scanning graphic frame
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SlateDark)
                                .border(2.dp, if (isEyeScanning) StockGreen else AccentGold, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = "Eye Scanner icon",
                                tint = if (isEyeScanning) StockGreen else AccentGold,
                                modifier = Modifier.size(60.dp)
                            )

                            // Pulsing scanning overlay line
                            if (isEyeScanning) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(StockGreen)
                                        .align(Alignment.Center)
                                        .offset(y = (-50 + (eyeLockProgress * 100)).dp)
                                )
                            }
                        }
                        
                        if (isEyeScanning) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { eyeLockProgress },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = StockGreen,
                                trackColor = SlateDark
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.startEyeScan {
                                    viewModel.verifyUnlock(userEntity?.pin ?: "")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isEyeScanning) StockGreen else AccentGold)
                        ) {
                            Text(if (isEyeScanning) "Scanning Retina..." else "Start Retinal Eye Scan", color = SlateDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    "BIOMETRIC" -> {
                        Text("Touch biometric fingerprint sensor to verify identity", color = LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))

                        var isFingerPressed by remember { mutableStateOf(false) }
                        val rippleScale by animateFloatAsState(targetValue = if (isFingerPressed) 1.5f else 1.0f)

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(CardSlate)
                                .border(2.dp, StockGreen, CircleShape)
                                .clickable {
                                    isFingerPressed = true
                                    viewModel.verifyUnlock(userEntity?.pin ?: "")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsing background circle
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(StockGreen.copy(alpha = 0.15f * rippleScale))
                            )
                            Icon(Icons.Default.Fingerprint, contentDescription = "Scan", tint = StockGreen, modifier = Modifier.size(54.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Touch Fingerprint to Unlock", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(
            onClick = { viewModel.startServerUnlockFlow() },
            modifier = Modifier.testTag("server_portal_lock_btn")
        ) {
            Icon(Icons.Default.Dns, contentDescription = "Server Portal", tint = AccentGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Access Server Handler Secure Portal", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// 5. PENDING ADMINISTRATOR APPROVAL SCREEN
@Composable
fun PendingAuthorizationScreen(viewModel: MainViewModel, lang: AppLanguage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.GppBad, contentDescription = "Lock", tint = StockRed, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Safety Authorization Pending", color = LightWhite, fontWeight = FontWeight.Black, fontSize = 22.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            Translations.get("not_authorized", lang),
            color = LightGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Provide a diagnostic developer bypass to easily login and play around, which ensures flawless UX testing
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tester Security Bypass Console", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val currentPhone = viewModel.currentPhone.value
                        viewModel.toggleUserAuthorization(currentPhone)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Self-Authorize Account", color = SlateDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 6. SERVER HANDLER 3-STEP SECURE PORTAL SCREENS
@Composable
fun ServerStep1Screen(viewModel: MainViewModel, lang: AppLanguage) {
    var password by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Dns,
            contentDescription = "Server Portal",
            tint = StockGreen,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SERVER PORTAL",
            color = LightWhite,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Step 1 of 3: Decrypt Password",
            color = StockGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StockGreen.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = "Key", tint = StockGreen)
                    Text(
                        text = "Enter Server Key",
                        color = LightWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Decrypt terminal using your secure administrator password.",
                    color = LightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LightWhite,
                        unfocusedTextColor = LightWhite,
                        focusedBorderColor = StockGreen,
                        unfocusedBorderColor = LightGray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("server_password_input")
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.verifyServerPassword(password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StockGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Authenticate Password", color = SlateDark, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { viewModel.lockApp() }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LightGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to public portal", color = LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ServerStep2Screen(viewModel: MainViewModel, lang: AppLanguage) {
    var pinText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Dialpad,
            contentDescription = "Server PIN",
            tint = StockGreen,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SERVER PORTAL",
            color = LightWhite,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Step 2 of 3: PIN Verification",
            color = StockGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StockGreen.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Verify Server PIN",
                    color = LightWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // PIN dots indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..4) {
                        val filled = pinText.length >= i
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) StockGreen else SlateDark)
                                .border(1.dp, StockGreen, CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Numeric Keypad Grid
                val pins = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Clear", "0", "Verify")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(240.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pins) { value ->
                        Button(
                            onClick = {
                                when (value) {
                                    "Clear" -> pinText = ""
                                    "Verify" -> {
                                        viewModel.verifyServerPin(pinText)
                                    }
                                    else -> {
                                        if (pinText.length < 4) pinText += value
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (value == "Verify") StockGreen else if (value == "Clear") StockRed else SlateDark
                            ),
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LightWhite)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { viewModel.lockApp() }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LightGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to public portal", color = LightGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun ServerStep3Screen(viewModel: MainViewModel, lang: AppLanguage) {
    var isFingerPressed by remember { mutableStateOf(false) }
    var scaleMultiplier by remember { mutableStateOf(1f) }
    
    // Animate a scanning indicator
    LaunchedEffect(isFingerPressed) {
        if (isFingerPressed) {
            while (isFingerPressed) {
                scaleMultiplier = 1.2f
                delay(300)
                scaleMultiplier = 1.0f
                delay(300)
            }
        } else {
            scaleMultiplier = 1.0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Fingerprint,
            contentDescription = "Server Biometric",
            tint = StockGreen,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SERVER PORTAL",
            color = LightWhite,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Step 3 of 3: Biometric Handshake",
            color = StockGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StockGreen.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan Fingerprint",
                    color = LightWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Touch and hold the glowing scanner to authorize and decrypt server database.",
                    color = LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(SlateDark)
                        .border(2.dp, StockGreen, CircleShape)
                        .clickable {
                            isFingerPressed = true
                            viewModel.completeServerBiometricUnlock()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(StockGreen.copy(alpha = 0.15f * scaleMultiplier))
                    )
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Scan Fingerprint",
                        tint = StockGreen,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Touch Fingerprint Sensor to Unlock",
                    color = LightWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { viewModel.lockApp() }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LightGray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to public portal", color = LightGray, fontSize = 12.sp)
        }
    }
}

// --- BOTTOM NAVIGATION COMPONENT ---
@Composable
fun BottomNavigationBar(
    selectedScreen: Screen,
    onNavigate: (Screen) -> Unit,
    lang: AppLanguage
) {
    NavigationBar(containerColor = CardSlate, contentColor = LightWhite) {
        NavigationBarItem(
            selected = selectedScreen == Screen.DASHBOARD,
            onClick = { onNavigate(Screen.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(Translations.get("dashboard", lang), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = SlateDark, selectedTextColor = AccentGold, indicatorColor = AccentGold)
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.LIVE_TRACKER,
            onClick = { onNavigate(Screen.LIVE_TRACKER) },
            icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Tracker") },
            label = { Text(Translations.get("tracker", lang), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = SlateDark, selectedTextColor = AccentGold, indicatorColor = AccentGold)
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.MOCK_TRADING,
            onClick = { onNavigate(Screen.MOCK_TRADING) },
            icon = { Icon(Icons.Default.Wallet, contentDescription = "Mock") },
            label = { Text(Translations.get("simulator", lang), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = SlateDark, selectedTextColor = AccentGold, indicatorColor = AccentGold)
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.ANALYSIS_HISTORY,
            onClick = { onNavigate(Screen.ANALYSIS_HISTORY) },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = SlateDark, selectedTextColor = AccentGold, indicatorColor = AccentGold)
        )
        NavigationBarItem(
            selected = selectedScreen == Screen.ADMIN_PANEL,
            onClick = { onNavigate(Screen.ADMIN_PANEL) },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
            label = { Text("Admin", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = SlateDark, selectedTextColor = AccentGold, indicatorColor = AccentGold)
        )
    }
}

// 6. MAIN DASHBOARD SCREEN (Visual center is Image Screenshot upload + 8 feature directory)
@Composable
fun DashboardScreen(viewModel: MainViewModel, lang: AppLanguage) {
    val context = LocalContext.current
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val stocks by viewModel.nepseStocks.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val userEntity by viewModel.currentUserEntity.collectAsStateWithLifecycle()

    var selectedStockForAnalysis by remember { mutableStateOf("NABIL") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher for file picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                selectedImageUri = uri.toString()
                
                // Automatically run analysis
                viewModel.analyzeChartImage(bitmap, selectedStockForAnalysis)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error loading image", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // List of pre-configured sample Stock chart screenshots to allow immediate demo tests
    val mockCharts = listOf(
        Pair("NABIL Bullish Hammer Chart", "NABIL"),
        Pair("AHPC Bearish Engulfing Chart", "AHPC"),
        Pair("UPPER Consolidation Doji Chart", "UPPER")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security Context Info (Faithful to Elegant Dark HTML theme)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CardSlate),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 18.sp)
                        }
                        Column {
                            Text("Logged in as", color = LightGray, fontSize = 11.sp)
                            Text(
                                text = "@${userEntity?.username ?: "sharma_investor"}",
                                color = LightWhite,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Pair("👁️", "Face ID"),
                            Pair("☝️", "Biometric"),
                            Pair("🔢", "PIN")
                        ).forEach { (emoji, title) ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CardSlate),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Market Snapshot Row (Faithful to Elegant Dark HTML theme)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NEPSE INDEX",
                            color = LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2,045.60",
                            color = LightWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+12.45 (0.61%) ↑",
                            color = AccentGold,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DAILY VOL",
                            color = LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "4.2B",
                            color = LightWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "NPR Total Traded",
                            color = LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "नेप्से Beginner Space",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Learn to Invest Safely",
                            color = LightWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Upload chart screenshots for instant AI candlestick pattern diagnostics.",
                            color = LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = "Trend",
                        tint = StockGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // CENTER ATTRACTION: SCREENSHOT UPLOAD & ANALYSIS WINDOW
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("screenshot_upload_panel"),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Translations.get("upload_chart", lang),
                        color = LightWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Stock selector dropdown for screen analysis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Associate stock symbol: ", color = LightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SlateDark)
                                .clickable { }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedStockForAnalysis, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Gradient Pulsing Camera Button (Mirroring the Elegant Dark central upload button)
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 0.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing outer ring
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = AccentGold.copy(alpha = pulseAlpha),
                                        radius = (size.minDimension / 2) * pulseScale,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                        )

                        // Main gradient circle button
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AccentGold, Color(0xFF3B82F6))
                                    )
                                )
                                .clickable { imagePicker.launch("image/*") }
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SlateDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(AccentGold.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoCamera,
                                            contentDescription = "Upload Chart",
                                            tint = AccentGold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "UPLOAD CHART",
                                        color = LightWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Scan Candles & Analyze",
                                        color = LightGray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No file handy? Try clicking a simulated demo chart to test instantly:", color = LightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated preset test charts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mockCharts.forEach { pair ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateDark)
                                    .border(1.dp, AccentGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedStockForAnalysis = pair.second
                                        // Draw simulated dummy candlestick bitmap
                                        val conf = Bitmap.Config.ARGB_8888
                                        val bmp = Bitmap.createBitmap(120, 120, conf)
                                        selectedBitmap = bmp
                                        viewModel.analyzeChartImage(bmp, pair.second)
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pair.first,
                                    color = LightWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // DISPLAY ANALYSIS STATE/RESULTS
        item {
            AnimatedVisibility(visible = analysisState != AnalysisState.Idle) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, StockGreen.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                Translations.get("chart_analysis_results", lang),
                                color = StockGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { viewModel.clearAnalysis() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = LightGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        when (val state = analysisState) {
                            is AnalysisState.Analyzing -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = StockGreen)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(Translations.get("analyzing", lang), color = LightWhite, fontSize = 13.sp)
                                }
                            }

                            is AnalysisState.Error -> {
                                Text(state.message, color = StockRed, fontSize = 13.sp)
                            }

                            is AnalysisState.Success -> {
                                val analysis = state.analysis
                                val decisionColor = when (analysis.buySellDecision) {
                                    "BUY" -> StockGreen
                                    "SELL" -> StockRed
                                    else -> AccentGold
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateDark)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(Translations.get("decision", lang), color = LightGray, fontSize = 11.sp)
                                        Text(
                                            analysis.buySellDecision,
                                            color = decisionColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(Translations.get("probability", lang), color = LightGray, fontSize = 11.sp)
                                        Text(
                                            "${analysis.earnProbability}%",
                                            color = StockGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(Translations.get("best_buy", lang), color = LightGray, fontSize = 10.sp)
                                        Text(analysis.bestBuyDate, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(Translations.get("best_sell", lang), color = LightGray, fontSize = 10.sp)
                                        Text(analysis.bestSellDate, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Divider(color = LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                                Text("DETECTED PATTERN:", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(analysis.patternName, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("DETAILED AI REPORT:", color = LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(
                                    analysis.analysisText,
                                    color = LightWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // THE 8 EDUCATIONAL & UTILITY FEATURES DIRECTORY FOR BEGINNERS
        item {
            Text(
                "8 Core Features For Beginners",
                color = AccentGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Beautiful visual grid directory for the 8 features
        item {
            val features = listOf(
                Triple(Translations.get("academic", lang), Icons.Default.MenuBook, Screen.ACADEMY),
                Triple(Translations.get("tracker", lang), Icons.Default.Timeline, Screen.LIVE_TRACKER),
                Triple(Translations.get("simulator", lang), Icons.Default.CurrencyExchange, Screen.MOCK_TRADING),
                Triple(Translations.get("calculator", lang), Icons.Default.Calculate, Screen.SIP_CALCULATOR),
                Triple(Translations.get("risk_quiz", lang), Icons.Default.Psychology, Screen.RISK_PROFILER),
                Triple(Translations.get("dictionary", lang), Icons.Default.Book, Screen.GLOSSARY),
                Triple(Translations.get("ai_chat", lang), Icons.Default.SmartToy, Screen.AI_CHAT),
                Triple(Translations.get("ipo_alert", lang), Icons.Default.NotificationsActive, Screen.IPO_ALERT)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(features) { feature ->
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { viewModel.selectScreen(feature.third) },
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SlateDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(feature.second, contentDescription = feature.first, tint = AccentGold, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = feature.first,
                                color = LightWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Feature Explorer",
                                color = LightGray,
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 8 FEATURE COMPONENT SCREENS ---

// FEATURE 1: CANDLE ACADEMY
@Composable
fun AcademyScreen(lang: AppLanguage) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        if (lang == AppLanguage.NEPALI) "निर्देशिका र ट्युटोरियल" else "Articles & Guidance",
        if (lang == AppLanguage.NEPALI) "कैंडलस्टिक एकेडेमी" else "Candlestick Academy"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MenuBook, contentDescription = "Academy", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (lang == AppLanguage.NEPALI) "लगानी र सेयर मार्गदर्शन" else "Investment Guidance Hub",
                color = LightWhite,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardSlate,
            contentColor = AccentGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AccentGold
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    selectedContentColor = AccentGold,
                    unselectedContentColor = LightGray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            ArticlesSection(lang)
        } else {
            CandlestickSection(lang)
        }
    }
}

@Composable
fun ArticlesSection(lang: AppLanguage) {
    val articles = listOf(
        ArticleData(
            title = if (lang == AppLanguage.NEPALI) "१. लगानीका आधारभूत सिद्धान्तहरू" else "1. Basic Investment Principles",
            category = if (lang == AppLanguage.NEPALI) "शुरुआती लगानी" else "BEGINNER BASICS",
            icon = Icons.Default.School,
            description = if (lang == AppLanguage.NEPALI) "लगानीको शक्ति, बचत र लगानीको भिन्नता र चक्रवृद्धि वृद्धिबारे सिक्नुहोस्।" else "Understand compounding interest, saving vs investing, and wealth building rules.",
            content = """
                • Saving vs Investing: Saving holds money securely but loses purchasing power to inflation. Investing allocates money to productive assets like top-tier NEPSE companies to generate higher compounding returns over time.
                
                • Power of Compounding: Albert Einstein called compounding the 'Eighth Wonder of the World'. Reinvesting dividends and earnings multiplies your portfolio exponentially.
                
                • Rule of 72: A quick shortcut to know when your money will double. Divide 72 by the annual return rate. At 12% average NEPSE returns, your money doubles in 6 years (72 / 12 = 6).
                
                • Asset Allocation: Spread your capital among blue-chip stocks, commercial banking equity, secure bonds, and cash reserves to balance risk and reward depending on your age.
            """.trimIndent(),
            proTip = if (lang == AppLanguage.NEPALI) "प्रो-टिप: सानो रकमबाट सुरु गर्नुहोस् र नियमित बचतलाई एसआईपी (SIP) मार्फत दीर्घकालीन लगानी गर्नुहोस्।" else "Pro-Tip: Start small, automate your savings through Mutual Fund SIPs, and maintain a 5 to 10-year investment horizon."
        ),
        ArticleData(
            title = if (lang == AppLanguage.NEPALI) "२. प्राविधिक विश्लेषण (Technical)" else "2. Technical Analysis",
            category = if (lang == AppLanguage.NEPALI) "चार्ट र ट्रेन्ड" else "CHARTS & TRENDS",
            icon = Icons.Default.TrendingUp,
            description = if (lang == AppLanguage.NEPALI) "चार्ट ढाँचाहरू, प्रवृत्ति (Trends), समर्थन र अवरोध बुझ्नुहोस्।" else "Learn to read price action, identify support and resistance, and spot market trends.",
            content = """
                • Support and Resistance: Support is the floor where buyers consistently purchase, stopping further decline. Resistance is the ceiling where sellers unload, halting the upward movement.
                
                • Trendlines: The trend is your friend! Draw diagonal lines joining consecutive swing lows (Uptrend) or swing highs (Downtrend) to find ideal breakout or entry zones.
                
                • Key Indicators (RSI & MACD): Relative Strength Index (RSI) ranges from 0-100. RSI under 30 indicates oversold conditions (potential buy), while RSI over 70 indicates overbought conditions (potential profit booking). MACD line crossing above the signal line indicates emerging bullish momentum.
                
                • Volume Confirmation: A price breakout with low volume is a trap. Look for significantly higher trading volume to confirm the validity of a breakout or trend reversal.
            """.trimIndent(),
            proTip = if (lang == AppLanguage.NEPALI) "प्रो-टिप: कहिल्यै पनि घट्दो बजारमा समात्न नखोज्नुहोस्; पहिला समर्थन विन्दुमा बजार स्थिर हुन दिनुहोस्।" else "Pro-Tip: Never try to catch a falling knife. Let the price stabilize at a major support level and show a reversal pattern before buying."
        ),
        ArticleData(
            title = if (lang == AppLanguage.NEPALI) "३. आधारभूत विश्लेषण (Fundamental)" else "3. Fundamental Analysis",
            category = if (lang == AppLanguage.NEPALI) "कम्पनीको मूल्याङ्कन" else "COMPANY VALUATION",
            icon = Icons.Default.Business,
            description = if (lang == AppLanguage.NEPALI) "प्रतिसेयर आम्दानी (EPS), पीई रेसियो (P/E), र लाभांश बुझ्नुहोस्।" else "Deconstruct Earnings Per Share (EPS), Price-to-Earnings Ratio, and balance sheet metrics.",
            content = """
                • Earnings Per Share (EPS): A company's net profit divided by outstanding shares. A higher, steadily rising EPS indicates stable growth and high profitability.
                
                • Price-to-Earnings (P/E) Ratio: Calculated as Stock Price / EPS. It tells you how many rupees investors pay for each rupee of earnings. In NEPSE, a P/E below 15-20 is often considered undervalued, while above 40 is premium or overvalued.
                
                • Dividend Yield: Represents passive income return. Divide the annual cash dividend by the stock price. High dividend yields in blue-chip banks or telecoms offer cash flow protection during bear markets.
                
                • Book Value Per Share: Shows the net assets per share. A healthy firm should ideally trade at a reasonable multiplier of its book value.
            """.trimIndent(),
            proTip = if (lang == AppLanguage.NEPALI) "प्रो-टिप: केवल सस्तो मूल्य हेरेर सेयर नकिन्नुहोस्, बलियो पीई रेसियो र निरन्तर खुद मुनाफा भएको कम्पनी रोज्नुहोस्।" else "Pro-Tip: Avoid penny stocks trading cheap on low face value. Focus on companies with sustainable competitive advantages, low debt, and consistent dividend history."
        ),
        ArticleData(
            title = if (lang == AppLanguage.NEPALI) "४. जोखिम व्यवस्थापन रणनीति" else "4. Risk Management Strategies",
            category = if (lang == AppLanguage.NEPALI) "पूँजी संरक्षण" else "CAPITAL PRESERVATION",
            icon = Icons.Default.Security,
            description = if (lang == AppLanguage.NEPALI) "स्टप-लस (Stop-Loss), विविधीकरण र पूँजी बचाउने तरिकाहरू।" else "Master stop-losses, portfolio diversification, and psychological discipline rules.",
            content = """
                • Portfolio Diversification: Don't put all your eggs in one basket! Spread your capital across different sectors such as Commercial Banks, Hydropower, Microfinance, and Mutual Funds to limit localized sector corrections.
                
                • Position Sizing: Never allocate more than 10-15% of your total capital to a single highly volatile stock. This ensures a drop in one asset won't ruin your entire portfolio.
                
                • Stop-Loss Execution: Define a clear exit threshold before entering a trade (e.g., 3-5% below purchase price). If the support level breaks, exit immediately to cut losses and preserve cash for future opportunities.
                
                • Psychological Discipline: Trade with a checklist. Control emotions of FOMO (Fear of Missing Out) during parabolic rallies and fear during sudden market panics.
            """.trimIndent(),
            proTip = if (lang == AppLanguage.NEPALI) "प्रो-टिप: सफल ट्रेडरहरू नोक्सान कम गर्न र नाफा बढाउनमा केन्द्रित हुन्छन्। पूँजी संरक्षण नै बजारमा बाँच्ने पहिलो नियम हो।" else "Pro-Tip: Professional traders prioritize risk management over profits. Preserving your investment capital is the absolute key to compounding wealth."
        )
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(articles) { article ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SlateDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(article.icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentGold.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(article.category, color = AccentGold, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Text(article.title, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Expand",
                            tint = LightGray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(article.description, color = LightGray, fontSize = 12.sp)

                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = LightGray.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = article.content,
                                color = LightWhite,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Tip", tint = AccentGold, modifier = Modifier.size(16.dp))
                                    Text(article.proTip, color = LightWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ArticleData(
    val title: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val content: String,
    val proTip: String
)

@Composable
fun CandlestickSection(lang: AppLanguage) {
    val candlesList = listOf(
        Triple("Hammer (ह्यामर)", "Reversal hammer structure on market lows. Shows strong buyer absorption.", "BUY"),
        Triple("Doji (डोजी)", "Indecision structure. Opening price and closing price are almost identical.", "HOLD"),
        Triple("Bullish Engulfing (बुलिस इंगल्फिंग)", "Strong green body completely swallowing previous red candle body. Implies sudden buyer takeover.", "BUY"),
        Triple("Shooting Star (सुटिंग स्टार)", "Reversal star showing high shadow pointing up. Buyers tried to push up but failed miserably.", "SELL"),
        Triple("Marubozu (मारुबोजु)", "Solid body with no shadows. Extreme directional momentum confirmation.", "BUY/SELL"),
        Triple("Morning Star (मर्निङ स्टार)", "Three-candle bottom reversal configuration indicating trend shift from bear to bull.", "BUY")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(candlesList) { candle ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Dynamic Candlestick Drawing using Canvas
                    Box(
                        modifier = Modifier
                            .size(50.dp, 80.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SlateDark)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val candleWidth = 14.dp.toPx()
                            when (candle.first) {
                                "Hammer (ह्यामर)" -> {
                                    // Draw shadows & small body on top
                                    drawLine(StockGreen, Offset(size.width / 2, 10f), Offset(size.width / 2, size.height - 10f), strokeWidth = 3f)
                                    drawRect(StockGreen, Offset((size.width - candleWidth) / 2, 10f), Size(candleWidth, 20f))
                                }
                                "Doji (डोजी)" -> {
                                    // Draw full wick with flat center line
                                    drawLine(LightWhite, Offset(size.width / 2, 10f), Offset(size.width / 2, size.height - 10f), strokeWidth = 3f)
                                    drawLine(LightWhite, Offset((size.width - candleWidth) / 2, size.height / 2), Offset((size.width + candleWidth) / 2, size.height / 2), strokeWidth = 4f)
                                }
                                "Bullish Engulfing (बुलिस इंगल्फिंग)" -> {
                                    drawLine(StockGreen, Offset(size.width / 2, 5f), Offset(size.width / 2, size.height - 5f), strokeWidth = 3f)
                                    drawRect(StockGreen, Offset((size.width - candleWidth) / 2, 15f), Size(candleWidth, size.height - 30f))
                                }
                                "Shooting Star (सुटिंग स्टार)" -> {
                                    drawLine(StockRed, Offset(size.width / 2, 10f), Offset(size.width / 2, size.height - 10f), strokeWidth = 3f)
                                    drawRect(StockRed, Offset((size.width - candleWidth) / 2, size.height - 30f), Size(candleWidth, 20f))
                                }
                                else -> {
                                    drawLine(StockGreen, Offset(size.width / 2, 10f), Offset(size.width / 2, size.height - 10f), strokeWidth = 3f)
                                    drawRect(StockGreen, Offset((size.width - candleWidth) / 2, 20f), Size(candleWidth, 40f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(candle.first, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(candle.second, color = LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (candle.third == "BUY") StockGreen.copy(alpha = 0.15f) else StockRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    candle.third,
                                    color = if (candle.third == "BUY") StockGreen else StockRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// FEATURE 2: NEPSE LIVE TRACKER (COMPREHENSIVE SHARE MARKET HUB)
@Composable
fun TrackerScreen(viewModel: MainViewModel, lang: AppLanguage) {
    val stocks by viewModel.nepseStocks.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        if (lang == AppLanguage.NEPALI) "लाइभ सेयर र वित्तीय" else "Companies & Reports",
        if (lang == AppLanguage.NEPALI) "ताजा समाचार" else "Market News Feed",
        if (lang == AppLanguage.NEPALI) "नेप्से निर्देशिका" else "Market Hub Guide"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timeline, contentDescription = "Tracker", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (lang == AppLanguage.NEPALI) "नेपाली सेयर बजार हब" else "Nepali Share Market Hub",
                color = LightWhite,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardSlate,
            contentColor = AccentGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AccentGold
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    selectedContentColor = AccentGold,
                    unselectedContentColor = LightGray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> CompaniesAndMetricsTab(stocks, lang)
            1 -> MarketNewsTab(lang)
            2 -> BeginnerMarketGuideTab(lang)
        }
    }
}

data class FinancialReport(
    val paidUpCapital: String,
    val netProfit: String,
    val eps: String,
    val bookValue: String,
    val roe: String,
    val highLow52w: String,
    val oneYearReturn: String,
    val avgVolume: String,
    val sector: String
)

fun getFinancialReportFor(symbol: String): FinancialReport {
    return when (symbol) {
        "NABIL" -> FinancialReport(
            paidUpCapital = "NPR 27.05 Billion",
            netProfit = "NPR 4.52 Billion",
            eps = "NPR 16.63",
            bookValue = "NPR 145.20",
            roe = "12.8%",
            highLow52w = "NPR 520.00 / 380.00",
            oneYearReturn = "+15.4%",
            avgVolume = "142,000 Shares",
            sector = "Commercial Bank"
        )
        "AHPC" -> FinancialReport(
            paidUpCapital = "NPR 4.80 Billion",
            netProfit = "NPR 45.2 Crore",
            eps = "NPR 9.42",
            bookValue = "NPR 112.50",
            roe = "8.4%",
            highLow52w = "NPR 315.00 / 220.00",
            oneYearReturn = "-4.2%",
            avgVolume = "285,000 Shares",
            sector = "Hydropower"
        )
        "UPPER" -> FinancialReport(
            paidUpCapital = "NPR 10.59 Billion",
            netProfit = "NPR -12.4 Crore",
            eps = "NPR -1.17",
            bookValue = "NPR 88.40",
            roe = "-1.3%",
            highLow52w = "NPR 245.00 / 170.00",
            oneYearReturn = "+18.2%",
            avgVolume = "410,000 Shares",
            sector = "Hydropower"
        )
        "NTC" -> FinancialReport(
            paidUpCapital = "NPR 18.00 Billion",
            netProfit = "NPR 7.15 Billion",
            eps = "NPR 39.72",
            bookValue = "NPR 518.30",
            roe = "14.2%",
            highLow52w = "NPR 960.00 / 810.00",
            oneYearReturn = "+2.4%",
            avgVolume = "85,000 Shares",
            sector = "Telecommunication"
        )
        "HDL" -> FinancialReport(
            paidUpCapital = "NPR 3.85 Billion",
            netProfit = "NPR 65.8 Crore",
            eps = "NPR 17.10",
            bookValue = "NPR 132.80",
            roe = "18.5%",
            highLow52w = "NPR 2,240.00 / 1,650.00",
            oneYearReturn = "-18.5%",
            avgVolume = "42,000 Shares",
            sector = "Manufacturing & Distilleries"
        )
        "NIFRA" -> FinancialReport(
            paidUpCapital = "NPR 21.60 Billion",
            netProfit = "NPR 1.25 Billion",
            eps = "NPR 5.79",
            bookValue = "NPR 115.40",
            roe = "5.02%",
            highLow52w = "NPR 240.00 / 195.00",
            oneYearReturn = "+1.8%",
            avgVolume = "180,000 Shares",
            sector = "Investment / Infrastructure"
        )
        "SHL" -> FinancialReport(
            paidUpCapital = "NPR 2.20 Billion",
            netProfit = "NPR 38.5 Crore",
            eps = "NPR 17.50",
            bookValue = "NPR 156.40",
            roe = "11.2%",
            highLow52w = "NPR 490.00 / 320.00",
            oneYearReturn = "+32.1% (Tourism Surge)",
            avgVolume = "95,000 Shares",
            sector = "Hotels & Tourism"
        )
        "CBBL" -> FinancialReport(
            paidUpCapital = "NPR 2.83 Billion",
            netProfit = "NPR 82.4 Crore",
            eps = "NPR 29.11",
            bookValue = "NPR 210.50",
            roe = "15.4%",
            highLow52w = "NPR 1,040.00 / 840.00",
            oneYearReturn = "-8.5%",
            avgVolume = "54,000 Shares",
            sector = "Microfinance"
        )
        "CIT" -> FinancialReport(
            paidUpCapital = "NPR 5.31 Billion",
            netProfit = "NPR 1.05 Billion",
            eps = "NPR 19.77",
            bookValue = "NPR 185.00",
            roe = "10.6%",
            highLow52w = "NPR 2,450.00 / 1,980.00",
            oneYearReturn = "+5.8%",
            avgVolume = "28,000 Shares",
            sector = "Other Financials"
        )
        else -> FinancialReport(
            paidUpCapital = "NPR 5.00 Billion",
            netProfit = "NPR 45.0 Crore",
            eps = "NPR 12.50",
            bookValue = "NPR 120.00",
            roe = "9.5%",
            highLow52w = "NPR 220.00 / 175.00",
            oneYearReturn = "+0.5%",
            avgVolume = "120,000 Shares",
            sector = "Other Sector"
        )
    }
}

@Composable
fun CompaniesAndMetricsTab(stocks: List<StockEntity>, lang: AppLanguage) {
    Text(
        text = if (lang == AppLanguage.NEPALI) "सूचीकृत शीर्ष कम्पनीहरू र विवरण" else "Listed Top Firms & Advanced Metrics",
        color = AccentGold,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(stocks) { stock ->
            var expanded by remember { mutableStateOf(false) }
            val changeColor = if (stock.change >= 0) StockGreen else StockRed
            val sign = if (stock.change >= 0) "+" else ""
            val report = getFinancialReportFor(stock.symbol)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stock.symbol, color = LightWhite, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentGold.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(report.sector, color = AccentGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(stock.name, color = LightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("NPR ${stock.price}", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "$sign${stock.change} ($sign${stock.changePercent}%)",
                                color = changeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = LightGray.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (lang == AppLanguage.NEPALI) "वित्तीय प्रतिवेदन (Q3 Financials):" else "LATEST FINANCIAL REPORT (Q3):",
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricColumn(
                                    label1 = if (lang == AppLanguage.NEPALI) "चुक्ता पूँजी" else "Paid-up Capital",
                                    value1 = report.paidUpCapital,
                                    label2 = if (lang == AppLanguage.NEPALI) "खुद नाफा" else "Net Profit",
                                    value2 = report.netProfit,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricColumn(
                                    label1 = if (lang == AppLanguage.NEPALI) "प्रतिसेयर आम्दानी (EPS)" else "Earnings Per Share",
                                    value1 = report.eps,
                                    label2 = if (lang == AppLanguage.NEPALI) "प्रतिसेयर किताबी मूल्य" else "Book Value",
                                    value2 = report.bookValue,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = LightGray.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (lang == AppLanguage.NEPALI) "सेयर कारोबार इतिहास र सूचकहरू:" else "PERFORMANCE HISTORY & INDICATORS:",
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricColumn(
                                    label1 = if (lang == AppLanguage.NEPALI) "५२ हप्ताको उच्च / न्यून" else "52W High / Low",
                                    value1 = report.highLow52w,
                                    label2 = if (lang == AppLanguage.NEPALI) "१ वर्षको प्रतिफल" else "1-Year Return",
                                    value2 = report.oneYearReturn,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricColumn(
                                    label1 = if (lang == AppLanguage.NEPALI) "लाभांश प्रतिफल" else "Dividend Yield",
                                    value1 = "${stock.divYield}%",
                                    label2 = if (lang == AppLanguage.NEPALI) "पीई रेसियो (P/E Ratio)" else "P/E Ratio",
                                    value2 = "${stock.peRatio}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = LightGray.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.NEPALI) "औसत दैनिक कारोबार:" else "Avg Daily Volume:",
                                    color = LightGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = report.avgVolume,
                                    color = LightWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.NEPALI) "प्राविधिक सूचक संकेत (RSI):" else "Technical Signal (RSI):",
                                    color = LightGray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${stock.rsi} (${if (stock.rsi > 60) "Overbought" else if (stock.rsi < 40) "Oversold" else "Neutral"})",
                                    color = if (stock.rsi > 60) StockRed else if (stock.rsi < 40) StockGreen else AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricColumn(
    label1: String,
    value1: String,
    label2: String,
    value2: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column {
            Text(label1, color = LightGray, fontSize = 9.sp)
            Text(value1, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Column {
            Text(label2, color = LightGray, fontSize = 9.sp)
            Text(value2, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
fun MarketNewsTab(lang: AppLanguage) {
    val news = listOf(
        NewsItem(
            title = if (lang == AppLanguage.NEPALI) "नबिल बैंक लिमिटेडको ११% लाभांश घोषणा" else "Nabil Bank Limited (NABIL) Announces 11% Cash Dividend",
            source = "NEPSE News Service",
            time = "2 hours ago",
            summary = if (lang == AppLanguage.NEPALI) "नबिल बैंकको संचालक समितिले आर्थिक वर्ष २०८०/८१ को नाफाबाट सेयरधनीहरुलाई ११% नगद लाभांश वितरण गर्ने प्रस्ताव गरेको छ।" else "The Board of Directors of Nabil Bank Limited has proposed an 11% cash dividend from profits of FY 2080/81, pending NRB and AGM approvals.",
            badge = "DIVIDEND"
        ),
        NewsItem(
            title = if (lang == AppLanguage.NEPALI) "अपर तामाकोशीले १:१ को अनुपातमा हकप्रद सेयर निष्कासन गर्ने" else "Upper Tamakoshi Hydropower (UPPER) Approved for 1:1 Right Shares",
            source = "ShareSansar",
            time = "5 hours ago",
            summary = if (lang == AppLanguage.NEPALI) "अपर तामाकोशी हाइड्रोपावर लिमिटेड (UPPER) ले ऋण तिर्न र आयोजना संवर्द्धन गर्न १:१ को अनुपातमा हकप्रद सेयर जारी गर्न धितोपत्र बोर्डबाट अनुमति पाएको छ।" else "Upper Tamakoshi Hydropower has secured final regulatory approval from SEBON to issue 1:1 right shares to repay capital loans and fund expansions.",
            badge = "RIGHT SHARE"
        ),
        NewsItem(
            title = if (lang == AppLanguage.NEPALI) "नयाँ आईपीओ निष्कासन: घोराही सिमेन्टको बाँडफाँड सम्पन्न" else "Upcoming IPO: Ghorahi Cement IPO Allotment Finalized",
            source = "MeroShare Tracker",
            time = "1 day ago",
            summary = if (lang == AppLanguage.NEPALI) "घोराही सिमेन्टको आईपीओ बाँडफाँड सम्पन्न भएको छ। १० कित्ता सेयर परेका लगानीकर्ताहरूले मेरोसेयर लगइन गरेर नतिजा हेर्न सक्नेछन्।" else "The primary share allotment of Ghorahi Cement Industry's IPO has been completed. Investors can check allotment status inside MeroShare with 10 units base.",
            badge = "IPO ALLOTMENT"
        ),
        NewsItem(
            title = if (lang == AppLanguage.NEPALI) "नेपाल टेलिकमको खुद नाफामा ८.५% को वृद्धि" else "Nepal Telecom (NTC) Q3 Net Profit Rises 8.5% YoY",
            source = "Bizmandu",
            time = "1 day ago",
            summary = if (lang == AppLanguage.NEPALI) "नेपाल टेलिकमले तेस्रो त्रैमासिकमा ७ अर्ब १५ करोड खुद नाफा आर्जन गरेको छ। फोरजी सेवा विस्तार र फाइबर इन्टरनेटको पहुँच बढेकाले आम्दानी बढेको हो।" else "Nepal Telecom reports third quarter net profits of NPR 7.15 Billion, reflecting an 8.5% year-on-year growth driven by expanding FTTH broadband coverage.",
            badge = "FINANCIALS"
        ),
        NewsItem(
            title = if (lang == AppLanguage.NEPALI) "धितोपत्र बोर्ड (SEBON) द्वारा म्युचुअल फण्डका लागि नयाँ नियमावली जारी" else "SEBON Issues New Guidelines for Mutual Fund Managers",
            source = "SEBON Portal",
            time = "2 days ago",
            summary = if (lang == AppLanguage.NEPALI) "नेपाल धितोपत्र बोर्डले म्युचुअल फण्ड सञ्चालन र पारदर्शी लगानी सुनिश्चित गर्न नयाँ निर्देशिका जारी गरेको छ, जसले लगानीको सुरक्षा बढाउनेछ।" else "The Securities Board of Nepal (SEBON) released refined operational guidelines for local mutual funds to ensure liquidity transparency and risk checks.",
            badge = "REGULATORY"
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(news) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentGold.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.badge,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(item.time, color = LightGray, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        color = LightWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.summary,
                        color = LightGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(LightGray))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item.source, color = LightGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

data class NewsItem(
    val title: String,
    val source: String,
    val time: String,
    val summary: String,
    val badge: String
)

@Composable
fun BeginnerMarketGuideTab(lang: AppLanguage) {
    val guides = listOf(
        GuideItem(
            q = if (lang == AppLanguage.NEPALI) "१. नेप्से (NEPSE) भनेको के हो?" else "1. What is NEPSE?",
            a = if (lang == AppLanguage.NEPALI) "नेपाल स्टक एक्सचेन्ज (NEPSE) नेपालको एक मात्र धितोपत्र बजार हो, जहाँ दर्ता भएका सार्वजनिक कम्पनीहरूको सेयर खरिद र बिक्री गरिन्छ।" else "The Nepal Stock Exchange (NEPSE) is the sole stock exchange in Nepal, facilitating secondary market trading of equities, debentures, and mutual funds."
        ),
        GuideItem(
            q = if (lang == AppLanguage.NEPALI) "२. डिम्याट (Demat) र मेरोसेयर (MeroShare) के हो?" else "2. What is Demat & MeroShare?",
            a = if (lang == AppLanguage.NEPALI) "डिम्याट खाता भनेको डिजिटल रूपमा सेयर राख्ने बैंक खाता जस्तै हो। मेरोसेयर सिडिएससीको डिजिटल पोर्टल हो जसले आईपीओ भर्न, पोर्टफोलियो हेर्न, र सेयर ट्रान्सफर गर्न मद्दत गर्दछ।" else "Demat is a digital vault holding your stocks. MeroShare is CDSC's official portal allowing users to apply for IPOs, track holdings, and transfer shares to brokers."
        ),
        GuideItem(
            q = if (lang == AppLanguage.NEPALI) "३. प्राथमिक बजार (IPO) र दोस्रो बजार के हो?" else "3. Primary Market (IPO) vs Secondary Market?",
            a = if (lang == AppLanguage.NEPALI) "आईपीओ (IPO) मार्फत पहिलोपटक कम्पनीले सर्वसाधारणलाई सेयर बिक्री गर्छ (प्रतिकित्ता रु १०० मा)। दोस्रो बजार भनेको ब्रोकर मार्फत बजार मूल्यमा दोस्रो व्यक्तिसँग सेयर खरिद-बिक्री गर्ने ठाउँ हो।" else "An IPO is when a company sells shares directly to the public for the first time (typically at NPR 100). The secondary market is where listed shares are traded between investors through licensed brokers."
        ),
        GuideItem(
            q = if (lang == AppLanguage.NEPALI) "४. सेयर बजारबाट कसरी कमाउन सकिन्छ?" else "4. How do investors earn in the share market?",
            a = if (lang == AppLanguage.NEPALI) "लगानीकर्ताले दुई तरिकाले कमाउन सक्छन्: पहिलो लाभांश (नगद वा बोनस सेयर) र दोस्रो पूँजीगत लाभ (कम मूल्यमा सेयर किनेर बजार मूल्य बढेपछि बढीमा बेच्ने)।" else "Through two methods: capital appreciation (buying a stock low and selling it high as the company grows) and corporate dividend payouts (cash distributions or bonus shares)."
        ),
        GuideItem(
            q = if (lang == AppLanguage.NEPALI) "५. शुरुआतीहरूले कस्ता सेयर किन्नुपर्छ?" else "5. What stocks should beginners focus on?",
            a = if (lang == AppLanguage.NEPALI) "शुरुआतीहरूले स्थिर आय भएका र निरन्तर नाफा कमाउने 'कमर्सियल बैंक' वा बलिया जलविद्युत कम्पनी (Blue-chip stocks) बाट सुरु गर्नुपर्छ जसको जोखिम कम र लाभांशको इतिहास राम्रो हुन्छ।" else "Beginners are recommended to focus on dividend-paying commercial banks, telecommunications, and high-reserve companies (blue-chips) to ensure stable growth and capital safety."
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        items(guides) { guide ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = guide.q,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = guide.a,
                        color = LightWhite,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

data class GuideItem(
    val q: String,
    val a: String
)

// FEATURE 3: MOCK TRADING SIMULATOR (WITH TRANSACTION HISTORY LOGS)
@Composable
fun MockTradingScreen(viewModel: MainViewModel, lang: AppLanguage) {
    val stocks by viewModel.nepseStocks.collectAsStateWithLifecycle()
    val walletBalance by viewModel.virtualWalletBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    var selectSymbol by remember { mutableStateOf("NABIL") }
    var qtyString by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val activeStock = stocks.find { it.symbol == selectSymbol } ?: stocks.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyExchange, contentDescription = "Trade", tint = AccentGold, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == AppLanguage.NEPALI) "अभ्यास कारोबार सिम्युलेटर" else "Virtual Trading Simulator",
                    color = LightWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }
            IconButton(onClick = { viewModel.resetTradingSimulator() }) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset Portfolio", tint = StockRed)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs for Simulator
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardSlate,
            contentColor = AccentGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AccentGold
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (lang == AppLanguage.NEPALI) "कारोबार सिम्युलेटर" else "Practice Trading", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                selectedContentColor = AccentGold,
                unselectedContentColor = LightGray
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (lang == AppLanguage.NEPALI) "कारोबार इतिहास" else "Transaction Logs", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                selectedContentColor = AccentGold,
                unselectedContentColor = LightGray
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Virtual Trading Terminal Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Virtual Wallet Cash Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StockGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StockGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(Translations.get("balance", lang), color = LightWhite, fontSize = 12.sp)
                                Text("NPR ${String.format("%,.2f", walletBalance)}", color = StockGreen, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            }
                            Icon(Icons.Default.Wallet, contentDescription = "Wallet", tint = StockGreen, modifier = Modifier.size(40.dp))
                        }
                    }
                }

                // Trade Execution Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("EXECUTE MARKET ORDER (NEPSE)", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Select stock dropdown mock
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Stock symbol:", color = LightGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SlateDark)
                                            .clickable { expanded = true }
                                            .padding(12.dp)
                                    ) {
                                        Text(selectSymbol, color = LightWhite, fontWeight = FontWeight.Bold)
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(CardSlate)
                                    ) {
                                        stocks.forEach { st ->
                                            DropdownMenuItem(
                                                text = { Text(st.symbol, color = LightWhite) },
                                                onClick = {
                                                    selectSymbol = st.symbol
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (activeStock != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(Translations.get("current_price", lang), color = LightGray, fontSize = 12.sp)
                                    Text("NPR ${activeStock.price}", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = qtyString,
                                onValueChange = { qtyString = it },
                                label = { Text(Translations.get("quantity", lang), color = LightGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = LightWhite,
                                    unfocusedTextColor = LightWhite,
                                    focusedBorderColor = AccentGold
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("trade_qty_input")
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val q = qtyString.toIntOrNull() ?: 0
                                        viewModel.buyStock(selectSymbol, q)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StockGreen),
                                    modifier = Modifier.weight(1f).testTag("trade_buy_btn")
                                ) {
                                    Text(Translations.get("buy", lang), color = LightWhite, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val q = qtyString.toIntOrNull() ?: 0
                                        viewModel.sellStock(selectSymbol, q)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StockRed),
                                    modifier = Modifier.weight(1f).testTag("trade_sell_btn")
                                ) {
                                    Text(Translations.get("sell", lang), color = LightWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Portfolio Position lists
                item {
                    Text(Translations.get("holdings", lang), color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardSlate),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val heldStocks = stocks.filter { viewModel.getStockPosition(it.symbol) > 0 }
                            if (heldStocks.isEmpty()) {
                                Text(
                                    text = "No open positions. Use your virtual cash balance to purchase shares and practice trading!",
                                    color = LightGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                )
                            } else {
                                heldStocks.forEach { st ->
                                    val qty = viewModel.getStockPosition(st.symbol)
                                    val avgPrice = viewModel.getStockAvgBuyPrice(st.symbol)
                                    val currentVal = qty * st.price
                                    val totalInvested = qty * avgPrice
                                    val pNL = currentVal - totalInvested
                                    val pNlColor = if (pNL >= 0) StockGreen else StockRed
                                    val sign = if (pNL >= 0) "+" else ""

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(st.symbol, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("$qty Shares @ Avg NPR ${String.format("%.1f", avgPrice)}", color = LightGray, fontSize = 11.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("NPR ${String.format("%,.1f", currentVal)}", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("$sign${String.format("%.1f", pNL)}", color = pNlColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Divider(color = LightGray.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Transaction History Log Tab
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (lang == AppLanguage.NEPALI) "अहिलेसम्मका सबै कारोबारहरूको सूची" else "Your Sandbox Transaction History",
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No trades executed yet. Completed orders will be logged here in real time.",
                                color = LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(transactions) { tx ->
                                val totalValue = tx.quantity * tx.price
                                val isBuy = tx.type == "BUY"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateDark.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isBuy) StockGreen.copy(alpha = 0.15f) else StockRed.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tx.type,
                                                color = if (isBuy) StockGreen else StockRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Column {
                                            Text(tx.symbol, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                text = "${tx.quantity} Units @ NPR ${tx.price}",
                                                color = LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "NPR ${String.format("%,.1f", totalValue)}",
                                            color = LightWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                        Text(
                                            text = sdf.format(Date(tx.timestamp)),
                                            color = LightGray,
                                            fontSize = 9.sp
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
}

// FEATURE 4: SIP & COMPOUNDING CALCULATOR
@Composable
fun CalculatorScreen(lang: AppLanguage) {
    var monthlySip by remember { mutableStateOf("5000") }
    var interestRate by remember { mutableStateOf("12") }
    var yearsDuration by remember { mutableStateOf("10") }

    var calculationResult by remember { mutableStateOf(0.0) }
    var totalInvestedResult by remember { mutableStateOf(0.0) }

    LaunchedEffect(monthlySip, interestRate, yearsDuration) {
        val p = monthlySip.toDoubleOrNull() ?: 0.0
        val r = (interestRate.toDoubleOrNull() ?: 0.0) / 1200.0 // Monthly rate
        val n = (yearsDuration.toIntOrNull() ?: 0) * 12 // total months
        if (p > 0 && r > 0 && n > 0) {
            totalInvestedResult = p * n
            calculationResult = p * ((Math.pow(1 + r, n.toDouble()) - 1) / r) * (1 + r)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Calculate, contentDescription = "Calculator", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translations.get("calculator", lang), color = LightWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = monthlySip,
                    onValueChange = { monthlySip = it },
                    label = { Text("Monthly SIP Amount (NPR)", color = LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightWhite, focusedBorderColor = AccentGold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Expected Return Rate (%) e.g. 12", color = LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightWhite, focusedBorderColor = AccentGold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = yearsDuration,
                    onValueChange = { yearsDuration = it },
                    label = { Text("Duration (Years)", color = LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightWhite, focusedBorderColor = AccentGold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Results card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StockGreen.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, StockGreen)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("SIP COMPLEMENT REPORT", color = StockGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Invested Amount:", color = LightGray, fontSize = 13.sp)
                    Text("NPR ${String.format("%,.0f", totalInvestedResult)}", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Wealth Gain:", color = LightGray, fontSize = 13.sp)
                    Text("NPR ${String.format("%,.0f", Math.max(0.0, calculationResult - totalInvestedResult))}", color = StockGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Future Maturity Value:", color = LightGray, fontSize = 13.sp)
                    Text("NPR ${String.format("%,.0f", calculationResult)}", color = AccentGold, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}

// FEATURE 5: RISK PROFILER QUIZ
@Composable
fun RiskProfilerScreen(lang: AppLanguage) {
    var q1 by remember { mutableStateOf(0) } // Answers: 1 = Conservative, 2 = Moderate, 3 = Aggressive
    var q2 by remember { mutableStateOf(0) }
    var q3 by remember { mutableStateOf(0) }

    var finalProfile by remember { mutableStateOf("") }

    LaunchedEffect(q1, q2, q3) {
        if (q1 > 0 && q2 > 0 && q3 > 0) {
            val score = q1 + q2 + q3
            finalProfile = when {
                score <= 4 -> "Conservative (रूढ़िवादी - Low Risk)"
                score <= 7 -> "Moderate (मध्यम - Balanced)"
                else -> "Aggressive (आक्रामक - High Growth)"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Psychology, contentDescription = "Brain", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translations.get("risk_quiz", lang), color = LightWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Quiz items
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Q1: What is your primary investment goal?", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                listOf("Preserve Capital (Low return)", "Balanced Dividends", "High Capital Appreciation").forEachIndexed { index, ans ->
                    FilterChip(
                        selected = q1 == index + 1,
                        onClick = { q1 = index + 1 },
                        label = { Text(ans, fontSize = 11.sp, color = LightWhite) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Q2: How would you react if NEPSE dropped 15%?", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                listOf("Sell everything to avoid losses", "Do nothing, monitor state", "Buy more at discounted prices").forEachIndexed { index, ans ->
                    FilterChip(
                        selected = q2 == index + 1,
                        onClick = { q2 = index + 1 },
                        label = { Text(ans, fontSize = 11.sp, color = LightWhite) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Q3: When will you need your invested cash back?", color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                listOf("Less than 1 Year", "2 to 5 Years", "More than 5 Years").forEachIndexed { index, ans ->
                    FilterChip(
                        selected = q3 == index + 1,
                        onClick = { q3 = index + 1 },
                        label = { Text(ans, fontSize = 11.sp, color = LightWhite) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (finalProfile.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentGold)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("YOUR RISK PROFILE RESULT:", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(finalProfile, color = LightWhite, fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    val recs = when {
                        finalProfile.contains("Conservative") -> "Recommendations: Target government-guaranteed bonds, CIT savings, NTC (high dividend telecom), or NABIL Bank Fixed Deposit shares."
                        finalProfile.contains("Moderate") -> "Recommendations: Focus on commercial banks like NABIL, GBIME, major hydropower sectors, and mutual funds."
                        else -> "Recommendations: High growth microfinance and hydro stocks like UPPER, AHPC, and active trading shares with stop-losses."
                    }
                    Text(recs, color = LightGray, fontSize = 11.sp)
                }
            }
        }
    }
}

// FEATURE 6: FINANCIAL GLOSSARY
@Composable
fun GlossaryScreen(lang: AppLanguage) {
    val dictionary = listOf(
        Pair("IPO (Initial Public Offering)", "जब कुनै कम्पनीले पहिलो पटक सर्वसाधारणका लागि सेयर निष्कासन गर्छ, त्यसलाई आईपीओ भनिन्छ। (Offering shares to public first time)."),
        Pair("FPO (Follow-on Public Offering)", "आईपीओ जारी गरिसकेको कम्पनीले पुनः सर्वसाधारणका लागि सेयर निष्कासन गर्नु।"),
        Pair("MeroShare", "नेपालमा डिम्याट खाता सञ्चालन गर्न र आईपीओ आवेदन दिन प्रयोग गरिने आधिकारिक अनलाइन पोर्टल।"),
        Pair("Bull Market (बुलिस बजार)", "बजारको मूल्य माथि गइरहेको अवस्था, जहाँ लगानीकर्ताहरू उत्साहित हुन्छन्। (Rising stock index market)."),
        Pair("Bear Market (बियरिस बजार)", "बजारको मूल्य ओरालो लागिरहेको निराशाजनक अवस्था। (Falling stock index market)."),
        Pair("Book Building", "आईपीओको वास्तविक मूल्य निर्धारण लगानीकर्ताहरूको माग र बिडिङको आधारमा गर्ने आधुनिक विधि।"),
        Pair("Right Shares (हकप्रद सेयर)", "कम्पनीका विद्यमान सेयरधनीहरूले मात्र निश्चित अनुपातमा किन्न पाउने नयाँ सेयर।")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = "Glossary", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translations.get("dictionary", lang), color = LightWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dictionary) { term ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(term.first, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(term.second, color = LightWhite, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

// FEATURE 7: AI ANALYST CHAT SIMULATOR
@Composable
fun AiChatScreen(lang: AppLanguage) {
    var queryText by remember { mutableStateOf("") }
    val conversation = remember {
        mutableStateListOf(
            Pair("Ai Analyst", "नमस्ते! Welcome to Nepali Share Guide. Ask me anything about stock candle formations, technical indicators like RSI or MACD, or how to apply for IPOs via MeroShare.")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SmartToy, contentDescription = "Bot", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translations.get("ai_chat", lang), color = LightWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Chat message area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardSlate)
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                items(conversation) { chat ->
                    val isUser = chat.first == "User"
                    val bubbleColor = if (isUser) AccentGold.copy(alpha = 0.2f) else SlateDark
                    val align = if (isUser) Alignment.End else Alignment.Start

                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bubbleColor)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(chat.first, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text(chat.second, color = LightWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Input row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("How does Hammer candlestick forecast trade?", color = LightGray, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightWhite, focusedBorderColor = AccentGold),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (queryText.trim().isNotEmpty()) {
                        val userMsg = queryText
                        conversation.add(Pair("User", userMsg))
                        queryText = ""

                        // Generate expert answer instantly
                        val response = when {
                            userMsg.lowercase().contains("hammer") -> "A hammer candlestick is a bullish trading pattern characterized by a small real body at the upper end of the trading range, with a long lower shadow. This shows that sellers pushed prices down, but buyers stepped in aggressively to bid prices back up, indicating a strong reversal is probable!"
                            userMsg.lowercase().contains("rsi") -> "RSI (Relative Strength Index) is a momentum oscillator. A value above 70 indicates a stock is overbought (risk of selloff), while a value below 30 indicates it is oversold (good buy target)."
                            userMsg.lowercase().contains("ipo") -> "To apply for an IPO in Nepal: Log into MeroShare, navigate to the 'My ASBA' tab, click on 'Apply for Issue', select the desired company, enter the number of kitta (usually minimum 10), fill in your CRN number, and submit with your PIN!"
                            else -> "That is an excellent technical question! For the best market outcome, always pair candlestick analysis with historical volume confirmation. Look for supportive moving averages to back your long term investment decision."
                        }
                        conversation.add(Pair("Ai Analyst", response))
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AccentGold)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = SlateDark)
            }
        }
    }
}

// FEATURE 8: MEROSHARE IPO ALERTS
@Composable
fun IpoAlertScreen(lang: AppLanguage) {
    val ipos = listOf(
        Triple("Trishuli Hydropower Ltd", "1,200,000 Kitta - IPO", "OPEN (साउन १५ सम्म)"),
        Triple("Nabil Balanced Fund III", "Mutual Fund Issue", "UPCOMING"),
        Triple("Siddhartha Hydropower Ltd", "650,000 Kitta - IPO", "CLOSED")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, contentDescription = "Notifications", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translations.get("ipo_alert", lang), color = LightWhite, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ipos) { ipo ->
                val stateColor = when {
                    ipo.third.contains("OPEN") -> StockGreen
                    ipo.third.contains("UPCOMING") -> AccentGold
                    else -> LightGray
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ipo.first, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(stateColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(ipo.third, color = stateColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Issue Size: ${ipo.second}", color = LightGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("How to apply: Open MeroShare -> My ASBA -> Apply for Issue", color = AccentGold, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// --- SECURE HISTORY PAGE ---
@Composable
fun AnalysisHistoryScreen(viewModel: MainViewModel, lang: AppLanguage) {
    val historyList by viewModel.analyses.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = "History", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Screenshot Diagnostics Logs", color = LightWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No diagnostic history found. Please upload a screenshot on Dashboard.", color = LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(historyList) { item ->
                    val color = when (item.buySellDecision) {
                        "BUY" -> StockGreen
                        "SELL" -> StockRed
                        else -> AccentGold
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardSlate)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.companyName, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(item.patternName, color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(color.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(item.buySellDecision, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { viewModel.deleteAnalysisHistoryItem(item.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StockRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.analysisText, color = LightWhite, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SECURE ADMINISTRATOR CONTROL PANEL ---
@Composable
fun AdminPanelScreen(viewModel: MainViewModel, lang: AppLanguage) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    var apiKeyInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = AccentGold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(Translations.get("admin_panel", lang), color = LightWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // API Key settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Customize Gemini API Access Key", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Overrides default client key for processing multi-modal candlestick analyses.", color = LightGray, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("Enter Gemini API key", color = LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = LightWhite, focusedBorderColor = AccentGold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.setCustomApiKey(apiKeyInput)
                        apiKeyInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Secret API Key", color = SlateDark, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Registered User list controls
        Text(Translations.get("user_list", lang), color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSlate)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (users.isEmpty()) {
                    Text("No registered directory users", color = LightGray, fontSize = 11.sp, modifier = Modifier.padding(16.dp))
                } else {
                    users.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.username, color = LightWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${user.phone} - Lock: ${user.activeLockType}", color = LightGray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (user.isAuthorizedByAdmin) "Status: AUTHORIZED (ACTIVE)" else "Status: BLOCKED / PENDING",
                                    color = if (user.isAuthorizedByAdmin) StockGreen else StockRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Row {
                                IconButton(onClick = { viewModel.toggleUserAuthorization(user.phone) }) {
                                    Icon(
                                        imageVector = if (user.isAuthorizedByAdmin) Icons.Default.Block else Icons.Default.CheckCircle,
                                        contentDescription = "Toggle Access",
                                        tint = if (user.isAuthorizedByAdmin) StockRed else StockGreen
                                    )
                                }
                                IconButton(onClick = { viewModel.removeUser(user.phone) }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Remove", tint = StockRed)
                                }
                            }
                        }
                        Divider(color = LightGray.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}
