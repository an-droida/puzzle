package com.games.puzzle

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform