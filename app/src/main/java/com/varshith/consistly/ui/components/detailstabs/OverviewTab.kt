package com.varshith.consistly.ui.components.detailstabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.varshith.consistly.R
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.data.models.getCustomReminderDays
import com.varshith.consistly.data.models.getMotivationalQuotes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun OverviewTab(streak: StreakEntity) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProgressCard(streak) }
        item {AchievementsCard(streak)}
        item { PerformanceInsightsCard(streak) }

//        item { CircularProgressCard(streak) }
//        item { StatisticsCard(streak) }
        item { ReminderSection(streak) }
        item { StreakCalendarView(streak) }

        if (streak.getMotivationalQuotes().isNotEmpty()) {
            item { QuotesCard(quotes = streak.getMotivationalQuotes()) }
        }
    }
}

@Composable
private fun ProgressCard(streak: StreakEntity) {
    val currentDate = remember { LocalDate.now() }
    val completionRate = remember(streak) {
        if (streak.dailyLogDates.isEmpty()) return@remember 0f
        val daysBetween = ChronoUnit.DAYS.between(streak.startDate, currentDate) + 1
        (streak.dailyLogDates.size.toFloat() / daysBetween.toFloat()) * 100
    }
    val safeCompletionRate = remember(completionRate) {
        when {
            completionRate.isNaN() -> 0f
            completionRate.isInfinite() -> 0f
            else -> completionRate.coerceIn(0f, 100f)
        }
    }

    val currentRotation = remember { Animatable(0f) }
    val progressColor = colorScheme.primary
    val backgroundColor = colorScheme.surfaceVariant

    LaunchedEffect(safeCompletionRate) {
        currentRotation.animateTo(
            targetValue = 360f * (safeCompletionRate / 100f),
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Background circle
                    drawCircle(
                        color = progressColor.copy(alpha = 0.1f),
                        style = Stroke(width = 16f)
                    )

                    // Progress arc with gradient
                    drawArc(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                progressColor,
                                progressColor.copy(alpha = 0.7f)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = currentRotation.value,
                        useCenter = false,
                        style = Stroke(
                            width = 16f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.cornerPathEffect(16f)
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${completionRate.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurface
                    )
                    AnimatedVisibility(
                        visible = completionRate > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = "Completion Rate",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRowEnhanced(
                    icon = painterResource(R.drawable.ic_whatshot),
                    label = "Current Streak",
                    value = "${streak.currentStreak}",
                    unit = "days",
                    color = progressColor
                )
                StatRowEnhanced(
                    icon = painterResource(R.drawable.ic_emojievents),
                    label = "Best Streak",
                    value = "${streak.longestStreak}",
                    unit = "days",
                    color = progressColor
                )
                StatRowEnhanced(
                    icon = painterResource(R.drawable.ic_today),
                    label = "Total Days",
                    value = "${streak.totalCompletedDays}",
                    unit = "completed",
                    color = progressColor
                )
            }
        }
    }
}

@Composable
private fun AchievementsCard(streak: StreakEntity) {
    val achievements = remember(streak) {
        listOf(
            Achievement(
                title = "Getting Started",
                description = "Started your first streak",
                isUnlocked = true,
                progress = 100f,
                iconResId = R.drawable.ic_whatshot
            ),
            Achievement(
                title = "Week Warrior",
                description = "Maintained streak for 7 days",
                isUnlocked = streak.currentStreak >= 7,
                progress = (streak.currentStreak.toFloat() / 7f * 100f).coerceIn(0f, 100f),
                iconResId = R.drawable.ic_today
            ),
            Achievement(
                title = "Consistency Master",
                description = "Reached ${streak.targetDays} days goal",
                isUnlocked = streak.currentStreak >= streak.targetDays,
                progress = (streak.currentStreak.toFloat() / streak.targetDays.toFloat() * 100f).coerceIn(0f, 100f),
                iconResId = R.drawable.ic_emojievents
            ),
            Achievement(
                title = "Perfect Week",
                description = "Completed all days in a week",
                isUnlocked = streak.dailyLogDates.takeLast(7).size == 7,
                progress = (streak.dailyLogDates.takeLast(7).size.toFloat() / 7f * 100f).coerceIn(0f, 100f),
                iconResId = R.drawable.ic_whatshot
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            achievements.forEach { achievement ->
                AchievementRow(achievement = achievement)
                if (achievement != achievements.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

private data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val progress: Float,
    val iconResId: Int  // Changed from Painter to Int resource ID
)

@Composable
private fun AchievementRow(achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (achievement.isUnlocked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(achievement.iconResId),  // Now using painterResource within Composable
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (achievement.isUnlocked)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = achievement.progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (achievement.isUnlocked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
@Composable
private fun PerformanceInsightsCard(streak: StreakEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trendingup),
                    contentDescription = "Performance Insights",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Performance Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Performance Metrics
            PerformanceMetric(
                label = "Consistency Rate",
                value = calculateConsistencyRate(streak),
                color = colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            PerformanceMetric(
                label = "Streak Completion",
                value = "${streak.totalCompletedDays} / ${streak.targetDays} days",
                color = colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Missed Days Insight
            val missedDays = calculateMissedDays(streak)
            PerformanceMetric(
                label = "Missed Days",
                value = "$missedDays days",
                color = colorScheme.error
            )
        }
    }
}

@Composable
private fun PerformanceMetric(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuotesCard(quotes: List<String>) {
    var expandedQuoteIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_format_quote),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Motivational Quotes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            quotes.forEachIndexed { index, quote ->
                val isExpanded = expandedQuoteIndex == index

                QuoteItem(
                    quote = quote,
                    isExpanded = isExpanded,
                    onToggle = {
                        expandedQuoteIndex = if (isExpanded) null else index
                    }
                )

                if (index < quotes.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
private fun StatRowEnhanced(
    icon: Painter,
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun QuoteItem(
    quote: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val maxLines by animateIntAsState(
        targetValue = if (isExpanded) Int.MAX_VALUE else 2,
        label = "maxLines"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Text(
            text = "\"$quote\"",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
        if (!isExpanded && quote.length > 100) {
            Text(
                text = "Read more",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun ReminderSection(streak: StreakEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_alarm),
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily Reminder",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Switch(
                    checked = streak.reminderEnabled,
                    onCheckedChange = { /* TODO: Handle reminder toggle */ }
                )
            }

            if (streak.reminderEnabled && streak.reminderTimeString != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reminder set for ${streak.reminderTimeString}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                if (streak.customReminderDays.isNotEmpty()) {
                    Text(
                        text = "Custom days: ${streak.getCustomReminderDays().joinToString(", ") { getDayName(it) }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun StreakCalendarView(streak: StreakEntity) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val currentMonth = remember { LocalDate.now() }
    var displayedMonth by remember { mutableStateOf(currentMonth) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    displayedMonth = displayedMonth.minusMonths(1)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cheveron_left),
                        contentDescription = "Previous month"
                    )
                }

                Text(
                    text = displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = {
                    displayedMonth = displayedMonth.plusMonths(1)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cheveron_right),
                        contentDescription = "Next month"
                    )
                }
            }

            // Weekday headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid using a regular Grid instead of LazyVerticalGrid
            CalendarGrid(
                displayedMonth = displayedMonth,
                selectedDate = selectedDate,
                streak = streak,
                onDateClick = { selectedDate = it }
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    displayedMonth: LocalDate,
    selectedDate: LocalDate?,
    streak: StreakEntity,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = displayedMonth.withDayOfMonth(1)
    val lastDayOfMonth = displayedMonth.withDayOfMonth(displayedMonth.lengthOfMonth())
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val totalDays = firstDayOfWeek + lastDayOfMonth.dayOfMonth
    val rows = (totalDays + 6) / 7 // Calculate number of rows needed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col - firstDayOfWeek
                    if (dayIndex in 0 until lastDayOfMonth.dayOfMonth) {
                        val date = displayedMonth.withDayOfMonth(dayIndex + 1)
                        val isSelected = selectedDate == date
                        val hasStreak = streak.dailyLogDates.contains(date)

                        CalendarDay(
                            date = date,
                            isSelected = selectedDate == date,
                            hasStreak = streak.dailyLogDates.contains(date),
                            hasMissedStreak = isMissedDay(streak, date), // New method to determine missed days
                            onDateClick = onDateClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty space for days before/after the month
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    hasStreak: Boolean,
    hasMissedStreak: Boolean, // New parameter
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> colorScheme.primary
                    hasStreak -> colorScheme.primary.copy(alpha = 0.1f)
                    hasMissedStreak -> colorScheme.error.copy(alpha = 0.1f) // Highlight missed days in light error color
                    else -> Color.Transparent
                }
            )
            .clickable { onDateClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isSelected -> colorScheme.onPrimary
                    hasStreak -> colorScheme.primary
                    hasMissedStreak -> colorScheme.error // Change text color for missed days
                    else -> colorScheme.onSurface
                }
            )
            if (hasStreak && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary)
                )
            }
            // Optional: Add a small dot for missed days
            if (hasMissedStreak && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colorScheme.error)
                )
            }
        }
    }
}

private fun getDayName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "Sunday"
        2 -> "Monday"
        3 -> "Tuesday"
        4 -> "Wednesday"
        5 -> "Thursday"
        6 -> "Friday"
        7 -> "Saturday"
        else -> ""
    }
}

private fun isMissedDay(streak: StreakEntity, date: LocalDate): Boolean {
    // Only mark days as missed between the streak's start date and today
    return date >= streak.startDate &&
            date < LocalDate.now() &&
            !streak.dailyLogDates.contains(date)
}

private fun calculateConsistencyRate(streak: StreakEntity): String {
    val totalDays = ChronoUnit.DAYS.between(streak.startDate, LocalDate.now()) + 1
    val completedDays = streak.dailyLogDates.size
    val consistencyRate = (completedDays.toFloat() / totalDays.toFloat() * 100).coerceIn(0f, 100f)
    return "${consistencyRate.toInt()}%"
}

private fun calculateMissedDays(streak: StreakEntity): Int {
    val totalDays = ChronoUnit.DAYS.between(streak.startDate, LocalDate.now()) + 1
    val completedDays = streak.dailyLogDates.size
    return (totalDays - completedDays).toInt()
}


