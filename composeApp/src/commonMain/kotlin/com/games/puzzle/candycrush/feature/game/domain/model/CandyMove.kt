package com.games.puzzle.candycrush.feature.game.domain.model

/**
 * A stable-id movement operation that is animation-friendly.
 *
 * Important: [candyId] must remain stable across swap/gravity moves; only newly spawned candies
 * get new ids.
 */
data class CandyMove(
    val candyId: Long,
    val from: Cell,
    val to: Cell,
)

