package com.example.ui.screens.gameplay

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiDifficultyTier
import com.example.ai.AiPersonalityEngine
import com.example.ai.AiPersonalityType
import com.example.audio.SoundManager
import com.example.game.*
import com.example.ui.MainViewModel
import com.example.ui.components.AnswerFeedbackBanner
import com.example.ui.components.ParticleExplosion
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayScreen(
    mode: String,
    difficulty: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val userStats by viewModel.userStats.collectAsState()
    val selectedAi by viewModel.selectedAi.collectAsState()
    val aiTier = remember(userStats?.aiRating) {
        AiDifficultyTier.fromRating(userStats?.aiRating ?: 1200)
    }

    val difficultyMultiplier = when (difficulty) {
        "intermediate" -> 1.5f
        "difficult" -> 2.0f
        else -> 1.0f
    }

    var score by remember { mutableIntStateOf(0) }
    var aiScore by remember { mutableIntStateOf(0) }
    var totalQuestions by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var currentRound by remember { mutableIntStateOf(1) }
    val maxRounds = if (mode == "speed" || mode == "attention") 12 else 8

    val initialTime = when (difficulty) {
        "difficult" -> if (mode == "speed") 25f else 15f
        "intermediate" -> if (mode == "speed") 35f else 20f
        else -> if (mode == "speed") 45f else 30f
    }
    var timeRemaining by remember { mutableFloatStateOf(initialTime) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Visual feedback state
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isCorrectAnswer by remember { mutableStateOf<Boolean?>(null) }
    var aiComment by remember { mutableStateOf(AiPersonalityEngine.getRandomInGameComment(selectedAi)) }
    var particleTrigger by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()

    // Mode-specific question states
    fun generateQuestionForMode(m: String): ChoiceQuestion {
        return when (m) {
            "logic" -> QuestionGenerator.generateLogicQuestion(difficulty, userStats?.aiRating ?: 1200)
            "speed", "arithmetic" -> QuestionGenerator.generateSpeedQuestion(difficulty)
            "pattern" -> QuestionGenerator.generatePatternQuestion(difficulty)
            "spatial" -> QuestionGenerator.generateSpatialQuestion(difficulty)
            "vocabulary" -> QuestionGenerator.generateVocabularyQuestion(difficulty)
            "stroop" -> QuestionGenerator.generateStroopQuestion(difficulty)
            "balance" -> QuestionGenerator.generateBalanceQuestion(difficulty)
            "ai_battle", "daily" -> {
                when (Random.nextInt(4)) {
                    0 -> QuestionGenerator.generateLogicQuestion(difficulty, userStats?.aiRating ?: 1200)
                    1 -> QuestionGenerator.generatePatternQuestion(difficulty)
                    2 -> QuestionGenerator.generateArithmeticQuestion(difficulty)
                    else -> QuestionGenerator.generateSpatialQuestion(difficulty)
                }
            }
            else -> QuestionGenerator.generateLogicQuestion(difficulty, userStats?.aiRating ?: 1200)
        }
    }

    var currentChoiceQuestion by remember { mutableStateOf(generateQuestionForMode(mode)) }

    // Memory Mode State
    var memorySequenceQuestion by remember { mutableStateOf(QuestionGenerator.generateMemorySequence(1, difficulty)) }
    var activeFlashingIndex by remember { mutableStateOf<Int?>(null) }
    var playerMemoryInput by remember { mutableStateOf(listOf<Int>()) }
    var isShowingSequence by remember { mutableStateOf(false) }

    // Reflex Mode State
    var reflexQuestion by remember { mutableStateOf(QuestionGenerator.generateReflexQuestion(difficulty)) }

    // Attention Grid State (Schulte Grid)
    var attentionNumbers by remember { mutableStateOf((1..16).shuffled()) }
    var nextAttentionTarget by remember { mutableIntStateOf(1) }

    // AI Battle background simulator
    if (mode == "ai_battle") {
        LaunchedEffect(currentRound) {
            val waitSec = (aiTier.responseTimeSec + Random.nextFloat() * 1.2f).coerceAtLeast(1.0f)
            delay((waitSec * 1000).toLong())
            val aiWillSucceed = Random.nextFloat() <= aiTier.baseAccuracy
            if (aiWillSucceed) {
                aiScore += (150 * aiTier.scoreMultiplier * difficultyMultiplier).toInt()
            }
            aiComment = AiPersonalityEngine.getRandomInGameComment(selectedAi)
        }
    }

    // Start Memory sequence display
    LaunchedEffect(currentRound, mode) {
        if (mode == "memory") {
            isShowingSequence = true
            playerMemoryInput = emptyList()
            delay(400)
            for (node in memorySequenceQuestion.sequence) {
                activeFlashingIndex = node
                SoundManager.playCountdownTick()
                delay(memorySequenceQuestion.speedMs)
                activeFlashingIndex = null
                delay(150)
            }
            isShowingSequence = false
        }
    }

    // Main Timer Loop
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timeRemaining > 0) {
            delay(100)
            timeRemaining -= 0.1f
            if (timeRemaining <= 5f && (timeRemaining * 10).toInt() % 10 == 0) {
                SoundManager.playCountdownTick()
            }
        }
        if (timeRemaining <= 0 && isTimerRunning) {
            isTimerRunning = false
            SoundManager.playGameOver()
            val finalAccuracy = if (totalQuestions > 0) ((correctAnswers.toFloat() / totalQuestions) * 100).toInt() else 0
            val isWin = if (mode == "ai_battle") score >= aiScore else correctAnswers >= (maxRounds * 0.5)
            viewModel.finishGame(
                score = score,
                accuracy = finalAccuracy,
                time = initialTime.toDouble(),
                isWin = isWin,
                mode = mode
            )
            onNavigateToResult()
        }
    }

    fun handleNextRound(wasCorrect: Boolean) {
        totalQuestions++
        if (wasCorrect) {
            correctAnswers++
            combo++
            maxCombo = maxOf(maxCombo, combo)
            val comboMultiplier = 1.0f + (combo * 0.15f)
            val basePts = (100 * difficultyMultiplier * comboMultiplier).toInt()
            score += basePts
            SoundManager.playCorrect()
            particleTrigger = Random.nextInt()
            isCorrectAnswer = true
        } else {
            combo = 0
            SoundManager.playWrong()
            isCorrectAnswer = false
        }

        scope.launch {
            delay(500)
            selectedOptionIndex = null
            isCorrectAnswer = null
            if (currentRound >= maxRounds) {
                isTimerRunning = false
                SoundManager.playLevelUp()
                val finalAccuracy = if (totalQuestions > 0) ((correctAnswers.toFloat() / totalQuestions) * 100).toInt() else 0
                val isWin = if (mode == "ai_battle") score >= aiScore else correctAnswers >= (maxRounds * 0.5)
                viewModel.finishGame(
                    score = score,
                    accuracy = finalAccuracy,
                    time = initialTime.toDouble(),
                    isWin = isWin,
                    mode = mode
                )
                onNavigateToResult()
            } else {
                currentRound++
                timeRemaining = initialTime
                when (mode) {
                    "memory" -> memorySequenceQuestion = QuestionGenerator.generateMemorySequence(currentRound, difficulty)
                    "reflex" -> reflexQuestion = QuestionGenerator.generateReflexQuestion(difficulty)
                    "attention" -> {
                        attentionNumbers = (1..16).shuffled()
                        nextAttentionTarget = 1
                    }
                    else -> currentChoiceQuestion = generateQuestionForMode(mode)
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkSlate,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${mode.uppercase()} • ${difficulty.uppercase()}",
                                color = Cyan400,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                            )
                            Text(
                                text = "Round $currentRound/$maxRounds",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(
                            color = GlassBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Text(
                                text = "⭐ $score PTS",
                                color = Amber400,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Timer Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TIME REMAINING", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text("${String.format("%.1f", timeRemaining)}s", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = if (timeRemaining < 6f) Color(0xFFFF1744) else Cyan400)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (timeRemaining / initialTime).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (timeRemaining < 6f) Color(0xFFFF1744) else Cyan400,
                        trackColor = GlassBorder
                    )
                }

                // AI Opponent Battle Banner (if applicable)
                if (mode == "ai_battle") {
                    Surface(
                        color = GlassBackground,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, selectedAi.primaryColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = selectedAi.primaryColor.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(selectedAi.displayName.first().toString(), color = selectedAi.primaryColor, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${selectedAi.displayName}: $aiScore PTS", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("\"$aiComment\"", color = selectedAi.primaryColor, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }

                // Combo & Feedback Banner
                AnswerFeedbackBanner(isCorrect = isCorrectAnswer, combo = combo)

                // Main Game Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (mode) {
                        "memory" -> {
                            // Memory Vault Grid
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = if (isShowingSequence) "MEMORIZE SEQUENCE..." else "REPEAT SEQUENCE (${playerMemoryInput.size}/${memorySequenceQuestion.sequence.size})",
                                    color = if (isShowingSequence) Amber400 else Cyan400,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.size(300.dp)
                                ) {
                                    items(9) { index ->
                                        val isFlashing = activeFlashingIndex == index
                                        val isSelectedInInput = playerMemoryInput.contains(index)
                                        Surface(
                                            color = when {
                                                isFlashing -> Cyan400
                                                isSelectedInInput -> Purple500
                                                else -> GlassBackground
                                            },
                                            shape = RoundedCornerShape(18.dp),
                                            border = BorderStroke(2.dp, if (isFlashing || isSelectedInInput) Cyan400 else GlassBorder),
                                            modifier = Modifier
                                                .size(88.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .clickable(enabled = !isShowingSequence) {
                                                    SoundManager.playClick()
                                                    val newInput = playerMemoryInput + index
                                                    playerMemoryInput = newInput
                                                    val expectedIndex = newInput.size - 1
                                                    if (expectedIndex < memorySequenceQuestion.sequence.size) {
                                                        if (memorySequenceQuestion.sequence[expectedIndex] != index) {
                                                            handleNextRound(false)
                                                        } else if (newInput.size == memorySequenceQuestion.sequence.size) {
                                                            handleNextRound(true)
                                                        }
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = if (isFlashing || isSelectedInInput) Color.Black else Slate400,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleLarge
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "reflex" -> {
                            // Reflex Matrix
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("TARGET SIGNAL", color = Slate400, style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(reflexQuestion.targetColorHex).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(2.dp, Color(reflexQuestion.targetColorHex)),
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(reflexQuestion.targetSymbol, fontSize = 48.sp, color = Color(reflexQuestion.targetColorHex))
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Text("CURRENT SIGNAL", color = Slate400, style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(reflexQuestion.currentColorHex).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(2.dp, Color(reflexQuestion.currentColorHex)),
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(reflexQuestion.currentSymbol, fontSize = 48.sp, color = Color(reflexQuestion.currentColorHex))
                                    }
                                }

                                Spacer(modifier = Modifier.height(40.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { handleNextRound(reflexQuestion.isMatch) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f).height(60.dp)
                                    ) {
                                        Text("MATCH ✓", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 16.sp)
                                    }
                                    Button(
                                        onClick = { handleNextRound(!reflexQuestion.isMatch) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f).height(60.dp)
                                    ) {
                                        Text("DIFFERENT ✗", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                        "attention" -> {
                            // Attention Schulte Grid
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "TAP NUMBER: $nextAttentionTarget",
                                    color = Amber400,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.size(320.dp)
                                ) {
                                    itemsIndexed(attentionNumbers) { _, num ->
                                        val isTappedBefore = num < nextAttentionTarget
                                        Surface(
                                            color = if (isTappedBefore) Color(0xFF00E676).copy(alpha = 0.2f) else GlassBackground,
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, if (isTappedBefore) Color(0xFF00E676) else GlassBorder),
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable(enabled = !isTappedBefore) {
                                                    SoundManager.playClick()
                                                    if (num == nextAttentionTarget) {
                                                        if (nextAttentionTarget == 16) {
                                                            handleNextRound(true)
                                                        } else {
                                                            nextAttentionTarget++
                                                        }
                                                    } else {
                                                        handleNextRound(false)
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "$num",
                                                    color = if (isTappedBefore) Color(0xFF00E676) else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // Standard Choice Question
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = GlassBackground),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(currentChoiceQuestion.title.uppercase(), color = Cyan400, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(currentChoiceQuestion.questionText, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), lineHeight = 24.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    currentChoiceQuestion.options.forEachIndexed { index, option ->
                                        val isSelected = selectedOptionIndex == index
                                        val isCorrect = index == currentChoiceQuestion.correctIndex
                                        val buttonColor = when {
                                            selectedOptionIndex != null && isCorrect -> Color(0xFF00E676).copy(alpha = 0.2f)
                                            isSelected && !isCorrect -> Color(0xFFFF1744).copy(alpha = 0.2f)
                                            else -> GlassBackground
                                        }
                                        val borderColor = when {
                                            selectedOptionIndex != null && isCorrect -> Color(0xFF00E676)
                                            isSelected && !isCorrect -> Color(0xFFFF1744)
                                            else -> GlassBorder
                                        }

                                        Surface(
                                            color = buttonColor,
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, borderColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable(enabled = selectedOptionIndex == null) {
                                                    selectedOptionIndex = index
                                                    handleNextRound(isCorrect)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = Cyan400.copy(alpha = 0.15f),
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(('A' + index).toString(), color = Cyan400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Text(
                                                    text = option,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            particleTrigger?.let {
                ParticleExplosion(triggerKey = it)
            }
        }
    }
}
