package com.games.puzzle.candycrush.feature.game.domain.model

data class GameState(
    val board: Board,
    val score: Int,
    val movesRemaining: Int,
    val selectedCell: Pair<Int, Int>?,
    val status: GameStatus,
    val config: GameConfig,
)
