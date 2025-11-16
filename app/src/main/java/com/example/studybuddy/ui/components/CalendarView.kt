package com.example.studybuddy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        MonthHeader(yearMonth, onPreviousMonth, onNextMonth)
        DayOfWeekHeader()
        CalendarGrid(yearMonth = yearMonth, events = events, onDateSelected = onDateSelected)
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
        }
        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
            style = MaterialTheme.typography.headlineSmall
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    val daysOfWeek = DayOfWeek.values().map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek
    val monthOffset = (firstDayOfMonth.value % 7)
    val numberOfDays = yearMonth.lengthOfMonth()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7)
    ) {
        items(monthOffset) {
            Box(modifier = Modifier.border(BorderStroke(0.5.dp, Color.LightGray)))
        }
        items(numberOfDays) { dayIndex ->
            val day = dayIndex + 1
            val date = yearMonth.atDay(day)
            val eventsForDay = events.filter {
                LocalDateTime.parse(it.start_time, DateTimeFormatter.ISO_DATE_TIME).toLocalDate() == date
            }
            DayCell(day = day, events = eventsForDay, onDateSelected = { onDateSelected(date) })
        }
    }
}

@Composable
private fun DayCell(day: Int, events: List<CalendarEvent>, onDateSelected: () -> Unit) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 100.dp)
            .border(BorderStroke(0.5.dp, Color.LightGray))
            .clickable { onDateSelected() }
            .padding(4.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Text(text = day.toString())
            events.forEach { event ->
                val startTime = LocalDateTime.parse(event.start_time, DateTimeFormatter.ISO_DATE_TIME)
                val formattedTime = startTime.format(timeFormatter)
                val displayText = if (event.event_type == "TASK_DUE_DATE") {
                    "${event.title} DUE AT: $formattedTime"
                } else {
                    "${event.title} at $formattedTime"
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (event.event_type == "TASK_DUE_DATE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}