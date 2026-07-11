package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

sealed class AppLockState {
    object Registering : AppLockState()
    object OtpSent : AppLockState()
    object SafetySetup : AppLockState()
    object Locked : AppLockState()
    object Unlocked : AppLockState()
    object NotAuthorized : AppLockState()
    object ServerStep1 : AppLockState()
    object ServerStep2 : AppLockState()
    object ServerStep3 : AppLockState()
}

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Analyzing : AnalysisState()
    data class Success(val analysis: AnalysisEntity) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

enum class Screen {
    DASHBOARD,
    ACADEMY,
    LIVE_TRACKER,
    MOCK_TRADING,
    SIP_CALCULATOR,
    RISK_PROFILER,
    GLOSSARY,
    AI_CHAT,
    IPO_ALERT,
    ADMIN_PANEL,
    ANALYSIS_HISTORY
}

class MainViewModel(
    application: Application,
    private val repository: Repository
) : AndroidViewModel(application) {

    // Localization state
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Authentication & App Lock States
    private val _appLockState = MutableStateFlow<AppLockState>(AppLockState.Registering)
    val appLockState: StateFlow<AppLockState> = _appLockState.asStateFlow()

    private val _currentPhone = MutableStateFlow("")
    val currentPhone: StateFlow<String> = _currentPhone.asStateFlow()

    private val _currentUsername = MutableStateFlow("")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _currentUserEntity = MutableStateFlow<UserEntity?>(null)
    val currentUserEntity: StateFlow<UserEntity?> = _currentUserEntity.asStateFlow()

    // Eye Lock simulation variables
    private val _eyeLockProgress = MutableStateFlow(0f)
    val eyeLockProgress: StateFlow<Float> = _eyeLockProgress.asStateFlow()

    private val _isEyeScanning = MutableStateFlow(false)
    val isEyeScanning: StateFlow<Boolean> = _isEyeScanning.asStateFlow()

    // Admin Users directory for authorization toggle
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated NEPSE stocks & Mock Trading
    val nepseStocks: StateFlow<List<StockEntity>> = repository.allStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analyses: StateFlow<List<AnalysisEntity>> = repository.allAnalyses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _virtualWalletBalance = MutableStateFlow(100000.0) // 1 Lakh NPR starter cash
    val virtualWalletBalance: StateFlow<Double> = _virtualWalletBalance.asStateFlow()

    // Active screen navigation
    private val _selectedScreen = MutableStateFlow(Screen.DASHBOARD)
    val selectedScreen: StateFlow<Screen> = _selectedScreen.asStateFlow()

    // Chart screenshot analysis states
    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Feedback messages
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        // Prepare mock NEPSE stocks
        viewModelScope.launch {
            repository.initializeMockStocks()
            // Auto simulate stock movements every 8 seconds to make market feel live!
            while (true) {
                delay(8000)
                repository.simulateLiveTick()
            }
        }

        // Verify initial user lock configuration state on launch
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                if (users.isNotEmpty()) {
                    // Pre-registered users found, default to locked state
                    val mainUser = users.firstOrNull()
                    if (_currentUserEntity.value == null) {
                        _currentUserEntity.value = mainUser
                        _currentPhone.value = mainUser?.phone ?: ""
                        _currentUsername.value = mainUser?.username ?: ""
                        
                        if (mainUser != null) {
                            if (!mainUser.isAuthorizedByAdmin) {
                                _appLockState.value = AppLockState.NotAuthorized
                            } else {
                                _appLockState.value = AppLockState.Locked
                            }
                        }
                    }
                } else {
                    _appLockState.value = AppLockState.Registering
                }
            }
        }
    }

    fun selectLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun selectScreen(screen: Screen) {
        _selectedScreen.value = screen
    }

    // --- Authentication & Otp Functions ---
    fun sendOtp(phone: String, username: String) {
        if (phone.length < 10) {
            viewModelScope.launch { _toastMessage.emit("Enter a valid 10-digit mobile number") }
            return
        }
        if (username.trim().isEmpty()) {
            viewModelScope.launch { _toastMessage.emit("Enter a valid username") }
            return
        }
        _currentPhone.value = phone
        _currentUsername.value = username
        _appLockState.value = AppLockState.OtpSent
        viewModelScope.launch {
            _toastMessage.emit("OTP Code '1234' sent safely via SMS to $phone")
        }
    }

    fun verifyOtp(otp: String) {
        if (otp == "1234" || otp == "9999") { // Accept demo passcode
            _appLockState.value = AppLockState.SafetySetup
        } else {
            viewModelScope.launch { _toastMessage.emit("Invalid OTP. Please enter '1234' to verify.") }
        }
    }

    fun registerLockCredentials(
        passwordVal: String,
        pinVal: String,
        eyeLockConfig: Boolean,
        biometricVal: Boolean,
        activeLock: String
    ) {
        if (activeLock == "PASSWORD" && passwordVal.trim().isEmpty()) {
            viewModelScope.launch { _toastMessage.emit("Password cannot be empty") }
            return
        }
        if (activeLock == "PIN" && pinVal.length < 4) {
            viewModelScope.launch { _toastMessage.emit("PIN must be 4 digits") }
            return
        }

        viewModelScope.launch {
            val isFirstUser = allUsers.value.isEmpty()
            // The first user registering is automatically approved/authorized to bypass deadlock,
            // subsequent users registered on this demo device can be controlled via Administrator Control Panel.
            val user = UserEntity(
                phone = _currentPhone.value,
                username = _currentUsername.value,
                passwordHash = passwordVal,
                pin = pinVal,
                eyeLockConfigured = eyeLockConfig,
                biometricEnabled = biometricVal,
                activeLockType = activeLock,
                isAuthorizedByAdmin = isFirstUser, // First registered is the admin/owner!
                otpVerified = true
            )
            repository.insertUser(user)
            _currentUserEntity.value = user
            
            if (user.isAuthorizedByAdmin) {
                _appLockState.value = AppLockState.Unlocked
                _toastMessage.emit("Access lock configured successfully. Welcome inside!")
            } else {
                _appLockState.value = AppLockState.NotAuthorized
                _toastMessage.emit("Registration successful. Awaiting Owner confirmation.")
            }
        }
    }

    // Trigger unlock verification
    fun verifyUnlock(enteredValue: String): Boolean {
        val user = _currentUserEntity.value ?: return false
        val method = user.activeLockType

        if (method == "PASSWORD" && enteredValue == user.passwordHash) {
            _appLockState.value = AppLockState.Unlocked
            return true
        } else if (method == "PIN" && enteredValue == user.pin) {
            _appLockState.value = AppLockState.Unlocked
            return true
        } else if (method == "BIOMETRIC") {
            _appLockState.value = AppLockState.Unlocked
            return true
        }
        
        viewModelScope.launch { _toastMessage.emit("Verification failed! Try again.") }
        return false
    }

    // Eye Scan animation trigger
    fun startEyeScan(onScanComplete: () -> Unit) {
        if (_isEyeScanning.value) return
        _isEyeScanning.value = true
        _eyeLockProgress.value = 0f
        
        viewModelScope.launch {
            while (_eyeLockProgress.value < 1.0f) {
                delay(100)
                _eyeLockProgress.value += 0.05f
            }
            _isEyeScanning.value = false
            onScanComplete()
        }
    }

    // Log out or lock app
    fun lockApp() {
        _appLockState.value = AppLockState.Locked
        _selectedScreen.value = Screen.DASHBOARD
    }

    fun startServerUnlockFlow() {
        _appLockState.value = AppLockState.ServerStep1
    }

    fun verifyServerPassword(pw: String): Boolean {
        if (pw == "shrhoni") {
            _appLockState.value = AppLockState.ServerStep2
            return true
        } else {
            viewModelScope.launch { _toastMessage.emit("Incorrect Server Password") }
            return false
        }
    }

    fun verifyServerPin(pin: String): Boolean {
        if (pin == "1111") {
            _appLockState.value = AppLockState.ServerStep3
            return true
        } else {
            viewModelScope.launch { _toastMessage.emit("Incorrect PIN") }
            return false
        }
    }

    fun completeServerBiometricUnlock() {
        viewModelScope.launch {
            val serverUser = UserEntity(
                phone = "SERVER_HANDLER",
                username = "Binaya (Server Handler)",
                passwordHash = "shrhoni",
                pin = "1111",
                eyeLockConfigured = false,
                biometricEnabled = true,
                activeLockType = "PASSWORD",
                isAuthorizedByAdmin = true,
                otpVerified = true
            )
            repository.insertUser(serverUser)
            _currentUserEntity.value = serverUser
            _currentPhone.value = "SERVER_HANDLER"
            _currentUsername.value = "Binaya (Server Handler)"
            _appLockState.value = AppLockState.Unlocked
            _toastMessage.emit("Server Handler Authentication Success. Access Granted.")
        }
    }

    // Owner controls: block or authorize users
    fun toggleUserAuthorization(phone: String) {
        viewModelScope.launch {
            val user = repository.getUserSync(phone)
            if (user != null) {
                // Check if this is the owner (first registered user on device) to prevent self-blocking
                val allSorted = allUsers.value
                val firstRegistered = allSorted.firstOrNull()?.phone
                if (phone == firstRegistered && user.isAuthorizedByAdmin) {
                    _toastMessage.emit("Cannot revoke authorization of the Owner!")
                    return@launch
                }

                val updated = user.copy(isAuthorizedByAdmin = !user.isAuthorizedByAdmin)
                repository.updateUser(updated)
                
                if (_currentPhone.value == phone) {
                    _currentUserEntity.value = updated
                    if (!updated.isAuthorizedByAdmin) {
                        _appLockState.value = AppLockState.NotAuthorized
                    }
                }
                
                _toastMessage.emit("User state updated successfully")
            }
        }
    }

    fun removeUser(phone: String) {
        viewModelScope.launch {
            val allSorted = allUsers.value
            val firstRegistered = allSorted.firstOrNull()?.phone
            if (phone == firstRegistered) {
                _toastMessage.emit("Cannot delete Owner profile!")
                return@launch
            }
            repository.deleteUser(phone)
            _toastMessage.emit("User removed successfully")
        }
    }

    // --- Mock Trading Functions ---
    fun buyStock(symbol: String, qty: Int) {
        if (qty <= 0) return
        val stock = nepseStocks.value.find { it.symbol == symbol } ?: return
        val cost = stock.price * qty
        if (_virtualWalletBalance.value < cost) {
            viewModelScope.launch { _toastMessage.emit("Insufficient wallet balance!") }
            return
        }

        viewModelScope.launch {
            val success = repository.executeTrade(symbol, "BUY", qty, stock.price)
            if (success) {
                _virtualWalletBalance.value -= cost
                _toastMessage.emit("Successfully purchased $qty shares of $symbol")
            }
        }
    }

    fun sellStock(symbol: String, qty: Int) {
        if (qty <= 0) return
        val holdings = getStockPosition(symbol)
        if (holdings < qty) {
            viewModelScope.launch { _toastMessage.emit("Insufficient shares in portfolio!") }
            return
        }

        val stock = nepseStocks.value.find { it.symbol == symbol } ?: return
        val earnings = stock.price * qty

        viewModelScope.launch {
            val success = repository.executeTrade(symbol, "SELL", qty, stock.price)
            if (success) {
                _virtualWalletBalance.value += earnings
                _toastMessage.emit("Successfully sold $qty shares of $symbol")
            }
        }
    }

    fun getStockPosition(symbol: String): Int {
        var qty = 0
        transactions.value.filter { it.symbol == symbol }.forEach {
            if (it.type == "BUY") qty += it.quantity else qty -= it.quantity
        }
        return qty
    }

    fun getStockAvgBuyPrice(symbol: String): Double {
        var totalCost = 0.0
        var totalQty = 0
        transactions.value.filter { it.symbol == symbol && it.type == "BUY" }.forEach {
            totalCost += it.price * it.quantity
            totalQty += it.quantity
        }
        return if (totalQty > 0) totalCost / totalQty else 0.0
    }

    fun resetTradingSimulator() {
        viewModelScope.launch {
            repository.clearTrades()
            _virtualWalletBalance.value = 100000.0
            _toastMessage.emit("Trading simulator reset successfully")
        }
    }

    // --- Gemini Multi-Modal Screenshot Analyzer ---
    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
    }

    fun analyzeChartImage(bitmap: Bitmap, associatedSymbol: String) {
        _analysisState.value = AnalysisState.Analyzing
        
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Convert bitmap to base64
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                
                val keyInput = if (_customApiKey.value.isNotBlank()) _customApiKey.value else null
                val result = repository.analyzeChartScreenshot(base64, associatedSymbol, keyInput)
                
                // Save analysis to history database
                repository.saveAnalysis(result)
                
                withContext(Dispatchers.Main) {
                    _analysisState.value = AnalysisState.Success(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _analysisState.value = AnalysisState.Error("Analysis failed: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearAnalysis() {
        _analysisState.value = AnalysisState.Idle
    }

    fun deleteAnalysisHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteAnalysis(id)
            _toastMessage.emit("History log removed")
        }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
