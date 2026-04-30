package com.games.puzzle.candycrush.feature.game.presentation.event

sealed class GameUiEvent {
    data class OnCellTapped(val row: Int, val col: Int) : GameUiEvent()
    data object OnRestartTapped : GameUiEvent()
    data object OnDismissDialog : GameUiEvent()
}
