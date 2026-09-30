package com.example.sharist.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Instant
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: Instant?,
    onValueChange: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = TimeZone.currentSystemDefault()
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }

    val datePickerState = rememberDatePickerState()

    val local = value?.toLocalDateTime(zone)
    val timePickerState = rememberTimePickerState(
        initialHour = local?.hour ?: 12,
        initialMinute = local?.minute ?: 0
    )

    val formattedValue =
        value?.toLocalDateTime(zone)?.let { dt ->
            "${dt.day.toString().padStart(2, '0')}/" +
                    "${dt.month.toString().padStart(2, '0')}/" +
                    "${dt.year} " +
                    "${dt.hour.toString().padStart(2, '0')}:" +
                    "${dt.minute.toString().padStart(2, '0')}"
        } ?: ""

    OutlinedTextField(
        value = formattedValue,
        onValueChange = {},
        readOnly = true,
        label = {
            Text(label)
        },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {

            Row {

                IconButton(
                    onClick = {
                        showDatePicker = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                }

                IconButton(
                    onClick = {
                        showTimePicker = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Select Time"
                    )
                }
            }
        }
    )

    // DATE PICKER
    if (showDatePicker) {

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        val millis =
                            datePickerState.selectedDateMillis

                        if (millis != null) {

                            val selectedDate =
                                Instant
                                    .fromEpochMilliseconds(millis)
                                    .toLocalDateTime(
                                        TimeZone.currentSystemDefault()
                                    )
                                    .date

                            val currentTime = value
                                ?.toLocalDateTime(zone)
                                ?.time
                                ?: LocalTime(12, 0)

                            val updatedLocalDateTime = LocalDateTime(
                                selectedDate,
                                currentTime
                            )
                            val updatedInstant = updatedLocalDateTime.toInstant(zone)

                            onValueChange(updatedInstant)
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {

            DatePicker(state = datePickerState)
        }
    }

    // TIME PICKER
    if (showTimePicker) {

        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        val currentLocal = value
                            ?.toLocalDateTime(zone)
                            ?: Clock.System.now()
                                .toLocalDateTime(zone)

                        val updatedLocal = LocalDateTime(
                            currentLocal.date,
                            LocalTime(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        )
                        val updatedInstant = updatedLocal.toInstant(zone)
                        onValueChange(updatedInstant)

                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            text = {

                TimePicker(
                    state = timePickerState
                )
            }
        )
    }
}