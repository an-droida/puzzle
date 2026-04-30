package com.games.puzzle.candycrush.feature.game.data.datasource

import com.games.puzzle.candycrush.feature.game.domain.model.Candy
import com.games.puzzle.candycrush.feature.game.domain.model.CandyType

class GameDataSource {
    private var idCounter = 0L

    fun nextId(): Long = ++idCounter

    fun resetIdCounter() {
        idCounter = 0L
    }

    fun randomCandy(): Candy = Candy(id = nextId(), type = CandyType.random())
}
