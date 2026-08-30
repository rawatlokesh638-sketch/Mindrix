package com.example.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Cyan400
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.GlassBorder

@Composable
fun SplashScreen() {
    // Logo scale animation - entrance effect
    val logoAnimationDuration = 800
    val logoPulseAnimation = rememberInfiniteTransition(label = "logoPulse")
    val logoScale by logoPulseAnimation.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    // Title fade-in animation
    val titleAnimationDuration = 600
    val titleDelayMs = 300
    val titleAnimation = rememberInfiniteTransition(label = "titleFade")
    val titleAlpha by titleAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleAlpha"
    )

    // Subtitle glow animation
    val subtitleAnimation = rememberInfiniteTransition(label = "subtitleGlow")
    val subtitleGlow by subtitleAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtitleGlow"
    )

    // Background orb animations
    val orbAnimation1 = rememberInfiniteTransition(label = "orb1")
    val orbOffset1X by orbAnimation1.animateFloat(
        initialValue = -50f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbOffset1X"
    )

    val orbAnimation2 = rememberInfiniteTransition(label = "orb2")
    val orbOffset2Y by orbAnimation2.animateFloat(
        initialValue = 50f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbOffset2Y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSlate),
        contentAlignment = Alignment.Center
    ) {
        // Animated background orb - top start
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopStart)
                .offset(x = orbOffset1X.dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Cyan400.copy(alpha = 0.25f),
                            Cyan400.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 140f
                    ),
                    CircleShape
                )
                .blur(60.dp)
        )

        // Animated background orb - bottom end
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = orbOffset2Y.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Cyan400.copy(alpha = 0.2f),
                            Cyan400.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 110f
                    ),
                    CircleShape
                )
                .blur(70.dp)
        )

        // Static subtle background orb - center
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .offset(x = 50.dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Cyan400.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 150f
                    ),
                    CircleShape
                )
                .blur(80.dp)
        )

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brain Logo Container with pulsing border
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Cyan400.copy(alpha = 0.25f),
                                Cyan400.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = 60f
                        ),
                        CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = Cyan400.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧠",
                    fontSize = 64.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // MINDRIX Title with staggered animation
            Text(
                text = "MINDRIX",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    fontSize = 48.sp
                ),
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha.coerceIn(0.5f, 1f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle with glow effect
            Text(
                text = "Neural Core Portal",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                ),
                color = Cyan400.copy(alpha = subtitleGlow),
                modifier = Modifier.alpha(subtitleGlow.coerceIn(0.4f, 1f))
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Loading spinner with custom colors
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Cyan400.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Cyan400,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(4.dp),
                    strokeWidth = 3.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading status text with pulsing effect
            Text(
                text = "Restoring Session...",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = Cyan400.copy(alpha = subtitleGlow.coerceIn(0.5f, 0.9f))
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Decorative bottom accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Cyan400.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Optional: Fingerprint-like corner accents for premium feel
        // Top-left accent
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = (-40).dp)
                .border(
                    width = 1.dp,
                    color = Cyan400.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )

        // Bottom-right accent
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
                .border(
                    width = 1.dp,
                    color = Cyan400.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
    }
}

// Helper extension to add border
fun Modifier.border(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    shape: androidx.compose.foundation.shape.Shape
) = androidx.compose.foundation.border(width, color, shape)
