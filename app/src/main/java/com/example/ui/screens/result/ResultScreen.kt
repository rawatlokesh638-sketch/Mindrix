package com.example.ui.screens.result

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiDifficultyTier
import com.example.ai.AiPersonalityEngine
import com.example.audio.SoundManager
import com.example.engine.XpEngine
import com.example.ui.MainViewModel
import com.example.ui.components.AchievementUnlockModal
import com.example.ui.components.AnimatedScoreCounter
import com.example.ui.components.LevelUpCelebrationModal
import com.example.ui.components.ParticleExplosion
import com.example.ui.components.StreakRewardModal
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
    val xpBreakdown by viewModel.lastXpBreakdown.collectAsState()

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

    val scrollState = rememberScrollState()

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
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            Text("FINAL SCORE", color = Slate400, style = MaterialTheme.typography.labelMedium)

            AnimatedScoreCounter(
                targetScore = score,
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Cyan400
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Combat Metric Grid
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
                        Text("COINS", color = Slate400, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("+${xpBreakdown?.totalCoinsEarned ?: (score * 0.08f).toInt()} 🪙", color = Amber400, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Server-Side XP & Progression Terminal Card
            Surface(
                color = GlassBackground,
                border = BorderStroke(1.dp, Cyan400.copy(alpha = 0.4f)),
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
                            Text("⚡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "XP PERFORMANCE BREAKDOWN",
                                color = Cyan400,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            )
                        }

                        Text(
                            "+${xpBreakdown?.totalXpEarned ?: 0} XP",
                            color = Amber400,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Itemized breakdown rows
                    xpBreakdown?.let { bd ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base Game XP", color = Slate400, fontSize = 12.sp)
                            Text("+${bd.baseXp} XP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (bd.accuracyBonusXp > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Precision Bonus ($accuracy%)", color = Slate400, fontSize = 12.sp)
                                Text("+${bd.accuracyBonusXp} XP", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (bd.speedBonusXp > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Reflex Speed Bonus", color = Slate400, fontSize = 12.sp)
                                Text("+${bd.speedBonusXp} XP", color = Cyan400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (bd.streakBonusXp > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Daily Streak Multiplier", color = Slate400, fontSize = 12.sp)
                                Text("+${bd.streakBonusXp} XP", color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (bd.achievementBonusXp > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Achievement Unlocks", color = Slate400, fontSize = 12.sp)
                                Text("+${bd.achievementBonusXp} XP", color = Purple500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = GlassBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Level Progress Bar
                        val rankTitle = XpEngine.getOperativeTitle(bd.newLevel)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LEVEL ${bd.newLevel} • $rankTitle",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "${bd.currentLevelXp} / ${bd.nextLevelTargetXp} XP",
                                color = Cyan400,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { bd.levelProgressRatio },
                            color = Cyan400,
                            trackColor = GlassBorder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = context as? android.app.Activity
            val doubleRewardClaimed by viewModel.doubleRewardClaimed.collectAsState()

            // 2X Double Match Bounty Card (Rewarded Ad)
            if (!doubleRewardClaimed && (xpBreakdown?.totalCoinsEarned ?: 0) > 0) {
                Surface(
                    color = Amber400.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Amber400),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "2X MATCH BOUNTY",
                                    color = Amber400,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Watch short ad to double your +${xpBreakdown?.totalCoinsEarned} 🪙 & +${xpBreakdown?.totalXpEarned} XP!",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                SoundManager.playClick()
                                activity?.let { act ->
                                    com.example.ads.AdManager.showRewardedAd(
                                        activity = act,
                                        onRewardEarned = { _, _ ->
                                            viewModel.claimDoubleMatchReward()
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CLAIM 2X", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else if (doubleRewardClaimed) {
                Surface(
                    color = Color(0xFF00E676).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF00E676)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2X BOUNTY APPLIED TO OPERATIVE ACCOUNT", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // AI Personality Debrief Box
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                border = BorderStroke(1.dp, selectedAi.primaryColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
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
                                .size(40.dp)
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"$aiComment\"",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = Slate100,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        onPlayAgain()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
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
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
            StreakRewardModal(
                streakDays = streak.streakDays,
                rewardCoins = streak.rewardCoins,
                rewardXp = streak.rewardXp,
                onDismiss = { viewModel.dismissStreakReward() }
            )
        }

        achievementUnlockEvent?.let { achievement ->
            AchievementUnlockModal(
                achievement = achievement,
                onDismiss = { viewModel.dismissAchievementUnlock() }
            )
        }
    }
}
