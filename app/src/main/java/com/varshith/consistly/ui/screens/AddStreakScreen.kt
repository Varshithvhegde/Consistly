package com.varshith.consistly.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.varshith.consistly.data.repositories.GoalFrequency
import com.varshith.consistly.viewmodels.StreakViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.varshith.consistly.R
import com.varshith.consistly.ui.components.CalendarDatePicker
import com.varshith.consistly.ui.components.TimePickerDialog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStreakScreen(
    viewModel: StreakViewModel,
    onNavigateBack: () -> Unit
) {
    var streakName by remember { mutableStateOf("") }
    var streakDescription by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#FF4081") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var targetDays by remember { mutableStateOf("1") }
    var goalFrequency by remember { mutableStateOf(GoalFrequency.DAILY) }
    var reminderTime by remember { mutableStateOf<LocalTime?>(null) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var showNameError by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val categories = listOf(
        "Health" to painterResource(id= R.drawable.ic_favorite),
        "Fitness" to painterResource(id = R.drawable.ic_directional_run),
        "Learning" to painterResource(id =R.drawable.ic_school),
        "Productivity" to painterResource(id =R.drawable.ic_task_alt),
        "Mindfulness" to painterResource(id =R.drawable.ic_self_improvement),
        "Other" to painterResource(id = R.drawable.ic_category)
    )

    // Time Picker State
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { time ->
                reminderTime = time  // This should now work correctly
                showTimePicker = false
            },
            initialTime = reminderTime ?: LocalTime.now()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Streak") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Navigate Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedHeader()

            // Basic Information Section
            OutlinedTextField(
                value = streakName,
                onValueChange = {
                    streakName = it
                    showNameError = false
                },
                label = { Text("Streak Name") },
                placeholder = { Text("e.g., Daily Exercise") },
                supportingText = {
                    AnimatedVisibility(visible = showNameError) {
                        Text(
                            text = "Name cannot be empty",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                isError = showNameError,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Edit, "Name") },
                trailingIcon = {
                    if (streakName.isNotEmpty()) {
                        IconButton(onClick = { streakName = "" }) {
                            Icon(Icons.Default.Close, "Clear name")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = streakDescription,
                onValueChange = { streakDescription = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Add details about your streak") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                leadingIcon = { Icon(painter = painterResource(R.drawable.ic_description), "Description") },
                trailingIcon = {
                    if (streakDescription.isNotEmpty()) {
                        IconButton(onClick = { streakDescription = "" }) {
                            Icon(Icons.Default.Close, "Clear description")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Category Selection
            CategorySelector(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                categories = categories
            )

            // Frequency Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Goal Frequency",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SegmentedButtons(
                        items = GoalFrequency.values().map { it.name },
                        selectedItem = goalFrequency.name,
                        onItemSelected = { goalFrequency = GoalFrequency.valueOf(it) }
                    )
                }
            }

            // Target Days Input
            OutlinedTextField(
                value = targetDays,
                onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) targetDays = it },
                label = { Text("Target Days") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(painter = painterResource(R.drawable.ic_flag), "Target") },
                shape = RoundedCornerShape(12.dp)
            )

            // Reminder Section
            ReminderSection(
                reminderEnabled = reminderEnabled,
                onReminderEnabledChange = { reminderEnabled = it },
                reminderTime = reminderTime,
                onShowTimePicker = { showTimePicker = true }
            )

            Spacer(modifier = Modifier.weight(1f))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Date Range",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Start Date Button
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_today),
                            contentDescription = "Start Date",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Start Date: ${startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
                    }

                    // End Date Button
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_today),
                            contentDescription = "End Date",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("End Date: ${endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
                    }
                }
            }
            // Create Button
            Button(
                onClick = {
                    if (streakName.isBlank()) {
                        showNameError = true
                        return@Button
                    }
                    viewModel.createStreak(
                        name = streakName.trim(),
                        description = streakDescription.takeIf { it.isNotBlank() }?.trim(),
                        category = selectedCategory,
                        goalFrequency = goalFrequency,
                        targetDays = targetDays.toIntOrNull() ?: 1,
                        reminderEnabled = reminderEnabled,
                        reminderTimeString = reminderTime?.format(DateTimeFormatter.ofPattern("HH:mm")),
                        startDate = startDate,
                        endDate = endDate
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Create Streak",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        if (showStartDatePicker) {
            AlertDialog(
                onDismissRequest = { showStartDatePicker = false },
                title = { Text("Select Start Date") },
                text = {
                    CalendarDatePicker(
                        selectedDate = startDate,
                        onDateSelected = { date ->
                            startDate = date
                            if (endDate.isBefore(date)) {
                                endDate = date
                            }
                            showStartDatePicker = false
                        },
                        minDate = LocalDate.now(),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEndDatePicker) {
            AlertDialog(
                onDismissRequest = { showEndDatePicker = false },
                title = { Text("Select End Date") },
                text = {
                    CalendarDatePicker(
                        selectedDate = endDate,
                        onDateSelected = { date ->
                            endDate = date
                            showEndDatePicker = false
                        },
                        minDate = startDate,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    state: TimePickerState,
    onTimeChange: (Int, Int) -> Unit
) {
    TimePicker(
        state = state,
        modifier = Modifier.padding(16.dp)
    )

    LaunchedEffect(state.hour, state.minute) {
        onTimeChange(state.hour, state.minute)
    }
}

@Composable
fun SegmentedButtons(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Button(
                onClick = { onItemSelected(item) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Transparent,
                    contentColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isSelected) 2.dp else 0.dp
                )
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}


@Composable
private fun CategorySelector(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    categories: List<Pair<String, Painter>>
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Choose Category",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories) { (category, icon) ->
                val isSelected = category == selectedCategory
                OutlinedCard(
                    modifier = Modifier
                        .animateContentSize()
                        .clickable { onCategorySelected(category) },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun ReminderSection(
    reminderEnabled: Boolean,
    onReminderEnabledChange: (Boolean) -> Unit,
    reminderTime: LocalTime?,
    onShowTimePicker: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (reminderEnabled)
                            Icons.Filled.Notifications
                        else
                            Icons.Outlined.Notifications,
                        contentDescription = "Reminder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Daily Reminder",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (reminderEnabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            AnimatedVisibility(
                visible = reminderEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedCard(
                    onClick = onShowTimePicker,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_schedule),
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = reminderTime?.format(
                                DateTimeFormatter.ofPattern("hh:mm a")
                            ) ?: "Set Reminder Time",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedHeader() {
    var isAnimated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAnimated = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer {
                alpha = if (isAnimated) 1f else 0f
                translationY = if (isAnimated) 0f else 50f
            }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Start Your New Streak",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Create a new habit and track your progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_task_alt),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}