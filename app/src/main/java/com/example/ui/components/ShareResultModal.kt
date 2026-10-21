package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.models.DailyGameStatus
import com.example.data.models.GameType
import com.example.data.models.ScoreHistoryItem
import com.example.data.models.UserProfile
import com.example.ui.theme.DarkBgBase
import com.example.ui.theme.DarkBgSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenContainer
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ShareResultModal(
    userProfile: UserProfile,
    dailyGames: List<DailyGameStatus>,
    scoreHistory: List<ScoreHistoryItem>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isSavedSuccessfully by remember { mutableStateOf(false) }

    val todayStr = remember { LocalDate.now().toString() }
    val todayScores = remember(scoreHistory, todayStr) {
        scoreHistory.filter { it.dateKey == todayStr }
    }

    LaunchedEffect(userProfile, dailyGames, todayScores) {
        withContext(Dispatchers.IO) {
            val avatarBitmap: Bitmap? = try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(userProfile.avatarUrl)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                result?.toBitmap()
            } catch (_: Exception) {
                null
            }

            val bitmap = generateShareCardBitmap(
                context = context,
                user = userProfile,
                dailyGames = dailyGames,
                todayScores = todayScores,
                avatarBitmap = avatarBitmap
            )
            withContext(Dispatchers.Main) {
                cardBitmap = bitmap
                isGenerating = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color(0xD9000000))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                color = DarkBgSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Compartilhar Resultado",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Seu card oficial do dia pronto para postar",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .testTag("close_share_modal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Image Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkBgBase)
                            .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGenerating || cardBitmap == null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = NeonGreen,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Gerando card em alta resolução...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            Image(
                                bitmap = cardBitmap!!.asImageBitmap(),
                                contentDescription = "Card de resultado gerado",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons: Salvar Imagem & Compartilhar Resultado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Button 1: Salvar Imagem
                        OutlinedButton(
                            onClick = {
                                val bmp = cardBitmap ?: return@OutlinedButton
                                isSaving = true
                                scope.launch(Dispatchers.IO) {
                                    val success = saveBitmapToDevice(context, bmp)
                                    withContext(Dispatchers.Main) {
                                        isSaving = false
                                        if (success) {
                                            isSavedSuccessfully = true
                                            Toast.makeText(
                                                context,
                                                "Imagem salva na galeria do dispositivo!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Falha ao salvar a imagem.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            enabled = cardBitmap != null && !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_image_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSavedSuccessfully) NeonGreen else OutlineBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isSavedSuccessfully) NeonGreen else TextPrimary,
                                containerColor = SurfaceContainer
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = NeonGreen,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isSavedSuccessfully) Icons.Default.Check else Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSavedSuccessfully) NeonGreen else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSavedSuccessfully) "Salva!" else "Salvar imagem",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Button 2: Compartilhar Resultado
                        Button(
                            onClick = {
                                val bmp = cardBitmap ?: return@Button
                                scope.launch(Dispatchers.IO) {
                                    shareBitmap(context, bmp, userProfile)
                                }
                            },
                            enabled = cardBitmap != null,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("share_result_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreenContainer,
                                contentColor = NeonGreenDark
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Compartilhar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Generates high-definition 1080x1080 square gamer card Bitmap with all requested details.
 */
fun generateShareCardBitmap(
    context: Context,
    user: UserProfile,
    dailyGames: List<DailyGameStatus>,
    todayScores: List<ScoreHistoryItem>,
    avatarBitmap: Bitmap?
): Bitmap {
    val size = 1080
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background color
    val bgPaint = Paint().apply {
        color = Color.parseColor("#0F1218")
        isAntiAlias = true
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    // Corner Neon Glow Effects
    val glowGreen = Paint().apply {
        isAntiAlias = true
        shader = LinearGradient(
            0f, 0f, 400f, 400f,
            Color.parseColor("#3300E676"),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, 400f, 400f, glowGreen)

    val glowCyan = Paint().apply {
        isAntiAlias = true
        shader = LinearGradient(
            size.toFloat(), size.toFloat(), size.toFloat() - 400f, size.toFloat() - 400f,
            Color.parseColor("#2800E5FF"),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(size.toFloat() - 400f, size.toFloat() - 400f, size.toFloat(), size.toFloat(), glowCyan)

    // Outer Neon Border
    val borderPaint = Paint().apply {
        color = Color.parseColor("#00E676")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    canvas.drawRoundRect(RectF(16f, 16f, size - 16f, size - 16f), 32f, 32f, borderPaint)

    // Header: RanKUs Title & Current Date
    val headerPaint = Paint().apply {
        color = Color.parseColor("#00E676")
        textSize = 48f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText("RanKUs", 70f, 100f, headerPaint)

    val subHeaderPaint = Paint().apply {
        color = Color.parseColor("#7C8BA1")
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText("ARENA DIÁRIA", 260f, 96f, subHeaderPaint)

    // Date Badge on Top-Right
    val todayFormatted = LocalDate.now().format(
        DateTimeFormatter.ofPattern("dd 'DE' MMM 'DE' yyyy", Locale("pt", "BR"))
    ).uppercase()

    val dateBadgeRect = RectF(size - 440f, 56f, size - 70f, 114f)
    val dateBgPaint = Paint().apply {
        color = Color.parseColor("#1B212D")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawRoundRect(dateBadgeRect, 20f, 20f, dateBgPaint)

    val dateTextPaint = Paint().apply {
        color = Color.parseColor("#CBD5E1")
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText(todayFormatted, dateBadgeRect.centerX(), dateBadgeRect.centerY() + 8f, dateTextPaint)

    // Separator line
    val linePaint = Paint().apply {
        color = Color.parseColor("#263143")
        strokeWidth = 2f
        isAntiAlias = true
    }
    canvas.drawLine(70f, 140f, size - 70f, 140f, linePaint)

    // User Profile Section (Y = 160 to 320)
    val avatarRadius = 70f
    val avatarCenterX = 70f + avatarRadius
    val avatarCenterY = 230f

    // Avatar ring
    val avatarRingPaint = Paint().apply {
        color = Color.parseColor("#00E676")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius + 4f, avatarRingPaint)

    if (avatarBitmap != null) {
        val roundedAvatar = getCircularBitmap(avatarBitmap, (avatarRadius * 2).toInt())
        canvas.drawBitmap(roundedAvatar, avatarCenterX - avatarRadius, avatarCenterY - avatarRadius, null)
    } else {
        val fallbackAvatarPaint = Paint().apply {
            color = Color.parseColor("#1F2735")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, fallbackAvatarPaint)

        val initialsPaint = Paint().apply {
            color = Color.parseColor("#00E676")
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val initialChar = user.name.firstOrNull()?.uppercase() ?: "G"
        canvas.drawText(initialChar, avatarCenterX, avatarCenterY + 18f, initialsPaint)
    }

    // Player Name & Handle (Aligned with Avatar Center Y)
    val textStartX = avatarCenterX + avatarRadius + 32f
    val maxNameWidth = (size - 70f) - textStartX

    val namePaint = Paint().apply {
        color = Color.WHITE
        textSize = 42f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    while (namePaint.measureText(user.name) > maxNameWidth && namePaint.textSize > 24f) {
        namePaint.textSize -= 2f
    }
    canvas.drawText(user.name, textStartX, avatarCenterY - 10f, namePaint)

    val handlePaint = Paint().apply {
        color = Color.parseColor("#8E9AA8")
        textSize = 24f
        isAntiAlias = true
    }
    val handleText = if (user.handle.startsWith("@")) user.handle else "@${user.handle}"
    canvas.drawText(handleText, textStartX, avatarCenterY + 30f, handlePaint)

    // Stats Section (Pontuação Hoje + Sequência) (Y = 330 to 450)
    val card1Rect = RectF(70f, 330f, 520f, 450f)
    val card2Rect = RectF(560f, 330f, size - 70f, 450f)

    val statBgPaint = Paint().apply {
        color = Color.parseColor("#171D26")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val statBorderPaint = Paint().apply {
        color = Color.parseColor("#263242")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    canvas.drawRoundRect(card1Rect, 20f, 20f, statBgPaint)
    canvas.drawRoundRect(card1Rect, 20f, 20f, statBorderPaint)

    canvas.drawRoundRect(card2Rect, 20f, 20f, statBgPaint)
    canvas.drawRoundRect(card2Rect, 20f, 20f, statBorderPaint)

    val statLabelPaint = Paint().apply {
        color = Color.parseColor("#94A3B8")
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText("PONTUAÇÃO HOJE", card1Rect.left + 24f, card1Rect.top + 40f, statLabelPaint)
    canvas.drawText("SEQUÊNCIA ATUAL", card2Rect.left + 24f, card2Rect.top + 40f, statLabelPaint)

    val statValGreen = Paint().apply {
        color = Color.parseColor("#00E676")
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val todayPts = if (user.todayScore > 0) user.todayScore else dailyGames.sumOf { it.points }
    canvas.drawText("$todayPts pts", card1Rect.left + 24f, card1Rect.top + 96f, statValGreen)

    val statValFlame = Paint().apply {
        color = Color.parseColor("#FFA726")
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText("🔥 ${user.streakDays} dias", card2Rect.left + 24f, card2Rect.top + 96f, statValFlame)

    // Game Results Title (Y = 495)
    val resultsTitlePaint = Paint().apply {
        color = Color.parseColor("#94A3B8")
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = 0.05f
        isAntiAlias = true
    }
    canvas.drawText("RESUMO DOS JOGOS DIÁRIOS", 70f, 500f, resultsTitlePaint)

    // Identify results and status for each game
    val termoScore = todayScores.firstOrNull { it.gameType == GameType.TERMO }
    val globleScore = todayScores.firstOrNull { it.gameType == GameType.GLOBLE }
    val sizeitupScore = todayScores.firstOrNull { it.gameType == GameType.SIZEITUP }

    val termoGame = dailyGames.firstOrNull { it.gameType == GameType.TERMO }
    val globleGame = dailyGames.firstOrNull { it.gameType == GameType.GLOBLE }
    val sizeitupGame = dailyGames.firstOrNull { it.gameType == GameType.SIZEITUP }

    // Termo Result Text
    val termoText = when {
        termoScore == null && (termoGame == null || !termoGame.isCompleted) -> "🫪 N fiz"
        termoScore != null && (termoScore.score <= 0 || termoScore.details.contains("Deu ruim", ignoreCase = true)) -> "🤡 Deu ruim"
        termoScore != null -> {
            val wordsSolved = Regex("""(\d+)/4\s*palavras""").find(termoScore.details)?.groupValues?.get(1)?.toIntOrNull()
                ?: Regex("""(\d+)\s*palavras""").find(termoScore.details)?.groupValues?.get(1)?.toIntOrNull()
                ?: 4

            if (wordsSolved < 4 && termoScore.score < 60) {
                "🤡 Deu ruim"
            } else {
                val tries = Regex("""(\d+)/9\s*tentativas""").find(termoScore.details)?.groupValues?.get(1)?.toIntOrNull()
                    ?: Regex("""em\s*(\d+)\s*tentativas""").find(termoScore.details)?.groupValues?.get(1)?.toIntOrNull()
                    ?: run {
                        val listMatch = Regex("""\[(.*?)\]""").find(termoScore.details)?.groupValues?.get(1)
                        if (listMatch != null) {
                            val nums = listMatch.split(",").mapNotNull { it.trim().toIntOrNull() }
                            if (nums.isNotEmpty()) nums.maxOrNull() else null
                        } else null
                    } ?: 9
                "$wordsSolved palavras em $tries tentativas"
            }
        }
        termoGame?.isCompleted == true -> {
            if (termoGame.points <= 0) "🤡 Deu ruim" else "4 palavras acertadas"
        }
        else -> "🫪 N fiz"
    }

    // Globle Result Text
    val globleText = when {
        globleScore == null && (globleGame == null || !globleGame.isCompleted) -> "🫪 N fiz"
        globleScore != null && globleScore.score <= 0 -> "🤡 Deu ruim"
        globleScore != null -> {
            val tries = Regex("""(\d+)\s*(?:palpites|tentativas|guesses|chutes)""").find(globleScore.details)?.groupValues?.get(1)?.toIntOrNull()
                ?: Regex("""=\s*(\d+)""").find(globleScore.details)?.groupValues?.get(1)?.toIntOrNull()
                ?: Regex("""(\d+)""").find(globleScore.details)?.groupValues?.get(1)?.toIntOrNull()
                ?: 6
            "Acertou em $tries tentativas"
        }
        globleGame?.isCompleted == true -> {
            if (globleGame.points <= 0) "🤡 Deu ruim" else "${globleGame.points} pts"
        }
        else -> "🫪 N fiz"
    }

    // SizeItUp Result Text (Displays original Overall Score, not scaled RanKUs score)
    val sizeitupText = when {
        sizeitupScore == null && (sizeitupGame == null || !sizeitupGame.isCompleted) -> "🫪 N fiz"
        sizeitupScore != null -> {
            val originalScore = Regex("""Overall\s*Score[:\s]+(\d+)""", RegexOption.IGNORE_CASE)
                .find(sizeitupScore.details)?.groupValues?.get(1)?.toIntOrNull()
                ?: sizeitupScore.rawGrid.firstNotNullOfOrNull { line ->
                    Regex("""Overall\s*Score[:\s]+(\d+)""", RegexOption.IGNORE_CASE)
                        .find(line)?.groupValues?.get(1)?.toIntOrNull()
                }
                ?: Regex("""(\d{2,4})""").find(sizeitupScore.details)?.groupValues?.get(1)?.toIntOrNull()
                ?: (sizeitupScore.score * 5)

            "$originalScore pts"
        }
        sizeitupGame?.isCompleted == true -> {
            val originalScore = sizeitupGame.points * 5
            "$originalScore pts"
        }
        else -> "🫪 N fiz"
    }

    // 3 Game Result Cards (Strict Y spacing, no overlap)
    drawGameRow(
        canvas = canvas,
        topY = 530f,
        badgeBg = Color.parseColor("#00E676"),
        badgeIconChar = "T",
        gameName = "Termo Quarteto",
        gameCategory = "Palavras Diárias",
        resultText = termoText,
        resultColor = when {
            termoText.contains("N fiz") -> Color.parseColor("#94A3B8")
            termoText.contains("Deu ruim") -> Color.parseColor("#FF5252")
            else -> Color.parseColor("#00E676")
        },
        resultBgColor = when {
            termoText.contains("N fiz") -> Color.parseColor("#171D26")
            termoText.contains("Deu ruim") -> Color.parseColor("#261416")
            else -> Color.parseColor("#10241A")
        },
        resultBorderColor = when {
            termoText.contains("N fiz") -> Color.parseColor("#2B3647")
            termoText.contains("Deu ruim") -> Color.parseColor("#5C2023")
            else -> Color.parseColor("#1E4D34")
        }
    )

    drawGameRow(
        canvas = canvas,
        topY = 665f,
        badgeBg = Color.parseColor("#00E5FF"),
        badgeIconChar = "G",
        gameName = "Globle",
        gameCategory = "Geografia",
        resultText = globleText,
        resultColor = when {
            globleText.contains("N fiz") -> Color.parseColor("#94A3B8")
            globleText.contains("Deu ruim") -> Color.parseColor("#FF5252")
            else -> Color.parseColor("#00E5FF")
        },
        resultBgColor = when {
            globleText.contains("N fiz") -> Color.parseColor("#171D26")
            globleText.contains("Deu ruim") -> Color.parseColor("#261416")
            else -> Color.parseColor("#10222B")
        },
        resultBorderColor = when {
            globleText.contains("N fiz") -> Color.parseColor("#2B3647")
            globleText.contains("Deu ruim") -> Color.parseColor("#5C2023")
            else -> Color.parseColor("#1D4B59")
        }
    )

    drawGameRow(
        canvas = canvas,
        topY = 800f,
        badgeBg = Color.parseColor("#D500F9"),
        badgeIconChar = "S",
        gameName = "SizeItUp",
        gameCategory = "Lógica & Tamanho",
        resultText = sizeitupText,
        resultColor = when {
            sizeitupText.contains("N fiz") -> Color.parseColor("#94A3B8")
            sizeitupText.contains("Deu ruim") -> Color.parseColor("#FF5252")
            else -> Color.parseColor("#E040FB")
        },
        resultBgColor = when {
            sizeitupText.contains("N fiz") -> Color.parseColor("#171D26")
            sizeitupText.contains("Deu ruim") -> Color.parseColor("#261416")
            else -> Color.parseColor("#22122B")
        },
        resultBorderColor = when {
            sizeitupText.contains("N fiz") -> Color.parseColor("#2B3647")
            sizeitupText.contains("Deu ruim") -> Color.parseColor("#5C2023")
            else -> Color.parseColor("#501B61")
        }
    )

    // Footer Branding (Y = 980 to 1020)
    val footerPaint = Paint().apply {
        color = Color.parseColor("#64748B")
        textSize = 21f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText("RanKUs • A arena competitiva dos jogos mentais diários", size / 2f, 980f, footerPaint)

    return bitmap
}

private fun drawGameRow(
    canvas: Canvas,
    topY: Float,
    badgeBg: Int,
    badgeIconChar: String,
    gameName: String,
    gameCategory: String,
    resultText: String,
    resultColor: Int,
    resultBgColor: Int,
    resultBorderColor: Int
) {
    val rowHeight = 116f
    val rowRect = RectF(70f, topY, 1080f - 70f, topY + rowHeight)

    // Row card background and border
    val rowBg = Paint().apply {
        color = Color.parseColor("#151A22")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val rowBorder = Paint().apply {
        color = Color.parseColor("#263242")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    canvas.drawRoundRect(rowRect, 18f, 18f, rowBg)
    canvas.drawRoundRect(rowRect, 18f, 18f, rowBorder)

    // Left Icon Badge (64x64 rounded square)
    val badgeSize = 64f
    val badgeLeft = rowRect.left + 22f
    val badgeTop = rowRect.centerY() - (badgeSize / 2f)
    val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeSize, badgeTop + badgeSize)

    val badgeBgPaint = Paint().apply {
        color = badgeBg
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawRoundRect(badgeRect, 14f, 14f, badgeBgPaint)

    val badgeTextPaint = Paint().apply {
        color = Color.parseColor("#0F1218")
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    canvas.drawText(badgeIconChar, badgeRect.centerX(), badgeRect.centerY() + 13f, badgeTextPaint)

    // Right Result Badge / Pill Area
    // Calculate width needed for result
    val resultPaint = Paint().apply {
        color = resultColor
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    var measuredTextWidth = resultPaint.measureText(resultText)
    val maxAvailableResultWidth = 430f
    while (measuredTextWidth > (maxAvailableResultWidth - 36f) && resultPaint.textSize > 16f) {
        resultPaint.textSize -= 1f
        measuredTextWidth = resultPaint.measureText(resultText)
    }

    val pillPaddingHorizontal = 20f
    val pillWidth = (measuredTextWidth + (pillPaddingHorizontal * 2f)).coerceIn(160f, maxAvailableResultWidth)
    val pillHeight = 52f
    val pillRight = rowRect.right - 22f
    val pillLeft = pillRight - pillWidth
    val pillTop = rowRect.centerY() - (pillHeight / 2f)
    val pillBottom = rowRect.centerY() + (pillHeight / 2f)
    val pillRect = RectF(pillLeft, pillTop, pillRight, pillBottom)

    val pillBgPaint = Paint().apply {
        color = resultBgColor
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val pillBorderPaint = Paint().apply {
        color = resultBorderColor
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    canvas.drawRoundRect(pillRect, 14f, 14f, pillBgPaint)
    canvas.drawRoundRect(pillRect, 14f, 14f, pillBorderPaint)

    // Center result text inside pillRect
    val fontMetrics = resultPaint.fontMetrics
    val textCenterY = pillRect.centerY() - (fontMetrics.descent + fontMetrics.ascent) / 2f
    canvas.drawText(resultText, pillRect.centerX(), textCenterY, resultPaint)

    // Left Title Area: Game Name and Category
    // Bounded strictly between badgeRect.right + 20f and pillLeft - 16f to PREVENT OVERLAPPING!
    val titleStartX = badgeRect.right + 20f
    val maxTitleWidth = (pillLeft - 16f) - titleStartX

    val namePaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    while (namePaint.measureText(gameName) > maxTitleWidth && namePaint.textSize > 18f) {
        namePaint.textSize -= 1f
    }
    canvas.drawText(gameName, titleStartX, rowRect.centerY() - 6f, namePaint)

    val categoryPaint = Paint().apply {
        color = Color.parseColor("#7C8BA1")
        textSize = 19f
        isAntiAlias = true
    }
    while (categoryPaint.measureText(gameCategory) > maxTitleWidth && categoryPaint.textSize > 14f) {
        categoryPaint.textSize -= 1f
    }
    canvas.drawText(gameCategory, titleStartX, rowRect.centerY() + 24f, categoryPaint)
}

private fun getCircularBitmap(bitmap: Bitmap, diameter: Int): Bitmap {
    val output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
    }
    val radius = diameter / 2f

    canvas.drawCircle(radius, radius, radius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
    val dstRect = Rect(0, 0, diameter, diameter)
    canvas.drawBitmap(bitmap, srcRect, dstRect, paint)

    return output
}

/**
 * Saves bitmap to user's gallery using MediaStore (Android 10+ compliant).
 */
fun saveBitmapToDevice(context: Context, bitmap: Bitmap): Boolean {
    val filename = "RanKUs_Resultado_${LocalDate.now()}_${System.currentTimeMillis()}.png"
    var outputStream: OutputStream? = null

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/RanKUs")
            }
            val uri: Uri? = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (uri != null) {
                outputStream = context.contentResolver.openOutputStream(uri)
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val rankusDir = File(imagesDir, "RanKUs")
            if (!rankusDir.exists()) rankusDir.mkdirs()
            val imageFile = File(rankusDir, filename)
            outputStream = FileOutputStream(imageFile)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        outputStream?.close()
    }
}

/**
 * Shares bitmap via Android ShareSheet using FileProvider.
 */
fun shareBitmap(context: Context, bitmap: Bitmap, user: UserProfile) {
    try {
        val cachePath = File(context.cacheDir, "shared_images")
        if (!cachePath.exists()) cachePath.mkdirs()

        val imageFile = File(cachePath, "rankus_resultado.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(
                Intent.EXTRA_TEXT,
                "Meu resultado diário no RanKUs! 🎮🏆\nConfira meu placar nos jogos Termo Quarteto, Globle e SizeItUp!"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartilhar meu resultado")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Erro ao abrir compartilhamento: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
