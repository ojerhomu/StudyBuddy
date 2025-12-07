package com.example.studybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            CalendarView(
                yearMonth = currentYearMonth,
                events = emptyList(), // dont show events in the picker
                onPreviousMonth = { currentYearMonth = currentYearMonth.minusMonths(1) },
                onNextMonth = { currentYearMonth = currentYearMonth.plusMonths(1) },
                onDateSelected = { date ->
                    onDateSelected(date)
                    onDismissRequest() // close the dialog on selection
                },
                onEventSelected = {},
                subjectDetailsMap = emptyMap(),
                classSchedule = emptyMap() // DO THIS: add the missing parameter here
            )
        }
    }
}
