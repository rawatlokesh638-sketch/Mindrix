package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Auth

@Serializable
object Splash

@Serializable
object Home

@Serializable
object ModeSelection

@Serializable
data class Gameplay(val mode: String = "logic", val difficulty: String = "normal")

@Serializable
object Result

@Serializable
object Profile

@Serializable
object Leaderboard

@Serializable
object Shop
