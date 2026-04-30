package com.games.puzzle.candycrush.feature.game.domain.model

sealed class GameStatus {
    data object Playing : GameStatus()
    data object Won : GameStatus()
    data object Lost : GameStatus()
}
