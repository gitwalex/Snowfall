package com.idapgroup.snowfall

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import com.idapgroup.snowfall.Constants.angleDivisor
import com.idapgroup.snowfall.Constants.angleSeedRange
import com.idapgroup.snowfall.Constants.baseFrameDurationMillis
import kotlin.math.cos
import kotlin.math.sin

interface Snowflake {
    fun update(elapsedMillis: Long)
    fun draw(contentDrawScope: ContentDrawScope)
}

internal class MeltingSnowflake(
    position: Offset,
    private val incrementFactor: Float,
    private val canvasSize: IntSize,
    private val maxAlpha: Float,
    private val painter: Painter,
) : Snowflake {

    init {
        require(maxAlpha in 0.1 .. 1.0)
    }

    private var position by mutableStateOf(position)
    private var alpha by mutableStateOf(0.001f)
    private var isIncreasing by mutableStateOf(true)

    override fun update(elapsedMillis: Long) {
        val increment = incrementFactor * (elapsedMillis / baseFrameDurationMillis) / 100f
        alpha = (if (isIncreasing) {
            alpha + increment
        } else {
            alpha - increment
        }).coerceIn(0f, maxAlpha)

        if (alpha == maxAlpha) {
            isIncreasing = false
        }

        if (alpha == 0f) {
            isIncreasing = true
            alpha = 0.001f
            position = canvasSize.randomPosition()
        }
    }

    override fun draw(contentDrawScope: ContentDrawScope) {
        with(contentDrawScope) {
            translate(
                left = position.x,
                top = position.y
            ) {
                with(painter) {
                    draw(
                        size = intrinsicSize,
                        alpha = alpha,
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}

internal class FallingSnowflake(
    private val incrementFactor: Float,
    private val size: Float,
    private val canvasSize: IntSize,
    position: Offset,
    angle: Double,
    private val painter: Painter,
) : Snowflake {
    private val baseSpeedPxAt60Fps = 5
    private var position by mutableStateOf(position)
    private var angle by mutableStateOf(angle)

    override fun update(elapsedMillis: Long) {
        val increment =
            incrementFactor * (elapsedMillis / baseFrameDurationMillis) * baseSpeedPxAt60Fps
        val xDelta = (increment * cos(angle)).toFloat()
        val yDelta = (increment * sin(angle)).toFloat()
        position = Offset(position.x + xDelta, position.y + yDelta)
        angle += angleSeedRange.random() / angleDivisor

        if (position.y > canvasSize.height + size) {
            position = Offset(canvasSize.width.random().toFloat(), -size)
        }
    }

    override fun draw(contentDrawScope: ContentDrawScope) {
        with(contentDrawScope) {
            translate(
                position.x,
                position.y
            ) {
                with(painter) {
                    draw(
                        intrinsicSize,
                        0.65f,
                        ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}