package com.example.data.models

enum class GameType(
    val id: String,
    val title: String,
    val category: String,
    val officialUrl: String,
    val maxScore: Int = 100
) {
    TERMO("termo", "Termo", "Palavras Diárias", "https://term.ooo/4"),
    GLOBLE("globle", "Globle", "Geografia", "https://globle-game.com"),
    SIZEITUP("sizeitup", "SizeItUp", "Lógica & Curiosidades", "https://magnitudle.com/size-it-up")
}

enum class TermoVariant(val id: String, val label: String, val words: Int, val maxTries: Int) {
    CLASSICO("classico", "Clássico (1)", 1, 6),
    DUETO("dueto", "Dueto (2)", 2, 7),
    QUARTETO("quarteto", "Quarteto (4)", 4, 9)
}

data class ParsedScoreResult(
    val gameType: GameType,
    val variant: TermoVariant? = null,
    val gameNumber: Int = 0,
    val score: Int = 0,
    val wordsSolved: Int = 0,
    val triesUsed: Int = 0,
    val maxTries: Int = 0,
    val remainingTries: Int = 0,
    val streak: Int? = null,
    val rawGrid: List<String> = emptyList(),
    val formulaDisplay: String = "",
    val details: String = "",
    val isValid: Boolean = true,
    val errorMessage: String? = null
)

data class Player(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String,
    val score: Int,
    val rank: Int,
    val streak: Int,
    val gamesCount: Int,
    val rankChange: Int = 0, // e.g. +1, -1, 0
    val isCurrentUser: Boolean = false,
    val badge: String? = null,
    val winRate: Int = 85,
    val globleAvg: Int = 92,
    val sizeItUpScore: Int = 420
)

data class ActivityFeedItem(
    val id: String,
    val playerName: String,
    val avatarUrl: String,
    val gameName: String,
    val gameLetter: String,
    val timeAgo: String,
    val pointsEarned: Int,
    val fireReactions: Int,
    val applauseReactions: Int,
    val targetReactions: Int,
    val userReactions: Set<String> = emptySet()
)

data class ScoreHistoryItem(
    val id: String,
    val userId: String,
    val gameType: GameType,
    val variant: TermoVariant? = null,
    val dateKey: String, // "YYYY-MM-DD"
    val score: Int,
    val details: String,
    val rawGrid: List<String> = emptyList(),
    val streak: Int? = null,
    val timestampMs: Long = System.currentTimeMillis()
) {
    val isToday: Boolean
        get() = dateKey == java.time.LocalDate.now().toString()
}

data class UserProfile(
    val id: String = "beto_gamer",
    val name: String = "Beto_Gamer",
    val handle: String = "beto.gamer@rankus.gg",
    val bio: String = "O rei do Termo Quarteto 🧩",
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuArX2Naz8deCUE-iK-e43qANrfcLUYgmrDcshFaoSdcgNxv26A0dOXusFi8nsBhQz5jD_wxS_q2Z0DRMIHQWTLWGQLGZUbL6vROHg8445-FoSGtS-c7wP-abafkJGrquQJc3SerLYwlLMZOq1JejFWQwgZlnejCcdhdfN09-qc3lONtDKzOtFgUNE7CDOI8eHMmFCQ0fx5g5FWB_mTzCWVA4nHbC6dF26RexBB-aoxC2JwR2vjRwT2naQ",
    val totalScore: Int = 0,
    val todayScore: Int = 0,
    val currentRank: Int = 1,
    val streakDays: Int = 0,
    val favoriteGame: GameType = GameType.TERMO,
    val matchesPlayed: Int = 0,
    val winRatePercent: Int = 0,
    val bestRank: Int = 1
)

data class UserAccount(
    val id: String,
    val nickname: String,
    val email: String,
    val passwordHash: String,
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuArX2Naz8deCUE-iK-e43qANrfcLUYgmrDcshFaoSdcgNxv26A0dOXusFi8nsBhQz5jD_wxS_q2Z0DRMIHQWTLWGQLGZUbL6vROHg8445-FoSGtS-c7wP-abafkJGrquQJc3SerLYwlLMZOq1JejFWQwgZlnejCcdhdfN09-qc3lONtDKzOtFgUNE7CDOI8eHMmFCQ0fx5g5FWB_mTzCWVA4nHbC6dF26RexBB-aoxC2JwR2vjRwT2naQ",
    val bio: String = "Competidor oficial RanKUs",
    val favoriteGame: GameType = GameType.TERMO,
    val createdAt: Long = System.currentTimeMillis()
)

data class DailyGameStatus(
    val gameType: GameType,
    val isCompleted: Boolean,
    val points: Int = 0,
    val friendsCount: Int = 0
)

data class WeeklyComparisonDay(
    val dayLabel: String,
    val lucasScore: Float = 0f,
    val carolScore: Float = 0f,
    val guiScore: Float = 0f,
    val userScore: Float = 0f,
    val lucasRaw: Int = 0,
    val carolRaw: Int = 0,
    val guiRaw: Int = 0,
    val userRaw: Int = 0
)
