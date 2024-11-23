package com.varshith.consistly.ui.components
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.varshith.consistly.R
import com.varshith.consistly.data.models.StreakEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StreakCard(
    streak: StreakEntity,
    onLogDay: () -> Unit,
    onBreakStreak: () -> Unit,
    onCardClick: () -> Unit,
    onDeleteStreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val cardHeight by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    DeleteConfirmationDialog(
        visible = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            showDeleteDialog = false
            onDeleteStreak()
        },
        itemName = streak.name,
        message = "Are you sure you want to delete '${streak.name}' streak? This action cannot be undone."
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .shadow(
                elevation = if (isExpanded) 16.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (streak.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            .clickable { onCardClick() }
            .combinedClickable(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    isExpanded = !isExpanded
                }
            )
            .semantics {
                contentDescription = buildString {
                    append("Streak for ${streak.name}. ")
                    append("Current streak: ${streak.currentStreak} days. ")
                    append("Status: ${if (streak.isActive) "Active" else "Broken"}. ")
                    append("Double tap to ${if (isExpanded) "collapse" else "expand"} details.")
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Modern Header with Animated Elements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (streak.isActive)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_whatshot),
                            contentDescription = null,
                            tint = if (streak.isActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = streak.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedVisibility(visible = !isExpanded) {
                            Text(
                                text = "${streak.currentStreak} days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusIndicator(
                        isActive = streak.isActive,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Details") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onCardClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
            if (streak.dailyLogDates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                streak.dailyLogDates
                    .sortedDescending()
                    .take(3)
                    .forEach { date ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = date.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // Modern Progress Visualization
                    ModernStreakProgress(
                        currentStreak = streak.currentStreak,
                        longestStreak = streak.longestStreak,
                        isActive = streak.isActive
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModernStatCard(
                            title = "Current",
                            value = "${streak.currentStreak}",
                            subtitle = "days",
                            icon = painterResource(id = R.drawable.ic_today),
                            color = if (streak.isActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )

                        ModernStatCard(
                            title = "Longest",
                            value = "${streak.longestStreak}",
                            subtitle = "days",
                            icon = painterResource(id = R.drawable.ic_emojievents),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Recent Logs Section
//                    if (streak.dailyLogDates.isNotEmpty()) {
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Text(
//                            text = "Recent Activity",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        streak.dailyLogDates
//                            .sortedDescending()
//                            .take(3)
//                            .forEach { date ->
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(vertical = 4.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Icon(
//                                        imageVector = Icons.Rounded.CheckCircle,
//                                        contentDescription = null,
//                                        tint = MaterialTheme.colorScheme.primary,
//                                        modifier = Modifier.size(16.dp)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text(
//                                        text = date.format(dateFormatter),
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                }
//                            }
//                    }
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onLogDay()
                    },
                    enabled = streak.isActive && !streak.dailyLogDates.contains(LocalDate.now()),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_task),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Progress")
                }

                FilledTonalButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBreakStreak()
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Break Streak")
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isActive) "Active" else "Broken",
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ModernStreakProgress(
    currentStreak: Int,
    longestStreak: Int,
    isActive: Boolean
) {
    val progress = (currentStreak.toFloat() / maxOf(longestStreak, 1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(progress * 100).toInt()}% of best streak",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModernStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: Painter,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.8f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}