package com.example.myzum

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myzum.model.AccountType
import com.example.myzum.model.User
import com.example.myzum.model.FundingSource
import com.example.myzum.model.FundingSourceType
import com.example.myzum.nfc.NfcManager
import com.example.myzum.nfc.NfcRepository
import com.example.myzum.payment.FakePaymentProvider
import com.example.myzum.payment.PaymentState
import com.example.myzum.repository.CatalogRepository
import com.example.myzum.repository.FundingSourceRepository
import com.example.myzum.repository.PaymentRepository
import com.example.myzum.repository.RefundRepository
import com.example.myzum.repository.StatisticsRepository
import com.example.myzum.repository.UserRepository
import com.example.myzum.repository.local.AppDatabase
import com.example.myzum.ui.*
import com.example.myzum.ui.funding.*
import com.example.myzum.ui.registration.*
import com.example.myzum.ui.catalog.*
import com.example.myzum.ui.refund.RefundScreen
import com.example.myzum.ui.statistics.BusinessStatisticsScreen
import com.example.myzum.ui.settings.*
import com.example.myzum.ui.theme.MyzumTheme
import com.example.myzum.util.CurrencyUtils
import com.example.myzum.viewmodel.PaymentViewModel
import com.example.myzum.viewmodel.UserState
import com.example.myzum.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private var nfcManager: NfcManager? = null
    private var nfcRepository: NfcRepository? = null
    private var paymentRepository: PaymentRepository? = null
    private var userRepository: UserRepository? = null
    private var fundingSourceRepository: FundingSourceRepository? = null
    private var catalogRepository: CatalogRepository? = null
    private var refundRepository: RefundRepository? = null
    private var statisticsRepository: StatisticsRepository? = null
    private val paymentProvider = FakePaymentProvider()
    
    private val biometricPromptManager by lazy {
        com.example.myzum.ui.security.BiometricPromptManager(this)
    }

    private val userViewModel: UserViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UserViewModel(userRepository!!) as T
            }
        }
    }

    private val paymentViewModel: PaymentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PaymentViewModel(
                    paymentProvider,
                    paymentRepository!!,
                    nfcRepository!!,
                    userViewModel.activeUserId
                ) as T
            }
        }
    }

    private val fundingSourceViewModel: com.example.myzum.viewmodel.FundingSourceViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return com.example.myzum.viewmodel.FundingSourceViewModel(
                    fundingSourceRepository!!,
                    userViewModel.activeUserId
                ) as T
            }
        }
    }

    private val catalogViewModel: com.example.myzum.viewmodel.CatalogViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return com.example.myzum.viewmodel.CatalogViewModel(
                    catalogRepository!!,
                    userViewModel.activeUserId
                ) as T
            }
        }
    }

    private val statisticsViewModel: com.example.myzum.viewmodel.StatisticsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return com.example.myzum.viewmodel.StatisticsViewModel(
                    statisticsRepository!!,
                    userViewModel.activeUserId
                ) as T
            }
        }
    }

    private val refundViewModel: com.example.myzum.viewmodel.RefundViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return com.example.myzum.viewmodel.RefundViewModel(
                    refundRepository!!,
                    userViewModel.activeUserId
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val database = AppDatabase.getDatabase(this)
            paymentRepository = PaymentRepository(database.paymentDao())
            userRepository = UserRepository(database.userDao())
            fundingSourceRepository = FundingSourceRepository(database.fundingSourceDao())
            catalogRepository = CatalogRepository(database.productDao())
            refundRepository = RefundRepository(database.refundDao(), database.paymentDao(), paymentProvider)
            statisticsRepository = StatisticsRepository(database.statisticsDao())
            
            nfcManager = NfcManager(this)
            nfcRepository = NfcRepository(nfcManager!!)

            MyzumNotificationManager.createNotificationChannel(this)
        } catch (e: Exception) {
            // If DB init fails, we might still want to show an error UI
            // For now, let it crash or handle properly
        }
        
        setContent {
            MyzumTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (userRepository == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Initialization Error. Please restart the app.")
                        }
                        return@Surface
                    }

                    val userState by userViewModel.userState.collectAsState()
                    var isLocked by remember { mutableStateOf(false) }
                    var hasAttemptedUnlock by remember { mutableStateOf(false) }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { _ -> }
                    )

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    LaunchedEffect(userState) {
                        if (userState is UserState.Registered && !hasAttemptedUnlock) {
                            val user = (userState as UserState.Registered).user
                            if (user.settings.biometricEnabled) {
                                isLocked = true
                                hasAttemptedUnlock = true
                                biometricPromptManager.showBiometricPrompt(
                                    title = "Unlock Myzum",
                                    description = "Authenticate to access your dataphone",
                                    onSuccess = { isLocked = false },
                                    onError = { }
                                )
                            } else {
                                hasAttemptedUnlock = true
                            }
                        }
                    }

                    if (isLocked) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("App Locked", style = MaterialTheme.typography.headlineMedium)
                        }
                    } else {
                        when (userState) {
                            is UserState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is UserState.Unregistered -> {
                                RegistrationNavigation()
                            }
                            is UserState.Registered -> {
                                MainNavigation((userState as UserState.Registered).user)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RegistrationNavigation() {
        val navController = rememberNavController()
        var selectedAccountType by remember { mutableStateOf<AccountType?>(null) }

        NavHost(navController = navController, startDestination = "welcome") {
            composable("welcome") {
                WelcomeScreen(
                    onLoginClick = { navController.navigate("login") },
                    onRegisterClick = { navController.navigate("account_selection") }
                )
            }
            composable("login") {
                LoginScreen(
                    onLoginClick = { id, password ->
                        userViewModel.login(id, password) { success ->
                            if (!success) {
                                Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onRegisterClick = { navController.navigate("account_selection") }
                )
            }
            composable("account_selection") {
                AccountSelectionScreen(onTypeSelected = { type ->
                    selectedAccountType = type
                    navController.navigate("registration_form")
                })
            }
            composable("registration_form") {
                val type = selectedAccountType ?: AccountType.PARTICULAR
                RegistrationFormScreen(
                    accountType = type,
                    onBackClick = { navController.popBackStack() },
                    onRegisterClick = { newUser ->
                        userViewModel.register(newUser)
                    }
                )
            }
        }
    }

    @Composable
    private fun MainNavigation(user: User) {
        val navController = rememberNavController()
        val paymentState by paymentViewModel.paymentState.collectAsState()
        val history by paymentViewModel.paymentHistory.collectAsState()
        val fundingSources by fundingSourceViewModel.fundingSources.collectAsState()
        val selectedPageIndex by fundingSourceViewModel.selectedPageIndex.collectAsState()
        val statsUiState by statisticsViewModel.uiState.collectAsState()
        val selectedPeriod by statisticsViewModel.selectedPeriod.collectAsState()
        val products by catalogViewModel.products.collectAsState()
        val refundUiState by refundViewModel.uiState.collectAsState()

        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                paymentViewModel.reset()
                HomeScreen(
                    userType = user.type,
                    fundingSources = fundingSources,
                    payments = history,
                    initialPageIndex = selectedPageIndex,
                    onDataphoneClick = {
                        navController.navigate("dataphone")
                    },
                    onHistoryClick = {
                        navController.navigate("history")
                    },
                    onStatisticsClick = {
                        navController.navigate("business_statistics")
                    },
                    onCatalogClick = {
                        navController.navigate("catalog")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onAddFundingSourceClick = {
                        navController.navigate("add_funding_type")
                    },
                    onFundingSourceSelected = { id, index ->
                        paymentViewModel.setSelectedFundingSourceId(id)
                        fundingSourceViewModel.setSelectedPageIndex(index)
                    },
                    onRemoveFundingSource = { id ->
                        fundingSourceViewModel.removeSource(id)
                    }
                )
            }
            composable("catalog") {
                CatalogScreen(
                    products = products,
                    onAddProduct = { name, price ->
                        catalogViewModel.addProduct(
                            com.example.myzum.model.Product(
                                id = java.util.UUID.randomUUID().toString(),
                                name = name,
                                price = price,
                                ownerId = user.id
                            )
                        )
                    },
                    onDeleteProduct = { productId ->
                        catalogViewModel.removeProduct(productId)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("business_statistics") {
                LaunchedEffect(Unit) {
                    statisticsViewModel.refresh()
                }
                
                BusinessStatisticsScreen(
                    uiState = statsUiState,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { statisticsViewModel.setPeriod(it) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("add_funding_type") {
                AddFundingSourceScreen(
                    onTypeSelected = { type ->
                        if (type == FundingSourceType.CARD) {
                            navController.navigate("add_card")
                        } else {
                            navController.navigate("add_account")
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("add_card") {
                AddCardScreen(
                    ownerId = user.id,
                    onCardAdded = { source ->
                        fundingSourceViewModel.addSource(source)
                        navController.popBackStack("home", inclusive = false)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("add_account") {
                AddAccountScreen(
                    ownerId = user.id,
                    onAccountAdded = { source ->
                        fundingSourceViewModel.addSource(source)
                        navController.popBackStack("home", inclusive = false)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    user = user,
                    onBackClick = { navController.popBackStack() },
                    onNotificationsClick = { navController.navigate("settings_notifications") },
                    onSecurityClick = { navController.navigate("settings_security") },
                    onLogoutClick = {
                        userViewModel.logout()
                        paymentViewModel.reset()
                        fundingSourceViewModel.reset()
                    }
                )
            }
            composable("settings_notifications") {
                NotificationSettingsScreen(
                    enabled = user.settings.notificationsEnabled,
                    onEnabledChange = { userViewModel.updateNotifications(it) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("settings_security") {
                SecuritySettingsScreen(
                    biometricEnabled = user.settings.biometricEnabled,
                    onBiometricEnabledChange = { userViewModel.updateBiometric(it) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("history") {
                HistoryScreen(
                    payments = history,
                    onBackClick = { navController.popBackStack() },
                    onPaymentClick = { paymentId ->
                        navController.navigate("payment_detail/$paymentId")
                    }
                )
            }
            composable(
                route = "payment_detail/{paymentId}",
                arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val paymentId = backStackEntry.arguments?.getString("paymentId")
                val payment = history.find { it.id == paymentId }
                PaymentDetailScreen(
                    payment = payment,
                    fundingSources = fundingSources,
                    onRefundClick = { navController.navigate("refund/$it") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "refund/{paymentId}",
                arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val paymentId = backStackEntry.arguments?.getString("paymentId")
                val payment = history.find { it.id == paymentId }
                
                RefundScreen(
                    payment = payment,
                    uiState = refundUiState,
                    onRefundConfirm = { amount, reason ->
                        paymentId?.let { refundViewModel.refund(it, amount, reason) }
                    },
                    onSuccessDismiss = {
                        refundViewModel.resetState()
                        navController.popBackStack()
                    },
                    onBackClick = {
                        refundViewModel.resetState()
                        navController.popBackStack()
                    }
                )
            }
            composable("dataphone") {
                DataphoneScreen(
                    products = products,
                    onBackClick = { navController.popBackStack() },
                    onPayClick = { amount ->
                        val numericAmount = CurrencyUtils.parseAmount(amount.replace("€", "").trim()) ?: 0.0
                        paymentViewModel.setAmount(numericAmount)
                        navController.navigate("waiting_for_payment/$amount")
                    }
                )
            }
            composable(
                route = "waiting_for_payment/{amount}",
                arguments = listOf(navArgument("amount") { type = NavType.StringType })
            ) { backStackEntry ->
                val amountStr = backStackEntry.arguments?.getString("amount") ?: "0,00 €"
                
                LaunchedEffect(Unit) {
                    paymentViewModel.startPayment()
                }
                
                WaitingForPaymentScreen(
                    amount = amountStr,
                    onBackClick = { 
                        paymentViewModel.stopPayment()
                        navController.popBackStack() 
                    },
                    isProcessing = paymentState is PaymentState.Processing,
                    onProcessing = {
                        navController.navigate("processing")
                    },
                    isPaymentProcessed = paymentState is PaymentState.Approved,
                    onPaymentSuccess = {
                        paymentViewModel.stopPayment()
                        navController.navigate("payment_success") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                )
            }
            composable("processing") {
                ProcessingScreen()
                
                LaunchedEffect(paymentState) {
                    if (paymentState is PaymentState.Approved) {
                        navController.navigate("payment_success") {
                            popUpTo("home") { inclusive = false }
                        }
                    } else if (paymentState is PaymentState.Rejected) {
                        navController.popBackStack("dataphone", inclusive = false)
                    }
                }
            }
            composable("payment_success") {
                PaymentSuccessScreen(onDoneClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                })

                LaunchedEffect(Unit) {
                    val currentSelection = userViewModel.userState.value
                    if (currentSelection is UserState.Registered) {
                        val activeUser = currentSelection.user
                        if (activeUser.settings.notificationsEnabled) {
                            val currentAmount = paymentViewModel.amount.value
                            val formattedAmount = String.format(java.util.Locale.getDefault(), "%.2f €", currentAmount)
                            MyzumNotificationManager.showPaymentNotification(this@MainActivity, formattedAmount)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        nfcRepository?.stopListening()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            if (paymentViewModel.isListening.value) {
                val detected = nfcManager?.onNewIntent(intent) ?: false
                if (detected) {
                    SoundManager.playPaymentBeep()
                    SoundManager.vibrate(this@MainActivity)
                }
            }
        }
    }
}
