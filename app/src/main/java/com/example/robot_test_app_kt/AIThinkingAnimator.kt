package com.example.robot_test_app_kt

import kotlinx.coroutines.*

class ThinkingAnimator(private val onUpdate: (String) -> Unit) {
    private var animationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val phrases = listOf(
        "Thinking",
        "Analyzing your request",
        "Consulting my brain",
        "Gathering information",
        "Processing",
        "Generating a response"
    )

    fun start() {
        stop()

        animationJob = scope.launch {
            val phrase = phrases.random()
            var dots = 0
            while (isActive) {
                val text = "$phrase${".".repeat(dots % 4)}"
                onUpdate(text)
                dots++
                delay(400)
            }
        }
    }

    fun stop() {
        animationJob?.cancel()
        animationJob = null
    }
}