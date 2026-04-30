package com.games.puzzle.candycrush.feature.game.domain.model

enum class CandyType {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    BLUE,
    PURPLE;

    companion object {
        fun random(): CandyType = entries.random()
    }
}
