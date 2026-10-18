package com.example

import com.example.data.models.GameType
import com.example.data.models.TermoVariant
import com.example.data.parser.ResultParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultParserTest {

    @Test
    fun testTermoQuartetoUserPattern() {
        val input = """
            term.ooo/4 #1695 🔥 1

            7️⃣8️⃣
            6️⃣9️⃣
        """.trimIndent()

        val result = ResultParser.parseResult(input)
        assertEquals(GameType.TERMO, result.gameType)
        assertEquals(TermoVariant.QUARTETO, result.variant)
        assertEquals(1695, result.gameNumber)
        assertEquals(1, result.streak)
        assertEquals(4, result.wordsSolved)
        assertEquals(9, result.triesUsed)
        assertEquals(0, result.remainingTries)
        // (4 * 15) + (0 * 8) = 60
        assertEquals(60, result.score)
    }

    @Test
    fun testTermoQuartetoEarlySolve() {
        val input = """
            term.ooo/4 #1695 🔥 3

            5️⃣6️⃣
            4️⃣7️⃣
        """.trimIndent()

        val result = ResultParser.parseResult(input)
        assertEquals(4, result.wordsSolved)
        assertEquals(7, result.triesUsed)
        assertEquals(2, result.remainingTries)
        // (4 * 15) + (2 * 8) = 60 + 16 = 76
        assertEquals(76, result.score)
        assertEquals(3, result.streak)
    }

    @Test
    fun testGlobleUserPattern() {
        val input = """
            🌎 Oct 13, 2026 🌍
            🔥 2 | Avg. Guesses: 8.48
            🟨🟨🟧🟥🟧🟥🟩 = 7

            https://globle-game.com
            #globle
        """.trimIndent()

        val result = ResultParser.parseResult(input)
        assertEquals(GameType.GLOBLE, result.gameType)
        assertEquals(2, result.streak)
        assertEquals(7, result.triesUsed)
        // 100 - ((7 - 2) * 3) = 100 - 15 = 85
        assertEquals(85, result.score)
    }

    @Test
    fun testSizeItUpUserPattern() {
        val input = """
            Size It Up
            Overall Score 280

            🟥⬜⬜⬜⬜ 27
            🟥🟥🟥⬜⬜ 61
            ⬜⬜⬜⬜⬜ 0
            🟥🟥🟥🟥🟥 97
            🟥🟥🟥🟥🟥 95
            https://magnitudle.com/size-it-up
        """.trimIndent()

        val result = ResultParser.parseResult(input)
        assertEquals(GameType.SIZEITUP, result.gameType)
        // 280 / 5 = 56
        assertEquals(56, result.score)
    }

    @Test
    fun testAutoDetection() {
        val termoText = "term.ooo/4 #1695 🔥 1\n7️⃣8️⃣\n6️⃣9️⃣"
        val globleText = "🌎 Oct 13, 2026 🌍\n🟨🟨🟧🟥🟧🟥🟩 = 7\n#globle"
        val sizeItUpText = "Size It Up\nOverall Score 280\nhttps://magnitudle.com/size-it-up"

        assertEquals(GameType.TERMO, ResultParser.parseResult(termoText).gameType)
        assertEquals(GameType.GLOBLE, ResultParser.parseResult(globleText).gameType)
        assertEquals(GameType.SIZEITUP, ResultParser.parseResult(sizeItUpText).gameType)
    }

    @Test
    fun testTermoClassicoAndDuetoZeroScoreInSeason2026() {
        val classicoText = "joguei termo.pt #500 4/6\n🟩🟩🟩🟩🟩"
        val duetoText = "joguei dueto #300 5/7\n🟩🟩🟩🟩🟩"

        val resClassico = ResultParser.parseResult(classicoText, forcedVariant = TermoVariant.CLASSICO)
        assertEquals(0, resClassico.score)

        val resDueto = ResultParser.parseResult(duetoText, forcedVariant = TermoVariant.DUETO)
        assertEquals(0, resDueto.score)
    }
}
