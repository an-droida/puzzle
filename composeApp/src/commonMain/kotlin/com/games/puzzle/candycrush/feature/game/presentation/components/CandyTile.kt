package com.games.puzzle.candycrush.feature.game.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.feature.game.domain.model.Candy
import com.games.puzzle.candycrush.feature.game.domain.model.CandyType
import org.jetbrains.compose.resources.painterResource

@Composable
fun CandyTile(
    candy: Candy?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "candy_scale",
    )

    Box(
        modifier = modifier
            .padding(2.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.CellBackground)
            .then(
                if (isSelected) Modifier.border(
                    3.dp,
                    AppColors.SelectionRing,
                    RoundedCornerShape(10.dp)
                )
                else Modifier
            )
            .clickable(enabled = candy != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (candy != null) {
            val (dark, light) = candyColors(candy.type)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .shadow(
                        elevation = if (isSelected) 8.dp else 4.dp,
                        shape = CircleShape,
                        ambientColor = dark.copy(alpha = 0.6f),
                        spotColor = dark.copy(alpha = 0.6f),
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(colors = listOf(light, dark)),
                    ),
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(candy.type.icon), contentDescription = null
                )
            }
        }
    }
}

private fun candyColors(type: CandyType): Pair<Color, Color> = when (type) {
    CandyType.RED -> AppColors.CandyRed to AppColors.CandyRedLight
    CandyType.ORANGE -> AppColors.CandyOrange to AppColors.CandyOrangeLight
    CandyType.YELLOW -> AppColors.CandyYellow to AppColors.CandyYellowLight
    CandyType.GREEN -> AppColors.CandyGreen to AppColors.CandyGreenLight
    CandyType.BLUE -> AppColors.CandyBlue to AppColors.CandyBlueLight
    CandyType.PURPLE -> AppColors.CandyPurple to AppColors.CandyPurpleLight
}
