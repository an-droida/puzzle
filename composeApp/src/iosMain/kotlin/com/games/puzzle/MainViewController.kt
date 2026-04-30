package com.games.puzzle

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.games.puzzle.candycrush.core.designsystem.theme.AppTheme
import com.games.puzzle.candycrush.feature.game.presentation.screen.CandyCrushGameScreen
import com.games.puzzle.candycrush.feature.game.presentation.viewmodel.GameViewModel

fun MainViewController() = ComposeUIViewController {
    val viewModel = remember { GameViewModel() }
    AppTheme {
        CandyCrushGameScreen(viewModel = viewModel)
    }
}
