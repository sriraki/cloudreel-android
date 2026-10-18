package com.cloudreel.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.theme.CRBrandGradient

/**
 * High-fidelity CloudReel Logo component.
 * Combines the official gradient infinity-cloud emblem with the brand wordmark.
 */
@Composable
fun CloudReelLogo(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 38.dp,
    textSize: Float = 24f,
    showWordmark: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Official CloudReel Emblem with brand gradient
        CloudReelEmblem(
            modifier = Modifier.size(emblemSize)
        )

        if (showWordmark) {
            val textColor = MaterialTheme.colorScheme.onBackground
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = textSize.sp,
                            letterSpacing = (-0.5).sp
                        )
                    ) {
                        append("Cloud")
                    }
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = textSize.sp,
                            letterSpacing = (-0.5).sp
                        )
                    ) {
                        append("Reel")
                    }
                }
            )
        }
    }
}

/**
 * Vector drawing of the official CloudReel gradient infinity emblem
 */
@Composable
fun CloudReelEmblem(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Proportional scale factor relative to SVG viewBox (166 x 108)
        val sx = width / 166f
        val sy = height / 108f

        val path = Path().apply {
            moveTo(162.6f * sx, 100.9f * sy)
            lineTo(132.4f * sx, 100.9f * sy)
            cubicTo(131.7f * sx, 100.9f * sy, 131.1f * sx, 100.6f * sy, 130.7f * sx, 100.0f * sy)
            lineTo(117.7f * sx, 81.4f * sy)
            cubicTo(117.2f * sx, 80.7f * sy, 117.3f * sx, 79.7f * sy, 117.7f * sx, 79.0f * sy)
            lineTo(125.5f * sx, 69.2f * sy)
            cubicTo(125.9f * sx, 68.7f * sy, 126.5f * sx, 68.4f * sy, 127.1f * sx, 68.4f * sy)
            lineTo(131.2f * sx, 68.4f * sy)
            cubicTo(137.0f * sx, 68.4f * sy, 141.6f * sx, 64.4f * sy, 141.6f * sx, 59.4f * sy)
            cubicTo(141.6f * sx, 54.4f * sy, 136.9f * sx, 50.6f * sy, 131.2f * sx, 50.4f * sy)
            cubicTo(125.4f * sx, 50.2f * sy, 121.5f * sx, 55.9f * sy, 121.4f * sx, 56.3f * sy)
            lineTo(106.4f * sx, 76.5f * sy)
            lineTo(101.3f * sx, 83.3f * sy)
            cubicTo(90.0f * sx, 98.9f * sy, 78.7f * sx, 104.6f * sy, 59.3f * sy, 104.4f * sy)
            cubicTo(23.9f * sx, 106.4f * sy, 1.1f * sx, 83.8f * sy, 0f, 55.9f * sy)
            cubicTo(-1.2f * sx, 25.3f * sy, 23.3f * sx, 0f, 53.8f * sx, 0f)
            cubicTo(88.1f * sx, 0f, 97.8f * sx, 22.9f * sy, 97.8f * sx, 22.9f * sy)
            cubicTo(92.5f * sx, 27.8f * sy, 88.6f * sx, 33.9f * sy, 83.0f * sx, 41.9f * sy)
            cubicTo(79.6f * sx, 31.5f * sy, 69.5f * sx, 24.2f * sy, 57.9f * sx, 24.8f * sy)
            cubicTo(45.0f * sx, 25.5f * sy, 34.6f * sx, 36.3f * sy, 34.4f * sx, 49.3f * sy)
            cubicTo(34.2f * sx, 63.3f * sy, 45.4f * sx, 74.7f * sy, 59.3f * sx, 74.7f * sy)
            cubicTo(66.0f * sx, 73.3f * sy, 76.5f * sx, 69.5f * sy, 84.1f * sx, 59.0f * sy)
            cubicTo(92.6f * sx, 47.2f * sy, 97.1f * sx, 39.2f * sy, 103.8f * sx, 32.9f * sy)
            cubicTo(110.2f * sx, 26.4f * sy, 119.8f * sx, 22.2f * sy, 130.5f * sx, 22.2f * sy)
            cubicTo(149.8f * sx, 22.2f * sy, 165.4f * sx, 35.6f * sy, 165.4f * sx, 52.2f * sy)
            cubicTo(165.4f * sx, 68.8f * sy, 160.2f * sx, 70.2f * sy, 152.1f * sx, 75.7f * sy)
            lineTo(163.9f * sx, 91.6f * sy)
            cubicTo(164.8f * sx, 92.9f * sy, 164.8f * sx, 94.8f * sy, 163.2f * sx, 94.8f * sy)
            close()
        }

        drawPath(
            path = path,
            brush = CRBrandGradient
        )
    }
}
