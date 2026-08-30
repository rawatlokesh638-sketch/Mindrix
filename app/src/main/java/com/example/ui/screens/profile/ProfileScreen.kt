package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiDifficultyTier
import com.example.audio.SoundManager
import com.example.data.AchievementCategory
import com.example.data.AchievementCatalog
import com.example.data.UserStats
import com.example.engine.XpEngine
import com.example.ui.MainViewModel
import com.example.ui.screens.home.StatItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val userStats by viewModel.userStats.collectAsState()
    val soundEnabled by SoundManager.soundEnabled.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf<AchievementCategory?>(null) }

    val aiTier = remember(userStats?.aiRating) {
        AiDifficultyTier.fromRating(userStats?.aiRating ?: 1200)
    }

    val totalGames = (userStats?.winsCount ?: 0) + (userStats?.lossesCount ?: 0)
    val winRate = if (totalGames > 0) ((userStats?.winsCount ?: 0) * 100) / totalGames else 0

    val level = userStats?.level ?: 1
    val totalLifetimeXp = userStats?.xp ?: 0
    val (currentLevelXp, nextLevelTargetXp) = remember(totalLifetimeXp) {
        XpEngine.calculateCurrentLevelProgress(totalLifetimeXp)
    }
    val operativeTitle = remember(level) { XpEngine.getOperativeTitle(level) }

    val unlockedSet = remember(userStats?.unlockedAchievements) {
        userStats?.unlockedAchievements?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    }

    val filteredAchievements = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            AchievementCatalog.allAchievements
        } else {
            AchievementCatalog.allAchievements.filter { it.category == selectedCategoryFilter }
        }
    }

    Scaffold(
        containerColor = DarkSlate,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("OPERATIVE DOSSIER", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("PROGRESSION & PERFORMANCE MATRIX", color = Cyan400, style = MaterialTheme.typography.labelSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSlate)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avatar_ai),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(3.dp, Cyan400, CircleShape)
                    )
                    Surface(
                        color = DarkSlate,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Cyan400),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$level",
                                color = Cyan400,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    userStats?.username ?: "CyberMage",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = Cyan400.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Cyan400.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = operativeTitle.uppercase(),
                        color = Cyan400,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Comprehensive XP & Level Progression Card
                Surface(
                    color = GlassBackground,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "LEVEL $level PROGRESSION",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                "$currentLevelXp / $nextLevelTargetXp XP",
                                color = Cyan400,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val progressRatio = (currentLevelXp.toFloat() / nextLevelTargetXp.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            color = Cyan400,
                            trackColor = GlassBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Lifetime XP: $totalLifetimeXp",
                                color = Slate400,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                            )
                            Text(
                                "Next: Level ${level + 1}",
                                color = Amber400,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            item {
                // Daily Streak Protocol Ladder
                val currentStreak = userStats?.streak ?: 1
                Surface(
                    color = Amber400.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Amber400.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "DAILY STREAK PROTOCOL",
                                        color = Amber400,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                    )
                                    Text(
                                        "$currentStreak Days Active",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Surface(
                                color = if (userStats?.dailySessionCompletedToday == true) Color(0xFF00E676).copy(alpha = 0.2f) else Slate400.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (userStats?.dailySessionCompletedToday == true) Color(0xFF00E676) else GlassBorder)
                            ) {
                                Text(
                                    text = if (userStats?.dailySessionCompletedToday == true) "TODAY COMPLETE" else "PENDING TODAY",
                                    color = if (userStats?.dailySessionCompletedToday == true) Color(0xFF00E676) else Slate400,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7-day visual node line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (day in 1..7) {
                                val isPast = day <= currentStreak
                                val isCurrent = day == ((currentStreak - 1) % 7 + 1)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = when {
                                            isCurrent -> Amber400
                                            isPast -> Amber400.copy(alpha = 0.3f)
                                            else -> DarkSlate
                                        },
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, if (isPast || isCurrent) Amber400 else GlassBorder),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isPast) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) Color.Black else Amber400,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            } else {
                                                Text("D$day", fontSize = 9.sp, color = Slate400, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "+${day * 25}🪙",
                                        fontSize = 8.sp,
                                        color = if (isPast || isCurrent) Amber400 else Slate400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Battle Stats Matrix & Mindrix Rank Dossier
                Surface(
                    color = GlassBackground,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛡️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MINDRIX COMBAT RANK", color = Cyan400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp))
                            }
                            Surface(
                                color = Cyan400.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Cyan400)
                            ) {
                                Text(
                                    text = "RATING: ${userStats?.aiRating ?: 1200}",
                                    color = Cyan400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = aiTier.levelName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "AI Tier: ${aiTier.badge}",
                                    color = Slate400,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = operativeTitle,
                                    color = Amber400,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Mindrix Standing",
                                    color = Slate400,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("AI WIN / LOSS RATIO & PUZZLE METRICS", color = Slate400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Win Rate", "$winRate%", Cyan400)
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("Puzzles Solved", "${userStats?.gamesPlayed ?: 0}", Amber400)
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("High Score", "${userStats?.bestScore ?: 0}", Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("AI Wins", "${userStats?.winsCount ?: 0}", Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("AI Losses", "${userStats?.lossesCount ?: 0}", Color(0xFFFF1744))
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("Streak", "${userStats?.streak ?: 1} Days", Amber400)
                        }
                    }
                }
            }

            item {
                // Achievements Showcase Header & Filter Chips
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "OPERATIVE ACHIEVEMENTS",
                            color = Slate400,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Text(
                            "${unlockedSet.size} / ${AchievementCatalog.allAchievements.size} UNLOCKED",
                            color = Cyan400,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val scrollChips = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollChips),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = {
                                SoundManager.playClick()
                                selectedCategoryFilter = null
                            },
                            label = { Text("All (${AchievementCatalog.allAchievements.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cyan400,
                                selectedLabelColor = Color.Black,
                                containerColor = GlassBackground,
                                labelColor = Slate400
                            )
                        )

                        AchievementCategory.values().forEach { cat ->
                            val count = AchievementCatalog.allAchievements.count { it.category == cat }
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = {
                                    SoundManager.playClick()
                                    selectedCategoryFilter = cat
                                },
                                label = { Text("${cat.icon} ${cat.displayName} ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Cyan400,
                                    selectedLabelColor = Color.Black,
                                    containerColor = GlassBackground,
                                    labelColor = Slate400
                                )
                            )
                        }
                    }
                }
            }

            // Achievement Cards
            filteredAchievements.forEach { ach ->
                item(key = ach.id) {
                    val isUnlocked = unlockedSet.contains(ach.id)
                    val stats = userStats ?: UserStats()

                    // Calculate live progress for each achievement
                    val currentProgressValue = when (ach.id) {
                        "first_step" -> minOf(stats.gamesPlayed, 1)
                        "century_matches" -> minOf(stats.gamesPlayed, 10)
                        "veteran_50" -> minOf(stats.gamesPlayed, 25)
                        "level_5" -> minOf(stats.level, 5)
                        "level_10" -> minOf(stats.level, 10)
                        "sharp" -> if (isUnlocked) 90 else 0
                        "flawless_run" -> if (isUnlocked) 100 else 0
                        "high_score_1500" -> minOf(stats.bestScore, 1500)
                        "high_score_3000" -> minOf(stats.bestScore, 3000)
                        "speed_demon" -> if (isUnlocked) 25 else 0
                        "ai_slayer_1" -> minOf(stats.winsCount, 1)
                        "ai_grandmaster" -> minOf(stats.aiRating, 1500)
                        "streak_3" -> minOf(stats.streak, 3)
                        "streak_7" -> minOf(stats.streak, 7)
                        "streak_14" -> minOf(stats.streak, 14)
                        "coin_hoarder" -> minOf(stats.coins, 1000)
                        else -> if (isUnlocked) 1 else 0
                    }

                    val progressRatio = if (isUnlocked) 1f else (currentProgressValue.toFloat() / ach.targetValue.toFloat()).coerceIn(0f, 1f)

                    Surface(
                        color = if (isUnlocked) Cyan400.copy(alpha = 0.08f) else GlassBackground,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, if (isUnlocked) Cyan400.copy(alpha = 0.4f) else GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isUnlocked) Cyan400.copy(alpha = 0.2f) else Slate400.copy(alpha = 0.1f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (isUnlocked) ach.iconSymbol else "🔒", fontSize = 20.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            ach.title,
                                            color = if (isUnlocked) Color.White else Slate400,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            ach.category.icon,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        ach.description,
                                        color = Slate400,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (isUnlocked) {
                                    Surface(
                                        color = Color(0xFF00E676).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "UNLOCKED",
                                            color = Color(0xFF00E676),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("+${ach.rewardCoins} 🪙", color = Amber400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                        Text("+${ach.rewardXp} XP", color = Cyan400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                    }
                                }
                            }

                            if (!isUnlocked) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Progress", color = Slate400, fontSize = 9.sp)
                                    Text("$currentProgressValue / ${ach.targetValue}", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    color = Cyan400,
                                    trackColor = GlassBorder,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Settings Section
                Surface(
                    color = GlassBackground,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Audio",
                                tint = if (soundEnabled) Cyan400 else Slate400
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Sound Effects & Chimes", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Synthesized interactive audio", color = Slate400, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { viewModel.toggleSoundEffects() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Cyan400,
                                uncheckedTrackColor = DarkSlate
                            )
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF1744).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("LOGOUT / SWITCH ACCOUNT", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
