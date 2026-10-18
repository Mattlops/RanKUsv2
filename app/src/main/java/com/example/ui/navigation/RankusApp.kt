package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RankusRepository
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.TopHeader
import com.example.ui.components.UserDropdownMenu
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CadastrarScreen
import com.example.ui.screens.HistoricoScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JogosScreen
import com.example.ui.screens.RankingsScreen
import com.example.ui.screens.RecoveryScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DarkBgBase
import com.example.ui.theme.DarkBgSurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.TextSecondary

enum class AppDestination {
    SPLASH,
    AUTH,
    RECOVERY,
    MAIN
}

enum class MainTab(val title: String, val icon: ImageVector) {
    INICIO("Início", Icons.Default.Dashboard),
    RANKINGS("Rankings", Icons.Default.Leaderboard),
    HISTORICO("Histórico", Icons.Default.History),
    JOGOS("Jogos", Icons.Default.SportsEsports)
}

@Composable
fun RankusApp(
    repository: RankusRepository = run {
        val context = androidx.compose.ui.platform.LocalContext.current
        remember { RankusRepository(context) }
    }
) {
    var appDestination by remember { mutableStateOf(AppDestination.SPLASH) }
    var currentTab by remember { mutableStateOf(MainTab.INICIO) }
    var isCadastrarOpen by remember { mutableStateOf(false) }
    var prefilledCadastrarText by remember { mutableStateOf<String?>(null) }
    var isUserDropdownOpen by remember { mutableStateOf(false) }
    var isEditProfileOpen by remember { mutableStateOf(false) }

    val isLoggedIn by repository.isLoggedIn.collectAsState()
    val hasDailyScores by repository.hasDailyScores.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val dailyGames by repository.dailyGames.collectAsState()
    val activities by repository.activities.collectAsState()
    val players by repository.players.collectAsState()
    val scoreHistory by repository.scoreHistory.collectAsState()

    when (appDestination) {
        AppDestination.SPLASH -> {
            SplashScreen(
                onEnterApp = {
                    appDestination = if (isLoggedIn) AppDestination.MAIN else AppDestination.AUTH
                }
            )
        }

        AppDestination.AUTH -> {
            BackHandler(enabled = !isLoggedIn) {
                // Keep on Auth screen
            }
            AuthScreen(
                onLogin = { emailOrUser, password ->
                    val result = repository.login(emailOrUser, password)
                    result.onSuccess {
                        appDestination = AppDestination.MAIN
                        currentTab = MainTab.INICIO
                    }
                    result
                },
                onRegister = { name, email, password ->
                    val result = repository.register(name, email, password)
                    result.onSuccess {
                        appDestination = AppDestination.MAIN
                        currentTab = MainTab.INICIO
                    }
                    result
                },
                onGoogleSuccess = { displayName ->
                    repository.onGoogleSignInSuccess(displayName)
                    appDestination = AppDestination.MAIN
                    currentTab = MainTab.INICIO
                },
                onNavigateToRecovery = {
                    appDestination = AppDestination.RECOVERY
                },
                isNicknameAvailable = { nick ->
                    repository.isNicknameAvailable(nick)
                }
            )
        }

        AppDestination.RECOVERY -> {
            BackHandler {
                appDestination = AppDestination.AUTH
            }
            RecoveryScreen(
                onSendRecoveryCode = { email ->
                    repository.sendRecoveryCode(email)
                },
                onVerifyRecoveryCode = { email, code ->
                    repository.verifyRecoveryCode(email, code)
                },
                onResetPassword = { email, newPassword ->
                    repository.resetPassword(email, newPassword)
                },
                onBackToLogin = {
                    appDestination = AppDestination.AUTH
                }
            )
        }

        AppDestination.MAIN -> {
            // If Cadastrar is open, back closes it and returns to Home
            if (isCadastrarOpen) {
                CadastrarScreen(
                    initialText = prefilledCadastrarText,
                    canSubmitGameToday = { game, variant -> repository.canSubmitGameToday(game, variant) },
                    onSubmitSuccess = { result -> repository.submitScore(result) },
                    onBackToHome = {
                        isCadastrarOpen = false
                        prefilledCadastrarText = null
                        currentTab = MainTab.INICIO
                    }
                )
            } else {
                BackHandler(enabled = currentTab != MainTab.INICIO) {
                    currentTab = MainTab.INICIO
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            TopHeader(
                                currentTitle = currentTab.title,
                                userProfile = currentUser,
                                onAvatarClick = { isUserDropdownOpen = !isUserDropdownOpen },
                                onHomeClick = { currentTab = MainTab.INICIO }
                            )
                        },
                        bottomBar = {
                            RankusBottomBar(
                                currentTab = currentTab,
                                onTabSelected = { tab ->
                                    currentTab = tab
                                }
                            )
                        },
                        containerColor = DarkBgBase
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                MainTab.INICIO -> {
                                    HomeScreen(
                                        userProfile = currentUser,
                                        dailyGames = dailyGames,
                                        activities = activities,
                                        hasDailyScores = hasDailyScores,
                                        onNavigateToCadastrar = { clipText ->
                                            prefilledCadastrarText = clipText
                                            isCadastrarOpen = true
                                        },
                                        onNavigateToRankings = { currentTab = MainTab.RANKINGS },
                                        onNavigateToJogos = { currentTab = MainTab.JOGOS },
                                        onReactToActivity = { id, emoji ->
                                            repository.reactToActivity(id, emoji)
                                        }
                                    )
                                }

                                MainTab.RANKINGS -> {
                                    RankingsScreen(
                                        players = players,
                                        getRankingPlayers = { g, t -> repository.getRankingPlayers(g, t) },
                                        getEvolutionData = { g, t -> repository.getEvolutionData(g, t) },
                                        onPlayClick = { currentTab = MainTab.JOGOS },
                                        onCadastrarClick = {
                                            prefilledCadastrarText = null
                                            isCadastrarOpen = true
                                        }
                                    )
                                }

                                MainTab.HISTORICO -> {
                                    HistoricoScreen(
                                        scoreHistory = scoreHistory,
                                        currentUserId = currentUser.id,
                                        onDeleteRecord = { record ->
                                            repository.deleteScore(record)
                                        },
                                        onNavigateToJogos = {
                                            currentTab = MainTab.JOGOS
                                        }
                                    )
                                }

                                MainTab.JOGOS -> {
                                    JogosScreen()
                                }
                            }
                        }
                    }

                    // Top-Right User Dropdown Popover
                    if (isUserDropdownOpen) {
                        UserDropdownMenu(
                            userProfile = currentUser,
                            onDismissRequest = { isUserDropdownOpen = false },
                            onOpenEditProfile = { isEditProfileOpen = true },
                            onLogout = {
                                isUserDropdownOpen = false
                                repository.logout()
                                appDestination = AppDestination.AUTH
                            }
                        )
                    }

                    // Edit Profile Dialog Modal
                    if (isEditProfileOpen) {
                        EditProfileDialog(
                            userProfile = currentUser,
                            isNicknameAvailable = { nick ->
                                repository.isNicknameAvailable(nick, currentUser.id)
                            },
                            onDismissRequest = { isEditProfileOpen = false },
                            onSave = { name, bio, favGame, avatarUri ->
                                repository.updateProfile(name, bio, favGame, avatarUri)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RankusBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBgSurface.copy(alpha = 0.95f))
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OutlineBorder.copy(alpha = 0.5f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                val iconColor = if (isSelected) NeonGreen else TextSecondary
                val textColor = if (isSelected) NeonGreen else TextSecondary

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 6.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}
