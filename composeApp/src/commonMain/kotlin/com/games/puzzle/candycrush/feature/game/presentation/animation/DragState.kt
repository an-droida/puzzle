package com.games.puzzle.candycrush.feature.game.presentation.animation

import androidx.compose.ui.geometry.Offset
import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import com.games.puzzle.candycrush.feature.game.presentation.gesture.SwipeDirection

enum class DragAxis {
    Horizontal,
    Vertical,
}

data class DragState(
    val candyId: Long,
    val fromCell: Cell,
    val offset: Offset,
    val lockedAxis: DragAxis?,
    /** Direction becomes non-null once the drag crosses the swipe threshold. */
    val pendingDirection: SwipeDirection?,
)

