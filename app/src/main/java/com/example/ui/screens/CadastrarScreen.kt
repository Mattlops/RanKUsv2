package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.GameType
import com.example.data.models.ParsedScoreResult
import com.example.data.models.TermoVariant
import com.example.data.parser.ResultParser
import com.example.ui.theme.AlertRed
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
import kotlinx.coroutines.delay

@Composable
fun CadastrarScreen(
    initialText: String? = null,
    canSubmitGameToday: (GameType, TermoVariant?) -> Boolean = { _, _ -> true },
    onSubmitSuccess: (ParsedScoreResult) -> Boolean,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToHome()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedGame by remember { mutableStateOf(GameType.TERMO) }
    var selectedVariant by remember { mutableStateOf(TermoVariant.QUARTETO) }
    var rawText by remember { mutableStateOf(initialText ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var toastPoints by remember { mutableStateOf(100) }
    var duplicateError by remember { mutableStateOf<String?>(null) }

    // Auto-dismiss celebration and return to Home after 1.2s
    LaunchedEffect(showSuccessAnimation) {
        if (showSuccessAnimation) {
            delay(1200)
            showSuccessAnimation = false
            onBackToHome()
        }
    }

    // Official user sample formats for Termo Quarteto, Globle and SizeItUp
    val sampleTermo = "term.ooo/4 #1695 🔥 1\n\n7️⃣8️⃣\n6️⃣9️⃣"
    val sampleGloble = "🌎 Oct 13, 2026 🌍\n🔥 2 | Avg. Guesses: 8.48\n🟨🟨🟧🟥🟧🟥🟩 = 7\n\nhttps://globle-game.com\n#globle"
    val sampleSizeItUp = "Size It Up\nOverall Score 280\n\n🟥⬜⬜⬜⬜ 27\n🟥🟥🟥⬜⬜ 61\n⬜⬜⬜⬜⬜ 0\n🟥🟥🟥🟥🟥 97\n🟥🟥🟥🟥🟥 95\nhttps://magnitudle.com/size-it-up"

    // Parse whenever text changes (null when empty to avoid pre-filling false data)
    val parsedResult = remember(rawText, selectedGame, selectedVariant) {
        if (rawText.isBlank()) {
            null
        } else {
            ResultParser.parseResult(rawText, selectedGame, selectedVariant)
        }
    }

    // Auto-detect and sync selected game and variant
    LaunchedEffect(parsedResult) {
        if (parsedResult != null) {
            if (parsedResult.gameType != selectedGame) {
                selectedGame = parsedResult.gameType
            }
            if (parsedResult.variant != null && parsedResult.variant != selectedVariant) {
                selectedVariant = parsedResult.variant
            }
        }
    }

    LaunchedEffect(initialText) {
        if (!initialText.isNullOrBlank()) {
            rawText = initialText
        }
    }

    val effectiveGame = parsedResult?.gameType ?: selectedGame
    val effectiveVariant = if (effectiveGame == GameType.TERMO) (parsedResult?.variant ?: selectedVariant) else null
    val isAlreadyDoneToday = !canSubmitGameToday(effectiveGame, effectiveVariant)

    Box(modifier = modifier.fillMaxSize().background(DarkBgBase)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Context Header with Back Arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .border(1.dp, OutlineBorder, CircleShape)
                            .testTag("btn_back_home")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar ao Início",
                            tint = NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Cadastrar Resultado",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "Submeta seu resultado diário para o ranking",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Already done today banner
            if (isAlreadyDoneToday) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AlertRed.copy(alpha = 0.12f))
                            .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (effectiveGame == GameType.TERMO && effectiveVariant != null)
                                        "Resultado do Termo (${effectiveVariant.label}) Já Registrado Hoje!"
                                    else
                                        "Resultado de ${effectiveGame.title} Já Registrado Hoje!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (effectiveGame == GameType.TERMO)
                                        "Você já cadastrou o Termo ${effectiveVariant?.label ?: ""} hoje. Você ainda pode registrar os outros modos (Clássico, Dueto ou Quarteto), ou excluir este no Histórico."
                                    else
                                        "Regra: apenas 1 registro por dia para cada jogo. Para refazer, exclua o registro de hoje no menu Histórico.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Season 2026 rule for Termo Clássico / Dueto
            if (effectiveGame == GameType.TERMO && selectedVariant != TermoVariant.QUARTETO) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(TrophyGold.copy(alpha = 0.12f))
                            .border(1.dp, TrophyGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TrophyGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Temporada 2026: Apenas Termo Quarteto Pontua",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrophyGold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Os modos Clássico e Dueto não geram pontos nesta temporada (0 pts). Jogue o Quarteto em term.ooo/4 para pontuar!",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Card: Jogo Competitivo Selection
            item {
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
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Jogo do Resultado",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Auto-detection badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Autodetecção Ativa",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Game Selector Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameType.values().forEach { game ->
                            val isSelected = selectedGame == game
                            val accentColor = when (game) {
                                GameType.TERMO -> NeonGreen
                                GameType.GLOBLE -> ElectricCyan
                                GameType.SIZEITUP -> NeonViolet
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) accentColor.copy(alpha = 0.15f) else SurfaceContainerHigh)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentColor else OutlineBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedGame = game
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = game.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) accentColor else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when (game) {
                                            GameType.TERMO -> "term.ooo/4"
                                            GameType.GLOBLE -> "globle-game"
                                            GameType.SIZEITUP -> "magnitudle"
                                        },
                                        fontSize = 9.sp,
                                        color = if (isSelected) accentColor.copy(alpha = 0.8f) else TextSecondary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Termo Variant Selector (Only shown if Termo)
                    if (selectedGame == GameType.TERMO) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Modalidade do Termo:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TermoVariant.values().forEach { variant ->
                                val isVarSelected = selectedVariant == variant
                                val isQuarteto = variant == TermoVariant.QUARTETO
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isVarSelected) NeonGreen.copy(alpha = 0.15f) else SurfaceContainerLow)
                                        .border(
                                            width = 1.dp,
                                            color = if (isVarSelected) NeonGreen else OutlineBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedVariant = variant }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = variant.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isVarSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isVarSelected) NeonGreen else TextSecondary
                                        )
                                        if (isQuarteto) {
                                            Text(
                                                text = "⭐ Oficial",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TrophyGold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card: Clipboard Input Area
            item {
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
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Texto Copiado do Jogo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Colar da Área de Transferência Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceBright)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clipText = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clipText.isNullOrBlank()) {
                                        rawText = clipText
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ContentPasteGo, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Colar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("result_input_field"),
                        placeholder = {
                            Text(
                                text = "Cole aqui a mensagem de vitória gerada pelo jogo (com emojis, streaks e scores)...",
                                fontSize = 12.sp,
                                color = TextSecondary.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = OutlineBorder,
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedContainerColor = SurfaceContainerLowest,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Clear Button if text present
                    if (rawText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Limpar campo",
                                fontSize = 10.sp,
                                color = AlertRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { rawText = "" }
                            )
                        }
                    }
                }
            }

            // Card: Decoder Preview & Formula Breakdown
            item {
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
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pré-visualização Decodificada",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (parsedResult != null) {
                            val statusColor = if (parsedResult.isValid) NeonGreen else AlertRed
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (parsedResult.isValid) "Formato Válido" else "Verifique o formato",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (parsedResult != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLowest)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = parsedResult.gameType.title + (parsedResult.variant?.let { " (${it.label})" } ?: ""),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = parsedResult.details,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                if (parsedResult.streak != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TrophyGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "🔥 ${parsedResult.streak} d",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TrophyGold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Formula breakdown pill
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerHigh)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Pontuação Calculada", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                    Text(
                                        text = "+${parsedResult.score} pts",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (parsedResult.score > 0) NeonGreen else TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = parsedResult.formulaDisplay,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Decoded Emoji Grid
                            if (parsedResult.rawGrid.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHigh.copy(alpha = 0.3f))
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    parsedResult.rawGrid.take(6).forEach { row ->
                                        Text(
                                            text = row,
                                            fontSize = 13.sp,
                                            letterSpacing = 2.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty State: Waiting for result input
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLowest)
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPasteGo,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Aguardando seu resultado...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cole o resultado copiado do jogo ou toque em um exemplo abaixo para testar:",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHigh)
                                        .clickable { rawText = sampleTermo }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Ex. Termo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHigh)
                                        .clickable { rawText = sampleGloble }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Ex. Globle", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceContainerHigh)
                                        .clickable { rawText = sampleSizeItUp }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Ex. SizeItUp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonViolet)
                                }
                            }
                        }
                    }
                }
            }

            // Primary Submit CTA
            item {
                val canSubmit = parsedResult != null && parsedResult.isValid && !isSubmitting && !isAlreadyDoneToday
                Button(
                    onClick = {
                        if (canSubmit && !isSubmitting && parsedResult != null) {
                            isSubmitting = true
                            duplicateError = null
                            coroutineScope.launch {
                                val success = onSubmitSuccess(parsedResult)
                                isSubmitting = false
                                if (success) {
                                    toastPoints = parsedResult.score
                                    showSuccessAnimation = true
                                } else {
                                    duplicateError = "Este jogo já foi registrado hoje! Apenas 1 registro por dia é permitido."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmit) NeonGreenContainer else SurfaceContainerHigh,
                        contentColor = if (canSubmit) NeonGreenDark else TextSecondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_score_btn")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = NeonGreenDark,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Validando e Computando...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                isAlreadyDoneToday -> "Jogo já registrado hoje"
                                parsedResult == null -> "Cole seu resultado para pontuar"
                                else -> "Confirmar e Registrar Pontos"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Prominent Green Checkmark Celebration Overlay with auto-dismiss and touch dismiss
        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable {
                        showSuccessAnimation = false
                        onBackToHome()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkBgSurface)
                        .border(2.dp, NeonGreen, RoundedCornerShape(24.dp))
                        .clickable {
                            showSuccessAnimation = false
                            onBackToHome()
                        }
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Green Checkmark
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .border(3.dp, NeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Sucesso",
                            tint = NeonGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Resultado Salvo!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "+$toastPoints pontos registrados no RanKUs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Retornando para a tela inicial...",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showSuccessAnimation = false
                            onBackToHome()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreenContainer,
                            contentColor = NeonGreenDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(text = "Ir para o Início", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
