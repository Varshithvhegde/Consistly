package com.varshith.consistly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onArchive: () -> Unit,
    streak: StreakEntity,
) {
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = streak.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    supportingContent = {
                        Column {
                            Text(
                                text = "${streak.currentStreak} days current streak",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Best: ${streak.longestStreak} days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_whatshot),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )

                ListItem(
                    headlineContent = { Text("View Details") },
                    supportingContent = { Text("See full streak statistics") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                            onDetails()
                        }
                        .padding(horizontal = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Edit Streak") },
                    supportingContent = { Text("Modify name and settings") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                            onEdit()
                        }
                        .padding(horizontal = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Share Progress") },
                    supportingContent = { Text("Export or share streak details") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                            onShare()
                        }
                        .padding(horizontal = 8.dp)
                )

                ListItem(
                    headlineContent = { Text("Archive Streak") },
                    supportingContent = { Text("Hide without deleting data") },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_archive),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                            onArchive()
                        }
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = {
                        Text(
                            "Delete Streak",
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    supportingContent = {
                        Text(
                            "This action cannot be undone",
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier
                        .clickable {
                            onDismiss()
                            onDelete()
                        }
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}