package com.example.data

enum class AchievementCategory(val displayName: String, val icon: String) {
    PROGRESSION("Progression", "📈"),
    PRECISION("Skill & Precision", "🎯"),
    SPEED("Speed & Reflex", "⚡"),
    COMBAT("AI Battles", "⚔️"),
    STREAKS("Streaks & Daily", "🔥"),
    ECONOMY("Vault & Gear", "🪙")
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconSymbol: String,
    val category: AchievementCategory,
    val targetValue: Int,
    val rewardCoins: Int,
    val rewardXp: Int
)

object AchievementCatalog {
    val allAchievements = listOf(
        // Progression
        Achievement(
            id = "first_step",
            title = "Neural Awakening",
            description = "Complete your first cognitive game session",
            iconSymbol = "🌱",
            category = AchievementCategory.PROGRESSION,
            targetValue = 1,
            rewardCoins = 50,
            rewardXp = 100
        ),
        Achievement(
            id = "century_matches",
            title = "Centurion",
            description = "Complete 10 total match sessions across any arena",
            iconSymbol = "🛡️",
            category = AchievementCategory.PROGRESSION,
            targetValue = 10,
            rewardCoins = 150,
            rewardXp = 250
        ),
        Achievement(
            id = "veteran_50",
            title = "Neural Veteran",
            description = "Complete 25 total match sessions",
            iconSymbol = "🎖️",
            category = AchievementCategory.PROGRESSION,
            targetValue = 25,
            rewardCoins = 300,
            rewardXp = 500
        ),
        Achievement(
            id = "level_5",
            title = "Ascendant I",
            description = "Advance your operative rank to Level 5",
            iconSymbol = "⭐",
            category = AchievementCategory.PROGRESSION,
            targetValue = 5,
            rewardCoins = 200,
            rewardXp = 350
        ),
        Achievement(
            id = "level_10",
            title = "Ascendant II",
            description = "Reach the prestigious milestone of Level 10",
            iconSymbol = "🌟",
            category = AchievementCategory.PROGRESSION,
            targetValue = 10,
            rewardCoins = 500,
            rewardXp = 800
        ),

        // Skill & Precision
        Achievement(
            id = "sharp",
            title = "Precision Synapse",
            description = "Complete a game session with >= 90% accuracy",
            iconSymbol = "🎯",
            category = AchievementCategory.PRECISION,
            targetValue = 90,
            rewardCoins = 120,
            rewardXp = 200
        ),
        Achievement(
            id = "flawless_run",
            title = "Flawless Core",
            description = "Achieve 100% flawless accuracy in any standard game session",
            iconSymbol = "💎",
            category = AchievementCategory.PRECISION,
            targetValue = 100,
            rewardCoins = 250,
            rewardXp = 400
        ),
        Achievement(
            id = "high_score_1500",
            title = "Overclocked",
            description = "Achieve a match score of 1,500 points or higher",
            iconSymbol = "🚀",
            category = AchievementCategory.PRECISION,
            targetValue = 1500,
            rewardCoins = 150,
            rewardXp = 250
        ),
        Achievement(
            id = "high_score_3000",
            title = "Quantum Apex",
            description = "Dominate the arena with 3,000+ points in a single match",
            iconSymbol = "🧠",
            category = AchievementCategory.PRECISION,
            targetValue = 3000,
            rewardCoins = 400,
            rewardXp = 600
        ),

        // Speed & Reflex
        Achievement(
            id = "speed_demon",
            title = "Quantum Reflex",
            description = "Solve and finish a victorious match in under 25 seconds",
            iconSymbol = "⚡",
            category = AchievementCategory.SPEED,
            targetValue = 25,
            rewardCoins = 200,
            rewardXp = 300
        ),

        // AI Battles
        Achievement(
            id = "ai_slayer_1",
            title = "Core Overlord",
            description = "Defeat an AI rival personality in direct AI Face-Off combat",
            iconSymbol = "🏆",
            category = AchievementCategory.COMBAT,
            targetValue = 1,
            rewardCoins = 180,
            rewardXp = 250
        ),
        Achievement(
            id = "ai_grandmaster",
            title = "Dominion Protocol",
            description = "Climb to 1,500+ AI combat ELO rating",
            iconSymbol = "👑",
            category = AchievementCategory.COMBAT,
            targetValue = 1500,
            rewardCoins = 350,
            rewardXp = 500
        ),

        // Streaks & Daily
        Achievement(
            id = "streak_3",
            title = "Synaptic Momentum",
            description = "Maintain an uninterrupted 3-day daily streak",
            iconSymbol = "🔥",
            category = AchievementCategory.STREAKS,
            targetValue = 3,
            rewardCoins = 150,
            rewardXp = 250
        ),
        Achievement(
            id = "streak_7",
            title = "Unstoppable Current",
            description = "Maintain an active 7-day daily cognitive streak",
            iconSymbol = "⚡",
            category = AchievementCategory.STREAKS,
            targetValue = 7,
            rewardCoins = 350,
            rewardXp = 500
        ),
        Achievement(
            id = "streak_14",
            title = "Chrono Singularity",
            description = "Sustain a legendary 14-day consecutive active streak",
            iconSymbol = "🌌",
            category = AchievementCategory.STREAKS,
            targetValue = 14,
            rewardCoins = 750,
            rewardXp = 1000
        ),

        // Economy
        Achievement(
            id = "coin_hoarder",
            title = "Cyber Tycoon",
            description = "Accumulate a balance of 1,000 neural coins in your vault",
            iconSymbol = "🪙",
            category = AchievementCategory.ECONOMY,
            targetValue = 1000,
            rewardCoins = 250,
            rewardXp = 350
        )
    )
}
