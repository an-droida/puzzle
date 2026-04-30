package com.games.puzzle.candycrush.feature.game.data.repository

import com.games.puzzle.candycrush.feature.game.domain.model.GameConfig
import com.games.puzzle.candycrush.feature.game.domain.model.GameState
import com.games.puzzle.candycrush.feature.game.domain.model.GameStatus
import com.games.puzzle.candycrush.feature.game.domain.repository.GameRepository
import com.games.puzzle.candycrush.feature.game.domain.usecase.CheckGameStatusUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.CreateNewGameUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.DetectMatchesUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.ResolveBoardUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.SelectCandyUseCase
import com.games.puzzle.candycrush.feature.game.domain.usecase.SwapCandiesUseCase
import com.games.puzzle.candycrush.feature.game.data.datasource.GameDataSource

class GameRepositoryImpl(
    private val dataSource: GameDataSource,
    private val createNewGameUseCase: CreateNewGameUseCase,
    private val selectCandyUseCase: SelectCandyUseCase,
    private val swapCandiesUseCase: SwapCandiesUseCase,
    private val detectMatchesUseCase: DetectMatchesUseCase,
    private val resolveBoardUseCase: ResolveBoardUseCase,
    private val checkGameStatusUseCase: CheckGameStatusUseCase,
) : GameRepository {

    override fun createGame(config: GameConfig): GameState {
        dataSource.resetIdCounter()
        return createNewGameUseCase(config, idCounter = dataSource::nextId)
    }

    override fun processSelection(state: GameState, row: Int, col: Int): GameState {
        if (state.status != GameStatus.Playing) return state

        val selected = state.selectedCell

        if (selected == null) {
            return selectCandyUseCase(state, row, col)
        }

        val (selRow, selCol) = selected

        if (selRow == row && selCol == col) {
            return state.copy(selectedCell = null)
        }

        if (state.board.isAdjacent(selRow, selCol, row, col)) {
            val swappedBoard = swapCandiesUseCase(state.board, selRow, selCol, row, col)
            val matches = detectMatchesUseCase(swappedBoard)

            if (matches.isEmpty()) {
                return state.copy(selectedCell = null)
            }

            val resolveResult = resolveBoardUseCase(
                board = swappedBoard,
                pointsPerCandy = state.config.pointsPerCandy,
                idCounter = dataSource::nextId,
            )

            val newState = state.copy(
                board = resolveResult.board,
                score = state.score + resolveResult.pointsEarned,
                movesRemaining = state.movesRemaining - 1,
                selectedCell = null,
            )
            return newState.copy(status = checkGameStatusUseCase(newState))
        }

        return selectCandyUseCase(state, row, col)
    }

    override fun restartGame(state: GameState): GameState =
        createGame(state.config)
}
