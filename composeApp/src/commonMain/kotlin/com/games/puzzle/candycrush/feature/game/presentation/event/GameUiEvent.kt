package com.games.puzzle.candycrush.feature.game.presentation.event

import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import com.games.puzzle.candycrush.feature.game.presentation.gesture.SwipeDirection

sealed interface GameUiEvent {

    data class CandyClicked(val cell: Cell) : GameUiEvent

    data class CandySwiped(
        val from: Cell,
        val direction: SwipeDirection,
    ) : GameUiEvent

    data object RestartClicked : GameUiEvent

    /**
     * Sent by the UI after it finishes playing the current turn's animation sequence.
     */
    data class AnimationsFinished(val turnId: Long) : GameUiEvent
}
