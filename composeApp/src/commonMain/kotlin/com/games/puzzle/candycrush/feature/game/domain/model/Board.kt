package com.games.puzzle.candycrush.feature.game.domain.model

import kotlin.math.abs

data class Board(
    val cells: List<List<Cell>>,
    val size: Int = 8,
) {
    fun getCell(row: Int, col: Int): Cell = cells[row][col]

    fun isValidPosition(row: Int, col: Int): Boolean =
        row in 0 until size && col in 0 until size

    fun withCell(row: Int, col: Int, candy: Candy?): Board {
        val newCells = cells.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == row && c == col) cell.withCandy(candy) else cell
            }
        }
        return copy(cells = newCells)
    }

    fun isAdjacent(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val rowDiff = abs(row1 - row2)
        val colDiff = abs(col1 - col2)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }
}
