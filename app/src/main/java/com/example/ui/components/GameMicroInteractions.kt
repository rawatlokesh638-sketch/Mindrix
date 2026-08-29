package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Purple500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val maxLife: Float,
    var life: Float = 0f
)

@Composable
fun ParticleExplosion(
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    colors: List<Color> = listOf(Cyan400, Purple500, Amber400, Color.White)
) {
    var particles by remember { mutableStateOf(listOf<Particle>()) }

    LaunchedEffect(triggerKey) {
        if (triggerKey != null && triggerKey != 0) {
            particles = List(particleCount) { id ->
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                val speed = Random.nextFloat() * 400f + 100f
                Particle(
                    id = id,
                    x = 0.5f,
                    y = 0.5f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = colors.random(),
                    size = Random.nextFloat() * 8f + 4f,
                    maxLife = Random.nextFloat() * 0.6f + 0.4f
                )
            }
        }
    }

    if (particles.isNotEmpty()) {
        val animProgress = remember { Animatable(0f) }
        LaunchedEffect(particles) {
            animProgress.snapTo(0f)
            animProgress.animateTo(1f, animationSpec = tween(700, easing = LinearEasing))
            particles = emptyList()
        }

        Canvas(modifier = modifier.fillMaxSize()) {
            val progress = animProgress.value
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            particles.forEach { p ->
                val currentX = centerX + p.vx * progress * (1f - progress * 0.3f)
                val currentY = centerY + p.vy * progress + (progress * progress * 200f) // slight gravity
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val currentSize = p.size * (1f - progress * 0.5f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = currentSize,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}

@Composable
fun AnimatedScoreCounter(
    targetScore: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    var displayedScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetScore) {
        val startScore = displayedScore
        val diff = targetScore - startScore
        if (diff == 0) return@LaunchedEffect

        val steps = 30
        val stepDuration = 20L
        for (i in 1..steps) {
            val progress = i / steps.toFloat()
            // Ease out cubic
            val eased = 1f - (1f - progress) * (1f - progress) * (1f - progress)
            displayedScore = startScore + (diff * eased).toInt()
            delay(stepDuration)
        }
        displayedScore = targetScore
    }

    Text(
        text = "$prefix$displayedScore$suffix",
        style = textStyle,
        color = Color.White
    )
}

@Composable
fun LevelUpCelebrationModal(
    newLevel: Int,
    rewardCoins: Int,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        SoundManager.playLevelUp()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ParticleExplosion(triggerKey = newLevel, modifier = Modifier.matchParentSize())

            Surface(
                color = DarkSlate,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Cyan400),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(listOf(Cyan400.copy(alpha = 0.3f), Color.Transparent)),
                                CircleShape
                            )
                            .border(2.dp, Cyan400, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MilitaryTech,
                            contentDescription = "Level Up",
                            tint = Cyan400,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "LEVEL UP!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Cyan400
                    )

                    Text(
                        text = "You ascended to Level $newLevel",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = GlassBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🪙 Reward:", color = Slate400, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+$rewardCoins COINS", color = Amber400, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            SoundManager.playClick()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan400),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            "CLAIM REWARD",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerFeedbackBanner(
    isCorrect: Boolean?,
    combo: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isCorrect != null,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        if (isCorrect == true) {
            Surface(
                color = Color(0xFF00E676).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓ CORRECT", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    if (combo > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF00E676),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "${combo}x COMBO",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        } else if (isCorrect == false) {
            Surface(
                color = Color(0xFFFF1744).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✕ MISSED", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StreakRewardModal(
    streakDays: Int,
    rewardCoins: Int,
    rewardXp: Int,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        SoundManager.playLevelUp()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ParticleExplosion(triggerKey = streakDays, modifier = Modifier.matchParentSize())

            Surface(
                color = DarkSlate,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Amber400),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(listOf(Amber400.copy(alpha = 0.3f), Color.Transparent)),
                                CircleShape
                            )
                            .border(2.dp, Amber400, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "DAILY STREAK $streakDays DAYS!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Amber400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Logged in and completed today's neural session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = GlassBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REWARD SECURED", color = Slate400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+$rewardCoins 🪙   •   +$rewardXp XP", color = Amber400, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            SoundManager.playClick()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            "CLAIM STREAK BONUS",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementUnlockModal(
    achievement: com.example.data.Achievement,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        SoundManager.playLevelUp()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ParticleExplosion(triggerKey = achievement.id, modifier = Modifier.matchParentSize())

            Surface(
                color = DarkSlate,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Cyan400),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(listOf(Cyan400.copy(alpha = 0.3f), Color.Transparent)),
                                CircleShape
                            )
                            .border(2.dp, Cyan400, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(achievement.iconSymbol, fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ACHIEVEMENT UNLOCKED!",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Cyan400
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = GlassBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Bonus: +${achievement.rewardCoins} 🪙  •  +${achievement.rewardXp} XP", color = Amber400, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            SoundManager.playClick()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan400),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            "AWESOME",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
