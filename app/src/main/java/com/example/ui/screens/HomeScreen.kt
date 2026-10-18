package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.ActivityFeedItem
import com.example.data.models.DailyGameStatus
import com.example.data.models.GameType
import com.example.data.models.Player
import com.example.data.models.UserProfile
import com.example.ui.theme.DarkBgBase
import com.example.ui.theme.DarkBgSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenContainer
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletContainer
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    dailyGames: List<DailyGameStatus>,
    activities: List<ActivityFeedItem>,
    players: List<Player> = emptyList(),
    hasDailyScores: Boolean = false,
    onNavigateToCadastrar: (String?) -> Unit,
    onNavigateToRankings: () -> Unit,
    onNavigateToJogos: () -> Unit,
    onReactToActivity: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEmptyState by remember(hasDailyScores) { mutableStateOf(!hasDailyScores) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgBase)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Switch to preview or toggle the Zero-State / Empty State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHigh)
                        .clickable { showEmptyState = !showEmptyState }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (showEmptyState) "👁 Ver Dashboard Normal" else "👁 Ver Modo Ranking Zerado",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
            }
        }

        if (showEmptyState) {
            // Render the Empty State Mode from Image 9 & 11
            item {
                EmptyStateCard(onCadastrarClick = { onNavigateToCadastrar(null) })
            }
            item {
                HowItWorksCard()
            }
            item {
                MiniGamesTodayCard(onJogarClick = onNavigateToJogos)
            }
        } else {
            // Normal Full Dashboard Mode from Image 5 & HTML
            item {
                HeroGreetingCard(userProfile = userProfile)
            }

            item {
                QuickActionsBento(
                    onCadastrar = { onNavigateToCadastrar(null) },
                    onVerificar = onNavigateToRankings,
                    onJogos = onNavigateToJogos
                )
            }

            item {
                DestaquesSection(
                    players = players,
                    userProfile = userProfile,
                    dailyGames = dailyGames,
                    onVerTabela = onNavigateToRankings,
                    onJogar = onNavigateToJogos
                )
            }

            item {
                RecentActivitiesSection(
                    activities = activities,
                    onReact = onReactToActivity
                )
            }

            item {
                QuickPasteSnifferCard(
                    onPaste = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        onNavigateToCadastrar(clipText)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroGreetingCard(userProfile: UserProfile) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerHigh)
            .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Temporada 2026 ativa",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bem-vindo ao RanKUs! 🔥",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Acompanhe as disputas diárias, envie seus recordes e veja quem lidera o ranking da galera.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // User Stat Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLow.copy(alpha = 0.8f))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sua Posição",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "#${userProfile.currentRank}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pontos Hoje",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${userProfile.todayScore}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sequência",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${userProfile.streakDays}d",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsBento(
    onCadastrar: () -> Unit,
    onVerificar: () -> Unit,
    onJogos: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AÇÕES RÁPIDAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = "Toque para agir",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Hero CTA Card: Cadastrar resultado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            NeonGreen.copy(alpha = 0.2f),
                            SurfaceContainer,
                            SurfaceContainer
                        )
                    )
                )
                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { onCadastrar() }
                .padding(16.dp)
                .testTag("hero_cadastrar_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = NeonGreenDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Cadastrar resultado",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cole seu resultado diário agora",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Twin Cards: Verificar RanKUs & Acessar Jogos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 2: Verificar RanKUs
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                    .clickable { onVerificar() }
                    .padding(14.dp)
                    .testTag("action_verificar_rankus")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyanContainer.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ao Vivo",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Verificar RanKUs",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Rankings gerais e estatísticas",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Card 3: Acessar Jogos
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                    .clickable { onJogos() }
                    .padding(14.dp)
                    .testTag("action_acessar_jogos")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonViolet.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonVioletContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Play",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonViolet
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Acessar Jogos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Jogue os desafios nas páginas oficiais",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DestaquesSection(
    players: List<Player>,
    userProfile: UserProfile,
    dailyGames: List<DailyGameStatus>,
    onVerTabela: () -> Unit,
    onJogar: () -> Unit
) {
    // 1 Real Highlight Player: Either the real #1 player with score > 0, or user if user has score > 0
    val spotlightPlayer = players.filter { it.score > 0 }.maxByOrNull { it.score }
        ?: if (userProfile.totalScore > 0) {
            Player(
                id = userProfile.id,
                name = userProfile.name,
                handle = userProfile.handle,
                avatarUrl = userProfile.avatarUrl,
                score = userProfile.totalScore,
                rank = 1,
                streak = userProfile.streakDays,
                gamesCount = userProfile.matchesPlayed,
                isCurrentUser = true
            )
        } else null

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DESTAQUE DA TEMPORADA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = "Ver tabela",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGreen,
                modifier = Modifier.clickable { onVerTabela() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (spotlightPlayer != null) {
            // Real Spotlight Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHigh)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(54.dp)) {
                        AsyncImage(
                            model = spotlightPlayer.avatarUrl,
                            contentDescription = spotlightPlayer.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, NeonGreen, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👑", fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceContainerLowest)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Text(text = "#${spotlightPlayer.rank}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = spotlightPlayer.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (spotlightPlayer.isCurrentUser) userProfile.bio else spotlightPlayer.handle,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${spotlightPlayer.score} pts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            if (spotlightPlayer.streak > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonVioletContainer.copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "🔥 ${spotlightPlayer.streak} dias seguidos",
                                        fontSize = 10.sp,
                                        color = NeonViolet,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Temporada 2026",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Clean empty state when no one has scored yet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHigh)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerLowest)
                            .border(1.dp, OutlineBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nenhum jogador em destaque ainda",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cadastre seu resultado hoje para ser o 1º jogador a brilhar no topo!",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Daily Game Availability Trackers (3 Columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dailyGames.forEach { status ->
                val gameTitle = status.gameType.title
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (status.isCompleted) SurfaceContainer else SurfaceContainerHigh)
                        .border(
                            1.dp,
                            if (status.isCompleted) OutlineBorder else NeonViolet.copy(alpha = 0.3f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = gameTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (status.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonGreenDark,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonViolet)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (status.isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✓ Concluído",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+${status.points} pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonViolet.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⏳ Pendente",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonViolet)
                                .clickable { onJogar() }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Jogar ↗",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBgBase
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${status.friendsCount} amigos",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivitiesSection(
    activities: List<ActivityFeedItem>,
    onReact: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "ATIVIDADES RECENTES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        activities.forEach { act ->
            ActivityCard(activity = act, onReact = onReact)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ActivityCard(
    activity: ActivityFeedItem,
    onReact: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainer)
            .border(1.dp, OutlineBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp)) {
                    AsyncImage(
                        model = activity.avatarUrl,
                        contentDescription = activity.playerName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                when (activity.gameName) {
                                    "Termo" -> NeonGreen
                                    "Globle" -> ElectricCyan
                                    else -> NeonViolet
                                }
                            )
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activity.gameLetter,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBgBase
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activity.playerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = " cravou ",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = activity.gameName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (activity.gameName) {
                                "Termo" -> NeonGreen
                                "Globle" -> ElectricCyan
                                else -> NeonViolet
                            }
                        )
                    }
                    Text(
                        text = activity.timeAgo,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+${activity.pointsEarned}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (activity.gameName) {
                        "Termo" -> NeonGreen
                        "Globle" -> ElectricCyan
                        else -> NeonViolet
                    }
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = TrophyGold,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = OutlineBorder.copy(alpha = 0.5f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Quick Interactive Reactions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReactionButton(
                emoji = "🔥",
                count = activity.fireReactions,
                isSelected = activity.userReactions.contains("🔥"),
                onClick = { onReact(activity.id, "🔥") }
            )
            ReactionButton(
                emoji = "👏",
                count = activity.applauseReactions,
                isSelected = activity.userReactions.contains("👏"),
                onClick = { onReact(activity.id, "👏") }
            )
            ReactionButton(
                emoji = "🎯",
                count = activity.targetReactions,
                isSelected = activity.userReactions.contains("🎯"),
                onClick = { onReact(activity.id, "🎯") }
            )
        }
    }
}

@Composable
private fun ReactionButton(
    emoji: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else SurfaceContainerHigh)
            .border(1.dp, if (isSelected) NeonGreen else OutlineBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$count",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) NeonGreen else TextSecondary
        )
    }
}

@Composable
private fun QuickPasteSnifferCard(onPaste: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPasteGo,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Copiar e colar rápido",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Acabou de jogar agora? Envie sem trocar de tela.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onPaste,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = NeonGreenDark
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("quick_paste_dashboard_btn")
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Colar resultado da área de transferência",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyStateCard(onCadastrarClick: () -> Unit) {
    val dayOfYear = LocalDate.now().dayOfYear

    fun calculateRemainingSeconds(): Long {
        val now = java.time.LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return java.time.Duration.between(now, midnight).seconds.coerceAtLeast(0)
    }

    var remainingSeconds by remember { mutableStateOf(calculateRemainingSeconds()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            remainingSeconds = calculateRemainingSeconds()
        }
    }

    val h = remainingSeconds / 3600
    val m = (remainingSeconds % 3600) / 60
    val s = remainingSeconds % 60
    val countdownStr = "%02d:%02d:%02d".format(h, m, s)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Daily edition badge & Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerHigh)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Edição diária #$dayOfYear",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = countdownStr, fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Temporada 2026 ativa pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(NeonGreen.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Temporada 2026 ativa",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGreen
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Trophy Bezel
        Box(
            modifier = Modifier
                .size(94.dp)
                .clip(CircleShape)
                .background(SurfaceContainerLowest)
                .border(2.dp, NeonGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.rankus_butt_trophy_icon_1792015743499),
                contentDescription = "Trophy",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Seja o 1º a Pontuar 🚀",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Nenhum resultado registrado hoje. O topo do pódio está esperando pelo seu nome!",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCadastrarClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreenContainer,
                contentColor = NeonGreenDark
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Cadastrar Primeiro Resultado", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HowItWorksCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Como Funciona", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "Regras Rápidas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
        }
        Spacer(modifier = Modifier.height(10.dp))

        StepRow(num = "1", title = "Jogue seu minigame", desc = "Resolva Termo, Globle ou SizeItUp diário.", color = NeonGreen)
        Spacer(modifier = Modifier.height(6.dp))
        StepRow(num = "2", title = "Copie o resultado", desc = "Copie o grid de emojis gerado pelo jogo.", color = ElectricCyan)
        Spacer(modifier = Modifier.height(6.dp))
        StepRow(num = "3", title = "Cole e suba no Rank", desc = "Receba até 100 pontos e desafie seus rivais!", color = NeonViolet)
    }
}

@Composable
private fun StepRow(num: String, title: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerLow)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(text = num, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = desc, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun MiniGamesTodayCard(onJogarClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Minigames de Hoje", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHigh)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "0/3 Feitos", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                }
            }
            Text(text = "Reset à meia-noite", fontSize = 10.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        MiniGameActionRow(title = "Termo & Quarteto", sub = "Palavras Diárias", color = NeonGreen, onPlay = onJogarClick)
        Spacer(modifier = Modifier.height(6.dp))
        MiniGameActionRow(title = "Globle", sub = "Adivinhe o País", color = ElectricCyan, onPlay = onJogarClick)
        Spacer(modifier = Modifier.height(6.dp))
        MiniGameActionRow(title = "SizeItUp", sub = "Escala & Proporção", color = NeonViolet, onPlay = onJogarClick)
    }
}

@Composable
private fun MiniGameActionRow(title: String, sub: String, color: Color, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerLow)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = sub, fontSize = 10.sp, color = TextSecondary)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceContainerHigh)
                .clickable { onPlay() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Jogar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                Spacer(modifier = Modifier.width(3.dp))
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            }
        }
    }
}
