package com.example.live

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.live.ui.theme.LiveTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LiveTheme {

                var isGenerating by remember {
                    mutableStateOf(true)
                }
                var playButtonIsVisibly by remember {
                    mutableStateOf(false)
                }

                LaunchedEffect(
                    key1 = playButtonIsVisibly,
                    key2 = isGenerating,
                ) {
                    delay(3000)
                    if (playButtonIsVisibly && isActive) {
                        playButtonIsVisibly = false
                    }
                }

                // A surface container using the 'background' color from the theme
                Scaffold(
                    floatingActionButton = {
                        Row {
                            FloatingActionButton(
                                onClick = { /* TODO */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val (screenWidth, screenHeight) = with(LocalConfiguration.current) {
                            with(LocalDensity.current) {
                                screenWidthDp.dp.toPx() to screenHeightDp.dp.toPx()
                            }
                        }
                        val gridSize = 20f
                        val generateSpeed = 100L

                        val (n, m) = (screenHeight / gridSize).roundToInt() to (screenWidth / gridSize).roundToInt()

                        var generate by remember {
                            mutableStateOf(
                                Array(n) {
                                    BooleanArray(m) {
                                        Random.nextBoolean()
                                    }
                                }
                            )
                        }

                        LaunchedEffect(
                            key1 = isGenerating,
                        ) {
                            while (isGenerating) {
                                generateGlider(generate)
                                generate = calculateNextGeneration(generate)
                                delay(generateSpeed)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        isGenerating = !isGenerating
                                        playButtonIsVisibly = true
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {

                            AnimatedVisibility(
                                modifier = Modifier
                                    .zIndex(1f)
                                    .size(60.dp),
                                visible = playButtonIsVisibly,
                                enter = fadeIn(tween(200)) + scaleIn(tween(300)),
                                exit = fadeOut(tween(200)) + scaleOut(tween(300)),
                            ) {
                                IconButton(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .fillMaxSize(),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color.Gray.copy(.5f)
                                    ),
                                    onClick = {
                                        isGenerating = !isGenerating
                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        painter = painterResource(
                                            id = if (isGenerating) R.drawable.ic_round_pause
                                            else R.drawable.ic_round_play_arrow
                                        ),
                                        contentDescription = null
                                    )
                                }
                            }

                            Canvas(
                                modifier = Modifier
                                    .background(Color.White)
                                    .fillMaxSize()

                            ) {
                                generate.forEachIndexed { i, m ->
                                    m.forEachIndexed { j, v ->
                                        if (v) {
                                            drawRect(
                                                color = Color.Black,
                                                size = Size(gridSize, gridSize),
                                                topLeft = Offset(
                                                    y = gridSize * i,
                                                    x = gridSize * j,
                                                )
                                            )
                                        }
                                    }
                                }

                                repeat((screenWidth / gridSize).roundToInt()) {
                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(
                                            x = gridSize * it,
                                            y = 0f
                                        ),
                                        end = Offset(
                                            x = gridSize * it,
                                            y = screenHeight
                                        ),
                                        colorFilter = ColorFilter.tint(Color.Black)
                                    )
                                }
                                repeat((screenHeight / gridSize).roundToInt()) {
                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(
                                            x = 0f,
                                            y = gridSize * it,
                                        ),
                                        end = Offset(
                                            x = screenWidth,
                                            y = gridSize * it,
                                        ),
                                        colorFilter = ColorFilter.tint(Color.Black)
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

fun generateGlider(currentGeneration: Array<BooleanArray>) {
    currentGeneration[0][1] = true
    currentGeneration[1][2] = true
    currentGeneration[2][0] = true
    currentGeneration[2][1] = true
    currentGeneration[2][2] = true
}

fun calculateNextGeneration(currentGeneration: Array<BooleanArray>): Array<BooleanArray> {
    val numRows = currentGeneration.size
    val numCols = currentGeneration[0].size
    val nextGeneration = Array(numRows) { BooleanArray(numCols) }

    val neighbors = arrayOf(
        intArrayOf(-1, -1), intArrayOf(-1, 0), intArrayOf(-1, 1),
        intArrayOf(0, -1), intArrayOf(0, 1),
        intArrayOf(1, -1), intArrayOf(1, 0), intArrayOf(1, 1)
    )

    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            val cell = currentGeneration[row][col]
            var liveNeighbors = 0

            for (neighborOffset in neighbors) {
                val newRow = row + neighborOffset[0]
                val newCol = col + neighborOffset[1]

                if (newRow in 0 until numRows && newCol in 0 until numCols) {
                    if (currentGeneration[newRow][newCol]) {
                        liveNeighbors++
                    }
                }
            }

            if (cell) {
                nextGeneration[row][col] = !(liveNeighbors < 2 || liveNeighbors > 3)
            } else {
                if (liveNeighbors == 3) {
                    nextGeneration[row][col] = true
                }
            }
        }
    }
    return nextGeneration
}