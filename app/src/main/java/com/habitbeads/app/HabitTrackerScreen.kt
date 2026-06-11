package com.habitbeads.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habitbeads.app.data.DatabaseProvider
import com.habitbeads.app.data.HabitRepository
import kotlinx.coroutines.launch

private val AddButtonBlue = Color(0xFF3F6DDB)
private val MinLandscapeCellSize = 54.dp
private val LandscapeHabitColumnWidth = 188.dp

@Composable
fun HabitTrackerScreen(
    themeChoice: AppThemeChoice,
    onThemeChoiceChange: (AppThemeChoice) -> Unit,
    showBeadNumbers: Boolean,
    onShowBeadNumbersChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) {
        val database = DatabaseProvider.getDatabase(context)
        HabitRepository(database.habitDao(), database.habitEntryDao())
    }
    var isLoaded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    val habits = remember { mutableStateListOf<Habit>() }
    val counts = remember { mutableStateMapOf<String, Int>() }
    val days = remember { recentDays() }
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val habitColorOptions = remember(themeChoice) { habitColorsForTheme(themeChoice) }

    suspend fun reloadFromRoom() {
        habits.clear()
        habits.addAll(repository.loadHabits())
        counts.clear()
        counts.putAll(repository.loadCounts())
        isLoaded = true
    }

    LaunchedEffect(repository) { reloadFromRoom() }

    fun moveHabit(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in habits.indices || toIndex !in habits.indices) return
        val habit = habits.removeAt(fromIndex)
        habits.add(toIndex, habit)
        scope.launch {
            repository.saveHabitOrder(habits.toList())
            HabitWidgetUpdater.updateAll(context)
        }
    }

    fun deleteHabit(habit: Habit) {
        habits.removeAll { it.id == habit.id }
        val prefix = "${habit.id}:"
        counts.keys.filter { it.startsWith(prefix) }.forEach { counts.remove(it) }
        scope.launch {
            repository.archiveHabit(habit.id)
            HabitWidgetUpdater.updateAll(context)
        }
    }

    fun resetAllData() {
        scope.launch {
            isLoaded = false
            repository.resetToDefaults()
            reloadFromRoom()
            HabitWidgetUpdater.updateAll(context)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isLandscape = maxWidth > maxHeight
        val horizontalPadding = if (isLandscape) 12.dp else 16.dp
        val topPadding = if (isLandscape) 18.dp else 72.dp
        val contentMaxWidth = maxWidth

        LaunchedEffect(isLoaded, habits.size, isLandscape) {
            if (isLoaded && habits.isNotEmpty() && !isLandscape) {
                withFrameNanos { }
                withFrameNanos { }
                horizontalScrollState.scrollTo(horizontalScrollState.maxValue)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = contentMaxWidth)
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = horizontalPadding, end = horizontalPadding, top = topPadding, bottom = 18.dp)
        ) {
            TrackerTopBar(
                onOptions = { showOptionsDialog = true },
                onAddHabit = { showAddDialog = true }
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 18.dp))

            when {
                !isLoaded -> LoadingHabitState()
                habits.isEmpty() -> EmptyHabitState(onAddHabit = { showAddDialog = true })
                else -> PortraitTrackerCard(
                    habits = habits,
                    days = days,
                    counts = counts,
                    isLandscape = isLandscape,
                    showBeadNumbers = showBeadNumbers,
                    horizontalScrollState = horizontalScrollState,
                    verticalScrollState = verticalScrollState,
                    onEditHabit = { habitToEdit = it },
                    onMoveHabit = ::moveHabit,
                    onIncrement = { habit, day, count ->
                        val key = "${habit.id}:${day.dateKey}"
                        val next = (count + 1).coerceAtMost(9)
                        counts[key] = next
                        scope.launch {
                            repository.saveCount(habit.id, day.dateKey, next)
                            HabitWidgetUpdater.updateAll(context)
                        }
                    },
                    onDecrement = { habit, day, count ->
                        val key = "${habit.id}:${day.dateKey}"
                        val next = (count - 1).coerceAtLeast(0)
                        if (next == 0) counts.remove(key) else counts[key] = next
                        scope.launch {
                            repository.saveCount(habit.id, day.dateKey, next)
                            HabitWidgetUpdater.updateAll(context)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 18.dp else 30.dp))
            Text(
                "Your data is stored locally on this device.",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6D7079)
            )
        }
    }

    if (showAddDialog) {
        HabitEditorDialog(
            title = "Add habit",
            initialHabit = null,
            colorOptions = habitColorOptions,
            confirmText = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, subtitle, color ->
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) {
                    scope.launch {
                        val added = repository.addHabit(trimmed, subtitle.trim(), color, habits.size)
                        habits.add(added)
                        HabitWidgetUpdater.updateAll(context)
                    }
                }
                showAddDialog = false
            },
            onRequestDelete = null
        )
    }

    habitToEdit?.let { habit ->
        HabitEditorDialog(
            title = "Edit habit",
            initialHabit = habit,
            colorOptions = habitColorOptions,
            confirmText = "Save",
            onDismiss = { habitToEdit = null },
            onConfirm = { name, subtitle, color ->
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) {
                    val index = habits.indexOfFirst { it.id == habit.id }
                    if (index >= 0) {
                        val updated = habit.copy(name = trimmed, subtitle = subtitle.trim(), color = color)
                        habits[index] = updated
                        scope.launch {
                            repository.updateHabit(updated, index)
                            HabitWidgetUpdater.updateAll(context)
                        }
                    }
                }
                habitToEdit = null
            },
            onRequestDelete = {
                habitToEdit = null
                habitToDelete = habit
            }
        )
    }

    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete habit?") },
            text = { Text("Delete ${habit.name} and its saved bead history? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteHabit(habit)
                    habitToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { OutlinedButton(onClick = { habitToDelete = null }) { Text("Cancel") } }
        )
    }

    if (showOptionsDialog) {
        OptionsDialog(
            themeChoice = themeChoice,
            onThemeChoiceChange = onThemeChoiceChange,
            showBeadNumbers = showBeadNumbers,
            onShowBeadNumbersChange = onShowBeadNumbersChange,
            onReset = {
                showOptionsDialog = false
                showResetDialog = true
            },
            onDismiss = { showOptionsDialog = false }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all data?") },
            text = { Text("This will restore the sample habits and delete all saved bead history.") },
            confirmButton = { TextButton(onClick = { resetAllData(); showResetDialog = false }) { Text("Reset") } },
            dismissButton = { OutlinedButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun TrackerTopBar(onOptions: () -> Unit, onAddHabit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Habit Beads",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF161820)
            )
            Text(
                "Tap to add a bead. Hold to remove one.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF777A83),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOptions, modifier = Modifier.size(48.dp)) {
                Icon(
                    painterResource(R.drawable.ic_options_24),
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                onClick = onAddHabit,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = AddButtonBlue,
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_add_24), contentDescription = "Add habit", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PortraitTrackerCard(
    habits: List<Habit>,
    days: List<DayInfo>,
    counts: Map<String, Int>,
    isLandscape: Boolean,
    showBeadNumbers: Boolean,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    verticalScrollState: androidx.compose.foundation.ScrollState,
    onEditHabit: (Habit) -> Unit,
    onMoveHabit: (Int, Int) -> Unit,
    onIncrement: (Habit, DayInfo, Int) -> Unit,
    onDecrement: (Habit, DayInfo, Int) -> Unit
) {
    val habitColumnWidth = if (isLandscape) LandscapeHabitColumnWidth else HabitColumnWidth

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val availableGridWidth = maxWidth - habitColumnWidth
            val fitsLandscape = isLandscape && availableGridWidth >= MinLandscapeCellSize * days.size.toFloat()
            val gridCellSize = when {
                fitsLandscape -> availableGridWidth / days.size.toFloat()
                isLandscape -> MinLandscapeCellSize
                else -> CellSize
            }
            val tableMaxHeight = if (isLandscape) 260.dp else 430.dp

            Box(
                modifier = Modifier
                    .heightIn(max = tableMaxHeight)
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
            ) {
                Row {
                    Column(
                        modifier = Modifier
                            .width(habitColumnWidth)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    ) {
                        Spacer(modifier = Modifier.height(44.dp))
                        habits.forEachIndexed { index, habit ->
                            HabitNameCell(
                                habit = habit,
                                canMoveUp = index > 0,
                                canMoveDown = index < habits.lastIndex,
                                rowHeight = gridCellSize,
                                onEdit = { onEditHabit(habit) },
                                onMoveUp = { onMoveHabit(index, index - 1) },
                                onMoveDown = { onMoveHabit(index, index + 1) }
                            )
                        }
                    }

                    days.forEach { day ->
                        Column(
                            modifier = Modifier
                                .width(gridCellSize)
                                .background(
                                    if (day.isToday) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                        ) {
                            DayHeader(day, cellSize = gridCellSize)
                            habits.forEach { habit ->
                                val key = "${habit.id}:${day.dateKey}"
                                val count = counts[key] ?: 0
                                BeadCell(
                                    habitName = habit.name,
                                    day = day,
                                    count = count,
                                    color = habit.color,
                                    isToday = day.isToday,
                                    showNumber = showBeadNumbers,
                                    cellSize = gridCellSize,
                                    onIncrement = { onIncrement(habit, day, count) },
                                    onDecrement = { onDecrement(habit, day, count) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionsDialog(
    themeChoice: AppThemeChoice,
    onThemeChoiceChange: (AppThemeChoice) -> Unit,
    showBeadNumbers: Boolean,
    onShowBeadNumbersChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Color palette", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeChoice.values().forEach { choice ->
                        if (choice == themeChoice) {
                            Button(onClick = { onThemeChoiceChange(choice) }) { Text(choice.label) }
                        } else {
                            OutlinedButton(onClick = { onThemeChoiceChange(choice) }) { Text(choice.label) }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bead numbers", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Show counts inside filled beads", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = showBeadNumbers, onCheckedChange = onShowBeadNumbersChange)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Build", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("v0.1.0 portrait debug", style = MaterialTheme.typography.bodySmall)
                    Text("Storage: local Room database", style = MaterialTheme.typography.bodySmall)
                    Text("Long press a habit row and drag to reorder.", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = onReset) { Text("Reset sample data") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}
