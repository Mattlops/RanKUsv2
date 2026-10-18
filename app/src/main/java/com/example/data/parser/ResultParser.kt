package com.example.data.parser

import com.example.data.models.GameType
import com.example.data.models.ParsedScoreResult
import com.example.data.models.TermoVariant
import kotlin.math.max
import kotlin.math.roundToInt

object ResultParser {

    fun parseResult(rawText: String, forcedGame: GameType? = null, forcedVariant: TermoVariant? = null): ParsedScoreResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return ParsedScoreResult(
                gameType = forcedGame ?: GameType.TERMO,
                variant = forcedVariant ?: TermoVariant.QUARTETO,
                score = 0,
                isValid = false,
                errorMessage = "Cole o resultado copiado do jogo."
            )
        }

        // Auto-detect game if not forced
        val detectedGame = when {
            forcedGame != null -> forcedGame
            trimmed.contains("globle", ignoreCase = true) ||
                trimmed.contains("avg. guesses", ignoreCase = true) ||
                trimmed.contains("globle-game.com", ignoreCase = true) ||
                (trimmed.contains("🌍") || trimmed.contains("🌎")) -> GameType.GLOBLE

            trimmed.contains("size it up", ignoreCase = true) ||
                trimmed.contains("sizeitup", ignoreCase = true) ||
                trimmed.contains("overall score", ignoreCase = true) ||
                trimmed.contains("magnitudle", ignoreCase = true) -> GameType.SIZEITUP

            trimmed.contains("term.ooo", ignoreCase = true) ||
                trimmed.contains("termo", ignoreCase = true) ||
                trimmed.contains("quarteto", ignoreCase = true) ||
                trimmed.contains("7️⃣") || trimmed.contains("8️⃣") ||
                trimmed.contains("6️⃣") || trimmed.contains("9️⃣") ||
                trimmed.contains("🟩") || trimmed.contains("🟨") -> GameType.TERMO

            else -> forcedGame ?: GameType.TERMO
        }

        return when (detectedGame) {
            GameType.TERMO -> parseTermo(trimmed, forcedVariant)
            GameType.GLOBLE -> parseGloble(trimmed)
            GameType.SIZEITUP -> parseSizeItUp(trimmed)
        }
    }

    private fun parseTermo(text: String, forcedVariant: TermoVariant?): ParsedScoreResult {
        // Extract streak e.g. 🔥 1 or 🔥 2
        val streakRegex = Regex("""🔥\s*(\d+)""")
        val streak = streakRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()

        // Extract game number e.g. #1695 or #742
        val gameNumRegex = Regex("""#?(\d{2,5})""")
        val gameNum = gameNumRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 1695

        val isQuartetoExplicit = text.contains("term.ooo/4", ignoreCase = true) ||
            text.contains("quarteto", ignoreCase = true) ||
            (forcedVariant == TermoVariant.QUARTETO)

        val isDuetoExplicit = text.contains("term.ooo/2", ignoreCase = true) ||
            text.contains("dueto", ignoreCase = true) ||
            (forcedVariant == TermoVariant.DUETO)

        // Keycap emojis: 1️⃣ to 9️⃣, 🔟, or ❌ / X
        // Unicode: [1-9] followed by optional \uFE0F and \u20E3, \uD83D\uDD1F (🔟), or [❌✖️Xx]
        val keycapRegex = Regex("""([1-9]|10)\uFE0F?\u20E3|\uD83D\uDD1F|([❌✖️Xx])\uFE0F?""")
        val keycapMatches = keycapRegex.findAll(text).toList()

        // If 4 keycaps found (like 7️⃣8️⃣ / 6️⃣9️⃣) or URL is term.ooo/4, it's Quarteto!
        val isQuarteto = isQuartetoExplicit || keycapMatches.size >= 4 || forcedVariant == null

        val variant = when {
            forcedVariant != null -> forcedVariant
            isQuarteto -> TermoVariant.QUARTETO
            isDuetoExplicit -> TermoVariant.DUETO
            else -> TermoVariant.CLASSICO
        }

        val maxTries = variant.maxTries
        var wordsSolved = 0
        var triesUsed = 0
        var remainingTries = 0
        var score = 0
        var formulaDisplay = ""
        var details = ""
        val gridLines = mutableListOf<String>()

        if (variant == TermoVariant.QUARTETO) {
            // Check if we have keycap results (e.g. 7️⃣8️⃣ \n 6️⃣9️⃣)
            if (keycapMatches.size >= 4) {
                val attempts = keycapMatches.take(4).map { match ->
                    val txt = match.value
                    when {
                        match.groupValues[1].isNotEmpty() -> match.groupValues[1].toInt()
                        txt == "\uD83D\uDD1F" -> 10
                        match.groupValues[2].isNotEmpty() -> -1 // Not solved
                        else -> -1
                    }
                }

                val solvedAttempts = attempts.filter { it in 1..9 }
                wordsSolved = solvedAttempts.size
                val allSolved = wordsSolved == 4

                if (allSolved) {
                    // In Quarteto, all 4 words are solved in parallel.
                    // The player's game ends on the maximum attempt among the 4 words!
                    triesUsed = solvedAttempts.maxOrNull() ?: 9
                    remainingTries = (9 - triesUsed).coerceAtLeast(0)
                } else {
                    // Incomplete game uses all 9 tries
                    triesUsed = 9
                    remainingTries = 0
                }

                // Official formula: (Palavras_Acertadas * 15) + (Tentativas_Sobrando * 8)
                val wordsPoints = wordsSolved * 15
                val bonusPoints = remainingTries * 8
                score = (wordsPoints + bonusPoints).coerceIn(0, 100)

                formulaDisplay = "($wordsSolved Palavras × 15) + ($remainingTries Restantes × 8) = $score pts"
                details = buildString {
                    append("Tentativas: [${attempts.joinToString(", ") { if (it > 0) it.toString() else "X" }}]")
                    append(" · $wordsSolved/4 palavras")
                    append(" · $triesUsed/9 tentativas")
                    if (streak != null) append(" · 🔥 Streak: $streak")
                }

                // Extract emoji lines
                text.lines().forEach { line ->
                    if (keycapRegex.containsMatchIn(line)) {
                        gridLines.add(line.trim())
                    }
                }
            } else {
                // Fallback ratio parsing e.g. 4/9, 9/9, X/9
                val triesRatioRegex = Regex("""([1-9X]|10)/9""", RegexOption.IGNORE_CASE)
                val triesMatch = triesRatioRegex.find(text)
                val solvedAll = triesMatch?.groupValues?.get(1)?.equals("X", ignoreCase = true) != true
                val used = triesMatch?.groupValues?.get(1)?.toIntOrNull() ?: 9

                wordsSolved = if (solvedAll) 4 else 3
                triesUsed = if (solvedAll) used else 9
                remainingTries = if (solvedAll) (9 - triesUsed).coerceAtLeast(0) else 0

                val wordsPoints = wordsSolved * 15
                val bonusPoints = remainingTries * 8
                score = (wordsPoints + bonusPoints).coerceIn(0, 100)

                formulaDisplay = "($wordsSolved Palavras × 15) + ($remainingTries Restantes × 8) = $score pts"
                details = "$triesUsed/9 tentativas · $wordsSolved/4 palavras" + (if (streak != null) " · 🔥 Streak: $streak" else "")
            }
        } else if (variant == TermoVariant.DUETO) {
            score = 0
            wordsSolved = 2
            triesUsed = 7
            formulaDisplay = "Modo Dueto não gera pontos na Temporada 2026 (0 pts)"
            details = "Dueto · 0 pts nesta temporada (apenas Quarteto pontua)"
        } else {
            // Clássico (1 palavra, 6 tentativas)
            score = 0
            wordsSolved = 1
            triesUsed = 6
            formulaDisplay = "Modo Clássico não gera pontos na Temporada 2026 (0 pts)"
            details = "Clássico · 0 pts nesta temporada (apenas Quarteto pontua)"
        }

        // If grid lines weren't filled by keycaps, check for standard square emojis
        if (gridLines.isEmpty()) {
            val emojiLines = text.lines().filter { line ->
                line.contains("🟩") || line.contains("🟨") || line.contains("⬛") || line.contains("⬜") || line.contains("🟥")
            }
            if (emojiLines.isNotEmpty()) {
                gridLines.addAll(emojiLines)
            } else {
                gridLines.add("term.ooo/4 #$gameNum" + (if (streak != null) " 🔥 $streak" else ""))
            }
        }

        return ParsedScoreResult(
            gameType = GameType.TERMO,
            variant = variant,
            gameNumber = gameNum,
            score = score,
            wordsSolved = wordsSolved,
            triesUsed = triesUsed,
            maxTries = maxTries,
            remainingTries = remainingTries,
            streak = streak,
            rawGrid = gridLines,
            formulaDisplay = formulaDisplay,
            details = details,
            isValid = true
        )
    }

    private fun parseGloble(text: String): ParsedScoreResult {
        // Globle Regras Oficiais:
        // SE(Tentativas <= 2; 100; MÁXIMO(10; 100 - ((Tentativas - 2) * 3)))
        // 1. Streak: 🔥 2
        val streakRegex = Regex("""🔥\s*(\d+)""")
        val streak = streakRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()

        // 2. Average guesses: Avg. Guesses: 8.48
        val avgRegex = Regex("""(?:Avg\.\s*Guesses|Média)[\s:=]+([0-9.]+)""", RegexOption.IGNORE_CASE)
        val avgGuesses = avgRegex.find(text)?.groupValues?.get(1)

        // 3. Guesses after '=' sign (e.g. 🟨🟨🟧🟥🟧🟥🟩 = 7)
        val equalSignRegex = Regex("""=\s*(\d+)""")
        val equalMatch = equalSignRegex.find(text)

        val triesFound = equalMatch?.groupValues?.get(1)?.toIntOrNull()
            ?: Regex("""(?:palpites|tentativas|guesses|chutes)[\s:=]+(\d+)""", RegexOption.IGNORE_CASE)
                .find(text)?.groupValues?.get(1)?.toIntOrNull()
            ?: run {
                // Count square emojis if present
                val squares = listOf("🟨", "🟧", "🟥", "🟩", "⬜", "⬛")
                val squaresCount = squares.sumOf { sq -> Regex(sq).findAll(text).count() }
                if (squaresCount > 0) squaresCount else 3
            }

        // Calculate score
        val score = if (triesFound <= 2) {
            100
        } else {
            max(10, 100 - ((triesFound - 2) * 3)).coerceIn(10, 100)
        }

        val formula = if (triesFound <= 2) {
            "Tentativas ≤ 2 → 100 pts garantidos!"
        } else {
            "100 - (($triesFound - 2) × 3) = $score pts (piso mín. 10 pts)"
        }

        val details = buildString {
            append("$triesFound palpites (após '=')")
            if (avgGuesses != null) append(" · Média: $avgGuesses")
            if (streak != null) append(" · 🔥 Streak: $streak")
        }

        // Find the emoji line with =
        val emojiLine = text.lines().firstOrNull { it.contains("=") && (it.contains("🟩") || it.contains("🟨") || it.contains("🟥") || it.contains("🟧")) }
            ?: "🟨🟨🟧🟥🟧🟥🟩 = $triesFound"

        return ParsedScoreResult(
            gameType = GameType.GLOBLE,
            gameNumber = 481,
            score = score,
            wordsSolved = 1,
            triesUsed = triesFound,
            maxTries = 10,
            remainingTries = 0,
            streak = streak,
            rawGrid = listOf(emojiLine),
            formulaDisplay = formula,
            details = details,
            isValid = true
        )
    }

    private fun parseSizeItUp(text: String): ParsedScoreResult {
        // SizeItUp Regras Oficiais:
        // "Neste jogo basta pegar o texto após OverallScore"
        // Fórmula: ARREDONDAR(Pontuação_Original / 5; 0)
        // Máximo 100 pts.
        val overallScoreRegex = Regex("""Overall\s*Score[:\s]+(\d+)""", RegexOption.IGNORE_CASE)
        val overallMatch = overallScoreRegex.find(text)

        val rawScore = overallMatch?.groupValues?.get(1)?.toDoubleOrNull()
            ?: Regex("""(?:score|pontuação|pontos)[\s:=]+(\d{1,4})""", RegexOption.IGNORE_CASE)
                .find(text)?.groupValues?.get(1)?.toDoubleOrNull()
            ?: 280.0

        val scaledScore = (rawScore / 5.0).roundToInt().coerceIn(0, 100)

        // Extract emoji rows with individual round scores e.g. 🟥⬜⬜⬜⬜ 27
        val scoreLines = text.lines().filter { line ->
            (line.contains("🟥") || line.contains("⬜") || line.contains("🟩")) && Regex("""\d+""").containsMatchIn(line)
        }

        val rawGrid = if (scoreLines.isNotEmpty()) {
            scoreLines
        } else {
            listOf("Size It Up", "Overall Score ${rawScore.roundToInt()}")
        }

        return ParsedScoreResult(
            gameType = GameType.SIZEITUP,
            gameNumber = 280,
            score = scaledScore,
            wordsSolved = 5,
            triesUsed = 1,
            maxTries = 1,
            remainingTries = 0,
            rawGrid = rawGrid,
            formulaDisplay = "Score original (${rawScore.roundToInt()}) ÷ 5 = $scaledScore pts (escala de 100)",
            details = "Overall Score: ${rawScore.roundToInt()} · Pontuação RanKUs: $scaledScore pts",
            isValid = true
        )
    }
}
