package com.games.puzzle.candycrush.feature.game.domain.model

data class GameConfig(
    val boardSize: Int = 8,
    val maxMoves: Int = 30,
    val targetScore: Int = 1000,
    val pointsPerCandy: Int = 10,
)
