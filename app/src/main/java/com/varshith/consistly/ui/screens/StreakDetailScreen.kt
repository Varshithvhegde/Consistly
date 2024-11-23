package com.varshith.consistly.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.data.models.getMotivationalQuotes
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.varshith.consistly.R
import com.varshith.consistly.data.models.getNotes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun StreakDetailsScreen(
    streak: StreakEntity,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Activity", "Details")

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = streak.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    StatusChip(isActive = streak.isActive)
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) with
                            fadeOut() + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start)
                }
            ) { targetTab ->
                when (targetTab) {
                    0 -> OverviewTab(streak)
                    1 -> ActivityTab(streak, dateFormatter)
                    2 -> DetailsTab(streak)
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(streak: StreakEntity) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CircularProgressCard(streak)
        }

        item {
            StatisticsCard(streak)
        }

        if (streak.getMotivationalQuotes().isNotEmpty()) {
            item {
                QuotesCard(quotes = streak.getMotivationalQuotes())
            }
        }
    }
}

@Composable
private fun CircularProgressCard(streak: StreakEntity) {
    val currentRotation = remember { Animatable(0f) }
    val colorScheme = MaterialTheme.colorScheme  // Add this line to get the color scheme

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
                    text = "${(streak.averageCompletionRate * 100).toInt()}%",
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
private fun ActivityTab(
    streak: StreakEntity,
    dateFormatter: DateTimeFormatter
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = streak.dailyLogDates.sortedDescending()
        ) { index, date ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it * (index + 1) }
                )
            ) {
                ActivityLogItem(date = date, dateFormatter = dateFormatter)
            }
        }
    }
}

@Composable
private fun DetailsTab(streak: StreakEntity) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DetailsCard(streak)
        }

        if (streak.getNotes().isNotEmpty()) {
            item {
                NotesCard(notes = streak.getNotes())
            }
        }
    }
}

@Composable
private fun DetailsCard(streak: StreakEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            DetailRow(
                icon = painterResource(id = R.drawable.ic_category),
                label = "Category",
                value = streak.category ?: "None"
            )
            DetailRow(
                icon = painterResource(id = R.drawable.ic_schedule),
                label = "Frequency",
                value = streak.goalFrequency.toString()
            )
            DetailRow(
                icon = painterResource(id = R.drawable.ic_flag),
                label = "Target Days",
                value = streak.targetDays.toString()
            )
            DetailRow(
                icon = painterResource(id = R.drawable.ic_calendar_today),
                label = "Started On",
                value = streak.startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            )
            if (streak.targetEndDate != null) {
                DetailRow(
                    icon = painterResource(id = R.drawable.ic_today),
                    label = "Target End",
                    value = streak.targetEndDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: Painter,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ... [QuotesCard and NotesCard implementations remain similar but with updated styling]

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
private fun NotesCard(notes: List<String>) {
    var expandedNoteIndex by remember { mutableStateOf<Int?>(null) }

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
                    painter = painterResource(id = R.drawable.ic_notes),
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            notes.forEachIndexed { index, note ->
                NoteItem(
                    note = note,
                    date = "Note ${index + 1}",  // You might want to add actual dates to your notes
                    isExpanded = expandedNoteIndex == index,
                    onToggle = {
                        expandedNoteIndex = if (expandedNoteIndex == index) null else index
                    }
                )

                if (index < notes.size - 1) {
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
private fun NoteItem(
    note: String,
    date: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onToggle) {
                Icon(
                    painter = if (isExpanded) painterResource(id = R.drawable.ic_expand_less) else painterResource(
                        id = R.drawable.ic_expand_more
                    ),
                    contentDescription = if (isExpanded) "Show less" else "Show more"
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (!isExpanded) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val containerColor = if (isActive) {
        colorScheme.primary.copy(alpha = 0.1f)
    } else {
        colorScheme.error.copy(alpha = 0.1f)
    }

    val contentColor = if (isActive) {
        colorScheme.primary
    } else {
        colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = if (isActive) painterResource(id = R.drawable.ic_check_circle) else painterResource(id = R.drawable.ic_cancel),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isActive) "Active" else "Broken",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ActivityLogItem(
    date: java.time.LocalDate,
    dateFormatter: DateTimeFormatter
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = date.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Streak maintained",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Helper functions for date formatting
private fun getRelativeTimeSpan(date: java.time.LocalDate): String {
    val today = java.time.LocalDate.now()
    val days = java.time.temporal.ChronoUnit.DAYS.between(date, today)

    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7L -> "$days days ago"
        days < 30L -> "${days / 7} weeks ago"
        days < 365L -> "${days / 30} months ago"
        else -> "${days / 365} years ago"
    }
}