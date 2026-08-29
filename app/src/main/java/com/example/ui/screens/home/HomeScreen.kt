package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiDifficultyTier
import com.example.ai.AiPersonalityType
import com.example.audio.SoundManager
import com.example.data.UserStats
import com.example.ui.MainViewModel
import com.example.ui.Mission
import com.example.ui.components.LevelUpCelebrationModal
import com.example.ui.components.ParticleExplosion
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToBattle: (String?) -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val selectedAi by viewModel.selectedAi.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val soundEnabled by SoundManager.soundEnabled.collectAsState()
    val levelUpEvent by viewModel.levelUpEvent.collectAsState()
    val streakRewardEvent by viewModel.streakRewardEvent.collectAsState()
    val achievementUnlockEvent by viewModel.achievementUnlockEvent.collectAsState()

    val aiTier = remember(userStats?.aiRating) {
        AiDifficultyTier.fromRating(userStats?.aiRating ?: 1200)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlate)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                userStats?.let {
                    TopProfileBar(
                        stats = it,
                        soundEnabled = soundEnabled,
                        onToggleSound = { viewModel.toggleSoundEffects() }
                    )
                }
            }

            item {
                // Header Title
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MINDRIX",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ),
                        color = Cyan400
                    )
                    Text(
                        text = "HUMAN VS AI COGNITIVE DUEL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp
                        ),
                        color = Cyan400.copy(alpha = 0.8f)
                    )
                }
            }

            item {
                // Featured Live Event: Daily Brain Sync
                FeaturedDailyChallenge(
                    isCompleted = userStats?.dailyChallengeCompleted ?: false,
                    onClick = {
                        SoundManager.playClick()
                        onNavigateToBattle("daily")
                    }
                )
            }

            item {
                // AI Companion Live Widget
                AiCompanionBanner(
                    selectedAi = selectedAi,
                    aiTier = aiTier,
                    onSwitchClick = {
                        SoundManager.playClick()
                        onNavigateToBattle(null) // Open Arena & AI selector
                    }
                )
            }

            item {
                // Arena Grid Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "INTELLIGENCE ARENAS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Slate400
                    )
                    Text(
                        "All Modes",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Cyan400,
                        modifier = Modifier.clickable {
                            SoundManager.playClick()
                            onNavigateToBattle(null)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Arena Grid Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ModeCard(
                        title = "AI Face-Off",
                        subtitle = "Duel ${selectedAi.displayName}",
                        icon = Icons.Default.SmartToy,
                        accentColor = selectedAi.primaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playClick()
                            onNavigateToBattle("ai_battle")
                        }
                    )
                    ModeCard(
                        title = "Logic Lab",
                        subtitle = "Deductive Syllogisms",
                        icon = Icons.Default.Psychology,
                        accentColor = Cyan400,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playClick()
                            onNavigateToBattle("logic")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Arena Grid Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ModeCard(
                        title = "Memory Vault",
                        subtitle = "Cyber Grid Recall",
                        icon = Icons.Default.Memory,
                        accentColor = Purple500,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playClick()
                            onNavigateToBattle("memory")
                        }
                    )
                    ModeCard(
                        title = "Speed Rush",
                        subtitle = "45s Lightning Blitz",
                        icon = Icons.Default.Bolt,
                        accentColor = Amber400,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            SoundManager.playClick()
                            onNavigateToBattle("speed")
                        }
                    )
                }
            }

            item {
                // Daily Missions Section
                DailyMissionsCard(
                    missions = missions,
                    onClaim = { missionId -> viewModel.claimMissionReward(missionId) }
                )
            }

            item {
                // Cognitive Performance Stats Row
                StatsRow(userStats, aiTier)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Level Up Celebration Modal Overlay
        levelUpEvent?.let { reward ->
            LevelUpCelebrationModal(
                newLevel = reward.level,
                rewardCoins = reward.rewardCoins,
                onDismiss = { viewModel.dismissLevelUp() }
            )
        }

        streakRewardEvent?.let { streak ->
            com.example.ui.components.StreakRewardModal(
                streakDays = streak.streakDays,
                rewardCoins = streak.rewardCoins,
                rewardXp = streak.rewardXp,
                onDismiss = { viewModel.dismissStreakReward() }
            )
        }

        achievementUnlockEvent?.let { achievement ->
            com.example.ui.components.AchievementUnlockModal(
                achievement = achievement,
                onDismiss = { viewModel.dismissAchievementUnlock() }
            )
        }
    }
}

@Composable
fun TopProfileBar(
    stats: UserStats,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.img_avatar_ai),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, Cyan400, CircleShape)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .background(DarkSlate, CircleShape)
                    .border(1.dp, Cyan400.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LVL ${stats.level}",
                    color = Cyan400,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Human Operative", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate400)
            Text(stats.username, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Sound Effects Toggle Button
        IconButton(
            onClick = {
                SoundManager.playClick()
                onToggleSound()
            }
        ) {
            Icon(
                imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = "Toggle Audio",
                tint = if (soundEnabled) Cyan400 else Slate400.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Coins Badge
        Surface(
            color = GlassBackground,
            border = BorderStroke(1.dp, GlassBorder),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("◆", color = Amber400, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("${stats.coins}", color = Slate100, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun FeaturedDailyChallenge(
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Surface(
                color = if (isCompleted) Color(0xFF00E676) else Cyan400,
                shape = RoundedCornerShape(bottomStart = 12.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
            ) {
                Text(
                    text = if (isCompleted) "✓ COMPLETED" else "LIVE EVENT",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Column {
                Text("Daily Brain Sync", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Multi-Discipline Sequence & Deductive Trial", style = MaterialTheme.typography.bodySmall, color = Slate400)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("EVENT REWARD", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Cyan400)
                        Text("+350 🪙 • 2X XP", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Amber400)
                    }
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.border(1.dp, Cyan400.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Text("SYNC NOW", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AiCompanionBanner(
    selectedAi: AiPersonalityType,
    aiTier: AiDifficultyTier,
    onSwitchClick: () -> Unit
) {
    Surface(
        color = GlassBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, selectedAi.primaryColor.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSwitchClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = selectedAi.primaryColor.copy(alpha = 0.15f),
                shape = CircleShape,
                border = BorderStroke(1.dp, selectedAi.primaryColor),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = selectedAi.displayName.first().toString(),
                        color = selectedAi.primaryColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI RIVAL: ${selectedAi.displayName}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = selectedAi.primaryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = aiTier.badge,
                            color = selectedAi.primaryColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedAi.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            Text("Change", color = Cyan400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun DailyMissionsCard(
    missions: List<Mission>,
    onClaim: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY MISSIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Slate400
                )
                Text(
                    text = "${missions.count { it.isCompleted }} / ${missions.size}",
                    color = Cyan400,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            missions.forEach { mission ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mission.title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(mission.description, color = Slate400, style = MaterialTheme.typography.bodySmall)
                    }

                    if (mission.isClaimed) {
                        Surface(
                            color = Slate400.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "CLAIMED",
                                color = Slate400,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (mission.isCompleted) {
                        Button(
                            onClick = { onClaim(mission.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "+${mission.rewardCoins} 🪙",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                        }
                    } else {
                        Text(
                            "${mission.progress}/${mission.target}",
                            color = Slate400,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                if (mission != missions.last()) {
                    Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun StatsRow(stats: UserStats?, aiTier: AiDifficultyTier) {
    val totalGames = (stats?.winsCount ?: 0) + (stats?.lossesCount ?: 0)
    val winRate = if (totalGames > 0) ((stats?.winsCount ?: 0) * 100) / totalGames else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassBackground, RoundedCornerShape(24.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem("Win Rate", "$winRate%", Cyan400)
        Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
        StatItem("Streak", "${stats?.streak ?: 1}D", Color.White)
        Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
        StatItem("AI Rating", "${stats?.aiRating ?: 1200}", Purple500)
    }
}

@Composable
fun StatItem(label: String, value: String, valueColor: Color = Color.White) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate400)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = Cyan400,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate400, textAlign = TextAlign.Center)
        }
    }
}
