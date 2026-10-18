package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.Player
import com.example.data.models.WeeklyComparisonDay
import com.example.ui.theme.DarkBgBase
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenContainer
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.NeonGreenLight
import com.example.ui.theme.NeonViolet
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
fun RankingsScreen(
    players: List<Player>,
    getRankingPlayers: ((gameFilter: String, timeFilter: String) -> List<Player>)? = null,
    getEvolutionData: (gameFilter: String, timeFilter: String) -> List<WeeklyComparisonDay>,
    onPlayClick: () -> Unit,
    onCadastrarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedGamePill by remember { mutableStateOf("Todos os Jogos") }
    var selectedTimeRange by remember { mutableStateOf("Semana") }
    var expandedPlayerId by remember { mutableStateOf<String?>(null) }
    var showRulesModal by remember { mutableStateOf(false) }

    // Dynamic evolution data based on chosen Game and Time filter
    val currentEvolutionData = remember(selectedGamePill, selectedTimeRange) {
        getEvolutionData(selectedGamePill, selectedTimeRange)
    }

    // Dynamic filtered players based on both Game and Time filter
    val filteredPlayers = remember(players, selectedGamePill, selectedTimeRange) {
        if (getRankingPlayers != null) {
            getRankingPlayers(selectedGamePill, selectedTimeRange)
        } else {
            players.sortedByDescending { it.score }.mapIndexed { index, p -> p.copy(rank = index + 1) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgBase)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Season Header & Rules modal button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TEMPORADA 2026 ATIVA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Restam 4d", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Row(
                    modifier = Modifier
                        .clickable { showRulesModal = true }
                        .padding(4.dp)
                        .testTag("rules_modal_trigger"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Help, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Regras de Pontos",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }
        }

        // Title Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RanKUs Leaderboard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Desempenho diário & corrida de precisão do esquadrão",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                }
            }
        }

        // Filters: Game Pills and Time Range Switcher (Audience Toggle removed as requested)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Game Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos os Jogos", "Termo", "Globle", "SizeItUp").forEach { pill ->
                        val isSelected = selectedGamePill == pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SurfaceContainerHighest else SurfaceContainer)
                                .border(1.dp, if (isSelected) NeonGreen.copy(alpha = 0.7f) else OutlineBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedGamePill = pill }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(NeonGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = pill,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonGreen else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Time Range Switcher [Hoje] [Semana] [Mês] [Geral]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLowest)
                        .border(1.dp, OutlineBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(3.dp)
                ) {
                    listOf("Hoje", "Semana", "Mês", "Geral").forEach { range ->
                        val isSelected = selectedTimeRange == range
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceContainerHigh else Color.Transparent)
                                .clickable { selectedTimeRange = range }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = range,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonGreen else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Top 3 Podium Experience (or Empty Podium State if 0 points)
        item {
            if (filteredPlayers.isEmpty() || filteredPlayers.all { it.score == 0 }) {
                EmptyPodiumCard(onCadastrarClick = onCadastrarClick)
            } else {
                PodiumCard(players = filteredPlayers)
            }
        }

        // Gráfico em Linha: Evolução Semanal da Galera (Canvas-drawn Line Chart)
        item {
            WeeklyEvolutionLineChartCard(
                weeklyData = currentEvolutionData,
                selectedGame = selectedGamePill,
                selectedTime = selectedTimeRange
            )
        }

        // Classificação Geral Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FormatListNumbered, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Classificação ($selectedGamePill)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = "${filteredPlayers.size} Competidores", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        // Competitors List
        if (filteredPlayers.isEmpty() || filteredPlayers.all { it.score == 0 }) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ainda não há registros no ranking",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Submeta seu primeiro resultado de jogo diário para abrir a tabela de classificação!",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onCadastrarClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = NeonGreenDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Cadastrar Resultado", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredPlayers) { player ->
                PlayerRankItem(
                    player = player,
                    isExpanded = expandedPlayerId == player.id,
                    onToggleExpand = {
                        expandedPlayerId = if (expandedPlayerId == player.id) null else player.id
                    }
                )
            }
        }

        // Milestone Card: Contagem regressiva até 31 de dezembro
        item {
            val today = java.time.LocalDate.now()
            val endOfYear = java.time.LocalDate.of(today.year, 12, 31)
            val daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, endOfYear).coerceAtLeast(0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerLow)
                    .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Faltam $daysRemaining dias para 31 de Dezembro!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "A Temporada ${today.year} encerra no dia 31 de dezembro. Pontue diariamente!",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = NeonGreenDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(text = "Jogar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal: Regras de Pontuação
    if (showRulesModal) {
        RulesModalDialog(onDismiss = { showRulesModal = false })
    }
}

@Composable
private fun EmptyPodiumCard(onCadastrarClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceContainerHigh, SurfaceContainer, SurfaceContainerLow)
                )
            )
            .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Pódio Vazio Nesta Rodada",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Seja o primeiro a enviar o resultado diário e garanta o 1º lugar no topo do pódio!",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onCadastrarClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = NeonGreenDark
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Pontuar Agora", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PodiumCard(players: List<Player>) {
    val rank1 = players.find { it.rank == 1 && it.score > 0 }
    val rank2 = players.find { it.rank == 2 && it.score > 0 }
    val rank3 = players.find { it.rank == 3 && it.score > 0 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceContainerHigh, SurfaceContainer, SurfaceContainerLow)
                )
            )
            .border(1.dp, OutlineBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            // #2 Silver
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.size(52.dp)) {
                    if (rank2 != null && rank2.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = rank2.avatarUrl,
                            contentDescription = rank2.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(2.dp, ElectricCyan, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .border(1.dp, OutlineBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                        }
                    }
                    if (rank2 != null) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = rank2?.name ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (rank2 != null) TextPrimary else TextSecondary)
                Text(text = if (rank2 != null) "${rank2.score}" else "--", fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (rank2 != null) ElectricCyan else TextSecondary)
                Text(text = if (rank2 != null) "pts" else "", fontSize = 9.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                // Pedestal Bar #2
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(SurfaceContainerHighest),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "2", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ElectricCyan, modifier = Modifier.padding(top = 4.dp))
                    Text(text = if (rank2 != null && rank2.rankChange > 0) "▲ +${rank2.rankChange}" else "", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen, modifier = Modifier.padding(bottom = 6.dp))
                }
            }

            // #1 Gold
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.2f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(TrophyGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "👑 LÍDER", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.size(68.dp)) {
                    if (rank1 != null && rank1.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = rank1.avatarUrl,
                            contentDescription = rank1.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, NeonGreen, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .border(1.dp, OutlineBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(26.dp))
                        }
                    }
                    if (rank1 != null) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = rank1?.name ?: "-", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (rank1 != null) TextPrimary else TextSecondary)
                Text(text = if (rank1 != null) "${rank1.score}" else "--", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (rank1 != null) NeonGreen else TextSecondary)
                Text(text = if (rank1 != null) "pts • Líder" else "", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(6.dp))
                // Pedestal Bar #1
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(SurfaceBright, SurfaceContainerHighest)
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1", fontSize = 26.sp, fontWeight = FontWeight.Black, color = NeonGreen, modifier = Modifier.padding(top = 6.dp))
                    Text(text = if (rank1 != null && rank1.streak > 0) "${rank1.streak} vitórias" else "", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                }
            }

            // #3 Bronze
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.size(52.dp)) {
                    if (rank3 != null && rank3.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = rank3.avatarUrl,
                            contentDescription = rank3.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(2.dp, NeonViolet, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .border(1.dp, OutlineBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                        }
                    }
                    if (rank3 != null) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(12.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = rank3?.name ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (rank3 != null) TextPrimary else TextSecondary)
                Text(text = if (rank3 != null) "${rank3.score}" else "--", fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (rank3 != null) NeonViolet else TextSecondary)
                Text(text = if (rank3 != null) "pts" else "", fontSize = 9.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                // Pedestal Bar #3
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(SurfaceContainerHighest),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "3", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NeonViolet, modifier = Modifier.padding(top = 4.dp))
                    Text(text = if (rank3 != null && rank3.rankChange < 0) "▼ ${rank3.rankChange}" else "", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErrorRed, modifier = Modifier.padding(bottom = 6.dp))
                }
            }
        }
    }
}

/**
 * Gráfico em Linha (Line Chart) da Evolução dos Amigos com seus respectivos nomes e pontuações
 */
@Composable
private fun WeeklyEvolutionLineChartCard(
    weeklyData: List<WeeklyComparisonDay>,
    selectedGame: String,
    selectedTime: String
) {
    var selectedDayIndex by remember { mutableStateOf(weeklyData.size - 1) }
    val safeIndex = selectedDayIndex.coerceIn(0, (weeklyData.size - 1).coerceAtLeast(0))
    val activeDay = weeklyData.getOrNull(safeIndex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainer)
            .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Evolução da Galera (Gráfico em Linha)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Pontuação de acordo com os amigos • $selectedTime", fontSize = 10.sp, color = TextSecondary)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "● AO VIVO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legenda de Evolução
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceContainerLowest)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendLineItem(color = NeonGreen, label = "Sua Evolução", score = activeDay?.userRaw ?: 0, isBold = true)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Canvas Line Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(weeklyData) {
                        detectTapGestures { offset ->
                            val count = weeklyData.size
                            if (count > 0) {
                                val colWidth = size.width / count
                                val clickedIndex = (offset.x / colWidth).toInt().coerceIn(0, count - 1)
                                selectedDayIndex = clickedIndex
                            }
                        }
                    }
            ) {
                val count = weeklyData.size
                if (count < 2) return@Canvas

                val width = size.width
                val height = size.height - 24.dp.toPx() // leave room at bottom for tags
                val stepX = width / (count - 1)

                // Grid reference horizontal lines (0%, 33%, 66%, 100%)
                val gridAlpha = 0.2f
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                for (i in 0..3) {
                    val y = height * (i / 3f)
                    drawLine(
                        color = OutlineBorder.copy(alpha = gridAlpha),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // Helper to get point coordinates
                fun getPoint(index: Int, normalizedScore: Float): Offset {
                    val x = index * stepX
                    // Invert Y so highest score is at top
                    val y = height - (normalizedScore.coerceIn(0.05f, 0.95f) * height)
                    return Offset(x, y)
                }

                // Draw Line Path for competitor
                data class CompetitorLine(
                    val color: Color,
                    val scores: List<Float>,
                    val name: String
                )

                val lines = listOf(
                    CompetitorLine(NeonGreen, weeklyData.map { it.userScore }, "Você")
                )

                lines.forEach { line ->
                    val path = Path()
                    val points = line.scores.mapIndexed { idx, score -> getPoint(idx, score) }

                    points.forEachIndexed { i, pt ->
                        if (i == 0) {
                            path.moveTo(pt.x, pt.y)
                        } else {
                            val prev = points[i - 1]
                            // Smooth quadratic curve
                            val midX = (prev.x + pt.x) / 2
                            path.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                        }
                    }

                    // Stroke the line
                    drawPath(
                        path = path,
                        color = line.color,
                        style = Stroke(
                            width = if (line.name == "Você") 3.dp.toPx() else 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw dots at each node
                    points.forEachIndexed { i, pt ->
                        val isSelected = i == safeIndex
                        val dotRadius = if (isSelected) 5.dp.toPx() else 3.dp.toPx()

                        // Glow on active node
                        if (isSelected) {
                            drawCircle(
                                color = line.color.copy(alpha = 0.35f),
                                radius = dotRadius * 2.2f,
                                center = pt
                            )
                        }

                        drawCircle(
                            color = line.color,
                            radius = dotRadius,
                            center = pt
                        )
                        drawCircle(
                            color = SurfaceContainerLowest,
                            radius = dotRadius * 0.45f,
                            center = pt
                        )
                    }
                }

                // Draw vertical indicator for selected day
                val selX = safeIndex * stepX
                drawLine(
                    color = NeonGreen.copy(alpha = 0.4f),
                    start = Offset(selX, 0f),
                    end = Offset(selX, height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            }
        }

        // X-Axis Day Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyData.forEachIndexed { idx, day ->
                val isSelected = idx == safeIndex
                Text(
                    text = day.dayLabel,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) NeonGreen else TextSecondary,
                    modifier = Modifier
                        .clickable { selectedDayIndex = idx }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = OutlineBorder.copy(alpha = 0.4f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Trend Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Evolução no $selectedGame", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            }
            Text(text = "Temporada 2026", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun LegendLineItem(
    color: Color,
    label: String,
    score: Int,
    isBold: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = if (isBold) NeonGreen else TextPrimary
            )
        }
        Text(
            text = "$score pts",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun PlayerRankItem(
    player: Player,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val isCurrentUser = player.isCurrentUser
    val itemBg = if (isCurrentUser) SurfaceContainerHigh else SurfaceContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(itemBg)
            .border(
                1.dp,
                if (isCurrentUser) NeonGreen.copy(alpha = 0.6f) else OutlineBorder.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank number + Avatar + Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank position badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when (player.rank) {
                                1 -> NeonGreen.copy(alpha = 0.2f)
                                2 -> ElectricCyan.copy(alpha = 0.2f)
                                3 -> NeonViolet.copy(alpha = 0.2f)
                                else -> SurfaceContainerLowest
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${player.rank}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = when (player.rank) {
                            1 -> NeonGreen
                            2 -> ElectricCyan
                            3 -> NeonViolet
                            else -> TextSecondary
                        }
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar
                Box(modifier = Modifier.size(42.dp)) {
                    AsyncImage(
                        model = player.avatarUrl,
                        contentDescription = player.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                if (isCurrentUser) NeonGreen else OutlineBorder,
                                CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentUser) NeonGreen else TextPrimary
                        )
                        if (player.badge != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = player.badge,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen
                                )
                            }
                        }
                    }
                    Text(
                        text = "${player.streak} dias de streak • ${player.gamesCount} jogos",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Score and Expand icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${player.score}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCurrentUser) NeonGreen else TextPrimary
                    )
                    Text(
                        text = "pontos",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expandable Detail stats
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceContainerLowest)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Termo", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "${player.winRate}% Vit.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Globle", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "${player.globleAvg} pts Méd.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SizeItUp", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "${player.sizeItUpScore} pts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonViolet)
                    }
                }
            }
        }
    }
}

@Composable
private fun RulesModalDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            color = SurfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Help, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Regras de Pontuação", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Termo Quarteto
                RuleSectionBox(
                    title = "Termo Quarteto",
                    formula = "(Palavras_Acertadas × 15) + (Tentativas_Sobrando × 8)",
                    rules = listOf(
                        "4 palavras em 4 tentativas: 100 pontos (perfeito!)",
                        "4 palavras em 9 tentativas: 60 pontos",
                        "Tentativas não utilizadas: 8 pontos de bônus cada"
                    ),
                    color = NeonGreen
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Globle
                RuleSectionBox(
                    title = "Globle (Geografia)",
                    formula = "SE(Tentativas ≤ 2; 100; MÁXIMO(10; 100 - ((Tentativas - 2) × 3)))",
                    rules = listOf(
                        "1 a 2 chutes: 100 pontos garantidos",
                        "A partir do 3º chute: -3 pontos por tentativa extra",
                        "Piso garantido de 10 pontos ao acertar o país"
                    ),
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. SizeItUp
                RuleSectionBox(
                    title = "SizeItUp (Escalas)",
                    formula = "ARREDONDAR(Pontuação_Original / 5; 0)",
                    rules = listOf(
                        "Escala proporcional de até 100 pontos",
                        "Ex: 423 pontos no jogo ÷ 5 = 85 pontos no RanKUs"
                    ),
                    color = NeonViolet
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Governance & Limits
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerLowest)
                        .padding(10.dp)
                ) {
                    Text(text = "LIMITES DO CAMPEONATO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• 1 registro diário por jogo por competidor\n• Teto máximo de 100 pontos por partida\n• Teto diário de 300 pontos acumulados\n• Reset diário às 00:00 (UTC-3)", fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreenContainer,
                        contentColor = NeonGreenDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(text = "Entendi as Regras", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RuleSectionBox(
    title: String,
    formula: String,
    rules: List<String>,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerLowest)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceContainerHigh)
                .padding(6.dp)
        ) {
            Text(text = formula, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        rules.forEach { r ->
            Text(text = "• $r", fontSize = 10.sp, color = TextSecondary)
        }
    }
}
