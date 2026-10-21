package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.R
import com.example.data.firestore.FirestoreActivity
import com.example.data.firestore.FirestoreScore
import com.example.data.firestore.FirestoreUser
import com.example.data.firestore.OperationType
import com.example.data.firestore.handleFirestoreError
import com.example.data.models.ActivityFeedItem
import com.example.data.models.DailyGameStatus
import com.example.data.models.GameType
import com.example.data.models.ParsedScoreResult
import com.example.data.models.Player
import com.example.data.models.ScoreHistoryItem
import com.example.data.models.TermoVariant
import com.example.data.models.UserAccount
import com.example.data.models.UserProfile
import com.example.data.models.WeeklyComparisonDay
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.LocalDate

class RankusRepository(
    private val context: Context? = null,
    private val db: FirebaseFirestore? = try {
        if (context != null) {
            FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("rankus_storage_v2", Context.MODE_PRIVATE)

    // Auth & Active User State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val defaultAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuArX2Naz8deCUE-iK-e43qANrfcLUYgmrDcshFaoSdcgNxv26A0dOXusFi8nsBhQz5jD_wxS_q2Z0DRMIHQWTLWGQLGZUbL6vROHg8445-FoSGtS-c7wP-abafkJGrquQJc3SerLYwlLMZOq1JejFWQwgZlnejCcdhdfN09-qc3lONtDKzOtFgUNE7CDOI8eHMmFCQ0fx5g5FWB_mTzCWVA4nHbC6dF26RexBB-aoxC2JwR2vjRwT2naQ"

    // Current User Profile State
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "gamer_temp",
            name = "Gamer",
            handle = "@gamer",
            bio = "Competidor oficial RanKUs",
            avatarUrl = defaultAvatarUrl,
            totalScore = 0,
            todayScore = 0,
            currentRank = 1,
            streakDays = 0,
            favoriteGame = GameType.TERMO,
            matchesPlayed = 0,
            winRatePercent = 0,
            bestRank = 1
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Daily Scores State (true when at least 1 score was recorded today)
    private val _hasDailyScores = MutableStateFlow(false)
    val hasDailyScores: StateFlow<Boolean> = _hasDailyScores.asStateFlow()

    // Daily Games Status (Termo, Globle, SizeItUp)
    private val _dailyGames = MutableStateFlow(
        listOf(
            DailyGameStatus(GameType.TERMO, isCompleted = false, points = 0, friendsCount = 0),
            DailyGameStatus(GameType.GLOBLE, isCompleted = false, points = 0, friendsCount = 0),
            DailyGameStatus(GameType.SIZEITUP, isCompleted = false, points = 0, friendsCount = 0)
        )
    )
    val dailyGames: StateFlow<List<DailyGameStatus>> = _dailyGames.asStateFlow()

    // Activity Feed (Recent scores across players)
    private val _activities = MutableStateFlow<List<ActivityFeedItem>>(emptyList())
    val activities: StateFlow<List<ActivityFeedItem>> = _activities.asStateFlow()

    // Leaderboard Players
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    // Ranking Visibility / Suspense Mode
    private val _isRankingHidden = MutableStateFlow(prefs?.getBoolean("is_ranking_hidden", false) ?: false)
    val isRankingHidden: StateFlow<Boolean> = _isRankingHidden.asStateFlow()

    // All Scores in the entire system (Cloud database + Local)
    private val _allScores = MutableStateFlow<List<ScoreHistoryItem>>(emptyList())

    // User's Personal Score History (Strictly for current logged-in user)
    private val _scoreHistory = MutableStateFlow<List<ScoreHistoryItem>>(emptyList())
    val scoreHistory: StateFlow<List<ScoreHistoryItem>> = _scoreHistory.asStateFlow()

    // Registered Accounts (Local Store + Cloud users)
    private val _registeredAccounts = mutableListOf<UserAccount>()

    // Cloud Users from Firestore
    private val _cloudUsers = MutableStateFlow<List<FirestoreUser>>(emptyList())

    // Password Recovery State: Email -> (Code, ExpiryTimestamp)
    private var activeRecoveryCode: String? = null
    private var activeRecoveryEmail: String? = null
    private var recoveryCodeExpiry: Long = 0L

    init {
        loadAccountsFromStorage()
        loadScoresFromStorage()
        attachCloudListeners()

        // Auto restore previous session if available
        val savedUserId = prefs?.getString("active_user_id", null)
        if (!savedUserId.isNullOrBlank()) {
            val account = _registeredAccounts.find { it.id == savedUserId }
            if (account != null) {
                activateUserSession(account)
            }
        }
    }

    // -------------------------------------------------------------
    // ACCOUNT STORAGE & AUTHENTICATION
    // -------------------------------------------------------------

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun loadAccountsFromStorage() {
        val accountsJson = prefs?.getString("accounts_json", null) ?: return
        try {
            val array = JSONArray(accountsJson)
            _registeredAccounts.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _registeredAccounts.add(
                    UserAccount(
                        id = obj.getString("id"),
                        nickname = obj.getString("nickname"),
                        email = obj.getString("email"),
                        passwordHash = obj.getString("passwordHash"),
                        avatarUrl = obj.optString("avatarUrl", defaultAvatarUrl),
                        bio = obj.optString("bio", "Competidor oficial RanKUs"),
                        favoriteGame = try {
                            GameType.valueOf(obj.optString("favoriteGame", "TERMO"))
                        } catch (_: Exception) {
                            GameType.TERMO
                        },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // Keep empty on error
        }
    }

    private fun saveAccountsToStorage() {
        try {
            val array = JSONArray()
            _registeredAccounts.forEach { acc ->
                val obj = JSONObject().apply {
                    put("id", acc.id)
                    put("nickname", acc.nickname)
                    put("email", acc.email)
                    put("passwordHash", acc.passwordHash)
                    put("avatarUrl", acc.avatarUrl)
                    put("bio", acc.bio)
                    put("favoriteGame", acc.favoriteGame.name)
                    put("createdAt", acc.createdAt)
                }
                array.put(obj)
            }
            prefs?.edit()?.putString("accounts_json", array.toString())?.apply()
        } catch (_: Exception) {
            // Ignore error
        }
    }

    /**
     * Checks if a nickname is unique.
     */
    fun isNicknameAvailable(nickname: String, currentUserId: String? = null): Boolean {
        val clean = nickname.trim()
        if (clean.length < 2) return false
        val takenInAccounts = _registeredAccounts.any {
            (currentUserId == null || it.id != currentUserId) && it.nickname.equals(clean, ignoreCase = true)
        }
        val takenInCloud = _cloudUsers.value.any {
            (currentUserId == null || it.userId != currentUserId) && it.nickname.equals(clean, ignoreCase = true)
        }
        return !takenInAccounts && !takenInCloud
    }

    /**
     * Registers a new user account with validations and persistence in SharedPreferences & Firestore.
     */
    fun register(nickname: String, email: String, password: String): Result<UserProfile> {
        val cleanNick = nickname.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanNick.length < 2) {
            return Result.failure(Exception("O apelido deve ter pelo menos 2 caracteres."))
        }
        if (!isNicknameAvailable(cleanNick)) {
            return Result.failure(Exception("O apelido '$cleanNick' já está em uso por outro usuário."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(Exception("Informe um e-mail válido (ex: seu.nome@dominio.com)."))
        }
        if (_registeredAccounts.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
            return Result.failure(Exception("Este e-mail já está cadastrado. Faça login ou recupere sua senha."))
        }
        if (password.length < 6) {
            return Result.failure(Exception("A senha deve conter no mínimo 6 caracteres."))
        }

        val newId = "user_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val hashed = hashPassword(password)

        val account = UserAccount(
            id = newId,
            nickname = cleanNick,
            email = cleanEmail,
            passwordHash = hashed,
            avatarUrl = defaultAvatarUrl,
            bio = "Competidor oficial RanKUs",
            favoriteGame = GameType.TERMO
        )

        _registeredAccounts.add(account)
        saveAccountsToStorage()

        // Sync new user to Firestore
        db?.collection("users")?.document(newId)?.set(
            FirestoreUser(
                userId = newId,
                nickname = cleanNick,
                email = cleanEmail,
                bio = account.bio,
                avatarUrl = defaultAvatarUrl
            ).toMap()
        )?.addOnFailureListener { e ->
            handleFirestoreError(e, OperationType.CREATE, "users/$newId")
        }

        activateUserSession(account)
        return Result.success(_currentUser.value)
    }

    /**
     * Authenticates with Email or Nickname and Password.
     * Prevents login with non-existent users or wrong credentials.
     */
    fun login(emailOrUser: String, password: String): Result<UserProfile> {
        val cleanInput = emailOrUser.trim()
        if (cleanInput.isBlank()) {
            return Result.failure(Exception("Digite seu e-mail ou apelido."))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Digite sua senha."))
        }

        val account = _registeredAccounts.find { acc ->
            acc.email.equals(cleanInput, ignoreCase = true) ||
                acc.nickname.equals(cleanInput, ignoreCase = true)
        }

        if (account == null) {
            return Result.failure(Exception("Usuário ou e-mail não cadastrado. Verifique os dados ou crie uma conta."))
        }

        val hashed = hashPassword(password)
        if (account.passwordHash != hashed) {
            return Result.failure(Exception("Senha incorreta. Tente novamente ou use 'Esqueci minha senha'."))
        }

        activateUserSession(account)
        return Result.success(_currentUser.value)
    }

    /**
     * Authenticates via Google Sign-In with auto-provisioning.
     */
    fun loginWithGoogle(
        firebaseUid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ): UserProfile {
        val chosenNick = displayName?.takeIf { it.isNotBlank() } ?: (email?.substringBefore("@") ?: "Gamer")
        val cleanEmail = email ?: "${firebaseUid.take(8)}@gmail.com"
        val chosenAvatar = photoUrl ?: defaultAvatarUrl

        var account = _registeredAccounts.find { it.id == firebaseUid || it.email.equals(cleanEmail, ignoreCase = true) }
        if (account == null) {
            account = UserAccount(
                id = firebaseUid,
                nickname = chosenNick,
                email = cleanEmail,
                passwordHash = "",
                avatarUrl = chosenAvatar,
                bio = "Competidor oficial RanKUs",
                favoriteGame = GameType.TERMO
            )
            _registeredAccounts.add(account)
            saveAccountsToStorage()

            db?.collection("users")?.document(firebaseUid)?.set(
                FirestoreUser(
                    userId = firebaseUid,
                    nickname = chosenNick,
                    email = cleanEmail,
                    avatarUrl = chosenAvatar,
                    bio = "Competidor oficial RanKUs"
                ).toMap()
            )?.addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.CREATE, "users/$firebaseUid")
            }
        }

        activateUserSession(account)
        return _currentUser.value
    }

    fun onGoogleSignInSuccess(displayName: String) {
        val cleanName = displayName.trim().ifEmpty { "Gamer" }
        loginWithGoogle(
            firebaseUid = "google_${cleanName.lowercase().replace(" ", "_")}",
            displayName = cleanName,
            email = "${cleanName.lowercase().replace(" ", "_")}@gmail.com",
            photoUrl = null
        )
    }

    private fun activateUserSession(account: UserAccount) {
        prefs?.edit()?.putString("active_user_id", account.id)?.apply()
        _isLoggedIn.value = true

        // Strict filter: scoreHistory MUST ONLY show records of the active logged-in user
        _scoreHistory.value = _allScores.value.filter { it.userId == account.id }

        _currentUser.value = UserProfile(
            id = account.id,
            name = account.nickname,
            handle = "@${account.nickname.lowercase().replace(" ", "")}",
            bio = account.bio,
            avatarUrl = account.avatarUrl,
            favoriteGame = account.favoriteGame
        )

        refreshActiveUserScores(account.id)
    }

    fun logout() {
        Firebase.auth.signOut()
        prefs?.edit()?.remove("active_user_id")?.apply()
        _isLoggedIn.value = false
        _hasDailyScores.value = false
        _dailyGames.update { list -> list.map { it.copy(isCompleted = false, points = 0) } }
        _scoreHistory.value = emptyList()
    }

    // -------------------------------------------------------------
    // FORGOT PASSWORD FLOW
    // -------------------------------------------------------------

    /**
     * Step 1: Generates a 6-digit random code for registered email.
     */
    fun sendRecoveryCode(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        val account = _registeredAccounts.find { it.email.equals(cleanEmail, ignoreCase = true) }
        if (account == null) {
            return Result.failure(Exception("E-mail não encontrado no sistema. Verifique o endereço digitado."))
        }

        val code = (100000..999999).random().toString()
        activeRecoveryCode = code
        activeRecoveryEmail = cleanEmail
        recoveryCodeExpiry = System.currentTimeMillis() + (15 * 60 * 1000) // 15 mins

        return Result.success(code)
    }

    /**
     * Step 2: Validates the entered 6-digit code.
     */
    fun verifyRecoveryCode(email: String, enteredCode: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val cleanCode = enteredCode.trim()

        if (activeRecoveryEmail != cleanEmail) return false
        if (activeRecoveryCode != cleanCode) return false
        if (System.currentTimeMillis() > recoveryCodeExpiry) return false

        return true
    }

    /**
     * Step 3: Replaces old password with new password linked to the account.
     */
    fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        if (newPassword.length < 6) {
            return Result.failure(Exception("A nova senha deve ter no mínimo 6 caracteres."))
        }

        val index = _registeredAccounts.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }
        if (index == -1) {
            return Result.failure(Exception("Conta não encontrada."))
        }

        val oldAcc = _registeredAccounts[index]
        val updated = oldAcc.copy(passwordHash = hashPassword(newPassword))
        _registeredAccounts[index] = updated
        saveAccountsToStorage()

        activeRecoveryCode = null
        activeRecoveryEmail = null
        recoveryCodeExpiry = 0L

        return Result.success(Unit)
    }

    // -------------------------------------------------------------
    // LOCAL AVATAR PERSISTENCE
    // -------------------------------------------------------------

    /**
     * Saves user-selected image from photo picker to internal app storage.
     * Prevents photos from disappearing on app restart.
     */
    fun saveAvatarLocally(sourceUri: Uri): String? {
        val ctx = context ?: return null
        return try {
            val inputStream = ctx.contentResolver.openInputStream(sourceUri) ?: return null
            val avatarsDir = File(ctx.filesDir, "avatars").apply { if (!exists()) mkdirs() }
            val avatarFile = File(avatarsDir, "avatar_${_currentUser.value.id}.jpg")

            FileOutputStream(avatarFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            Uri.fromFile(avatarFile).toString()
        } catch (_: Exception) {
            null
        }
    }

    fun updateProfile(
        name: String,
        bio: String,
        favoriteGame: GameType,
        avatarUriString: String?
    ) {
        val current = _currentUser.value
        val finalAvatar = avatarUriString ?: current.avatarUrl
        val cleanName = name.trim().ifEmpty { current.name }
        val generatedHandle = "@${cleanName.lowercase().replace(" ", "")}"

        val updated = current.copy(
            name = cleanName,
            handle = generatedHandle,
            bio = bio.trim().ifEmpty { current.bio },
            favoriteGame = favoriteGame,
            avatarUrl = finalAvatar
        )
        _currentUser.value = updated

        // Sync with local account
        val accIdx = _registeredAccounts.indexOfFirst { it.id == current.id }
        if (accIdx != -1) {
            _registeredAccounts[accIdx] = _registeredAccounts[accIdx].copy(
                nickname = updated.name,
                bio = updated.bio,
                favoriteGame = favoriteGame,
                avatarUrl = finalAvatar
            )
            saveAccountsToStorage()
        }

        // Sync with Firestore users collection
        db?.collection("users")?.document(current.id)?.update(
            mapOf(
                "nickname" to updated.name,
                "bio" to updated.bio,
                "favoriteGame" to favoriteGame.name,
                "avatarUrl" to finalAvatar
            )
        )?.addOnFailureListener { e ->
            handleFirestoreError(e, OperationType.UPDATE, "users/${current.id}")
        }

        rebuildPlayersLeaderboard()
    }

    // -------------------------------------------------------------
    // SCORE PERSISTENCE & HISTORY
    // -------------------------------------------------------------

    private fun loadScoresFromStorage() {
        val scoresJson = prefs?.getString("scores_json_v2", null) ?: return
        try {
            val array = JSONArray(scoresJson)
            val list = mutableListOf<ScoreHistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val rawGridJson = obj.optJSONArray("rawGrid")
                val grid = mutableListOf<String>()
                if (rawGridJson != null) {
                    for (j in 0 until rawGridJson.length()) {
                        grid.add(rawGridJson.getString(j))
                    }
                }
                list.add(
                    ScoreHistoryItem(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        gameType = GameType.valueOf(obj.getString("gameType")),
                        variant = obj.optString("variant").takeIf { it.isNotEmpty() }?.let {
                            try { TermoVariant.valueOf(it) } catch (_: Exception) { null }
                        },
                        dateKey = obj.getString("dateKey"),
                        score = obj.getInt("score"),
                        details = obj.optString("details", ""),
                        rawGrid = grid,
                        streak = obj.optInt("streak", 0).let { if (it > 0) it else null }
                    )
                )
            }
            _allScores.value = list
            _scoreHistory.value = list.filter { it.userId == _currentUser.value.id }
        } catch (_: Exception) {
            // Keep empty
        }
    }

    private fun saveScoresToStorage() {
        try {
            val array = JSONArray()
            _allScores.value.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("userId", item.userId)
                    put("gameType", item.gameType.name)
                    if (item.variant != null) put("variant", item.variant.name)
                    put("dateKey", item.dateKey)
                    put("score", item.score)
                    put("details", item.details)
                    if (item.streak != null) put("streak", item.streak)
                    val gridArray = JSONArray()
                    item.rawGrid.forEach { gridArray.put(it) }
                    put("rawGrid", gridArray)
                }
                array.put(obj)
            }
            prefs?.edit()?.putString("scores_json_v2", array.toString())?.apply()
        } catch (_: Exception) {
            // Ignore error
        }
    }

    /**
     * Strict streak calculation:
     * - Counts consecutive days backwards.
     * - If yesterday was missed and today hasn't been played, streak is 0.
     * - If yesterday was missed and today was played, streak is 1.
     */
    private fun calculateConsecutiveStreak(userScores: List<ScoreHistoryItem>): Int {
        val distinctDates = userScores.map { it.dateKey }.distinct().toSet()
        val today = LocalDate.now()
        val todayStr = today.toString()
        val yesterdayStr = today.minusDays(1).toString()

        return if (distinctDates.contains(todayStr)) {
            var streak = 1
            var checkDate = today.minusDays(1)
            while (distinctDates.contains(checkDate.toString())) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
            streak
        } else if (distinctDates.contains(yesterdayStr)) {
            var streak = 1
            var checkDate = today.minusDays(2)
            while (distinctDates.contains(checkDate.toString())) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
            streak
        } else {
            0
        }
    }

    private fun refreshActiveUserScores(uid: String) {
        val userScores = _allScores.value.filter { it.userId == uid }
        val todayStr = LocalDate.now().toString()
        val todayScores = userScores.filter { it.dateKey == todayStr }

        val totalPoints = userScores.sumOf { it.score }
        val todayPoints = todayScores.sumOf { it.score }
        val streak = calculateConsecutiveStreak(userScores)

        _hasDailyScores.value = todayScores.isNotEmpty()

        _dailyGames.update { list ->
            list.map { g ->
                val record = todayScores.find { it.gameType == g.gameType }
                if (record != null) {
                    g.copy(isCompleted = true, points = record.score)
                } else {
                    g.copy(isCompleted = false, points = 0)
                }
            }
        }

        _currentUser.update {
            it.copy(
                totalScore = totalPoints,
                todayScore = todayPoints,
                streakDays = streak,
                matchesPlayed = userScores.size
            )
        }

        rebuildPlayersLeaderboard()
    }

    fun canSubmitGameToday(gameType: GameType, variant: TermoVariant? = null): Boolean {
        val uid = _currentUser.value.id
        val todayStr = LocalDate.now().toString()
        val userScores = _allScores.value.filter { it.userId == uid && it.dateKey == todayStr }

        return if (gameType == GameType.TERMO) {
            val targetVariant = variant ?: TermoVariant.QUARTETO
            val alreadySubmitted = userScores.any {
                it.gameType == GameType.TERMO && (it.variant ?: TermoVariant.QUARTETO) == targetVariant
            }
            !alreadySubmitted
        } else {
            val alreadySubmitted = userScores.any { it.gameType == gameType }
            !alreadySubmitted
        }
    }

    fun submitScore(result: ParsedScoreResult): Boolean {
        if (!canSubmitGameToday(result.gameType, result.variant)) {
            return false
        }

        // Rule: Termo Clássico and Dueto don't award points in Season 2026, only Quarteto
        val isScoringTermo = result.gameType == GameType.TERMO && (result.variant ?: TermoVariant.QUARTETO) == TermoVariant.QUARTETO
        val effectivePoints = if (result.gameType == GameType.TERMO && !isScoringTermo) {
            0
        } else {
            result.score.coerceIn(0, 1000)
        }

        val uid = _currentUser.value.id
        val todayStr = LocalDate.now().toString()
        val effectiveVariant = if (result.gameType == GameType.TERMO) (result.variant ?: TermoVariant.QUARTETO) else null
        val variantSuffix = if (effectiveVariant != null) "_${effectiveVariant.id}" else ""
        val scoreId = "${uid}_${result.gameType.title.lowercase()}${variantSuffix}_$todayStr"

        val historyItem = ScoreHistoryItem(
            id = scoreId,
            userId = uid,
            gameType = result.gameType,
            variant = effectiveVariant,
            dateKey = todayStr,
            score = effectivePoints,
            details = result.details,
            rawGrid = result.rawGrid,
            streak = result.streak
        )

        // Add to both system-wide scores and user history
        _allScores.update { listOf(historyItem) + it.filterNot { item -> item.id == scoreId } }
        _scoreHistory.update { listOf(historyItem) + it.filterNot { item -> item.id == scoreId } }
        saveScoresToStorage()

        // Write score to Firestore
        db?.let { firestore ->
            val firestoreScore = FirestoreScore(
                userId = uid,
                nickname = _currentUser.value.name,
                avatarUrl = _currentUser.value.avatarUrl,
                gameType = result.gameType.title.lowercase(),
                dateKey = todayStr,
                score = effectivePoints.toLong(),
                rawText = result.rawGrid.joinToString("\n"),
                details = result.details
            )
            firestore.collection("scores").document(scoreId).set(firestoreScore.toMap())
                .addOnFailureListener { e ->
                    handleFirestoreError(e, OperationType.CREATE, "scores/$scoreId")
                }

            if (effectivePoints > 0) {
                val actId = "act_${System.currentTimeMillis()}"
                val firestoreAct = FirestoreActivity(
                    userId = uid,
                    playerName = _currentUser.value.name,
                    avatarUrl = _currentUser.value.avatarUrl,
                    gameName = if (effectiveVariant != null) "${result.gameType.title} (${effectiveVariant.label})" else result.gameType.title,
                    gameLetter = result.gameType.title.first().toString(),
                    pointsEarned = effectivePoints.toLong()
                )
                firestore.collection("activities").document(actId).set(firestoreAct.toMap())
                    .addOnFailureListener { e ->
                        handleFirestoreError(e, OperationType.CREATE, "activities/$actId")
                    }
            }
        }

        // Recalculate user totals directly from scores (the single source of truth!)
        refreshActiveUserScores(uid)

        // Update totalScore in Firestore users/$uid
        db?.collection("users")?.document(uid)?.update(
            mapOf(
                "totalScore" to _currentUser.value.totalScore.toLong(),
                "streakDays" to _currentUser.value.streakDays.toLong(),
                "matchesPlayed" to _currentUser.value.matchesPlayed.toLong()
            )
        )

        return true
    }

    fun deleteScore(record: ScoreHistoryItem): Boolean {
        val uid = _currentUser.value.id
        if (record.userId != uid) return false

        _allScores.update { list -> list.filterNot { it.id == record.id } }
        _scoreHistory.update { list -> list.filterNot { it.id == record.id } }
        saveScoresToStorage()

        db?.collection("scores")?.document(record.id)?.delete()
            ?.addOnFailureListener { e ->
                handleFirestoreError(e, OperationType.DELETE, "scores/${record.id}")
            }

        refreshActiveUserScores(uid)

        db?.collection("users")?.document(uid)?.update(
            mapOf(
                "totalScore" to _currentUser.value.totalScore.toLong(),
                "streakDays" to _currentUser.value.streakDays.toLong(),
                "matchesPlayed" to _currentUser.value.matchesPlayed.toLong()
            )
        )

        return true
    }

    // -------------------------------------------------------------
    // LEADERBOARD & MULTI-FILTER RANKINGS
    // -------------------------------------------------------------

    private fun rebuildPlayersLeaderboard() {
        val currentUid = _currentUser.value.id
        val myUser = _currentUser.value

        // Aggregate all unique competitors from local accounts + cloud users + current user
        val competitorsMap = mutableMapOf<String, UserAccount>()

        _registeredAccounts.forEach { acc ->
            competitorsMap[acc.id] = acc
        }

        _cloudUsers.value.forEach { cu ->
            if (!competitorsMap.containsKey(cu.userId)) {
                competitorsMap[cu.userId] = UserAccount(
                    id = cu.userId,
                    nickname = cu.nickname,
                    email = cu.email ?: "${cu.nickname.lowercase()}@rankus.gg",
                    passwordHash = "",
                    avatarUrl = cu.avatarUrl ?: defaultAvatarUrl,
                    bio = cu.bio ?: "Competidor oficial RanKUs",
                    favoriteGame = try {
                        GameType.valueOf(cu.favoriteGame ?: "TERMO")
                    } catch (_: Exception) {
                        GameType.TERMO
                    }
                )
            }
        }

        // Compute totalScore and streak per user from ALL scores
        val scoresByUser = _allScores.value.groupBy { it.userId }

        val playersList = mutableListOf<Player>()

        // 1. Current user
        val myScores = scoresByUser[currentUid] ?: emptyList()
        val myTotal = myScores.sumOf { it.score }
        val myStreak = calculateConsecutiveStreak(myScores)
        playersList.add(
            Player(
                id = currentUid,
                name = myUser.name,
                handle = myUser.handle,
                avatarUrl = myUser.avatarUrl,
                score = myTotal,
                rank = 1,
                streak = myStreak,
                gamesCount = myScores.size,
                isCurrentUser = true,
                badge = "VOCÊ"
            )
        )

        // 2. All other competitors
        competitorsMap.values.filterNot { it.id == currentUid }.forEach { acc ->
            val accScores = scoresByUser[acc.id] ?: emptyList()
            val accTotal = accScores.sumOf { it.score }
            val accStreak = calculateConsecutiveStreak(accScores)
            playersList.add(
                Player(
                    id = acc.id,
                    name = acc.nickname,
                    handle = acc.email,
                    avatarUrl = acc.avatarUrl,
                    score = accTotal,
                    rank = 2,
                    streak = accStreak,
                    gamesCount = accScores.size,
                    isCurrentUser = false
                )
            )
        }

        val sorted = playersList.sortedByDescending { it.score }.mapIndexed { idx, p ->
            val rank = idx + 1
            p.copy(
                rank = rank,
                badge = if (rank == 1 && p.score > 0) "LÍDER" else if (p.isCurrentUser) "VOCÊ" else null
            )
        }

        _players.value = sorted

        // Update current rank in currentUser profile
        val myRank = sorted.find { it.isCurrentUser }?.rank ?: 1
        _currentUser.update { it.copy(currentRank = myRank) }
    }

    /**
     * Filters players dynamically based on selected Game pill and Time range.
     * Computes exact sum of valid records for each player according to selected filters.
     */
    fun getRankingPlayers(gameFilter: String, timeFilter: String): List<Player> {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val weekStartStr = today.minusDays(6).toString()
        val monthStartStr = today.minusDays(29).toString()

        val matchingScores = _allScores.value.filter { item ->
            val matchGame = when (gameFilter) {
                "Termo" -> item.gameType == GameType.TERMO
                "Globle" -> item.gameType == GameType.GLOBLE
                "SizeItUp" -> item.gameType == GameType.SIZEITUP
                else -> true
            }
            val matchTime = when (timeFilter) {
                "Hoje" -> item.dateKey == todayStr
                "Semana" -> item.dateKey >= weekStartStr
                "Mês" -> item.dateKey >= monthStartStr
                else -> true
            }
            matchGame && matchTime
        }

        val scoresByUser = matchingScores.groupBy { it.userId }
        val allCompetitors = _players.value

        val filtered = allCompetitors.map { player ->
            val userRecords = scoresByUser[player.id] ?: emptyList()
            val filteredScore = userRecords.sumOf { it.score }
            val games = userRecords.size
            player.copy(
                score = filteredScore,
                gamesCount = games
            )
        }

        return filtered.sortedByDescending { it.score }.mapIndexed { index, p ->
            val rank = index + 1
            p.copy(
                rank = rank,
                badge = if (rank == 1 && p.score > 0) "LÍDER" else if (p.isCurrentUser) "VOCÊ" else null
            )
        }
    }

    /**
     * Calculates line chart evolution points for the logged in user based on selected filter.
     */
    fun getEvolutionData(gameFilter: String, timeFilter: String): List<WeeklyComparisonDay> {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val currentUid = _currentUser.value.id

        val userFilteredScores = _allScores.value.filter { item ->
            item.userId == currentUid && when (gameFilter) {
                "Termo" -> item.gameType == GameType.TERMO
                "Globle" -> item.gameType == GameType.GLOBLE
                "SizeItUp" -> item.gameType == GameType.SIZEITUP
                else -> true
            }
        }

        val totalUserPoints = userFilteredScores.sumOf { it.score }.coerceAtLeast(1)

        return when (timeFilter) {
            "Hoje" -> {
                val slots = listOf("08h", "11h", "14h", "17h", "20h", "22h", "Agora")
                val todayScores = userFilteredScores.filter { it.dateKey == todayStr }
                val sumToday = todayScores.sumOf { it.score }
                slots.mapIndexed { idx, label ->
                    val fraction = if (sumToday > 0) ((idx + 1).toFloat() / slots.size).coerceIn(0.1f, 1f) else 0f
                    val raw = (fraction * sumToday).toInt()
                    WeeklyComparisonDay(
                        dayLabel = label,
                        userScore = fraction,
                        userRaw = raw
                    )
                }
            }
            "Mês" -> {
                val weeks = listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4")
                weeks.mapIndexed { idx, label ->
                    val daysBackStart = (3 - idx) * 7
                    val daysBackEnd = daysBackStart - 7
                    val start = today.minusDays(daysBackStart.toLong()).toString()
                    val end = today.minusDays(daysBackEnd.coerceAtLeast(0).toLong()).toString()
                    val weekPts = userFilteredScores.filter { it.dateKey in start..end }.sumOf { it.score }
                    val fraction = if (totalUserPoints > 0) (weekPts.toFloat() / 300f).coerceIn(0f, 1f) else 0f
                    WeeklyComparisonDay(
                        dayLabel = label,
                        userScore = fraction,
                        userRaw = weekPts
                    )
                }
            }
            "Geral" -> {
                val months = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun")
                val avgFraction = (totalUserPoints.toFloat() / (months.size * 200f)).coerceIn(0.1f, 1f)
                months.mapIndexed { idx, label ->
                    val fraction = (avgFraction * ((idx + 1).toFloat() / months.size)).coerceIn(0.1f, 1f)
                    val raw = (fraction * totalUserPoints).toInt()
                    WeeklyComparisonDay(
                        dayLabel = label,
                        userScore = fraction,
                        userRaw = raw
                    )
                }
            }
            else -> {
                // "Semana" default (7 days)
                val dayLabels = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
                dayLabels.mapIndexed { idx, label ->
                    val dayDate = today.minusDays((6 - idx).toLong()).toString()
                    val dayPts = userFilteredScores.filter { it.dateKey == dayDate }.sumOf { it.score }
                    val fraction = (dayPts.toFloat() / 100f).coerceIn(0f, 1f)
                    WeeklyComparisonDay(
                        dayLabel = label,
                        userScore = fraction,
                        userRaw = dayPts
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------
    // CLOUD REACTION HANDLER
    // -------------------------------------------------------------

    fun addReactionToActivity(activityId: String, reactionType: String) {
        val field = when (reactionType) {
            "fire" -> "fireReactions"
            "applause" -> "applauseReactions"
            "target" -> "targetReactions"
            else -> return
        }

        db?.collection("activities")?.document(activityId)?.update(
            field, com.google.firebase.firestore.FieldValue.increment(1)
        )?.addOnFailureListener { e ->
            handleFirestoreError(e, OperationType.UPDATE, "activities/$activityId")
        }

        // Optimistic UI update
        _activities.update { list ->
            list.map { item ->
                if (item.id == activityId) {
                    when (reactionType) {
                        "fire" -> item.copy(fireReactions = item.fireReactions + 1)
                        "applause" -> item.copy(applauseReactions = item.applauseReactions + 1)
                        "target" -> item.copy(targetReactions = item.targetReactions + 1)
                        else -> item
                    }
                } else item
            }
        }
    }

    fun reactToActivity(activityId: String, emoji: String) {
        val reactionType = when (emoji) {
            "🔥" -> "fire"
            "👏" -> "applause"
            "🎯" -> "target"
            else -> "fire"
        }
        addReactionToActivity(activityId, reactionType)
    }

    private fun attachCloudListeners() {
        val firestore = db ?: return

        // 1. Real-time snapshot listener on cloud activities
        firestore.collection("activities")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "activities")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val cloudActivities = snapshot.documents.mapNotNull { doc ->
                        val playerName = doc.getString("playerName") ?: return@mapNotNull null
                        val gameName = doc.getString("gameName") ?: "Jogo"
                        val points = doc.getLong("pointsEarned")?.toInt() ?: 0
                        val fire = doc.getLong("fireReactions")?.toInt() ?: 0
                        val applause = doc.getLong("applauseReactions")?.toInt() ?: 0
                        val target = doc.getLong("targetReactions")?.toInt() ?: 0
                        val avatarUrl = doc.getString("avatarUrl") ?: defaultAvatarUrl
                        ActivityFeedItem(
                            id = doc.id,
                            playerName = playerName,
                            avatarUrl = avatarUrl,
                            gameName = gameName,
                            gameLetter = gameName.first().toString(),
                            timeAgo = "recentemente",
                            pointsEarned = points,
                            fireReactions = fire,
                            applauseReactions = applause,
                            targetReactions = target
                        )
                    }
                    _activities.value = cloudActivities
                }
            }

        // 2. Real-time snapshot listener on cloud users
        firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val usersList = snapshot.documents.mapNotNull { doc ->
                        val nick = doc.getString("nickname") ?: return@mapNotNull null
                        FirestoreUser(
                            id = doc.id,
                            userId = doc.getString("userId") ?: doc.id,
                            nickname = nick,
                            email = doc.getString("email"),
                            bio = doc.getString("bio"),
                            avatarUrl = doc.getString("avatarUrl"),
                            favoriteGame = doc.getString("favoriteGame"),
                            totalScore = doc.getLong("totalScore") ?: 0L,
                            streakDays = doc.getLong("streakDays") ?: 0L,
                            matchesPlayed = doc.getLong("matchesPlayed") ?: 0L
                        )
                    }
                    _cloudUsers.value = usersList

                    // Merge into registered accounts if not present
                    var accountsUpdated = false
                    usersList.forEach { cu ->
                        if (_registeredAccounts.none { it.id == cu.userId || it.nickname.equals(cu.nickname, ignoreCase = true) }) {
                            _registeredAccounts.add(
                                UserAccount(
                                    id = cu.userId,
                                    nickname = cu.nickname,
                                    email = cu.email ?: "${cu.nickname.lowercase()}@rankus.gg",
                                    passwordHash = "",
                                    avatarUrl = cu.avatarUrl ?: defaultAvatarUrl,
                                    bio = cu.bio ?: "Competidor oficial RanKUs",
                                    favoriteGame = try {
                                        GameType.valueOf(cu.favoriteGame ?: "TERMO")
                                    } catch (_: Exception) {
                                        GameType.TERMO
                                    }
                                )
                            )
                            accountsUpdated = true
                        }
                    }
                    if (accountsUpdated) {
                        saveAccountsToStorage()
                    }
                    rebuildPlayersLeaderboard()
                }
            }

        // 3. Real-time snapshot listener on cloud scores
        firestore.collection("scores")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "scores")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val todayStr = LocalDate.now().toString()
                    val scoresList = snapshot.documents.mapNotNull { doc ->
                        val gameTypeStr = doc.getString("gameType") ?: return@mapNotNull null
                        val gType = when (gameTypeStr.lowercase()) {
                            "termo" -> GameType.TERMO
                            "globle" -> GameType.GLOBLE
                            "sizeitup" -> GameType.SIZEITUP
                            else -> return@mapNotNull null
                        }
                        val uid = doc.getString("userId") ?: return@mapNotNull null
                        val scoreVal = doc.getLong("score")?.toInt() ?: 0
                        val dateKey = doc.getString("dateKey") ?: todayStr
                        val details = doc.getString("details") ?: ""
                        val rawText = doc.getString("rawText") ?: ""

                        ScoreHistoryItem(
                            id = doc.id,
                            userId = uid,
                            gameType = gType,
                            dateKey = dateKey,
                            score = scoreVal,
                            details = details,
                            rawGrid = if (rawText.isNotEmpty()) rawText.lines() else emptyList()
                        )
                    }

                    val currentUid = _currentUser.value.id
                    val localScores = _allScores.value.filter { it.userId == currentUid }
                    val merged = (scoresList + localScores).distinctBy { it.id }

                    _allScores.value = merged
                    // CRITICAL: Historico MUST only show records of current logged-in user
                    _scoreHistory.value = merged.filter { it.userId == currentUid }

                    refreshActiveUserScores(currentUid)
                }
            }

        // 4. Real-time snapshot listener on app settings / ranking suspense mode
        firestore.collection("config").document("app_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, "config/app_settings")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val hidden = snapshot.getBoolean("isRankingHidden") ?: false
                    _isRankingHidden.value = hidden
                    prefs?.edit()?.putBoolean("is_ranking_hidden", hidden)?.apply()
                }
            }
    }

    /**
     * Checks if a user profile has administrator rights to toggle Suspense Mode.
     * Admin: Matheus Lopes (google_matheus_lopes / WTFLopes / matheus_lopes@gmail.com / omatheuslops@gmail.com)
     */
    fun isUserAdmin(profile: UserProfile? = null): Boolean {
        val user = profile ?: _currentUser.value
        val hasAdminEmail = _registeredAccounts.any {
            (it.id == user.id || it.nickname.equals(user.name, ignoreCase = true)) &&
            (it.email.contains("matheus_lopes", ignoreCase = true) || it.email.contains("omatheuslops", ignoreCase = true))
        }
        return user.id == "google_matheus_lopes" ||
               user.name.equals("WTFLopes", ignoreCase = true) ||
               user.handle.contains("matheus_lopes", ignoreCase = true) ||
               user.handle.contains("omatheuslops", ignoreCase = true) ||
               user.handle.contains("WTFLopes", ignoreCase = true) ||
               hasAdminEmail
    }

    /**
     * Updates the Ranking visibility in local state, SharedPreferences, and Firestore.
     */
    fun setRankingHidden(hidden: Boolean) {
        _isRankingHidden.value = hidden
        prefs?.edit()?.putBoolean("is_ranking_hidden", hidden)?.apply()

        val firestore = db ?: return
        val data = mapOf(
            "isRankingHidden" to hidden,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("config").document("app_settings")
            .set(data)
            .addOnFailureListener { error ->
                handleFirestoreError(error, OperationType.UPDATE, "config/app_settings")
            }
    }
}
