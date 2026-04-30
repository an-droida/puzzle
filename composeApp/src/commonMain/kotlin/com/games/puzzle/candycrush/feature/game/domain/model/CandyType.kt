package com.games.puzzle.candycrush.feature.game.domain.model

import org.jetbrains.compose.resources.DrawableResource
import puzzle.composeapp.generated.resources.Res
import puzzle.composeapp.generated.resources.ic_banana
import puzzle.composeapp.generated.resources.ic_berries
import puzzle.composeapp.generated.resources.ic_cherry
import puzzle.composeapp.generated.resources.ic_orange
import puzzle.composeapp.generated.resources.ic_pear
import puzzle.composeapp.generated.resources.ic_pineapple

enum class CandyType(val icon: DrawableResource) {
    RED(Res.drawable.ic_cherry),
    ORANGE(Res.drawable.ic_orange),
    YELLOW(Res.drawable.ic_banana),
    GREEN(Res.drawable.ic_pear),
    BLUE(Res.drawable.ic_berries),
    PURPLE(Res.drawable.ic_pineapple);

    companion object {
        fun random(): CandyType = entries.random()
    }
}
