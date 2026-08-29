package com.example.data

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconSymbol: String,
    val rewardCoins: Int,
    val rewardXp: Int
)

object AchievementCatalog {
    val allAchievements = listOf(
        Achievement("first_step", "Neural Awakening", "Complete your first game session", "🌱", 50, 100),
        Achievement("streak_3", "Momentum", "Maintain a 3-day daily streak", "🔥", 100, 150),
        Achievement("streak_7", "Unstoppable", "Maintain a 7-day daily streak", "⚡", 250, 300),
        Achievement("high_score", "Quantum Mind", "Score over 2,000 points in any arena", "🧠", 200, 250),
        Achievement("ai_slayer", "Core Overlord", "Defeat an AI opponent in AI Face-Off", "🏆", 150, 200),
        Achievement("century", "Centurion", "Complete 10 total game sessions", "🛡️", 150, 200),
        Achievement("sharp", "Precision Synapse", "Complete a game with >= 90% accuracy", "🎯", 150, 200)
    )
}
