package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager
import com.example.engine.XpEngine
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Particle(
    val initialX: Float,
    val initialY: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val size: Float,
    val lifeTime: Float
)

@Composable
fun ParticleExplosion(
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    particleCount: Int = 36
) {
    val particles = remember(triggerKey) {
        val colors = listOf(Cyan400, Amber400, Purple500, Color(0xFF00E676), Color(0xFFFF4081))
        List(particleCount) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextDouble(120.0, 450.0).toFloat()
            Particle(
                initialX = 0.5f,
                initialY = 0.5f,
                speedX = (Math.cos(angle) * speed).toFloat(),
                speedY = (Math.sin(angle) * speed).toFloat(),
                color = colors.random(),
                size = Random.nextDouble(4.0, 10.0).toFloat(),
                lifeTime = Random.nextDouble(0.7, 1.4).toFloat()
            )
        }
    }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing)
        )
    }

    if (animProgress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val p = animProgress.value

            particles.forEach { particle ->
                val currentX = cx + particle.speedX * p
                val currentY = cy + particle.speedY * p + (90f * p * p) // slight gravity
                val alpha = (1f - p).coerceIn(0f, 1f)
                val currentSize = particle.size * (1f - p * 0.4f)

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = currentSize,
                    center = Offset(currentX, currentY)
                )
            }
        }
    }
}

@Composable
fun HolographicLevelHalo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(130.dp)
            .scale(scalePulse),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
            val radius = size.minDimension / 2 - 8f
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Cyan400.copy(alpha = 0.8f),
                        Purple500.copy(alpha = 0.4f),
                        Amber400.copy(alpha = 0.8f),
                        Cyan400.copy(alpha = 0.8f)
                    )
                ),
                radius = radius,
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
                )
            )
        }
    }
}

@Composable
fun AnimatedScoreCounter(
    targetScore: Int,
    modifier: Modifier = Modifier,
    durationMs: Int = 1000,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium,
    prefix: String = "",
    suffix: String = " PTS"
) {
    var displayedScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetScore) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < durationMs) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / durationMs
            val easedProgress = 1f - (1f - progress) * (1f - progress)
            displayedScore = (targetScore * easedProgress).toInt()
            delay(16)
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

    val rankTitle = remember(newLevel) { XpEngine.getOperativeTitle(newLevel) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ParticleExplosion(triggerKey = newLevel, modifier = Modifier.matchParentSize(), particleCount = 45)

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
                    Box(contentAlignment = Alignment.Center) {
                        HolographicLevelHalo()

                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(
                                    Brush.radialGradient(listOf(Cyan400.copy(alpha = 0.35f), Color.Transparent)),
                                    CircleShape
                                )
                                .border(2.dp, Cyan400, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("LEVEL", fontSize = 10.sp, color = Cyan400, fontWeight = FontWeight.Black)
                                Text(
                                    "$newLevel",
                                    fontSize = 32.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "RANK ASCENSION!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = Cyan400
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = Cyan400.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = rankTitle.uppercase(),
                            color = Cyan400,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Neural synaptic throughput expanded to Level $newLevel.",
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
                            Text("🪙 Ascension Bounty:", color = Slate400, fontWeight = FontWeight.Medium)
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
                            "CLAIM ASCENSION",
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
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Brush.radialGradient(listOf(Amber400.copy(alpha = 0.35f), Color.Transparent)),
                                CircleShape
                            )
                            .border(2.dp, Amber400, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "DAILY STREAK: $streakDays DAYS!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Amber400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Neural activity secured. Continuous operation active!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 7-Day Streak Ladder Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (day in 1..7) {
                            val isPast = day <= streakDays
                            val isCurrent = day == ((streakDays - 1) % 7 + 1)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = when {
                                        isCurrent -> Amber400
                                        isPast -> Amber400.copy(alpha = 0.3f)
                                        else -> DarkSlate
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isPast || isCurrent) Amber400 else GlassBorder
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isPast) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = if (isCurrent) Color.Black else Amber400, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text("D$day", fontSize = 9.sp, color = Slate400, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "+${day * 20}",
                                    fontSize = 9.sp,
                                    color = if (isPast || isCurrent) Amber400 else Slate400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Surface(
                        color = GlassBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TODAY'S STREAK BOUNTY", color = Slate400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+$rewardCoins 🪙   •   +$rewardXp XP", color = Amber400, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            SoundManager.playClick()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            "CLAIM STREAK REWARD",
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

                    Spacer(modifier = Modifier.height(4.dp))

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
                            Text("Bounty:", color = Slate400, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+${achievement.rewardCoins} 🪙  •  +${achievement.rewardXp} XP", color = Amber400, fontWeight = FontWeight.Bold)
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
                            "CLAIM BOUNTY",
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
fun AiOutsmartBanner(
    combo: Int,
    aiName: String,
    modifier: Modifier = Modifier
) {
    if (combo < 2) return

    val infiniteTransition = rememberInfiniteTransition(label = "outsmart_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val scaleAnim = remember { Animatable(0.8f) }
    LaunchedEffect(combo) {
        scaleAnim.snapTo(0.8f)
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scaleAnim.value),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Cyan400.copy(alpha = 0.18f * pulseAlpha),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Cyan400),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Cyan400.copy(alpha = 0.3f),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🧠", fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🧠 AI OUTSMARTED! (${combo}x COMBO)",
                            color = Cyan400,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "You've outmaneuvered $aiName in consecutive rounds!",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }
}

