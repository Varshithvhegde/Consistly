package com.varshith.consistly.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.varshith.consistly.R
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.ui.components.StreakCard
import com.varshith.consistly.viewmodels.StreakViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun HomeScreen(
    viewModel: StreakViewModel,
    onAddStreak: () -> Unit,
    onStreakClick: (String) -> Unit
) {
    // State management
    val streaks by viewModel.streaks.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()

    // Optimized scroll state calculation with suspension
    val isScrolled by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0
        }
    }

    // Initial loading state
    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isInitialLoad = false
    }

    // Content fade-in animation
    val contentAlpha by animateFloatAsState(
        targetValue = if (isInitialLoad) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "Content Fade"
    )

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .graphicsLayer { alpha = contentAlpha },
        topBar = {
            LargeTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isScrolled,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with
                                    fadeOut(animationSpec = tween(300))
                        },
                        label = "Title Animation"
                    ) { scrolled ->
                        Column {
                            Text(
                                text = if (scrolled) "Consistly" else "Your Consistency Journey",
                                style = if (scrolled)
                                    MaterialTheme.typography.titleLarge
                                else
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                            )
                            if (!scrolled) {
                                Text(
                                    text = "Track your daily progress",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { /* Dashboard action */ }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dashboard),
                            contentDescription = "Dashboard",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        // Smooth scroll to top when adding new streak
                        lazyListState.animateScrollToItem(0)
                        onAddStreak()
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Streak"
                    )
                },
                text = { Text("New Streak") },
                expanded = !isScrolled,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CrossfadeContent(
                targetState = streaks.isEmpty(),
                onAddStreak = onAddStreak,
                streaks = streaks,
                lazyListState = lazyListState,
                viewModel = viewModel,
                onStreakClick = onStreakClick
            )
        }
    }
}

@Composable
private fun CrossfadeContent(
    targetState: Boolean,
    onAddStreak: () -> Unit,
    streaks: List<StreakEntity>,
    lazyListState: LazyListState,
    viewModel: StreakViewModel,
    onStreakClick: (String) -> Unit
) {
    Crossfade(
        targetState = targetState,
        animationSpec = tween(durationMillis = 400),
        label = "Content Crossfade"
    ) { isEmpty ->
        if (isEmpty) {
            EmptyStateView(onAddStreak)
        } else {
            StreakList(
                streaks = streaks,
                lazyListState = lazyListState,
                viewModel = viewModel,
                onStreakClick = onStreakClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StreakList(
    streaks: List<StreakEntity>,
    lazyListState: LazyListState,
    viewModel: StreakViewModel,
    onStreakClick: (String) -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = streaks,
            key = { it.hashCode() }
        ) { streak ->
            StreakCard(
                streak = streak,
                modifier = Modifier
                    .animateItemPlacement(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                onCardClick = { onStreakClick(streak.id) },
                onLogDay = { viewModel.logStreakDay(streak.id) },
                onBreakStreak = { viewModel.breakStreak(streak.id) },
                onDeleteStreak = {
                    viewModel.deleteStreak(streak.id, streak.startDate, streak.endDate)
                },
                onEditStreak = { /* Implement edit functionality */ }
            )
        }
    }
}

@Composable
private fun EmptyStateView(onAddStreak: () -> Unit) {
    // Create animation values within the Composable context
    val infiniteTransition = rememberInfiniteTransition(label = "Empty State Animation")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Floating Animation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .graphicsLayer {
                    // Apply the animated offset
                    translationY = offsetY
                }
        ) {
            // Create a pulsing effect for the icon
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Scale Animation"
            )

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Empty Streak",
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Animate the text appearance
            val textAlpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Text Alpha Animation"
            )

            Text(
                text = "Start Your Journey",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )

            Text(
                text = "Create your first streak and begin building habits",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            )

            // Add a slight bounce effect to the button
            val buttonScale by remember {
                mutableStateOf(Animatable(1f))
            }

            LaunchedEffect(Unit) {
                // Create a subtle continuous bounce effect
                while (true) {
                    buttonScale.animateTo(
                        targetValue = 1.02f,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )
                    buttonScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )
                }
            }

            Button(
                onClick = onAddStreak,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .graphicsLayer {
                        scaleX = buttonScale.value
                        scaleY = buttonScale.value
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Streak")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create First Streak")
            }
        }
    }
}