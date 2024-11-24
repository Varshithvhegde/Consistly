package com.varshith.consistly.ui.components.detailstabs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
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

@Composable
fun OverviewTab(streak: StreakEntity) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { CircularProgressCard(streak) }
        item { StatisticsCard(streak) }
        item { ReminderSection(streak) }
        item { StreakCalendarView(streak) }

        if (streak.getMotivationalQuotes().isNotEmpty()) {
            item { QuotesCard(quotes = streak.getMotivationalQuotes()) }
        }
    }
}

@Composable
private fun CircularProgressCard(streak: StreakEntity) {
    val currentRotation = remember { Animatable(0f) }
    val colorScheme = colorScheme  // Add this line to get the color scheme

    LaunchedEffect(streak) {
        currentRotation.animateTo(
            targetValue = 360f * streak.averageCompletionRate,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Background circle
                drawCircle(
                    color = colorScheme.surfaceVariant,  // Use colorScheme from MaterialTheme
                    style = Stroke(width = 24f)
                )

                // Progress arc
                drawArc(
                    color = colorScheme.primary,  // Use colorScheme from MaterialTheme
                    startAngle = -90f,
                    sweepAngle = currentRotation.value,
                    useCenter = false,
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${streak.averageCompletionRate.toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Completion Rate",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurfaceVariant  // Use colorScheme from MaterialTheme
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(streak: StreakEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedStatItem(
                value = streak.currentStreak,
                label = "Current",
                icon = painterResource(id = R.drawable.ic_whatshot)
            )
            AnimatedStatItem(
                value = streak.longestStreak,
                label = "Longest",
                icon = painterResource(id = R.drawable.ic_emojievents)
            )
            AnimatedStatItem(
                value = streak.totalCompletedDays,
                label = "Total Days",
                icon = painterResource(id = R.drawable.ic_today)
            )
        }
    }
}


@Composable
private fun AnimatedStatItem(
    value: Int,
    label: String,
    icon: Painter,
    modifier: Modifier = Modifier
) {
    var currentValue by remember { mutableStateOf(0) }

    LaunchedEffect(value) {
        animate(
            initialValue = currentValue.toFloat(),
            targetValue = value.toFloat(),
            animationSpec = tween(1000)
        ) { animatedValue, _ ->
            currentValue = animatedValue.toInt()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$currentValue",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
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
                            isSelected = isSelected,
                            hasStreak = hasStreak,
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
                    else -> Color.Transparent
                }
            )
            .clickable { onDateClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isSelected -> colorScheme.onPrimary
                hasStreak -> colorScheme.primary
                else -> colorScheme.onSurface
            }
        )
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


