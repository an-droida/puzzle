package com.games.puzzle.candycrush.feature.game.domain.model

/**
 * Domain-friendly events that describe what happened to the board.
 *
 * The presentation layer can consume these to run animations, without re-implementing game rules.
 */
sealed interface CandyAnimationEvent {

    data class Swap(
        val from: Cell,
        val to: Cell,
    ) : CandyAnimationEvent

    /**
     * A swap that doesn't create a match. Presentation should animate swap there-and-back.
     */
    data class InvalidSwap(
        val from: Cell,
        val to: Cell,
    ) : CandyAnimationEvent

    /**
     * Cells whose candies should disappear.
     *
     * Note: the presentation layer should read candy ids from its current rendered board snapshot.
     */
    data class Remove(
        val cells: Set<Cell>,
    ) : CandyAnimationEvent

    /**
     * Existing candies moved due to gravity.
     */
    data class Fall(
        val moves: List<CandyMove>,
    ) : CandyAnimationEvent

    /**
     * Newly spawned candies (these are the only ones that get new stable ids).
     */
    data class Spawn(
        val cells: List<Cell>,
    ) : CandyAnimationEvent

    data object BoardResolved : CandyAnimationEvent
}

