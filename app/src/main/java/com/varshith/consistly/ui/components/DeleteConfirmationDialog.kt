package com.varshith.consistly.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

/**
 * A modern, visually appealing delete confirmation dialog with enhanced user experience.
 *
 * @param visible Whether the dialog is currently visible
 * @param onDismiss Callback when the dialog is dismissed
 * @param onConfirm Callback when deletion is confirmed
 * @param itemName Name of the item to be deleted
 * @param title Custom title for the dialog (defaults to "Delete {itemName}")
 * @param message Custom message for the dialog
 * @param confirmButtonText Text for the confirm button
 * @param dismissButtonText Text for the dismiss button
 */
@Composable
fun DeleteConfirmationDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    itemName: String,
    title: String = "Delete $itemName",
    message: String = "Are you sure you want to delete '$itemName'? This action cannot be undone.",
    confirmButtonText: String = "Delete",
    dismissButtonText: String = "Cancel"
) {
    // Animation scale for the dialog
    val dialogScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        label = "Dialog Scale Animation"
    )

    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            // Use a card for more depth and modern look
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Delete Warning",
                    modifier = Modifier
                        .scale(1.2f) // Slightly larger icon
                        .padding(top = 16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.scale(dialogScale)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = confirmButtonText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.scale(dialogScale)
                ) {
                    Text(
                        text = dismissButtonText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            // Enhance dialog appearance
            containerColor = MaterialTheme.colorScheme.surface,
            iconContentColor = MaterialTheme.colorScheme.error,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

            // Add more dialog properties for a smoother experience
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),

            // Increased elevation for more depth
            tonalElevation = 12.dp,

            // Adjust shape for a modern look
            shape = MaterialTheme.shapes.large
        )
    }
}
