package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

enum class GameState {
    PLAYING,
    DRAINING,
    CONTINUE_PROMPT,
    GAME_OVER
}

enum class GameOverReason {
    TOO_EARLY,
    OVERFILL
}

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // Game states
    var score by mutableStateOf(0)
        private set

    var highScore by mutableStateOf(0)
        private set

    var currentLevel by mutableStateOf(0.0f)
        private set

    var targetLevel by mutableStateOf(0.6f) // starting random target
        private set

    var isPressing by mutableStateOf(false)
        private set

    var gameState by mutableStateOf(GameState.PLAYING)
        private set

    var lastGameOverReason by mutableStateOf<GameOverReason?>(null)
        private set

    var showPerfectEffect by mutableStateOf(false)
        private set

    var countdownSeconds by mutableStateOf(5)
        private set

    var isAdReady by mutableStateOf(false)

    var onShowAdRequested: (() -> Unit)? = null

    // Tolerance configuration: 4.5% of the glass height
    val tolerance = 0.045f

    private var fillJob: Job? = null

    init {
        // Observe best high score from DB
        viewModelScope.launch {
            repository.highScoreFlow.collectLatest { dbScore ->
                highScore = dbScore?.score ?: 0
            }
        }
        resetRound()
    }

    private fun generateRandomTarget(): Float {
        // Random target height between 40% and 80% of the glass
        return Random.nextFloat() * 0.4f + 0.4f
    }

    fun startPressing() {
        if (gameState != GameState.PLAYING || isPressing) return
        isPressing = true
        currentLevel = 0.0f
        showPerfectEffect = false

        fillJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isPressing) {
                delay(16) // roughly 60fps
                val now = System.currentTimeMillis()
                val delta = (now - lastTime) / 1000f
                lastTime = now

                // Increase liquid height linearly (fills complete glass in ~2.5 seconds)
                val fillSpeed = 0.40f
                currentLevel = (currentLevel + fillSpeed * delta).coerceAtMost(1.1f)

                // Instant Game Over if overfilled
                if (currentLevel > targetLevel + tolerance) {
                    isPressing = false
                    triggerGameOver(GameOverReason.OVERFILL)
                    break
                }
            }
        }
    }

    fun endPressing() {
        if (!isPressing) return
        isPressing = false
        fillJob?.cancel()

        val diff = kotlin.math.abs(currentLevel - targetLevel)
        if (diff <= tolerance) {
            triggerWin(isPerfect = diff <= 0.015f)
        } else {
            triggerGameOver(GameOverReason.TOO_EARLY)
        }
    }

    private fun triggerWin(isPerfect: Boolean) {
        viewModelScope.launch {
            gameState = GameState.DRAINING
            if (isPerfect) {
                showPerfectEffect = true
            }

            // Keep the water full for a split second, then drain it smoothly
            delay(if (isPerfect) 400 else 150)
            
            val startLevel = currentLevel
            val drainDuration = 350L // 350ms
            val startTime = System.currentTimeMillis()

            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= drainDuration) {
                    currentLevel = 0.0f
                    break
                }
                val fraction = elapsed.toFloat() / drainDuration
                // Smooth cubic ease out for drainage
                val easeOut = 1f - (1f - fraction).toDouble().pow(3.0).toFloat()
                currentLevel = startLevel * (1f - easeOut)
                delay(16)
            }

            // Increment score
            score += 1
            if (score > highScore) {
                highScore = score
                repository.saveHighScore(highScore)
            }

            // Randomize next target and resume
            targetLevel = generateRandomTarget()
            showPerfectEffect = false
            gameState = GameState.PLAYING
        }
    }

    private fun triggerGameOver(reason: GameOverReason) {
        fillJob?.cancel()
        lastGameOverReason = reason
        gameState = GameState.CONTINUE_PROMPT
        startCountdown()
    }

    private var countdownJob: Job? = null

    private fun startCountdown() {
        countdownSeconds = 5
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (countdownSeconds > 0 && gameState == GameState.CONTINUE_PROMPT) {
                delay(1000)
                countdownSeconds--
            }
            if (gameState == GameState.CONTINUE_PROMPT) {
                goToGameOver()
            }
        }
    }

    fun goToGameOver() {
        countdownJob?.cancel()
        viewModelScope.launch {
            if (score > highScore) {
                highScore = score
                repository.saveHighScore(highScore)
            }
            gameState = GameState.GAME_OVER
        }
    }

    fun skipAndRestart() {
        countdownJob?.cancel()
        score = 0
        currentLevel = 0.0f
        targetLevel = generateRandomTarget()
        showPerfectEffect = false
        lastGameOverReason = null
        gameState = GameState.PLAYING
    }

    fun requestAdShow() {
        onShowAdRequested?.invoke()
    }

    fun onAdRewardCompleted() {
        countdownJob?.cancel()
        currentLevel = 0.0f
        isPressing = false
        gameState = GameState.PLAYING
    }

    fun restartGame() {
        score = 0
        currentLevel = 0.0f
        targetLevel = generateRandomTarget()
        showPerfectEffect = false
        lastGameOverReason = null
        gameState = GameState.PLAYING
    }

    private fun resetRound() {
        currentLevel = 0.0f
        targetLevel = generateRandomTarget()
        gameState = GameState.PLAYING
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
