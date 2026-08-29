package com.example.ui.screens.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiDifficultyTier
import com.example.ai.AiPersonalityEngine
import com.example.audio.SoundManager
import com.example.ui.MainViewModel
import com.example.ui.components.AnimatedScoreCounter
import com.example.ui.components.LevelUpCelebrationModal
import com.example.ui.components.ParticleExplosion
import com.example.ui.theme.*

@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    onNavigateHome: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val score by viewModel.lastScore.collectAsState()
    val accuracy by viewModel.lastAccuracy.collectAsState()
    val time by viewModel.lastTime.collectAsState()
    val isWin by viewModel.lastGameWon.collectAsState()
    val mode by viewModel.lastGameMode.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val selectedAi by viewModel.selectedAi.collectAsState()
    val levelUpEvent by viewModel.levelUpEvent.collectAsState()
    val streakRewardEvent by viewModel.streakRewardEvent.collectAsState()
    val achievementUnlockEvent by viewModel.achievementUnlockEvent.collectAsState()

    val aiTier = remember(userStats?.aiRating) {
        AiDifficultyTier.fromRating(userStats?.aiRating ?: 1200)
    }

    val aiComment = remember(score, accuracy, isWin, selectedAi) {
        AiPersonalityEngine.getPostGameAnalysis(
            personality = selectedAi,
            score = score,
            accuracy = accuracy,
            isWin = isWin
        )
    }

    LaunchedEffect(Unit) {
        if (isWin) {
            SoundManager.playCorrect()
        } else {
            SoundManager.playGameOver()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlate)
    ) {
        if (isWin) {
            ParticleExplosion(triggerKey = score, particleCount = 40)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = if (isWin) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF1744).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isWin) Color(0xFF00E676) else Color(0xFFFF1744)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isWin) "VICTORY ACHIEVED" else "CHALLENGE CONCLUDED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                        color = if (isWin) Color(0xFF00E676) else Color(0xFFFF1744),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("FINAL SCORE", color = Slate400, style = MaterialTheme.typography.labelMedium)
                
                AnimatedScoreCounter(
                    targetScore = score,
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Cyan400
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats Grid
                Surface(
                    color = GlassBackground,
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACCURACY", color = Slate400, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$accuracy%", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.width(1.dp).height(28.dp).background(GlassBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AI RATING", color = Slate400, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${userStats?.aiRating ?: 1200}", color = selectedAi.primaryColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.width(1.dp).height(28.dp).background(GlassBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REWARD", color = Slate400, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+${(score * 0.08f).toInt()} 🪙", color = Amber400, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AI Personality Debrief Box
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                    border = BorderStroke(1.dp, selectedAi.primaryColor.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_avatar_ai),
                                contentDescription = selectedAi.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, selectedAi.primaryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${selectedAi.displayName} DEBRIEF",
                                    color = selectedAi.primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = selectedAi.title,
                                    color = Slate400,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "\"$aiComment\"",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = Slate100,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        onPlayAgain()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan400),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("PLAY AGAIN", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick = {
                        SoundManager.playClick()
                        onNavigateHome()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
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
