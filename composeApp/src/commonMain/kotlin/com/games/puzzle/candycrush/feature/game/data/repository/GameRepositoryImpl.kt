package com.games.puzzle.candycrush.feature.game.data.repository

import com.games.puzzle.candycrush.feature.game.domain.model.CandyAnimationEvent
import com.games.puzzle.candycrush.feature.game.domain.model.Cell
import com.games.puzzle.candycrush.feature.game.domain.model.GameConfig
import com.games.puzzle.candycrush.feature.game.domain.model.GameState
import com.games.puzzle.candycrush.feature.game.domain.model.GameStatus
import com.games.puzzle.candycrush.feature.game.domain.model.TurnResult
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
        return if (selected?.first == row && selected.second == col) {
            state.copy(selectedCell = null)
        } else {
            selectCandyUseCase(state, row, col)
        }
    }

    override fun processSwipe(state: GameState, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): TurnResult {
        if (state.status != GameStatus.Playing) return TurnResult(finalState = state, events = emptyList())

        if (!state.board.isValidPosition(fromRow, fromCol) || !state.board.isValidPosition(toRow, toCol)) {
            return TurnResult(finalState = state, events = emptyList())
        }

        if (!state.board.isAdjacent(fromRow, fromCol, toRow, toCol)) {
            return TurnResult(
                finalState = state,
                events = listOf(
                    CandyAnimationEvent.InvalidSwap(
                        from = Cell(row = fromRow, col = fromCol, candy = null),
                        to = Cell(row = toRow, col = toCol, candy = null),
                    ),
                ),
            )
        }

        val fromCandy = state.board.getCell(fromRow, fromCol).candy
        val toCandy = state.board.getCell(toRow, toCol).candy
        if (fromCandy == null || toCandy == null) {
            return TurnResult(finalState = state, events = emptyList())
        }

        val swappedBoard = swapCandiesUseCase(state.board, fromRow, fromCol, toRow, toCol)
        val matches = detectMatchesUseCase(swappedBoard)

        if (matches.isEmpty()) {
            return TurnResult(
                finalState = state.copy(selectedCell = null),
                events = listOf(
                    CandyAnimationEvent.InvalidSwap(
                        from = Cell(row = fromRow, col = fromCol, candy = null),
                        to = Cell(row = toRow, col = toCol, candy = null),
                    ),
                ),
            )
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
        val finalState = newState.copy(status = checkGameStatusUseCase(newState))

        return TurnResult(
            finalState = finalState,
            events = buildList {
                add(
                    CandyAnimationEvent.Swap(
                        from = Cell(row = fromRow, col = fromCol, candy = null),
                        to = Cell(row = toRow, col = toCol, candy = null),
                    ),
                )
                addAll(resolveResult.events)
            },
        )
    }

    override fun restartGame(state: GameState): GameState =
        createGame(state.config)
}
