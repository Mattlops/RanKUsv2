package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GameType
import com.example.data.models.ScoreHistoryItem
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBgBase
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
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrophyGold

@Composable
fun HistoricoScreen(
    scoreHistory: List<ScoreHistoryItem>,
    currentUserId: String = "",
    onDeleteRecord: (ScoreHistoryItem) -> Unit,
    onNavigateToJogos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<GameType?>(null) }
    var itemToDelete by remember { mutableStateOf<ScoreHistoryItem?>(null) }

    // Strictly show ONLY records belonging to current logged-in user
    val userRecords = remember(scoreHistory, currentUserId) {
        if (currentUserId.isBlank()) scoreHistory
        else scoreHistory.filter { it.userId == currentUserId }
    }

    val filteredList = remember(userRecords, selectedFilter) {
        if (selectedFilter == null) userRecords
        else userRecords.filter { it.gameType == selectedFilter }
    }

    val totalPoints = remember(userRecords) { userRecords.sumOf { it.score } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgBase)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Card
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Histórico de Registros",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Audite ou exclua suas submissões",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Total points pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceBright)
                            .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalPoints pts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total de Partidas", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "${scoreHistory.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Hoje", fontSize = 10.sp, color = TextSecondary)
                        val todayCount = scoreHistory.count { it.isToday }
                        Text(text = "$todayCount / 3 jogos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Temporada", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "2026 Ativa", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Game Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "Todos (${scoreHistory.size})",
                isSelected = selectedFilter == null,
                onClick = { selectedFilter = null },
                modifier = Modifier.weight(1f)
            )
            FilterChipItem(
                label = "Termo",
                isSelected = selectedFilter == GameType.TERMO,
                onClick = { selectedFilter = GameType.TERMO },
                modifier = Modifier.weight(1f)
            )
            FilterChipItem(
                label = "Globle",
                isSelected = selectedFilter == GameType.GLOBLE,
                onClick = { selectedFilter = GameType.GLOBLE },
                modifier = Modifier.weight(1f)
            )
            FilterChipItem(
                label = "SizeItUp",
                isSelected = selectedFilter == GameType.SIZEITUP,
                onClick = { selectedFilter = GameType.SIZEITUP },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Records List or Empty State
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .border(1.dp, OutlineBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhum Registro Encontrado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Você ainda não possui resultados salvos para este filtro. Jogue hoje e registre seus pontos!",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onNavigateToJogos,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreenContainer,
                            contentColor = NeonGreenDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Acessar Jogos Oficiais", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { record ->
                    HistoryRecordCard(
                        record = record,
                        onDeleteClick = { itemToDelete = record }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Delete Confirmation / Alert Popup Dialogs
    itemToDelete?.let { record ->
        if (record.isToday) {
            // Delete TODAY's score -> Allows re-submitting today
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                containerColor = SurfaceContainerHigh,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Excluir resultado de hoje? 🔄",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Você está prestes a excluir o resultado de hoje do ${record.gameType.title} (+${record.score} pts).",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonGreen.copy(alpha = 0.1f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "✅ Como este resultado é de HOJE, você poderá registrar um novo resultado para este jogo após a exclusão.",
                                fontSize = 11.sp,
                                color = NeonGreen,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 15.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteRecord(record)
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Excluir e Liberar Jogo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { itemToDelete = null },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            )
        } else {
            // Delete PAST DAY's score -> WARN CANNOT BE RE-RECORDED
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                containerColor = SurfaceContainerHigh,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TrophyGold,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Atenção: Registro Antigo ⚠️",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = TrophyGold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Este registro de ${record.gameType.title} é de um dia anterior (${record.dateKey}).",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AlertRed.copy(alpha = 0.12f))
                                .border(1.dp, AlertRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "⚠️ AVISO IMPORTANTE:\nSe você excluí-lo, os ${record.score} pontos serão subtraídos do seu total e NÃO será possível registrar novamente este resultado do dia passado.",
                                fontSize = 11.sp,
                                color = AlertRed,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Deseja realmente excluir permanentemente?",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteRecord(record)
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Excluir Definitivamente", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { itemToDelete = null },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun HistoryRecordCard(
    record: ScoreHistoryItem,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val accentColor = when (record.gameType) {
        GameType.TERMO -> NeonGreen
        GameType.GLOBLE -> ElectricCyan
        GameType.SIZEITUP -> NeonViolet
    }

    val iconLetter = when (record.gameType) {
        GameType.TERMO -> "T"
        GameType.GLOBLE -> "G"
        GameType.SIZEITUP -> "S"
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Game letter badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconLetter,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (record.variant != null) "${record.gameType.title} ${record.variant.label}" else record.gameType.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (record.isToday) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "HOJE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen
                                )
                            }
                        } else {
                            Text(
                                text = record.dateKey,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (record.details.isNotBlank()) {
                        Text(
                            text = record.details,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Right side: Points pill + Delete button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceBright)
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${record.score} pts",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (record.score > 0) accentColor else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceBright)
                        .testTag("delete_record_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir registro",
                        tint = AlertRed.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Expandable Raw Grid Section
        if (record.rawGrid.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Ocultar grid do jogo" else "Ver grid de emojis do jogo",
                    fontSize = 11.sp,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(10.dp)
                ) {
                    record.rawGrid.forEach { line ->
                        Text(
                            text = line,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonGreen.copy(alpha = 0.15f) else SurfaceContainerHigh)
            .border(
                width = 1.dp,
                color = if (isSelected) NeonGreen else OutlineBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) NeonGreen else TextSecondary
        )
    }
}
