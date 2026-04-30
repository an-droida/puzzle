package com.games.puzzle.candycrush.feature.game.domain.model

/**
 * Result of a user turn that is intentionally split into:
 * - an animation script ([events])
 * - the final committed [finalState]
 */
data class TurnResult(
    val finalState: GameState,
    val events: List<CandyAnimationEvent>,
)

