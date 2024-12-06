package com.varshith.consistly.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varshith.consistly.R
import com.varshith.consistly.ui.components.StreakCard
import com.varshith.consistly.viewmodels.StreakViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: StreakViewModel,
    onAddStreak: () -> Unit,
    onStreakClick: (String) -> Unit
) {
    val streaks by viewModel.streaks.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyListState = rememberLazyListState()

    // Derived state to check if list is scrolled
    val isScrolled by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isScrolled,
                        label = "Title Animation"
                    ) { scrolled ->
                        if (scrolled) {
                            Text(
                                text = "Consistly",
                                style = MaterialTheme.typography.titleLarge
                            )
                        } else {
                            Text(
                                text = "Your Consistency Journey",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {

                    Icon(
                        painter = painterResource(id = R.drawable.ic_dashboard),
                        contentDescription = "Dashboard",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddStreak,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Streak"
                    )
                },
                text = { Text("New Streak") },
                expanded = !isScrolled
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = streaks.isEmpty(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            EmptyStateView(onAddStreak)
        }

        AnimatedVisibility(
            visible = streaks.isNotEmpty(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = streaks,
                    key = { it.id }
                ) { streak ->
                    StreakCard(
                        streak = streak,
                        modifier = Modifier
                            .animateItemPlacement(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                        onCardClick = { onStreakClick(streak.id) },
                        onLogDay = { viewModel.logStreakDay(streak.id) },
                        onBreakStreak = { viewModel.breakStreak(streak.id) },
                        onDeleteStreak = {
                            // Add your delete logic here
                            viewModel.deleteStreak(streak.id, streak.startDate, streak.endDate)
                        },
                        onEditStreak = {print("REached Edit")}
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(onAddStreak: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Empty Streak",
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "No streaks yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Start your consistency journey now!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Button(
                onClick = onAddStreak,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Streak")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create First Streak")
            }
        }
    }
}