package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreScore
import com.example.data.firestore.FirestoreUser
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RankusFirestoreRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createUserProfile_authenticated_succeeds() = runBlocking {
    val uid = signInTestUser("gamer1@rankus.gg")

    val profile = FirestoreUser(
      userId = uid,
      nickname = "GamerOne",
      email = "gamer1@rankus.gg",
      bio = "Speedrunner do Termo"
    )

    withTimeout(5000L) {
      firestore.collection("users").document(uid).set(profile.toMap()).await()
    }

    val snapshot = withTimeout(5000L) {
      firestore.collection("users").document(uid).get().await()
    }

    assertTrue(snapshot.exists())
    assertEquals("GamerOne", snapshot.getString("nickname"))
  }

  @Test
  fun submitDailyScore_authenticated_succeeds() = runBlocking {
    val uid = signInTestUser("gamer2@rankus.gg")
    val scoreId = "${uid}_termo_2026-10-15"

    val score = FirestoreScore(
      userId = uid,
      nickname = "GamerTwo",
      gameType = "termo",
      dateKey = "2026-10-15",
      score = 92L,
      details = "4 palavras em 5 tentativas"
    )

    withTimeout(5000L) {
      firestore.collection("scores").document(scoreId).set(score.toMap()).await()
    }

    val snapshot = withTimeout(5000L) {
      firestore.collection("scores").document(scoreId).get().await()
    }

    assertTrue(snapshot.exists())
    assertEquals(92L, snapshot.getLong("score"))
  }

  @Test
  fun submitDailyScore_unauthenticated_fails() = runBlocking {
    auth.signOut()
    val score = FirestoreScore(
      userId = "unauth_user",
      gameType = "termo",
      dateKey = "2026-10-15",
      score = 80L
    )

    try {
      withTimeout(5000L) {
        firestore.collection("scores").document("unauth_termo_2026-10-15").set(score.toMap()).await()
      }
      fail("Expected write to fail when unauthenticated")
    } catch (expected: Exception) {
      assertNotNull(expected)
    }
  }
}
