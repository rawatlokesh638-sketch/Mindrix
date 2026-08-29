package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiDifficultyTier
import com.example.audio.SoundManager
import com.example.ui.MainViewModel
import com.example.ui.screens.home.StatItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val userStats by viewModel.userStats.collectAsState()
    val soundEnabled by SoundManager.soundEnabled.collectAsState()
    val aiTier = remember(userStats?.aiRating) {
        AiDifficultyTier.fromRating(userStats?.aiRating ?: 1200)
    }

    val totalGames = (userStats?.winsCount ?: 0) + (userStats?.lossesCount ?: 0)
    val winRate = if (totalGames > 0) ((userStats?.winsCount ?: 0) * 100) / totalGames else 0

    Scaffold(
        containerColor = DarkSlate,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("OPERATIVE DOSSIER", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("NEURAL MATRIX METRICS", color = Cyan400, style = MaterialTheme.typography.labelSmall)
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
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${userStats?.level ?: 1}",
                                color = Cyan400,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(userStats?.username ?: "CyberMage", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(aiTier.badge, color = Cyan400, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                Spacer(modifier = Modifier.height(8.dp))

                // XP Progress Bar
                val currentXp = (userStats?.xp ?: 0) % 1000
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LEVEL PROGRESS", color = Slate400, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp))
                        Text("$currentXp / 1000 XP", color = Cyan400, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { currentXp / 1000f },
                        color = Cyan400,
                        trackColor = GlassBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            item {
                // Battle Stats Matrix
                Surface(
                    color = GlassBackground,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("CAREER COMBAT STATS", color = Slate400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Win Rate", "$winRate%", Cyan400)
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("Matches", "${userStats?.gamesPlayed ?: 0}", Color.White)
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("High Score", "${userStats?.bestScore ?: 0}", Amber400)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Wins", "${userStats?.winsCount ?: 0}", Color(0xFF00E676))
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("Losses", "${userStats?.lossesCount ?: 0}", Color(0xFFFF1744))
                            Spacer(modifier = Modifier.width(1.dp).height(32.dp).background(GlassBorder))
                            StatItem("AI Rating", "${userStats?.aiRating ?: 1200}", Purple500)
                        }
                    }
                }
            }

            item {
                // Cognitive Breakdown
                Surface(
                    color = GlassBackground,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("NEURAL ATTRIBUTES", color = Slate400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))

                        CognitiveSkillBar("Deductive Logic", 0.88f, Cyan400)
                        CognitiveSkillBar("Sequence Recall", 0.74f, Purple500)
                        CognitiveSkillBar("Processing Speed", 0.92f, Amber400)
                        CognitiveSkillBar("Pattern Synthesis", 0.82f, Color(0xFF00E676))
                        CognitiveSkillBar("Reflex Synchronization", 0.78f, Color(0xFFFF0055))
                    }
                }
            }

            item {
                // Achievements & Daily Streak Dossier
                val unlockedSet = remember(userStats?.unlockedAchievements) {
                    userStats?.unlockedAchievements?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
                }

                Surface(
                    color = GlassBackground,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ACHIEVEMENT BADGES & STREAK", color = Slate400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                            Text("${unlockedSet.size} / ${com.example.data.AchievementCatalog.allAchievements.size}", color = Cyan400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        // Streak Status Banner
                        Surface(
                            color = Amber400.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Amber400.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔥", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Daily Streak: ${userStats?.streak ?: 1} Days", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(if (userStats?.dailySessionCompletedToday == true) "Today's neural session completed! Streak secured." else "Complete a session today to keep streak active.", color = Amber400, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Achievement Badges List
                        com.example.data.AchievementCatalog.allAchievements.forEach { ach ->
                            val isUnlocked = unlockedSet.contains(ach.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isUnlocked) Cyan400.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(16.dp))
                                    .border(1.dp, if (isUnlocked) Cyan400.copy(alpha = 0.3f) else GlassBorder, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
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
                                    Text(ach.title, color = if (isUnlocked) Color.White else Slate400, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(ach.description, color = Slate400, style = MaterialTheme.typography.bodySmall)
                                }
                                if (isUnlocked) {
                                    Surface(
                                        color = Color(0xFF00E676).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("UNLOCKED", color = Color(0xFF00E676), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                } else {
                                    Text("+${ach.rewardCoins} 🪙", color = Amber400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
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

@Composable
fun CognitiveSkillBar(name: String, value: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            Text("${(value * 100).toInt()}%", color = color, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value },
            color = color,
            trackColor = GlassBorder,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
