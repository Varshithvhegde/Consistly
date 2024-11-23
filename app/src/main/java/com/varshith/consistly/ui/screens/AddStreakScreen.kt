package com.varshith.consistly.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.varshith.consistly.data.repositories.GoalFrequency
import com.varshith.consistly.viewmodels.StreakViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.varshith.consistly.R
import com.varshith.consistly.ui.components.TimePickerDialog

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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Start Your New Streak",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Create a new habit and track your progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    leadingIcon = {
                        Icon(
                            painter = categories.find { it.first == selectedCategory }?.second
                                ?: painterResource(id = R.drawable.ic_category),
                            contentDescription = "Category Icon"
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    categories.forEach { (category, icon) ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                showCategoryDropdown = false
                            },
                            leadingIcon = {
                                Icon(
                                    painter = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Reminder",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Daily Reminder")
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }

                    AnimatedVisibility(
                        visible = reminderEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                               painter = painterResource(id = R.drawable.ic_schedule),
                                contentDescription = "Time",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                reminderTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                    ?: "Set Reminder Time"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                        reminderTimeString = reminderTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
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