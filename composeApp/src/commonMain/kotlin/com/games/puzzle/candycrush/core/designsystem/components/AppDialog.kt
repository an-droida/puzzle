package com.games.puzzle.candycrush.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.games.puzzle.candycrush.core.designsystem.colors.AppColors
import com.games.puzzle.candycrush.core.designsystem.spacing.AppSpacing

@Composable
fun AppDialog(
    title: String,
    message: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    containerColor: Color = AppColors.BoardSurface,
    onDismiss: () -> Unit = onPrimaryClick,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(
            shape = RoundedCornerShape(AppSpacing.cardRadius),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                AppText(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                AppText(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                AppButton(
                    text = primaryButtonText,
                    onClick = onPrimaryClick,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (secondaryButtonText != null && onSecondaryClick != null) {
                    AppButton(
                        text = secondaryButtonText,
                        onClick = onSecondaryClick,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = AppColors.CellBackground,
                    )
                }
            }
        }
    }
}
