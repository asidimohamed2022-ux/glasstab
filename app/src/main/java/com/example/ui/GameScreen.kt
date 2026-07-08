package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonOrange
import com.example.ui.theme.ButtonOrangePressed
import com.example.ui.theme.DarkBlueDashed
import com.example.ui.theme.DarkGreyText
import com.example.ui.theme.LiquidBlue
import com.example.ui.theme.TransparentGlassTop
import com.example.ui.theme.VibrantYellow

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VibrantYellow)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Main Game Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Stats / Score Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "SCORE: ${viewModel.score}",
                    style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        color = DarkGreyText,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.testTag("score_text")
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "BEST: ${viewModel.highScore}",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreyText.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.testTag("best_score_text")
                )
            }

            // Central Game Area containing the Glass cup and Dash targets
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GlassCup(
                    currentLevel = viewModel.currentLevel,
                    targetLevel = viewModel.targetLevel,
                    tolerance = viewModel.tolerance,
                    modifier = Modifier.size(width = 200.dp, height = 340.dp)
                )

                // Perfect tap pop-up message animation
                androidx.compose.animation.AnimatedVisibility(
                    visible = viewModel.showPerfectEffect,
                    enter = fadeIn() + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-160).dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "PERFECT! 🌟",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkBlueDashed
                            )
                        )
                    }
                }
            }

            // Bottom Controller Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Circular orange action button
                HoldButton(
                    isPressing = viewModel.isPressing,
                    onPressStart = { viewModel.startPressing() },
                    onPressEnd = { viewModel.endPressing() },
                    modifier = Modifier.testTag("hold_button")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TAP & HOLD TO FILL\nRELEASE ON DASHED LINE",
                    textAlign = TextAlign.Center,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreyText.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // Game Over overlay screen
        AnimatedVisibility(
            visible = viewModel.gameState == GameState.GAME_OVER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GameOverOverlay(
                score = viewModel.score,
                bestScore = viewModel.highScore,
                reason = viewModel.lastGameOverReason ?: GameOverReason.TOO_EARLY,
                onRestart = { viewModel.restartGame() },
                modifier = Modifier.testTag("game_over_overlay")
            )
        }

        // Continue / Game Over temporary overlay screen
        AnimatedVisibility(
            visible = viewModel.gameState == GameState.CONTINUE_PROMPT,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ContinuePromptOverlay(
                score = viewModel.score,
                countdownSeconds = viewModel.countdownSeconds,
                isAdReady = viewModel.isAdReady,
                onWatchAd = { viewModel.requestAdShow() },
                onSkip = { viewModel.skipAndRestart() },
                modifier = Modifier.testTag("continue_prompt_overlay")
            )
        }
    }
}

@Composable
fun GlassCup(
    currentLevel: Float,
    targetLevel: Float,
    tolerance: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Config values in dp
    val cupHeightDp = 280.dp
    val topWidthDp = 150.dp
    val bottomWidthDp = 100.dp
    val strokeWidthDp = 6.dp

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val topWidthPx = with(density) { topWidthDp.toPx() }
        val bottomWidthPx = with(density) { bottomWidthDp.toPx() }
        val cupHeightPx = with(density) { cupHeightDp.toPx() }
        val strokeWidthPx = with(density) { strokeWidthDp.toPx() }

        // Align vertically and horizontally in canvas space
        val startY = (height - cupHeightPx) / 2
        val endY = startY + cupHeightPx

        val topLeftX = (width - topWidthPx) / 2
        val topRightX = topLeftX + topWidthPx

        val bottomLeftX = (width - bottomWidthPx) / 2
        val bottomRightX = bottomLeftX + bottomWidthPx

        // Path representing the inner glass container
        val glassPath = Path().apply {
            moveTo(topLeftX, startY)
            lineTo(bottomLeftX, endY)
            lineTo(bottomRightX, endY)
            lineTo(topRightX, startY)
            close()
        }

        // Draw clipped background & liquid filling
        clipPath(glassPath) {
            // Empty glass subtle light background color
            drawRect(
                color = TransparentGlassTop,
                topLeft = Offset(0f, 0f),
                size = Size(width, height)
            )

            // Calculate liquid fill rect height and top offset
            val liquidHeightPx = (currentLevel * cupHeightPx).coerceAtMost(cupHeightPx * 1.1f)
            val liquidTopY = endY - liquidHeightPx

            drawRect(
                color = LiquidBlue,
                topLeft = Offset(0f, liquidTopY),
                size = Size(width, liquidHeightPx + 50f) // add padding at bottom to avoid micro-gaps
            )
        }

        // Draw the distinct, dark grey thick container frame on top
        drawPath(
            path = glassPath,
            color = DarkGreyText,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw the horizontal dark blue dashed target line
        // Level positions start from the bottom line of the cup
        val targetY = endY - (targetLevel * cupHeightPx)
        
        drawLine(
            color = DarkBlueDashed,
            start = Offset(topLeftX - 30f, targetY),
            end = Offset(topRightX + 30f, targetY),
            strokeWidth = 4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        )

        // Optional target zone glow visualization to make it feel arcade-polished
        val zoneTopY = endY - ((targetLevel + tolerance) * cupHeightPx)
        val zoneBottomY = endY - ((targetLevel - tolerance) * cupHeightPx)
        drawRect(
            color = DarkBlueDashed.copy(alpha = 0.08f),
            topLeft = Offset(topLeftX - 20f, zoneTopY),
            size = Size(topWidthPx + 40f, zoneBottomY - zoneTopY)
        )
    }
}

@Composable
fun HoldButton(
    isPressing: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant spring press scale effect
    val scale by animateFloatAsState(
        targetValue = if (isPressing) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(100.dp)
            .shadow(
                elevation = if (isPressing) 2.dp else 8.dp,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(if (isPressing) ButtonOrangePressed else ButtonOrange)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        try {
                            awaitRelease()
                        } finally {
                            onPressEnd()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual circular ring indicator inside
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(Color.Transparent, CircleShape)
                .shadow(
                    elevation = 0.dp,
                    shape = CircleShape,
                    clip = false
                )
                .border(
                    width = 4.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HOLD",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

// Extension to draw custom borders on Box easily
@Composable
fun Modifier.border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): Modifier {
    return this.then(
        Modifier.background(color = Color.Transparent, shape = shape)
            .shadow(0.dp)
            .padding(width)
            .clip(shape)
    )
}

@Composable
fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    reason: GameOverReason,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.85f),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME OVER",
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (reason) {
                    GameOverReason.TOO_EARLY -> "TOO EARLY! 🧊\nYou released too soon."
                    GameOverReason.OVERFILL -> "OVERFILL! 🌊\nThe liquid overflowed."
                },
                textAlign = TextAlign.Center,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Score Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SCORE",
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "$score",
                            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BEST",
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "$bestScore",
                            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            // Interactive orange tap to restart button
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("restart_button")
            ) {
                Text(
                    text = "PLAY AGAIN",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ContinuePromptOverlay(
    score: Int,
    countdownSeconds: Int,
    isAdReady: Boolean,
    onWatchAd: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.9f),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CONTINUE?",
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large Countdown Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .border(width = 4.dp, color = ButtonOrange, shape = CircleShape)
            ) {
                Text(
                    text = "$countdownSeconds",
                    style = androidx.compose.material3.MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Keep your score of $score and continue playing!",
                textAlign = TextAlign.Center,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isAdReady) {
                Button(
                    onClick = onWatchAd,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonOrange),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                        .testTag("watch_ad_button")
                ) {
                    Text(
                        text = "🎬 WATCH AD TO CONTINUE",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }
            } else {
                Text(
                    text = "Loading Ad...",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.5f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("skip_restart_button")
            ) {
                Text(
                    text = "NO THANKS, RESTART",
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
