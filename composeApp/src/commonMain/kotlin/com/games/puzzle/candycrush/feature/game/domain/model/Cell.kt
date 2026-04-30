package com.games.puzzle.candycrush.feature.game.domain.model

data class Cell(
    val row: Int,
    val col: Int,
    val candy: Candy?,
) {
    val isEmpty: Boolean get() = candy == null

    fun withCandy(candy: Candy?): Cell = copy(candy = candy)
}
