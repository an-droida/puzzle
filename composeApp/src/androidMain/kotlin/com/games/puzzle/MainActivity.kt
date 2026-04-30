package com.games.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.games.puzzle.candycrush.core.designsystem.theme.AppTheme
import com.games.puzzle.candycrush.feature.game.presentation.screen.CandyCrushGameScreen
import com.games.puzzle.candycrush.feature.game.presentation.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val vm: GameViewModel = viewModel()
                CandyCrushGameScreen(viewModel = vm)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val vm: GameViewModel = viewModel()
    CandyCrushGameScreen(viewModel = vm)
}