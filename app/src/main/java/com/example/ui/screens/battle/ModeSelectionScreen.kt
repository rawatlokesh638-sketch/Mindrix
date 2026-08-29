package com.example.ui.screens.battle

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiDifficultyTier
import com.example.ai.AiPersonalityType
import com.example.audio.SoundManager
import com.example.ui.MainViewModel
import com.example.ui.theme.*

data class GameModeInfo(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val xpBonus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onModeSelected: (String, String) -> Unit
) {
    val selectedAi by viewModel.selectedAi.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    var selectedDifficulty by remember { mutableStateOf("normal") } // normal, intermediate, difficult

    val aiTier = remember(userStats?.aiRating) {
        AiDifficultyTier.fromRating(userStats?.aiRating ?: 1200)
    }

    val modes = listOf(
        GameModeInfo(
            id = "ai_battle",
            title = "AI Face-Off",
            category = "PVP BATTLE",
            description = "Live dual confrontation against your chosen AI core in real-time.",
            icon = Icons.Default.SmartToy,
            accentColor = selectedAi.primaryColor,
            xpBonus = "+150% XP"
        ),
        GameModeInfo(
            id = "logic",
            title = "Logic Lab",
            category = "DEDUCTION",
            description = "Syllogisms, truth analysis, relational scale balances.",
            icon = Icons.Default.Psychology,
            accentColor = Cyan400,
            xpBonus = "+100% XP"
        ),
        GameModeInfo(
            id = "memory",
            title = "Memory Vault",
            category = "RECALL",
            description = "Memorize and reconstruct glowing cyber sequence grids.",
            icon = Icons.Default.Memory,
            accentColor = Purple500,
            xpBonus = "+120% XP"
        ),
        GameModeInfo(
            id = "pattern",
            title = "Pattern Prediction",
            category = "SERIES",
            description = "Accelerating mathematical progressions and cipher matrices.",
            icon = Icons.Default.AutoGraph,
            accentColor = Color(0xFF00E676),
            xpBonus = "+100% XP"
        ),
        GameModeInfo(
            id = "speed",
            title = "Speed Rush",
            category = "RAPID BLITZ",
            description = "45-second lightning blitz testing mental speed under pressure.",
            icon = Icons.Default.Bolt,
            accentColor = Amber400,
            xpBonus = "+130% XP"
        ),
        GameModeInfo(
            id = "reflex",
            title = "Reflex Matrix",
            category = "NEURAL SYNC",
            description = "High-velocity signal scanner testing reaction times under 800ms.",
            icon = Icons.Default.Speed,
            accentColor = Color(0xFFFF0055),
            xpBonus = "+110% XP"
        ),
        GameModeInfo(
            id = "spatial",
            title = "Spatial Rotation",
            category = "3D COGNITION",
            description = "Mental rotation, shape folding, and mirror orientation matching.",
            icon = Icons.Default.ViewInAr,
            accentColor = Color(0xFF38BDF8),
            xpBonus = "+130% XP"
        ),
        GameModeInfo(
            id = "attention",
            title = "Attention Grid",
            category = "FOCUS & SCAN",
            description = "Schulte grid number tracking from 1 to N under strict time limits.",
            icon = Icons.Default.GridOn,
            accentColor = Color(0xFFEC4899),
            xpBonus = "+140% XP"
        ),
        GameModeInfo(
            id = "vocabulary",
            title = "Cognitive Lexicon",
            category = "SEMANTIC",
            description = "Advanced vocabulary definitions, synonyms, and etymology tests.",
            icon = Icons.Default.MenuBook,
            accentColor = Color(0xFFA855F7),
            xpBonus = "+110% XP"
        ),
        GameModeInfo(
            id = "stroop",
            title = "Stroop Inhibition",
            category = "NEURAL INHIBITION",
            description = "Test cognitive interference between word semantics and ink colors.",
            icon = Icons.Default.Palette,
            accentColor = Color(0xFFFF9800),
            xpBonus = "+150% XP"
        ),
        GameModeInfo(
            id = "arithmetic",
            title = "Arithmetic Chain",
            category = "NUMERICAL",
            description = "Multi-step rapid mental calculations and equation balancing.",
            icon = Icons.Default.Calculate,
            accentColor = Color(0xFF00E676),
            xpBonus = "+120% XP"
        ),
        GameModeInfo(
            id = "balance",
            title = "Quantum Scale Balance",
            category = "LOGIC BALANCE",
            description = "Weigh complex multi-pan algebraic cyber scales.",
            icon = Icons.Default.Balance,
            accentColor = Color(0xFF00BCD4),
            xpBonus = "+130% XP"
        ),
        GameModeInfo(
            id = "daily",
            title = "Daily Brain Sync",
            category = "EVENT",
            description = "Curated multi-discipline trial with special bonus rewards.",
            icon = Icons.Default.EmojiEvents,
            accentColor = Color(0xFF22D3EE),
            xpBonus = "2X REWARDS"
        )
    )

    Scaffold(
        containerColor = DarkSlate,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ARENA SELECTION", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("CHOOSE YOUR CHALLENGE & DIFFICULTY", color = Cyan400, style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Difficulty Selector (Normal, Intermediate, Difficult - applies to ALL games)
                Text(
                    "DIFFICULTY LEVEL (ALL GAMES)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val difficulties = listOf(
                        Triple("normal", "Normal", Color(0xFF00E676)),
                        Triple("intermediate", "Intermediate", Amber400),
                        Triple("difficult", "Difficult", Color(0xFFFF1744))
                    )
                    difficulties.forEach { (id, label, col) ->
                        val isSelected = selectedDifficulty == id
                        Surface(
                            color = if (isSelected) col.copy(alpha = 0.2f) else GlassBackground,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) col else GlassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    SoundManager.playClick()
                                    selectedDifficulty = id
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) col else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (id) {
                                        "normal" -> "1.0x Pts"
                                        "intermediate" -> "1.5x Pts"
                                        else -> "2.0x Pts"
                                    },
                                    color = Slate400,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                // AI Personality Selector Section
                Text(
                    "OPPONENT AI PERSONALITY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Slate400
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AiPersonalityType.entries) { ai ->
                        val isSelected = selectedAi == ai
                        Surface(
                            color = if (isSelected) ai.primaryColor.copy(alpha = 0.15f) else GlassBackground,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) ai.primaryColor else GlassBorder),
                            modifier = Modifier
                                .width(140.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    SoundManager.playClick()
                                    viewModel.selectAiPersonality(ai)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = ai.primaryColor.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(ai.displayName.first().toString(), color = ai.primaryColor, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(ai.displayName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    when (ai) {
                                        AiPersonalityType.NOVA -> "Friendly"
                                        AiPersonalityType.VEX -> "Sarcastic"
                                        AiPersonalityType.ZERO -> "Ruthless"
                                    },
                                    color = ai.primaryColor,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AVAILABLE BRAIN TRAINING ARENAS (${modes.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Slate400
                    )
                }
            }

            items(modes) { modeItem ->
                Surface(
                    color = GlassBackground,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            SoundManager.playClick()
                            onModeSelected(modeItem.id, selectedDifficulty)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = modeItem.accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, modeItem.accentColor.copy(alpha = 0.3f)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = modeItem.icon,
                                    contentDescription = modeItem.title,
                                    tint = modeItem.accentColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = modeItem.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = modeItem.accentColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = modeItem.xpBonus,
                                        color = modeItem.accentColor,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = modeItem.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Start",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
