package com.varshith.consistly.ui.components.detailstabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.varshith.consistly.R
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.data.models.getNotes
import java.time.format.DateTimeFormatter

@Composable
fun DetailsTab(streak: StreakEntity) {
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