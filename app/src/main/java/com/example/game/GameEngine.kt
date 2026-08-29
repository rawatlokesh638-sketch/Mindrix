package com.example.game

import kotlin.random.Random

sealed class GameQuestion(
    val prompt: String,
    val subPrompt: String,
    val category: String
)

data class ChoiceQuestion(
    val title: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val modeCategory: String = "Logic"
) : GameQuestion(title, questionText, modeCategory)

data class MemorySequenceQuestion(
    val sequence: List<Int>, // list of node indices 0..8
    val gridSize: Int = 9,
    val speedMs: Long = 650L
) : GameQuestion("Memory Vault", "Memorize the glowing sequence and repeat it accurately.", "Memory")

data class ReflexQuestion(
    val targetSymbol: String,
    val targetColorHex: Long,
    val currentSymbol: String,
    val currentColorHex: Long,
    val isMatch: Boolean
) : GameQuestion("Reflex Matrix", "Tap 'MATCH' only when both Symbol & Color align!", "Reflex")

data class AttentionGridQuestion(
    val numbers: List<Int>,
    val targetNumber: Int,
    val gridSize: Int = 4 // 4x4 = 16 numbers
) : GameQuestion("Attention Grid", "Tap numbers from 1 to N in ascending order as fast as possible.", "Attention")

data class SpatialQuestion(
    val shapeString: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
) : GameQuestion("Spatial Rotation", "Determine the correct rotated or mirrored configuration.", "Spatial")

object QuestionGenerator {

    fun generateLogicQuestion(difficulty: String, rating: Int): ChoiceQuestion {
        val multiplier = when (difficulty) {
            "intermediate" -> 1.2f
            "difficult" -> 1.5f
            else -> 1.0f
        }
        val logicPuzzles = listOf(
            ChoiceQuestion(
                title = "Syllogism Analysis",
                questionText = "All Quantum nodes emit photons. Some photons warp spacetime. Therefore:",
                options = listOf(
                    "All Quantum nodes warp spacetime",
                    "Some Quantum nodes may warp spacetime",
                    "No Quantum nodes warp spacetime",
                    "Spacetime warps all photons"
                ),
                correctIndex = 1,
                explanation = "Since only 'some' photons warp spacetime, quantum nodes intersecting those photons may warp spacetime.",
                modeCategory = "Logic Lab"
            ),
            ChoiceQuestion(
                title = "Truth & Deception",
                questionText = "Bot A says: 'Bot B is lying.'\nBot B says: 'Both of us are telling the truth.'\nWho is telling the truth?",
                options = listOf(
                    "Bot A is truthful, Bot B is lying",
                    "Bot B is truthful, Bot A is lying",
                    "Both are truthful",
                    "Both are lying"
                ),
                correctIndex = 0,
                explanation = "If B were truthful, A's statement would make them both liars (a contradiction). Hence B lies, and A speaks truth.",
                modeCategory = "Logic Lab"
            ),
            ChoiceQuestion(
                title = "Relational Speed",
                questionText = "Cyber Core X is faster than Y but slower than Z. Core W is faster than Z. Which core is the fastest?",
                options = listOf("Core X", "Core Y", "Core Z", "Core W"),
                correctIndex = 3,
                explanation = "W > Z > X > Y. Thus Core W is the fastest.",
                modeCategory = "Logic Lab"
            ),
            ChoiceQuestion(
                title = "Binary State Deduction",
                questionText = "If switch A is ON, switch B is OFF. If switch B is OFF, switch C is ON. Switch A is ON. What is C?",
                options = listOf("C is ON", "C is OFF", "C is in quantum flux", "Cannot be determined"),
                correctIndex = 0,
                explanation = "A is ON → B is OFF → C is ON.",
                modeCategory = "Logic Lab"
            ),
            ChoiceQuestion(
                title = "Balance Scale Logic",
                questionText = "2 Cubes = 3 Spheres. 1 Sphere = 2 Pyramids. How many Pyramids equal 1 Cube?",
                options = listOf("2 Pyramids", "3 Pyramids", "4 Pyramids", "6 Pyramids"),
                correctIndex = 1,
                explanation = "2 Cubes = 3 Spheres = 3 × 2 Pyramids = 6 Pyramids. Therefore 1 Cube = 3 Pyramids.",
                modeCategory = "Logic Lab"
            ),
            ChoiceQuestion(
                title = "Categorical Logic",
                questionText = "No synthetic virus is harmless. All nanobots are synthetic viruses. Which conclusion is valid?",
                options = listOf(
                    "All nanobots are harmful",
                    "Some nanobots are harmless",
                    "No nanobots are viruses",
                    "Harmless entities are synthetic"
                ),
                correctIndex = 0,
                explanation = "Nanobots are synthetic viruses, and no synthetic virus is harmless; thus all nanobots are harmful.",
                modeCategory = "Logic Lab"
            ),
            ChoiceQuestion(
                title = "Temporal Sequence",
                questionText = "Event α occurs before β. Event γ occurs after β but before δ. What is the chronological order?",
                options = listOf("α → β → γ → δ", "β → α → γ → δ", "α → γ → β → δ", "δ → γ → β → α"),
                correctIndex = 0,
                explanation = "Direct sequence links α before β, γ after β, and δ after γ.",
                modeCategory = "Logic Lab"
            )
        )
        return logicPuzzles.random()
    }

    fun generatePatternQuestion(difficulty: String): ChoiceQuestion {
        val patternTypes = Random.nextInt(4)
        return when (patternTypes) {
            0 -> {
                val start = Random.nextInt(2, if (difficulty == "difficult") 15 else 8)
                val step = Random.nextInt(2, if (difficulty == "difficult") 7 else 4)
                val seq = listOf(
                    start,
                    start + step,
                    start + step * 2 + 1,
                    start + step * 3 + 3,
                    start + step * 4 + 6
                )
                val nextVal = start + step * 5 + 10
                val options = listOf(nextVal, nextVal - 2, nextVal + 3, nextVal + 5).shuffled()
                ChoiceQuestion(
                    title = "Accelerating Progression",
                    questionText = "${seq.joinToString(", ")} , [ ? ]",
                    options = options.map { it.toString() },
                    correctIndex = options.indexOf(nextVal),
                    explanation = "The difference between consecutive numbers increases by 1 each step.",
                    modeCategory = "Pattern Prediction"
                )
            }
            1 -> {
                val a = Random.nextInt(1, 6)
                val b = Random.nextInt(2, 7)
                val c = a + b
                val d = b + c
                val e = c + d
                val answer = d + e
                val options = listOf(answer, answer - 3, answer + 2, answer + 4).shuffled()
                ChoiceQuestion(
                    title = "Fibonacci Synapse",
                    questionText = "$a, $b, $c, $d, $e, [ ? ]",
                    options = options.map { it.toString() },
                    correctIndex = options.indexOf(answer),
                    explanation = "Each number is the sum of the two preceding numbers.",
                    modeCategory = "Pattern Prediction"
                )
            }
            2 -> {
                val startChar = 'B' + Random.nextInt(0, 4)
                val step = if (difficulty == "difficult") 4 else 3
                val c1 = startChar
                val c2 = c1 + step
                val c3 = c2 + step
                val c4 = c3 + step
                val answer = (c4 + step).toString()
                val options = listOf(answer, (c4 + step - 1).toString(), (c4 + step + 1).toString(), (c4 + step + 2).toString()).shuffled()
                ChoiceQuestion(
                    title = "Alpha Cypher Matrix",
                    questionText = "$c1  →  $c2  →  $c3  →  $c4  →  [ ? ]",
                    options = options,
                    correctIndex = options.indexOf(answer),
                    explanation = "Each letter advances by +$step positions in the alphabet.",
                    modeCategory = "Pattern Prediction"
                )
            }
            else -> {
                val base = Random.nextInt(2, 5)
                val mult = if (difficulty == "difficult") 4 else 3
                val s1 = base
                val s2 = s1 * mult
                val s3 = s2 * mult
                val s4 = s3 * mult
                val answer = s4 * mult
                val options = listOf(answer, answer - mult, answer + 18, answer * 2).shuffled()
                ChoiceQuestion(
                    title = "Exponential Grid",
                    questionText = "$s1, $s2, $s3, $s4, [ ? ]",
                    options = options.map { it.toString() },
                    correctIndex = options.indexOf(answer),
                    explanation = "Each element is multiplied by $mult.",
                    modeCategory = "Pattern Prediction"
                )
            }
        }
    }

    fun generateSpeedQuestion(difficulty: String): ChoiceQuestion {
        val op = Random.nextInt(3)
        val rangeMultiplier = when (difficulty) {
            "intermediate" -> 1.5
            "difficult" -> 2.5
            else -> 1.0
        }
        return when (op) {
            0 -> {
                val x = Random.nextInt((12 * rangeMultiplier).toInt(), (45 * rangeMultiplier).toInt())
                val y = Random.nextInt((15 * rangeMultiplier).toInt(), (55 * rangeMultiplier).toInt())
                val ans = x + y
                val options = listOf(ans, ans + 10, ans - 10, ans + 2).shuffled()
                ChoiceQuestion(
                    title = "Speed Pulse",
                    questionText = "$x + $y = ?",
                    options = options.map { it.toString() },
                    correctIndex = options.indexOf(ans),
                    explanation = "$x + $y = $ans",
                    modeCategory = "Speed Rush"
                )
            }
            1 -> {
                val x = Random.nextInt((6 * rangeMultiplier).toInt(), (14 * rangeMultiplier).toInt())
                val y = Random.nextInt((7 * rangeMultiplier).toInt(), (16 * rangeMultiplier).toInt())
                val ans = x * y
                val options = listOf(ans, ans - 6, ans + 8, ans + 12).shuffled()
                ChoiceQuestion(
                    title = "Speed Pulse",
                    questionText = "$x × $y = ?",
                    options = options.map { it.toString() },
                    correctIndex = options.indexOf(ans),
                    explanation = "$x × $y = $ans",
                    modeCategory = "Speed Rush"
                )
            }
            else -> {
                val x = Random.nextInt((40 * rangeMultiplier).toInt(), (150 * rangeMultiplier).toInt())
                val y = Random.nextInt((15 * rangeMultiplier).toInt(), (45 * rangeMultiplier).toInt())
                val ans = x - y
                val options = listOf(ans, ans + 5, ans - 10, ans + 12).shuffled()
                ChoiceQuestion(
                    title = "Speed Pulse",
                    questionText = "$x - $y = ?",
                    options = options.map { it.toString() },
                    correctIndex = options.indexOf(ans),
                    explanation = "$x - $y = $ans",
                    modeCategory = "Speed Rush"
                )
            }
        }
    }

    fun generateMemorySequence(level: Int, difficulty: String): MemorySequenceQuestion {
        val baseLength = when (difficulty) {
            "intermediate" -> 4
            "difficult" -> 5
            else -> 3
        }
        val length = (baseLength + (level / 2)).coerceIn(baseLength, if (difficulty == "difficult") 9 else 7)
        val seq = mutableListOf<Int>()
        for (i in 0 until length) {
            seq.add(Random.nextInt(0, 9))
        }
        val baseSpeed = when (difficulty) {
            "intermediate" -> 550L
            "difficult" -> 400L
            else -> 700L
        }
        val speed = (baseSpeed - (level * 30L)).coerceAtLeast(250L)
        return MemorySequenceQuestion(seq, gridSize = 9, speedMs = speed)
    }

    fun generateReflexQuestion(difficulty: String): ReflexQuestion {
        val symbols = listOf("▲", "■", "◆", "●", "★")
        val colors = listOf(0xFF22D3EE, 0xFFA855F7, 0xFFFBBF24, 0xFFFF0055, 0xFF00E676)
        
        val matchProbability = when (difficulty) {
            "difficult" -> 0.35
            "intermediate" -> 0.45
            else -> 0.5
        }
        val isMatch = Random.nextFloat() <= matchProbability
        val targetSym = symbols.random()
        val targetCol = colors.random()

        return if (isMatch) {
            ReflexQuestion(targetSym, targetCol, targetSym, targetCol, true)
        } else {
            val currSym = if (Random.nextBoolean()) targetSym else symbols.filter { it != targetSym }.random()
            val currCol = if (currSym == targetSym) colors.filter { it != targetCol }.random() else colors.random()
            ReflexQuestion(targetSym, targetCol, currSym, currCol, false)
        }
    }

    fun generateSpatialQuestion(difficulty: String): ChoiceQuestion {
        val questions = listOf(
            ChoiceQuestion(
                title = "Mental 3D Rotation",
                questionText = "An L-shape block is rotated 90° clockwise then 180° counter-clockwise. What is its orientation?",
                options = listOf("Inverted L pointing West", "Standard L pointing East", "Upside down L pointing North", "Mirrored Z-shape"),
                correctIndex = 0,
                explanation = "90° CW followed by 180° CCW results in a net 90° CCW rotation from start, pointing West.",
                modeCategory = "Spatial Rotation"
            ),
            ChoiceQuestion(
                title = "Cube Face Reflection",
                questionText = "A cube with a Red dot on Top and Blue on Front is rolled Forward twice. Which color faces Front?",
                options = listOf("Red", "Blue", "Green", "Yellow"),
                correctIndex = 0,
                explanation = "Rolling forward twice rotates the top face to the back, and the bottom to the top. The original top (Red) comes back to front after 2 full rolls.",
                modeCategory = "Spatial Rotation"
            ),
            ChoiceQuestion(
                title = "Polygon Folding",
                questionText = "A paper square has 4 corners folded to the center, forming a smaller square. If rotated 45°, what symmetry does it possess?",
                options = listOf("8-fold rotational symmetry", "4-fold rotational symmetry", "Asymmetric", "Linear symmetry only"),
                correctIndex = 1,
                explanation = "Folded squares maintain 4-fold rotational symmetry.",
                modeCategory = "Spatial Rotation"
            )
        )
        return questions.random()
    }

    fun generateVocabularyQuestion(difficulty: String): ChoiceQuestion {
        val questions = listOf(
            ChoiceQuestion(
                title = "Cognitive Lexicon",
                questionText = "What is the precise definition of 'Perspicacious'?",
                options = listOf(
                    "Having a ready insight into things; sagacious",
                    "Overly talkative and energetic",
                    "Prone to sudden unpredictable mood shifts",
                    "Exhibiting extreme physical agility"
                ),
                correctIndex = 0,
                explanation = "Perspicacious means having keen mental perception and understanding.",
                modeCategory = "Vocabulary Lexicon"
            ),
            ChoiceQuestion(
                title = "Semantic Synonyms",
                questionText = "Choose the exact synonym for 'Ephemeral':",
                options = listOf("Transient / Short-lived", "Permanent / Enduring", "Complex / Obscure", "Rigid / Inflexible"),
                correctIndex = 0,
                explanation = "Ephemeral describes something lasting for a very short time.",
                modeCategory = "Vocabulary Lexicon"
            ),
            ChoiceQuestion(
                title = "Abstract Etymology",
                questionText = "What does the root 'Cogn-' signify in cognitive terminology?",
                options = listOf("Knowledge / Knowing", "Speed / Velocity", "Memory storage", "Electrical impulse"),
                correctIndex = 0,
                explanation = "'Cogn' stems from Latin cognoscere, meaning to know or recognize.",
                modeCategory = "Vocabulary Lexicon"
            )
        )
        return questions.random()
    }

    fun generateStroopQuestion(difficulty: String): ChoiceQuestion {
        val colors = listOf("RED", "BLUE", "GREEN", "AMBER", "PURPLE")
        val colorHexes = listOf(0xFFFF1744, 0xFF22D3EE, 0xFF00E676, 0xFFFBBF24, 0xFFA855F7)
        val textWord = colors.random()
        val inkColorIndex = colors.indices.random()
        val inkColorName = colors[inkColorIndex]

        return ChoiceQuestion(
            title = "Stroop Inhibition Test",
            questionText = "What is the ACTUAL INK COLOR of the word '$textWord' rendered in $inkColorName?",
            options = colors,
            correctIndex = inkColorIndex,
            explanation = "The Stroop test measures cognitive inhibition by forcing you to ignore word semantics and identify ink color.",
            modeCategory = "Stroop Test"
        )
    }

    fun generateArithmeticQuestion(difficulty: String): ChoiceQuestion {
        val mult = when (difficulty) {
            "difficult" -> 3
            "intermediate" -> 2
            else -> 1
        }
        val a = Random.nextInt(12 * mult, 35 * mult)
        val b = Random.nextInt(5 * mult, 15 * mult)
        val c = Random.nextInt(2, 6)
        val ans = (a * c) - b
        val options = listOf(ans, ans + 5, ans - 10, ans + 12).shuffled()

        return ChoiceQuestion(
            title = "Mental Arithmetic Chain",
            questionText = "($a × $c) - $b = ?",
            options = options.map { it.toString() },
            correctIndex = options.indexOf(ans),
            explanation = "$a × $c = ${a * c}, minus $b = $ans",
            modeCategory = "Arithmetic Chain"
        )
    }

    fun generateBalanceQuestion(difficulty: String): ChoiceQuestion {
        return ChoiceQuestion(
            title = "Quantum Scale Balance",
            questionText = "Left Pan: 3 Alpha spheres + 1 Beta cube. Right Pan: 2 Alpha spheres + 3 Beta cubes. If balanced, 1 Alpha = ?",
            options = listOf("2 Beta cubes", "1.5 Beta cubes", "3 Beta cubes", "0.5 Beta cube"),
            correctIndex = 0,
            explanation = "3A + 1B = 2A + 3B → A = 2B.",
            modeCategory = "Scale Balance"
        )
    }
}
