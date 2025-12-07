package com.example.studybuddy.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studybuddy.network.CalendarEvent
import com.example.studybuddy.network.SubjectDetails
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

//https://www.youtube.com/watch?v=Ba0Q-cK1fJo
//https://www.geeksforgeeks.org/android/android-creating-a-calendar-view-app/
@Composable
fun CalendarView(
    yearMonth: YearMonth,
    events: List<CalendarEvent>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onEventSelected: (CalendarEvent) -> Unit, 
    subjectDetailsMap: Map<String, SubjectDetails>,
    classSchedule: Map<String, SubjectDetails>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        MonthHeader(yearMonth, onPreviousMonth, onNextMonth)
        DayOfWeekHeader()
        CalendarGrid(yearMonth, events, onDateSelected, onEventSelected, subjectDetailsMap, classSchedule)
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
    onDateSelected: (LocalDate) -> Unit,
    onEventSelected: (CalendarEvent) -> Unit, 
    subjectDetailsMap: Map<String, SubjectDetails>,
    classSchedule: Map<String, SubjectDetails>
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
                try {
                    LocalDateTime.parse(it.start_time, DateTimeFormatter.ISO_DATE_TIME).toLocalDate() == date
                } catch (e: DateTimeParseException) {
                    Log.e("CalendarView", "Failed to parse event start_time: ${it.start_time}", e)
                    false
                }
            }
            val classesForDay = classSchedule.filter { (_, details) -> details.schedule.any { it.day == date.dayOfWeek } }

            DayCell(day, eventsForDay, classesForDay, { onDateSelected(date) }, onEventSelected, subjectDetailsMap)
        }
    }
}

@Composable
private fun DayCell(
    day: Int, 
    events: List<CalendarEvent>, 
    classes: Map<String, SubjectDetails>,
    onDateSelected: () -> Unit, 
    onEventSelected: (CalendarEvent) -> Unit, 
    subjectDetailsMap: Map<String, SubjectDetails>
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 120.dp)
            .border(BorderStroke(0.5.dp, Color.LightGray))
            .clickable { onDateSelected() }
            .padding(4.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Text(text = day.toString())
            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                classes.values.forEach { details ->
                    val color = try { Color(android.graphics.Color.parseColor(details.color)) } catch (e: Exception) { Color.Gray }
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val parsableEvents = events.mapNotNull { event ->
                try {
                    val startTime = LocalDateTime.parse(event.start_time, DateTimeFormatter.ISO_DATE_TIME)
                    Pair(event, startTime)
                } catch (e: DateTimeParseException) {
                    Log.e("CalendarView", "Skipping event with unparsable start_time: ${event.start_time}", e)
                    null
                }
            }

            parsableEvents.take(2).forEach { (event, startTime) ->
                val formattedTime = startTime.format(timeFormatter)
                val baseModifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).clickable { onEventSelected(event) }

                if (event.event_type == "TASK_DUE_DATE") {
                    val colorString = subjectDetailsMap[event.subject]?.color ?: "#888888"
                    val subjectColor = try { Color(android.graphics.Color.parseColor(colorString)) } catch (e: Exception) { Color.Gray }
                    Box(
                        modifier = baseModifier
                            .background(subjectColor.copy(alpha = 0.5f))
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "${event.title} DUE AT: $formattedTime",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Box(modifier = baseModifier.padding(2.dp)) {
                        Text(
                            text = "${event.title} at $formattedTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (events.size > 2) {
                Text("+ ${events.size - 2} more", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}