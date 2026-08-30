package com.example.engine

import com.example.ai.AiDifficultyTier
import com.example.data.Achievement
import com.example.data.AchievementCatalog
import com.example.data.UserStats

data class XpBreakdown(
    val baseXp: Int,
    val accuracyBonusXp: Int,
    val speedBonusXp: Int,
    val streakBonusXp: Int,
    val achievementBonusXp: Int,
    val totalXpEarned: Int,
    val baseCoins: Int,
    val streakBonusCoins: Int,
    val achievementBonusCoins: Int,
    val levelBonusCoins: Int,
    val totalCoinsEarned: Int,
    val oldLevel: Int,
    val newLevel: Int,
    val isLevelUp: Boolean,
    val currentLevelXp: Int,
    val nextLevelTargetXp: Int,
    val levelProgressRatio: Float,
    val operativeTitle: String,
    val nextOperativeTitle: String,
    val unlockedAchievements: List<Achievement>
)

object XpEngine {

    /**
     * Progressive XP requirement for each level.
     * Level 1 -> 2: 600 XP
     * Level 2 -> 3: 800 XP
     * Level 3 -> 4: 1000 XP
     * Formula: Level N requires (500 + N * 200) XP to reach Level N+1
     */
    fun xpRequiredForLevel(level: Int): Int {
        return 500 + (level * 200)
    }

    /**
     * Cumulative XP required to reach a specific level.
     */
    fun cumulativeXpForLevel(targetLevel: Int): Int {
        var total = 0
        for (lvl in 1 until targetLevel) {
            total += xpRequiredForLevel(lvl)
        }
        return total
    }

    /**
     * Determines current level from total accumulated lifetime XP.
     */
    fun calculateLevelFromTotalXp(totalXp: Int): Int {
        var currentLevel = 1
        var remainingXp = totalXp
        while (true) {
            val req = xpRequiredForLevel(currentLevel)
            if (remainingXp >= req) {
                remainingXp -= req
                currentLevel++
            } else {
                break
            }
        }
        return currentLevel
    }

    /**
     * Calculates the progress inside the current level.
     * Returns Pair(currentXpInLevel, totalXpNeededForNextLevel)
     */
    fun calculateCurrentLevelProgress(totalXp: Int): Pair<Int, Int> {
        var currentLevel = 1
        var remainingXp = totalXp
        while (true) {
            val req = xpRequiredForLevel(currentLevel)
            if (remainingXp >= req) {
                remainingXp -= req
                currentLevel++
            } else {
                return Pair(remainingXp, req)
            }
        }
    }

    /**
     * Titles based on Operative Level
     */
    fun getOperativeTitle(level: Int): String {
        return when {
            level <= 2 -> "Neural Initiate"
            level <= 4 -> "Synapse Scout"
            level <= 7 -> "Cyber Vanguard"
            level <= 10 -> "Neural Architect"
            level <= 14 -> "Quantum Sovereign"
            level <= 19 -> "Apex Archon"
            else -> "Singularity Overlord"
        }
    }

    fun getNextOperativeTitle(level: Int): String {
        return getOperativeTitle(level + 1)
    }

    /**
     * Authoritative calculation of XP, Coins, Level, Streaks and Achievement progression.
     */
    fun evaluateGamePerformance(
        currentStats: UserStats,
        score: Int,
        accuracy: Int,
        timeSeconds: Double,
        isWin: Boolean,
        mode: String,
        newStreak: Int,
        isFirstSessionToday: Boolean
    ): XpBreakdown {
        val tier = AiDifficultyTier.fromRating(currentStats.aiRating)

        // 1. Base Game XP & Coins
        val baseXp = (score * 0.12f * tier.scoreMultiplier).toInt().coerceAtLeast(25)
        val baseCoins = (score * 0.06f * tier.scoreMultiplier).toInt().coerceAtLeast(15)

        // 2. Accuracy Bonus
        val accuracyBonusXp = when {
            accuracy >= 100 -> (baseXp * 0.50f).toInt()
            accuracy >= 90 -> (baseXp * 0.35f).toInt()
            accuracy >= 80 -> (baseXp * 0.20f).toInt()
            else -> 0
        }

        // 3. Speed Bonus (Fast completes under 45s)
        val speedBonusXp = if (timeSeconds in 1.0..45.0 && isWin) {
            ((45.0 - timeSeconds) * 2.5).toInt().coerceIn(10, 80)
        } else {
            0
        }

        // 4. Streak Daily Bonus & Daily Challenge Elite Bonus
        var streakBonusCoins = 0
        var streakBonusXp = 0
        if (isFirstSessionToday) {
            streakBonusCoins = 50 + (newStreak * 25)
            streakBonusXp = 100 + (newStreak * 35)
        }

        val dailyChallengeBonusCoins = if (mode == "daily") 350 else 0
        val dailyChallengeBonusXp = if (mode == "daily") 200 else 0

        // 5. Achievements Verification
        val currentUnlockedSet = currentStats.unlockedAchievements
            .split(",")
            .filter { it.isNotBlank() }
            .toMutableSet()

        val nextGamesPlayed = currentStats.gamesPlayed + 1
        val nextWins = if (isWin) currentStats.winsCount + 1 else currentStats.winsCount
        val nextBestScore = maxOf(currentStats.bestScore, score)
        val newAiRating = currentStats.aiRating // already evaluated or evaluated concurrently

        val unlockedList = mutableListOf<Achievement>()
        var achievementBonusCoins = 0
        var achievementBonusXp = 0

        AchievementCatalog.allAchievements.forEach { ach ->
            if (!currentUnlockedSet.contains(ach.id)) {
                val isUnlocked = when (ach.id) {
                    "first_step" -> nextGamesPlayed >= 1
                    "streak_3" -> newStreak >= 3
                    "streak_7" -> newStreak >= 7
                    "streak_14" -> newStreak >= 14
                    "high_score_1500" -> nextBestScore >= 1500
                    "high_score_3000" -> nextBestScore >= 3000
                    "flawless_run" -> accuracy >= 100 && score > 0
                    "speed_demon" -> timeSeconds in 1.0..25.0 && isWin
                    "ai_slayer_1" -> (mode == "ai_battle" && isWin) || nextWins > 0
                    "ai_grandmaster" -> newAiRating >= 1500
                    "century_matches" -> nextGamesPlayed >= 10
                    "veteran_50" -> nextGamesPlayed >= 25
                    "level_5" -> calculateLevelFromTotalXp(currentStats.xp + baseXp + accuracyBonusXp) >= 5
                    "level_10" -> calculateLevelFromTotalXp(currentStats.xp + baseXp + accuracyBonusXp) >= 10
                    "coin_hoarder" -> (currentStats.coins + baseCoins) >= 1000
                    "sharp" -> accuracy >= 90
                    else -> false
                }

                if (isUnlocked) {
                    currentUnlockedSet.add(ach.id)
                    unlockedList.add(ach)
                    achievementBonusCoins += ach.rewardCoins
                    achievementBonusXp += ach.rewardXp
                }
            }
        }

        // 6. Level & Progression
        val totalEarnedXp = baseXp + accuracyBonusXp + speedBonusXp + streakBonusXp + achievementBonusXp + dailyChallengeBonusXp
        val newTotalLifetimeXp = currentStats.xp + totalEarnedXp

        val oldLevel = currentStats.level
        val newLevel = calculateLevelFromTotalXp(newTotalLifetimeXp)
        val isLevelUp = newLevel > oldLevel
        val levelBonusCoins = if (isLevelUp) (newLevel - oldLevel) * 150 else 0

        val totalCoinsEarned = baseCoins + streakBonusCoins + achievementBonusCoins + levelBonusCoins + dailyChallengeBonusCoins

        val (currentLvlXp, nextLvlReq) = calculateCurrentLevelProgress(newTotalLifetimeXp)
        val progressRatio = (currentLvlXp.toFloat() / nextLvlReq.toFloat()).coerceIn(0f, 1f)

        return XpBreakdown(
            baseXp = baseXp,
            accuracyBonusXp = accuracyBonusXp,
            speedBonusXp = speedBonusXp,
            streakBonusXp = streakBonusXp,
            achievementBonusXp = achievementBonusXp,
            totalXpEarned = totalEarnedXp,
            baseCoins = baseCoins,
            streakBonusCoins = streakBonusCoins,
            achievementBonusCoins = achievementBonusCoins,
            levelBonusCoins = levelBonusCoins,
            totalCoinsEarned = totalCoinsEarned,
            oldLevel = oldLevel,
            newLevel = newLevel,
            isLevelUp = isLevelUp,
            currentLevelXp = currentLvlXp,
            nextLevelTargetXp = nextLvlReq,
            levelProgressRatio = progressRatio,
            operativeTitle = getOperativeTitle(newLevel),
            nextOperativeTitle = getNextOperativeTitle(newLevel),
            unlockedAchievements = unlockedList
        )
    }
}
