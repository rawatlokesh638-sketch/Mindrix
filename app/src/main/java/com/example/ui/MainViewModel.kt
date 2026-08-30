package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiDifficultyTier
import com.example.ai.AiPersonalityType
import com.example.audio.SoundManager
import com.example.data.Achievement
import com.example.data.AchievementCatalog
import com.example.data.MindrixRepository
import com.example.data.UserStats
import com.example.engine.XpBreakdown
import com.example.engine.XpEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LevelUpReward(
    val level: Int,
    val rewardCoins: Int
)

data class StreakReward(
    val streakDays: Int,
    val rewardCoins: Int,
    val rewardXp: Int
)

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val rewardXp: Int,
    val progress: Int,
    val target: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

class MainViewModel(private val repository: MindrixRepository) : ViewModel() {
    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allUsers: StateFlow<List<UserStats>> = repository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allUsersByRating: StateFlow<List<UserStats>> = repository.allUsersByRating
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            if (cleanEmail.isBlank() || password.isBlank()) {
                onResult(false, "Please enter email and password")
                return@launch
            }
            val existing = repository.getUserByEmail(cleanEmail)
            if (existing == null) {
                onResult(false, "Account not found. Please Sign Up.")
            } else if (existing.password != password) {
                onResult(false, "Incorrect password.")
            } else {
                repository.logoutAll()
                repository.saveUserStats(existing.copy(isLoggedIn = true))
                onResult(true, "Login successful!")
            }
        }
    }

    fun signUp(email: String, password: String, username: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            if (cleanEmail.isBlank() || password.isBlank() || username.isBlank()) {
                onResult(false, "Please fill in all fields.")
                return@launch
            }
            if (password.length < 4) {
                onResult(false, "Password must be at least 4 characters.")
                return@launch
            }
            val existing = repository.getUserByEmail(cleanEmail)
            if (existing != null) {
                onResult(false, "Email already registered. Please Login.")
            } else {
                repository.logoutAll()
                val newUser = UserStats(
                    email = cleanEmail,
                    password = password,
                    username = username.trim(),
                    isLoggedIn = true,
                    coins = 250,
                    aiRating = 1200
                )
                repository.saveUserStats(newUser)
                onResult(true, "Account created successfully!")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutAll()
        }
    }

    private val _lastScore = MutableStateFlow(0)
    val lastScore = _lastScore.asStateFlow()

    private val _lastAccuracy = MutableStateFlow(0)
    val lastAccuracy = _lastAccuracy.asStateFlow()
    
    private val _lastTime = MutableStateFlow(0.0)
    val lastTime = _lastTime.asStateFlow()

    private val _lastGameWon = MutableStateFlow(false)
    val lastGameWon = _lastGameWon.asStateFlow()

    private val _lastGameMode = MutableStateFlow("logic")
    val lastGameMode = _lastGameMode.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<LevelUpReward?>(null)
    val levelUpEvent = _levelUpEvent.asStateFlow()

    private val _streakRewardEvent = MutableStateFlow<StreakReward?>(null)
    val streakRewardEvent = _streakRewardEvent.asStateFlow()

    private val _achievementUnlockEvent = MutableStateFlow<Achievement?>(null)
    val achievementUnlockEvent = _achievementUnlockEvent.asStateFlow()

    private val _lastXpBreakdown = MutableStateFlow<XpBreakdown?>(null)
    val lastXpBreakdown = _lastXpBreakdown.asStateFlow()

    private val pendingAchievements = mutableListOf<Achievement>()

    fun dismissStreakReward() {
        _streakRewardEvent.value = null
    }

    fun dismissAchievementUnlock() {
        if (pendingAchievements.isNotEmpty()) {
            _achievementUnlockEvent.value = pendingAchievements.removeAt(0)
        } else {
            _achievementUnlockEvent.value = null
        }
    }

    private val _selectedAi = MutableStateFlow(AiPersonalityType.NOVA)
    val selectedAi = _selectedAi.asStateFlow()

    private val _missions = MutableStateFlow(
        listOf(
            Mission("m1", "Daily Synapse", "Play any 2 game modes", 100, 150, 1, 2, false, false),
            Mission("m2", "Logic Overclock", "Score over 1,500 points in any arena", 150, 200, 0, 1, false, false),
            Mission("m3", "AI Challenger", "Face off against VEX or ZERO", 200, 250, 0, 1, false, false),
            Mission("m4", "Precision Master", "Complete a match with >85% accuracy", 250, 300, 0, 1, false, false)
        )
    )
    val missions = _missions.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userStats.collect { stats ->
                if (stats != null) {
                    SoundManager.setSoundEnabled(stats.soundEffectsEnabled)
                    val aiType = try {
                        AiPersonalityType.valueOf(stats.selectedAI)
                    } catch (_: Exception) {
                        AiPersonalityType.NOVA
                    }
                    _selectedAi.value = aiType
                }
            }
        }
    }

    fun selectAiPersonality(personality: AiPersonalityType) {
        _selectedAi.value = personality
        val currentStats = userStats.value ?: UserStats()
        viewModelScope.launch {
            repository.saveUserStats(currentStats.copy(selectedAI = personality.name))
        }
    }

    fun toggleSoundEffects(): Boolean {
        val newState = SoundManager.toggleSound()
        val currentStats = userStats.value ?: UserStats()
        viewModelScope.launch {
            repository.saveUserStats(currentStats.copy(soundEffectsEnabled = newState))
        }
        return newState
    }

    fun dismissLevelUp() {
        _levelUpEvent.value = null
    }

    fun finishGame(
        score: Int,
        accuracy: Int,
        time: Double,
        isWin: Boolean,
        mode: String
    ) {
        _lastScore.value = score
        _lastAccuracy.value = accuracy
        _lastTime.value = time
        _lastGameWon.value = isWin
        _lastGameMode.value = mode

        val currentStats = userStats.value ?: UserStats()
        val currentTier = AiDifficultyTier.fromRating(currentStats.aiRating)

        // Adaptive AI rating adjustment (Elo style based on performance)
        val ratingDelta = if (isWin) {
            val base = 25 * currentTier.scoreMultiplier
            val bonus = if (accuracy >= 85) 10 else 0
            (base + bonus).toInt()
        } else {
            val penalty = if (accuracy >= 60) -15 else -25
            penalty
        }
        val newAiRating = (currentStats.aiRating + ratingDelta).coerceIn(800, 3000)

        // Daily Streak & Session completion check
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayDate = java.util.Date()
        val todayStr = dateFormat.format(todayDate)

        val lastActive = currentStats.lastActiveDate
        val isSameDay = lastActive == todayStr

        val isConsecutiveDay = if (lastActive.isBlank()) {
            false
        } else {
            try {
                val lastDate = dateFormat.parse(lastActive)
                val calToday = java.util.Calendar.getInstance().apply { this.time = todayDate }
                val calLast = java.util.Calendar.getInstance().apply { this.time = lastDate!! }
                calToday.add(java.util.Calendar.DAY_OF_YEAR, -1)
                calToday.get(java.util.Calendar.YEAR) == calLast.get(java.util.Calendar.YEAR) &&
                calToday.get(java.util.Calendar.DAY_OF_YEAR) == calLast.get(java.util.Calendar.DAY_OF_YEAR)
            } catch (_: Exception) {
                false
            }
        }

        val newStreak = when {
            isSameDay -> currentStats.streak
            isConsecutiveDay -> currentStats.streak + 1
            else -> 1
        }

        val isFirstSessionToday = !currentStats.dailySessionCompletedToday || !isSameDay

        // Authoritative XP Engine calculation
        val xpReport = XpEngine.evaluateGamePerformance(
            currentStats = currentStats,
            score = score,
            accuracy = accuracy,
            timeSeconds = time,
            isWin = isWin,
            mode = mode,
            newStreak = newStreak,
            isFirstSessionToday = isFirstSessionToday
        )
        _lastXpBreakdown.value = xpReport

        if (isFirstSessionToday) {
            _streakRewardEvent.value = StreakReward(
                newStreak,
                xpReport.streakBonusCoins,
                xpReport.streakBonusXp
            )
        }

        // Handle unlocked achievements queue
        if (xpReport.unlockedAchievements.isNotEmpty()) {
            pendingAchievements.clear()
            pendingAchievements.addAll(xpReport.unlockedAchievements)
            _achievementUnlockEvent.value = pendingAchievements.removeAt(0)
        }

        // Trigger Level-Up Celebration
        if (xpReport.isLevelUp) {
            _levelUpEvent.value = LevelUpReward(xpReport.newLevel, xpReport.levelBonusCoins)
        }

        // Update missions progress
        _missions.value = _missions.value.map { m ->
            when (m.id) {
                "m1" -> m.copy(
                    progress = (m.progress + 1).coerceAtMost(m.target),
                    isCompleted = m.progress + 1 >= m.target
                )
                "m2" -> if (score >= 1500) m.copy(progress = 1, isCompleted = true) else m
                "m3" -> if (mode == "ai_battle" || _selectedAi.value != AiPersonalityType.NOVA) m.copy(progress = 1, isCompleted = true) else m
                "m4" -> if (accuracy >= 85) m.copy(progress = 1, isCompleted = true) else m
                else -> m
            }
        }

        val updatedUnlockedAchievements = (currentStats.unlockedAchievements.split(",") + xpReport.unlockedAchievements.map { it.id })
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(",")

        viewModelScope.launch {
            repository.saveUserStats(
                currentStats.copy(
                    xp = currentStats.xp + xpReport.totalXpEarned,
                    level = xpReport.newLevel,
                    coins = currentStats.coins + xpReport.totalCoinsEarned,
                    gamesPlayed = currentStats.gamesPlayed + 1,
                    winsCount = if (isWin) currentStats.winsCount + 1 else currentStats.winsCount,
                    lossesCount = if (!isWin) currentStats.lossesCount + 1 else currentStats.lossesCount,
                    bestScore = maxOf(currentStats.bestScore, score),
                    streak = newStreak,
                    aiRating = newAiRating,
                    dailyChallengeCompleted = if (mode == "daily") true else currentStats.dailyChallengeCompleted,
                    lastActiveDate = todayStr,
                    dailySessionCompletedToday = true,
                    unlockedAchievements = updatedUnlockedAchievements
                )
            )
        }
    }

    fun claimMissionReward(missionId: String) {
        val mission = _missions.value.firstOrNull { it.id == missionId } ?: return
        if (!mission.isCompleted || mission.isClaimed) return

        SoundManager.playCorrect()
        _missions.value = _missions.value.map {
            if (it.id == missionId) it.copy(isClaimed = true) else it
        }

        val currentStats = userStats.value ?: return
        val newXp = currentStats.xp + mission.rewardXp
        val newCoins = currentStats.coins + mission.rewardCoins
        val newLevel = XpEngine.calculateLevelFromTotalXp(newXp)

        if (newLevel > currentStats.level) {
            val levelBonusCoins = (newLevel - currentStats.level) * 150
            _levelUpEvent.value = LevelUpReward(newLevel, levelBonusCoins)
        }

        viewModelScope.launch {
            repository.saveUserStats(
                currentStats.copy(
                    xp = newXp,
                    coins = newCoins,
                    level = newLevel
                )
            )
        }
    }

    fun buyAvatar(avatarId: String, cost: Int): Boolean {
        val currentStats = userStats.value ?: return false
        if (currentStats.coins < cost) return false

        val currentUnlocked = currentStats.unlockedAvatars.split(",").toMutableSet()
        currentUnlocked.add(avatarId)

        SoundManager.playLevelUp()
        viewModelScope.launch {
            repository.saveUserStats(
                currentStats.copy(
                    coins = currentStats.coins - cost,
                    unlockedAvatars = currentUnlocked.joinToString(","),
                    activeAvatar = avatarId
                )
            )
        }
        return true
    }

    fun equipAvatar(avatarId: String) {
        val currentStats = userStats.value ?: return
        SoundManager.playClick()
        viewModelScope.launch {
            repository.saveUserStats(currentStats.copy(activeAvatar = avatarId))
        }
    }

    fun buyTheme(themeId: String, cost: Int): Boolean {
        val currentStats = userStats.value ?: return false
        if (currentStats.coins < cost) return false

        val currentUnlocked = currentStats.unlockedThemes.split(",").toMutableSet()
        currentUnlocked.add(themeId)

        SoundManager.playLevelUp()
        viewModelScope.launch {
            repository.saveUserStats(
                currentStats.copy(
                    coins = currentStats.coins - cost,
                    unlockedThemes = currentUnlocked.joinToString(","),
                    activeTheme = themeId
                )
            )
        }
        return true
    }

    private val _doubleRewardClaimed = MutableStateFlow(false)
    val doubleRewardClaimed = _doubleRewardClaimed.asStateFlow()

    fun resetMatchState() {
        _doubleRewardClaimed.value = false
    }

    fun claimAdReward(coins: Int, xp: Int = 0) {
        val currentStats = userStats.value ?: return
        val newCoins = currentStats.coins + coins
        val newXp = currentStats.xp + xp
        val newLevel = XpEngine.calculateLevelFromTotalXp(newXp)

        if (newLevel > currentStats.level) {
            val levelBonusCoins = (newLevel - currentStats.level) * 150
            _levelUpEvent.value = LevelUpReward(newLevel, levelBonusCoins)
        }

        SoundManager.playLevelUp()
        viewModelScope.launch {
            repository.saveUserStats(
                currentStats.copy(
                    coins = newCoins,
                    xp = newXp,
                    level = newLevel
                )
            )
        }
    }

    fun claimDoubleMatchReward(): Boolean {
        if (_doubleRewardClaimed.value) return false
        val currentStats = userStats.value ?: return false
        val breakdown = _lastXpBreakdown.value ?: return false

        val extraCoins = breakdown.totalCoinsEarned
        val extraXp = breakdown.totalXpEarned

        _doubleRewardClaimed.value = true
        claimAdReward(coins = extraCoins, xp = extraXp)
        return true
    }

    fun equipTheme(themeId: String) {
        val currentStats = userStats.value ?: return
        SoundManager.playClick()
        viewModelScope.launch {
            repository.saveUserStats(currentStats.copy(activeTheme = themeId))
        }
    }
}
