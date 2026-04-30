package com.games.puzzle.candycrush.feature.game.presentation.gesture

import com.games.puzzle.candycrush.feature.game.domain.model.Cell

fun Cell.neighbor(direction: SwipeDirection): Cell {
    return when (direction) {
        SwipeDirection.UP -> copy(row = row - 1)
        SwipeDirection.DOWN -> copy(row = row + 1)
        SwipeDirection.LEFT -> copy(col = col - 1)
        SwipeDirection.RIGHT -> copy(col = col + 1)
    }
}

