package com.games.puzzle.candycrush.feature.game.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing
import com.games.puzzle.candycrush.feature.game.presentation.components.CandyBoard
import com.games.puzzle.candycrush.feature.game.presentation.components.GameStatusDialog
import com.games.puzzle.candycrush.feature.game.presentation.components.GameTopBar
import com.games.puzzle.candycrush.feature.game.presentation.components.MovesCounter
import com.games.puzzle.candycrush.feature.game.presentation.components.ScoreBoard
import com.games.puzzle.candycrush.feature.game.presentation.event.GameUiEvent
import com.games.puzzle.candycrush.feature.game.presentation.viewmodel.GameViewModel

@Composable
fun CandyCrushGameScreen(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.TopBarBackground, AppColors.BoardBackground),
                ),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GameTopBar(
            onRestartClick = { viewModel.onEvent(GameUiEvent.OnRestartTapped) },
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md)
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreBoard(
                score = state.score,
                targetScore = state.targetScore,
            )
            MovesCounter(
                movesRemaining = state.movesRemaining,
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        CandyBoard(
            board = state.board,
            selectedPosition = state.selectedPosition,
            onCellClick = { row, col -> viewModel.onEvent(GameUiEvent.OnCellTapped(row, col)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(AppSpacing.sm),
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))
    }

    GameStatusDialog(
        status = state.status,
        score = state.score,
        targetScore = state.targetScore,
        onRestart = { viewModel.onEvent(GameUiEvent.OnRestartTapped) },
    )
}
