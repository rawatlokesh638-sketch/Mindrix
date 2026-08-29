package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val email: String = "guest@mindrix.com",
    val password: String = "",
    val username: String = "CyberMage",
    val isLoggedIn: Boolean = false,
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 250,
    val streak: Int = 1,
    val gamesPlayed: Int = 0,
    val winsCount: Int = 0,
    val lossesCount: Int = 0,
    val bestScore: Int = 0,
    val aiRating: Int = 1200,
    val selectedAI: String = "NOVA",
    val activeAvatar: String = "avatar_cyber",
    val unlockedAvatars: String = "avatar_cyber",
    val activeTheme: String = "theme_cyan",
    val unlockedThemes: String = "theme_cyan",
    val soundEffectsEnabled: Boolean = true,
    val dailyChallengeCompleted: Boolean = false,
    val lastActiveDate: String = "",
    val dailySessionCompletedToday: Boolean = false,
    val unlockedAchievements: String = ""
)
