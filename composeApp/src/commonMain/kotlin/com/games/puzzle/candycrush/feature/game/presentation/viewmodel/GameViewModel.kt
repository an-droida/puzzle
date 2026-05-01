package com.games.puzzle.candycrush.feature.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candycrush.feature.game.presentation.state.GameUiState
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
import com.games.puzzle.candycrush.feature.game.presentation.gesture.neighbor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val config = GameConfig()
    private val repository: GameRepository = buildRepository()

    private var internalState: GameState = repository.createGame(config)

    private var pendingTurnId: Long? = null
    private var pendingFinalState: GameState? = null
    private var turnIdCounter: Long = 1L

    private val _uiState = MutableStateFlow(internalState.toUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onEvent(event: GameUiEvent) {
        viewModelScope.launch {
            when (event) {
                is GameUiEvent.CandyClicked -> handleCandyClick(event.cell.row, event.cell.col)
                is GameUiEvent.CandySwiped -> handleCandySwipe(
                    event.from.row,
                    event.from.col,
                    event.direction
                )

                is GameUiEvent.RestartClicked -> handleRestart()
                is GameUiEvent.AnimationsFinished -> handleAnimationsFinished(event.turnId)
            }
        }
    }

    private fun handleCandyClick(row: Int, col: Int) {
        if (_uiState.value.isBoardLocked) return
        if (internalState.status != GameStatus.Playing) return

        internalState = repository.processSelection(internalState, row, col)
        _uiState.update { internalState.toUiState() }
    }

    private fun handleCandySwipe(
        fromRow: Int,
        fromCol: Int,
        direction: com.games.puzzle.candycrush.feature.game.presentation.gesture.SwipeDirection
    ) {
        if (_uiState.value.isBoardLocked) return
        if (internalState.status != GameStatus.Playing) return

        val from = com.games.puzzle.candycrush.feature.game.domain.model.Cell(
            row = fromRow,
            col = fromCol,
            candy = null
        )
        val to = from.neighbor(direction)
        if (!internalState.board.isValidPosition(to.row, to.col)) return

        // Clear selection immediately (presentation only) and lock the board.
        internalState = internalState.copy(selectedCell = null)

        val result = repository.processSwipe(
            state = internalState,
            fromRow = fromRow,
            fromCol = fromCol,
            toRow = to.row,
            toCol = to.col,
        )

        val turnId = turnIdCounter++
        pendingTurnId = turnId
        pendingFinalState = result.finalState

        _uiState.update {
            it.copy(
                isProcessing = true,
                isBoardLocked = true,
                animationTurnId = turnId,
                pendingAnimationEvents = result.events,
            )
        }
    }

    private fun handleAnimationsFinished(turnId: Long) {
        if (pendingTurnId != turnId) return
        val finalState = pendingFinalState ?: return

        pendingTurnId = null
        pendingFinalState = null

        internalState = finalState
        _uiState.update { internalState.toUiState() }
    }

    private fun handleRestart() {
        pendingTurnId = null
        pendingFinalState = null
        internalState = repository.restartGame(internalState)
        _uiState.update { internalState.toUiState() }
    }

    private fun GameState.toUiState() = GameUiState(
        board = board,
        score = score,
        movesRemaining = movesRemaining,
        status = status,
        targetScore = config.targetScore,
        isProcessing = false,
        isBoardLocked = false,
        animationTurnId = 0L,
        pendingAnimationEvents = emptyList(),
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
