package com.example.studybuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun DurationPickerDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onDurationSet: (hours: Int, minutes: Int) -> Unit,
    showHours: Boolean = true
) {
    var selectedHours by remember { mutableIntStateOf(0) }
    var selectedMinutes by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                if (showHours) {
                    InfiniteNumberPicker(range = 0..23, onValueChange = { selectedHours = it })
                    Text("hours", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                InfiniteNumberPicker(range = 0..59, onValueChange = { selectedMinutes = it })
                Text("min", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { onDurationSet(selectedHours, selectedMinutes) }) {
                Text("Set")
            }
        }
    }
}

@Composable
private fun InfiniteNumberPicker(
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val count = range.count()
    val listState = rememberLazyListState(Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % count)
    var containerHeight by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState.isScrollInProgress, containerHeight) {
        if (!listState.isScrollInProgress && containerHeight > 0) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@LaunchedEffect

            val itemHeight = visibleItems.first().size
            if (itemHeight == 0) return@LaunchedEffect

            val viewportCenter = containerHeight / 2f

            val closestItem = visibleItems.minByOrNull { abs((it.offset + it.size / 2f) - viewportCenter) } ?: return@LaunchedEffect

            val newValue = range.first + (closestItem.index % count)
            onValueChange(newValue)

            val itemCenter = closestItem.offset + itemHeight / 2f
            val scrollAdjustment = itemCenter - viewportCenter

            if (abs(scrollAdjustment) > 0.5f) {
                coroutineScope.launch {
                    listState.animateScrollBy(scrollAdjustment)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .height(100.dp)
            .width(60.dp)
            .onSizeChanged { size -> containerHeight = size.height }
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fadingEdge(Brush.verticalGradient(0f to Color.Transparent, 0.5f to Color.Black, 1f to Color.Transparent))
        ) {
            items(Int.MAX_VALUE) { index ->
                val value = range.first + (index % count)
                Text("$value", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }
