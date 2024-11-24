package com.varshith.consistly.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.varshith.consistly.data.models.getCustomReminderDays
import com.varshith.consistly.data.models.getNotes
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.varshith.consistly.ui.components.detailstabs.ActivityTab
import com.varshith.consistly.ui.components.detailstabs.OverviewTab
import com.varshith.consistly.ui.components.detailstabs.DetailsTab
enum class StreakDetailsTab {
    OVERVIEW, ACTIVITY, DETAILS
}

// Components.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailsScreen(
    streak: StreakEntity,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(StreakDetailsTab.OVERVIEW) }
    val pagerState = rememberPagerState(initialPage = 0) { StreakDetailsTab.values().size }
    val scope = rememberCoroutineScope()

    // Remove the first LaunchedEffect as it creates a circular dependency
    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab.ordinal != pagerState.currentPage) {
            selectedTab = StreakDetailsTab.values()[pagerState.currentPage]
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = streak.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (streak.description != null) {
                            Text(
                                text = streak.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
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
                    IconButton(onClick = { /* TODO: Add share functionality */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share streak"
                        )
                    }
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
            SwipeableTabs(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    scope.launch {
                        pagerState.animateScrollToPage(
                            page = tab.ordinal,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                },
                pagerState = pagerState,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp
            ) { page ->
                when (StreakDetailsTab.values()[page]) {
                    StreakDetailsTab.OVERVIEW -> OverviewTab(streak)
                    StreakDetailsTab.ACTIVITY -> ActivityTab(
                        streak,
                        dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                    )
                    StreakDetailsTab.DETAILS -> DetailsTab(streak)
                }
            }
        }
    }
}

@Composable
fun SwipeableTabs(
    selectedTab: StreakDetailsTab,
    onTabSelected: (StreakDetailsTab) -> Unit,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal])
            )
        }
    ) {
        StreakDetailsTab.values().forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.name.capitalize(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        painter = when (tab) {
                            StreakDetailsTab.OVERVIEW -> painterResource(R.drawable.ic_dashboard)
                            StreakDetailsTab.ACTIVITY -> painterResource(R.drawable.ic_history)
                            StreakDetailsTab.DETAILS -> painterResource(R.drawable.ic_info)
                        },
                        contentDescription = null
                    )
                }
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
private fun getRelativeTimeSpan(date: LocalDate): String {
    val today = LocalDate.now()
    val days = ChronoUnit.DAYS.between(date, today)

    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7L -> "$days days ago"
        days < 30L -> "${days / 7} weeks ago"
        days < 365L -> "${days / 30} months ago"
        else -> "${days / 365} years ago"
    }
}