package com.games.puzzle.candycrush.feature.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.games.puzzle.candycrush.feature.game.data.datasource.GameDataSource
import com.games.puzzle.candycrush.feature.game.data.repository.GameRepositoryImpl
import com.games.puzzle.candycrush.feature.game.domain.model.GameConfig
import com.games.puzzle.candycrush.feature.game.domain.model.GameState
import com.games.puzzle.candycrush.feature.game.domain.model.GameStatus
import com.games.puzzle.candycrush.feature.game.domain.repository.GameRepository
import com.games.puzzle.candycrush.feature.game.domain.usecase.ApplyGravityUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.CheckGameStatusUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.CreateNewGameUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.DetectMatchesUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.RefillBoardUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.ResolveBoardUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.SelectCandyUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.SwapCandiesUseCase
import com.games.puzzle.candycrush.feature.game.presentation.event.GameUiEvent
import com.candycrush.feature.game.presentation.state.GameUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val config = GameConfig()
    private val repository: GameRepository = buildRepository()

    private var internalState: GameState = repository.createGame(config)

    private val _uiState = MutableStateFlow(internalState.toUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onEvent(event: GameUiEvent) {
        viewModelScope.launch {
            when (event) {
                is GameUiEvent.OnCellTapped -> handleCellTap(event.row, event.col)
                is GameUiEvent.OnRestartTapped -> handleRestart()
                is GameUiEvent.OnDismissDialog -> Unit
            }
        }
    }

    private fun handleCellTap(row: Int, col: Int) {
        if (internalState.status != GameStatus.Playing) return
        internalState = repository.processSelection(internalState, row, col)
        _uiState.update { internalState.toUiState() }
    }

    private fun handleRestart() {
        internalState = repository.restartGame(internalState)
        _uiState.update { internalState.toUiState() }
    }

    private fun GameState.toUiState() = GameUiState(
        board = board,
        score = score,
        movesRemaining = movesRemaining,
        selectedPosition = selectedCell,
        status = status,
        targetScore = config.targetScore,
        isProcessing = false,
    )

    private fun buildRepository(): GameRepository {
        val dataSource = GameDataSource()
        val detectMatches = DetectMatchesUseCase()
        val applyGravity = ApplyGravityUseCase()
        val refillBoard = RefillBoardUseCase()
        return GameRepositoryImpl(
            dataSource = dataSource,
            createNewGameUseCase = CreateNewGameUseCase(),
            selectCandyUseCase = SelectCandyUseCase(),
            swapCandiesUseCase = SwapCandiesUseCase(),
            detectMatchesUseCase = detectMatches,
            resolveBoardUseCase = ResolveBoardUseCase(detectMatches, applyGravity, refillBoard),
            checkGameStatusUseCase = CheckGameStatusUseCase(),
        )
    }
}
