package com.example.data.firestore

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject

@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val email: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val favoriteGame: String? = null,
    val totalScore: Long = 0L,
    val streakDays: Long = 0L,
    val matchesPlayed: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("nickname", nickname)
        email?.let { put("email", it) }
        bio?.let { put("bio", it) }
        avatarUrl?.let { put("avatarUrl", it) }
        favoriteGame?.let { put("favoriteGame", it) }
        put("totalScore", totalScore)
        put("streakDays", streakDays)
        put("matchesPlayed", matchesPlayed)
        put("createdAt", createdAt ?: FieldValue.serverTimestamp())
        put("updatedAt", updatedAt ?: FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class FirestoreScore(
    @DocumentId val id: String = "",
    val userId: String = "",
    val nickname: String = "",
    val avatarUrl: String = "",
    val gameType: String = "",
    val dateKey: String = "",
    val score: Long = 0L,
    val rawText: String = "",
    val details: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("nickname", nickname)
        put("avatarUrl", avatarUrl)
        put("gameType", gameType)
        put("dateKey", dateKey)
        put("score", score)
        if (rawText.isNotEmpty()) put("rawText", rawText)
        if (details.isNotEmpty()) put("details", details)
        put("createdAt", createdAt ?: FieldValue.serverTimestamp())
    }
}

@IgnoreExtraProperties
data class FirestoreActivity(
    @DocumentId val id: String = "",
    val userId: String = "",
    val playerName: String = "",
    val avatarUrl: String = "",
    val gameName: String = "",
    val gameLetter: String = "",
    val pointsEarned: Long = 0L,
    val fireReactions: Long = 0L,
    val applauseReactions: Long = 0L,
    val targetReactions: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> = buildMap {
        put("userId", userId)
        put("playerName", playerName)
        put("avatarUrl", avatarUrl)
        put("gameName", gameName)
        put("gameLetter", gameLetter)
        put("pointsEarned", pointsEarned)
        put("fireReactions", fireReactions)
        put("applauseReactions", applauseReactions)
        put("targetReactions", targetReactions)
        put("createdAt", createdAt ?: FieldValue.serverTimestamp())
    }
}

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}
