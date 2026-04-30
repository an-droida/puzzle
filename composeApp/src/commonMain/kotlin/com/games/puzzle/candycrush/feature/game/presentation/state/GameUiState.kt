package com.candycrush.feature.game.presentation.state

import com.games.puzzle.candycrush.feature.game.domain.model.Board
import com.games.puzzle.candycrush.feature.game.domain.model.GameStatus

data class GameUiState(
    val board: Board,
    val score: Int,
    val movesRemaining: Int,
    val selectedPosition: Pair<Int, Int>?,
    val status: GameStatus,
    val targetScore: Int,
    val isProcessing: Boolean,
)
