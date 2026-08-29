package com.example.ui.screens.leaderboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiDifficultyTier
import com.example.audio.SoundManager
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.userStats.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allUsersByRating by viewModel.allUsersByRating.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkSlate,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GLOBAL RANKINGS", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("REGISTERED COGNITIVE LADDER", color = Cyan400, style = MaterialTheme.typography.labelSmall)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = GlassBackground,
                    contentColor = Cyan400,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Cyan400
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            SoundManager.playClick()
                            selectedTab = 0
                        },
                        text = { Text("GLOBAL ARENA", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            SoundManager.playClick()
                            selectedTab = 1
                        },
                        text = { Text("AI ELO TIERS", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            val list = if (selectedTab == 0) allUsers else allUsersByRating

            if (list.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No registered users found.", color = Slate400, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                itemsIndexed(list) { index, user ->
                    val rank = index + 1
                    val isTop3 = rank <= 3
                    val isCurrentUser = currentUser?.email == user.email
                    val rankColor = when (rank) {
                        1 -> Amber400
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> Slate400
                    }

                    val tier = AiDifficultyTier.fromRating(user.aiRating).name

                    Surface(
                        color = if (isCurrentUser) Cyan400.copy(alpha = 0.15f) else GlassBackground,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(if (isCurrentUser) 2.dp else 1.dp, if (isCurrentUser) Cyan400 else GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isTop3) rankColor.copy(alpha = 0.2f) else Slate400.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#$rank",
                                        color = if (isTop3) rankColor else Slate400,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isCurrentUser) "${user.username} (You)" else user.username,
                                        color = if (isCurrentUser) Cyan400 else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Text(
                                    text = "Tier: $tier  •  Level ${user.level}",
                                    color = Slate400,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                )
                            }

                            Text(
                                text = if (selectedTab == 0) "${user.bestScore} PTS" else "${user.aiRating} ELO",
                                color = if (isCurrentUser) Amber400 else Color.White,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
