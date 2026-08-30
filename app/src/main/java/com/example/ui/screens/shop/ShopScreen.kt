package com.example.ui.screens.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.audio.SoundManager
import com.example.ui.MainViewModel
import com.example.ui.theme.*

data class ShopItem(
    val id: String,
    val title: String,
    val description: String,
    val cost: Int,
    val type: String, // "avatar", "theme", "powerup"
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(viewModel: MainViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val unlockedAvatars = remember(userStats?.unlockedAvatars) {
        userStats?.unlockedAvatars?.split(",")?.toSet() ?: setOf("avatar_cyber")
    }
    val unlockedThemes = remember(userStats?.unlockedThemes) {
        userStats?.unlockedThemes?.split(",")?.toSet() ?: setOf("theme_cyan")
    }

    val avatarItems = listOf(
        ShopItem("avatar_cyber", "Cybernetic Neural", "Default human cybernetic operative frame.", 0, "avatar", Icons.Default.SmartToy, Cyan400),
        ShopItem("avatar_quantum", "Quantum Valkyrie", "Holographic photon-forged battle avatar.", 350, "avatar", Icons.Default.Shield, Purple500),
        ShopItem("avatar_synth", "Synthwave Phantom", "Retro-futuristic laser grid consciousness.", 600, "avatar", Icons.Default.Bolt, Amber400),
        ShopItem("avatar_oracle", "Omni Oracle", "Higher dimensional singularity consciousness.", 1200, "avatar", Icons.Default.AutoAwesome, Color(0xFFFF0055))
    )

    val themeItems = listOf(
        ShopItem("theme_cyan", "Cyber Cyan", "Standard high-contrast cyan neon interface.", 0, "theme", Icons.Default.Palette, Cyan400),
        ShopItem("theme_matrix", "Matrix Emerald", "Sub-surface emerald code waterfall glow.", 400, "theme", Icons.Default.Terminal, Color(0xFF00E676)),
        ShopItem("theme_solar", "Solar Flare", "High energy golden hyper-drive luminescence.", 750, "theme", Icons.Default.WbSunny, Amber400),
        ShopItem("theme_void", "Void Magenta", "Dark matter ultra-violet singularity aesthetic.", 1000, "theme", Icons.Default.NightlightRound, Color(0xFFFF0055))
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    Scaffold(
        containerColor = DarkSlate,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CYBERNETIC ARMORY", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("UPGRADE & CUSTOMIZE", color = Cyan400, style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    Surface(
                        color = GlassBackground,
                        border = BorderStroke(1.dp, GlassBorder),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("◆", color = Amber400, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${userStats?.coins ?: 0}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
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
                // Free Coin & XP Supply Requisition (Rewarded Video Ad)
                Surface(
                    color = Amber400.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Amber400.copy(alpha = 0.5f)),
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
                                color = Amber400.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🎁", fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "FREE REQUISITION",
                                    color = Amber400,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)
                                )
                                Text(
                                    "Watch short ad for +150 🪙 & +50 XP",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                SoundManager.playClick()
                                activity?.let { act ->
                                    com.example.ads.AdManager.showRewardedAd(
                                        activity = act,
                                        onRewardEarned = { _, _ ->
                                            viewModel.claimAdReward(coins = 150, xp = 50)
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("WATCH", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

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
                        text = { Text("AVATARS", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            SoundManager.playClick()
                            selectedTab = 1
                        },
                        text = { Text("NEON THEMES", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            val currentList = if (selectedTab == 0) avatarItems else themeItems

            items(currentList) { item ->
                val isUnlocked = if (item.type == "avatar") unlockedAvatars.contains(item.id) else unlockedThemes.contains(item.id)
                val isEquipped = if (item.type == "avatar") userStats?.activeAvatar == item.id else userStats?.activeTheme == item.id
                val canAfford = (userStats?.coins ?: 0) >= item.cost

                Surface(
                    color = GlassBackground,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(if (isEquipped) 2.dp else 1.dp, if (isEquipped) item.color else GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = item.color.copy(alpha = 0.15f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, item.color.copy(alpha = 0.4f)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(26.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                if (isEquipped) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = item.color.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "EQUIPPED",
                                            color = item.color,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = Slate400)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        if (isEquipped) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = item.color, modifier = Modifier.size(28.dp))
                        } else if (isUnlocked) {
                            OutlinedButton(
                                onClick = {
                                    if (item.type == "avatar") viewModel.equipAvatar(item.id) else viewModel.equipTheme(item.id)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Cyan400),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("EQUIP", color = Cyan400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        } else {
                            Button(
                                onClick = {
                                    val success = if (item.type == "avatar") {
                                        viewModel.buyAvatar(item.id, item.cost)
                                    } else {
                                        viewModel.buyTheme(item.id, item.cost)
                                    }
                                    if (!success) {
                                        SoundManager.playWrong()
                                    }
                                },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) Amber400 else Slate400.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "${item.cost} 🪙",
                                    color = if (canAfford) Color.Black else Slate400,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
