package com.varshith.consistly.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    initialTime: LocalTime = LocalTime.now()
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .background(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val timePickerState = rememberTimePickerState(
                    initialHour = initialTime.hour,
                    initialMinute = initialTime.minute
                )

                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val newTime = LocalTime.of(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onTimeSelected(newTime)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}