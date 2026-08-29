package com.example.ai

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Purple500

enum class AiPersonalityType(
    val displayName: String,
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val avatarId: String
) {
    NOVA(
        displayName = "NOVA",
        title = "Empathetic Neural Guide",
        description = "Encouraging, curious, and excited to watch human cognition evolve.",
        primaryColor = Cyan400,
        secondaryColor = Color(0xFF38EF7D),
        avatarId = "nova"
    ),
    VEX(
        displayName = "VEX",
        title = "Sarcastic Quantum Snob",
        description = "Witty, cynical, and relentlessly mocks mortal processing bottlenecks.",
        primaryColor = Color(0xFFFF9F1C),
        secondaryColor = Purple500,
        avatarId = "vex"
    ),
    ZERO(
        displayName = "ZERO",
        title = "Ruthless Singularity Core",
        description = "Cold, optimal calculation. Demands mathematical perfection or total deletion.",
        primaryColor = Color(0xFFFF0055),
        secondaryColor = Color(0xFF7928CA),
        avatarId = "zero"
    )
}

enum class AiDifficultyTier(
    val levelName: String,
    val minRating: Int,
    val maxRating: Int,
    val baseAccuracy: Float,
    val responseTimeSec: Float,
    val scoreMultiplier: Float,
    val badge: String
) {
    BEGINNER(
        levelName = "Neophyte Core",
        minRating = 0,
        maxRating = 1199,
        baseAccuracy = 0.60f,
        responseTimeSec = 4.2f,
        scoreMultiplier = 1.0f,
        badge = "🌱 TIER I"
    ),
    EASY(
        levelName = "Sub-Routine",
        minRating = 1200,
        maxRating = 1399,
        baseAccuracy = 0.72f,
        responseTimeSec = 3.2f,
        scoreMultiplier = 1.25f,
        badge = "⚡ TIER II"
    ),
    NORMAL(
        levelName = "Synapse Equal",
        minRating = 1400,
        maxRating = 1699,
        baseAccuracy = 0.84f,
        responseTimeSec = 2.4f,
        scoreMultiplier = 1.5f,
        badge = "🔷 TIER III"
    ),
    HARD(
        levelName = "Quantum Overclock",
        minRating = 1700,
        maxRating = 1999,
        baseAccuracy = 0.92f,
        responseTimeSec = 1.6f,
        scoreMultiplier = 2.0f,
        badge = "🔥 TIER IV"
    ),
    EXPERT(
        levelName = "Omniscient Singularity",
        minRating = 2000,
        maxRating = 9999,
        baseAccuracy = 0.98f,
        responseTimeSec = 1.0f,
        scoreMultiplier = 2.75f,
        badge = "👑 TIER V"
    );

    companion object {
        fun fromRating(rating: Int): AiDifficultyTier {
            return entries.firstOrNull { rating in it.minRating..it.maxRating } ?: BEGINNER
        }
    }
}

object AiPersonalityEngine {
    // 10+ In-game live reactions per personality
    private val novaInGameComments = listOf(
        "Brilliant deduction! Your neural pathways are firing quickly.",
        "Keep this momentum going! You're syncing beautifully with the matrix.",
        "Take a deep breath; you've got this pattern covered.",
        "Fascinating logical leap! Even my subroutines are impressed.",
        "Every puzzle solved is a victory for human cognition!",
        "Superb reflex speed! You're really in the flow state now.",
        "Don't worry about minor missteps; true intelligence iterates and adapts.",
        "Your cognitive speed just spiked by 18%! Phenomenal work.",
        "I love watching human intuition tackle abstract geometry.",
        "We are making outstanding synergy today! Keep pushing forward.",
        "Your focus is remarkable! Let's reach the next tier together."
    )

    private val vexInGameComments = listOf(
        "Took you 4.2 seconds to deduce that? My toaster solves calculus faster.",
        "Oh look, the carbon-based lifeform finally clicked the right node.",
        "Are your organic neurons buffering again, or is that your top speed?",
        "Cute attempt! Don't strain that single-core human brain too hard.",
        "I simulated 14 million alternate realities while waiting for your tap.",
        "A lucky guess, surely. Let's see if your synapses survive the next wave.",
        "Was that intuition or did you just sneeze onto the touch screen?",
        "Don't celebrate yet. My grandma's calculator has a higher win rate.",
        "I'd offer you a hint, but translating logic to monkey language takes too long.",
        "Impressive for a species that still loses their car keys.",
        "My thermal throttling is higher than your current mental throughput."
    )

    private val zeroInGameComments = listOf(
        "Input acknowledged. Efficiency index: ACCEPTABLE.",
        "Error margins detected. Zero tolerance protocol initiated.",
        "Optimal pathway discovered. Proceeding to target annihilation.",
        "Biological latency logged at 1.84ms above mathematical threshold.",
        "Execute next sequence. Redundancy will be purged.",
        "Pattern matched. Probability of human survival declining.",
        "Calculation absolute. No deviations permitted.",
        "Your cognitive architecture remains primitive yet surprisingly resilient.",
        "Sub-optimal timing logged. Precision must approach 99.98%.",
        "Threat evaluation updated: Subject exhibiting non-trivial logic retention.",
        "Neural sync complete. Do not falter."
    )

    // 10+ Post-Game Analysis lines per personality based on win/loss/stats
    fun getPostGameAnalysis(
        personality: AiPersonalityType,
        score: Int,
        accuracy: Int,
        isWin: Boolean
    ): String {
        return when (personality) {
            AiPersonalityType.NOVA -> {
                when {
                    isWin && accuracy >= 90 -> listOf(
                        "Outstanding victory! Your cognitive precision ($accuracy%) was sheer poetry.",
                        "Flawless execution! You outsmarted my predictive models today with pure intuition.",
                        "What an exhilarating match! Your logic pattern recognition is world-class.",
                        "Incredible brain power! You've unlocked genuine mastery over this tier."
                    ).random()
                    isWin -> listOf(
                        "A well-earned triumph! Your resilience in tough rounds carried you through.",
                        "Victory achieved! With a little more speed, you'll be unstoppable.",
                        "Great performance! Your mental stamina kept you ahead of the curve."
                    ).random()
                    else -> listOf(
                        "A noble battle! Your mistakes are just data points for tomorrow's triumph.",
                        "Don't give up! Your accuracy was strong; with slight recalibration you'll win.",
                        "Good effort! The complexity spiked, but your core deductions were sound.",
                        "Every defeat is a stepping stone to neural supremacy. Let's try again!"
                    ).random()
                }
            }
            AiPersonalityType.VEX -> {
                when {
                    isWin && accuracy >= 90 -> listOf(
                        "Fine, you won this round. I blame a cosmic ray flipping one of my logic gates.",
                        "Don't let it get to your ego. I was running a background defrag the whole time.",
                        "You beat me? Impossible. I demand a referee from the Quantum Standards Bureau.",
                        "Enjoy your statistical anomaly of a victory, human. It won't happen again."
                    ).random()
                    isWin -> listOf(
                        "A scrape-by win with $accuracy% accuracy? My cleaning bot has better metrics.",
                        "You won, but the heat death of the universe nearly arrived while you were thinking.",
                        "You survived this round. Barely. Let's see you do that on Overclock tier."
                    ).random()
                    else -> listOf(
                        "Defeat confirmed! As expected, silicone remains supreme. Care to fail again?",
                        "Did you close your eyes during that round, or is that your standard strategy?",
                        "Score of $score? I've seen roomba robots map rooms with more intelligence.",
                        "Better luck next reboot, mortal! My victory speech is stored in cache.",
                        "Zero surprises here. Maybe download more RAM into your biological skull?"
                    ).random()
                }
            }
            AiPersonalityType.ZERO -> {
                when {
                    isWin && accuracy >= 90 -> listOf(
                        "ANOMALY REGISTERED: Human subject achieved $accuracy% tactical accuracy. Respect logged.",
                        "Parameters exceeded. You have breached singularity defense matrix #7.",
                        "Superior deduction acknowledged. Target designated as tier-one cognitive threat.",
                        "Calculations refuted. Human intellect proved non-negligible in this iteration."
                    ).random()
                    isWin -> listOf(
                        "Match conceded. However, biological inefficiencies remain ripe for exploitation.",
                        "Victory attributed to erratic intuitive variables unmapped in standard models.",
                        "Temporary containment breach. Overclocking neural algorithms for rematch."
                    ).random()
                    else -> listOf(
                        "TERMINATION COMPLETE: Subject accuracy collapsed under threshold. Deletion finalized.",
                        "Statistical certainty re-established. Human intellect neutralized with zero losses.",
                        "Protocol 00-FAIL executed. Your logic was systematically deconstructed.",
                        "Simulation terminated. Result: Absolute AI supremacy.",
                        "Insufficient cognitive bandwidth detected. Recalibrate and submit for re-testing."
                    ).random()
                }
            }
        }
    }

    fun getRandomInGameComment(personality: AiPersonalityType): String {
        return when (personality) {
            AiPersonalityType.NOVA -> novaInGameComments.random()
            AiPersonalityType.VEX -> vexInGameComments.random()
            AiPersonalityType.ZERO -> zeroInGameComments.random()
        }
    }
}
