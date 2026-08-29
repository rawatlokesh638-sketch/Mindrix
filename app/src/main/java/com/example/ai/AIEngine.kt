package com.example.ai

enum class AIPersonality(val displayName: String) {
    NOVA("NOVA (Friendly)"),
    VEX("VEX (Sarcastic)"),
    ZERO("ZERO (Ruthless)")
}

object AIEngine {
    fun getDifficultyTier(rating: Int): String {
        return when {
            rating < 800 -> "Beginner"
            rating < 1200 -> "Easy"
            rating < 1500 -> "Normal"
            rating < 1800 -> "Hard"
            rating < 2200 -> "Expert"
            else -> "Master"
        }
    }

    fun calculateRatingChange(accuracy: Int, time: Double): Int {
        var change = 0
        // Accuracy impact
        if (accuracy >= 90) change += 40
        else if (accuracy >= 70) change += 15
        else if (accuracy < 50) change -= 30
        
        // Time impact
        if (time < 10.0) change += 20
        else if (time > 20.0) change -= 10
        
        return change
    }

    fun getComment(personality: String, isWin: Boolean, accuracy: Int): String {
        val ai = try { AIPersonality.valueOf(personality.uppercase()) } catch (e: Exception) { AIPersonality.NOVA }
        
        return when (ai) {
            AIPersonality.NOVA -> {
                if (isWin && accuracy > 80) listOf(
                    "Brilliant! Your neural pathways are firing on all cylinders.",
                    "Excellent work! I'm impressed by your processing speed.",
                    "You navigated that perfectly! Let's keep this momentum going.",
                    "Wow! Flawless logic, I'm taking notes on your strategy."
                ).random()
                else if (isWin) listOf(
                    "Good job! We can work on your accuracy, but a win is a win.",
                    "You made it! A little bumpy, but you survived.",
                    "Nice try. With a bit more practice, you'll master this."
                ).random()
                else listOf(
                    "Don't worry, even human brains need a reboot sometimes.",
                    "That was a tough one. Take a deep breath and try again!",
                    "Learning is a process. I believe in you for the next round."
                ).random()
            }
            AIPersonality.VEX -> {
                if (isWin && accuracy > 80) listOf(
                    "Oh, look who decided to use their brain today. Cute.",
                    "Wow, you didn't fail. Should I print you a certificate?",
                    "Not terrible. For a human, anyway.",
                    "I suppose even a broken clock is right twice a day."
                ).random()
                else if (isWin) listOf(
                    "You survived. Barely. Don't let it go to your head.",
                    "I've seen faster processors in a toaster, but sure, you won.",
                    "A win? I must have left my difficulty slider on 'Toddler'."
                ).random()
                else listOf(
                    "Predictable. Absolutely predictable.",
                    "Did you even try? Or did your finger just slip?",
                    "I'd say 'better luck next time', but luck won't help you."
                ).random()
            }
            AIPersonality.ZERO -> {
                if (isWin && accuracy > 80) listOf(
                    "Efficiency optimal. Target neutralized.",
                    "Acceptable performance. Commencing next sequence.",
                    "Data indicates a temporary spike in human cognitive function.",
                    "Anomaly detected: Human outsmarted logic grid."
                ).random()
                else if (isWin) listOf(
                    "Suboptimal path taken. Victory achieved despite inefficiencies.",
                    "Warning: Error rate high. Recalibration required.",
                    "Task completed. Minimal parameters met."
                ).random()
                else listOf(
                    "Human limitation confirmed. Subject terminated.",
                    "Failure imminent. Processing zero potential.",
                    "Weakness identified. You cannot outcalculate the void."
                ).random()
            }
        }
    }
}
