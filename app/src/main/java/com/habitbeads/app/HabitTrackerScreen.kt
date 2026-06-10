package com.habitbeads.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.habitbeads.app.data.DatabaseProvider
import com.habitbeads.app.data.HabitRepository
import kotlinx.coroutines.launch

private val PortraitBackground = Color(0xFFFCFAF7)
private val CardBorder = Color(0xFFE5E1DA)
private val GridLine = Color(0xFFECE8E1)
private val TodayHighlight = Color(0xFFFFF1C9)
private val AddButtonBlue = Color(0xFF3F6DDB)

@Composable
fun HabitTrackerScreen(themeChoice: AppThemeChoice, onThemeChoiceChange: (AppThemeChoice) -> Unit) {
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
        scope.launch { repository.saveHabitOrder(habits.toList()) }
    }

    fun deleteHabit(habit: Habit) {
        habits.removeAll { it.id == habit.id }
        val prefix = "${habit.id}:"
        counts.keys.filter { it.startsWith(prefix) }.forEach { counts.remove(it) }
        scope.launch { repository.archiveHabit(habit.id) }
    }

    fun resetAllData() {
        scope.launch {
            isLoaded = false
            repository.resetToDefaults()
            reloadFromRoom()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PortraitBackground)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 18.dp)
    ) {
        TrackerTopBar(
            onOptions = { showOptionsDialog = true },
            onAddHabit = { showAddDialog = true }
        )

        Spacer(modifier = Modifier.height(18.dp))

        when {
            !isLoaded -> LoadingHabitState()
            habits.isEmpty() -> EmptyHabitState(onAddHabit = { showAddDialog = true })
            else -> PortraitTrackerCard(
                habits = habits,
                days = days,
                counts = counts,
                horizontalScrollState = horizontalScrollState,
                onEditHabit = { habitToEdit = it },
                onMoveHabit = ::moveHabit,
                onIncrement = { habit, day, count ->
                    val key = "${habit.id}:${day.dateKey}"
                    val next = (count + 1).coerceAtMost(9)
                    counts[key] = next
                    scope.launch { repository.saveCount(habit.id, day.dateKey, next) }
                },
                onDecrement = { habit, day, count ->
                    val key = "${habit.id}:${day.dateKey}"
                    val next = (count - 1).coerceAtLeast(0)
                    if (next == 0) counts.remove(key) else counts[key] = next
                    scope.launch { repository.saveCount(habit.id, day.dateKey, next) }
                }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text(
            "Your data is stored locally on this device.",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6D7079)
        )
    }

    if (showAddDialog) {
        HabitEditorDialog(
            title = "Add habit",
            initialHabit = null,
            confirmText = "Add",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, subtitle, color ->
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) {
                    scope.launch {
                        val added = repository.addHabit(trimmed, subtitle.trim(), color, habits.size)
                        habits.add(added)
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
            confirmText = "Save",
            onDismiss = { habitToEdit = null },
            onConfirm = { name, subtitle, color ->
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) {
                    val index = habits.indexOfFirst { it.id == habit.id }
                    if (index >= 0) {
                        val updated = habit.copy(name = trimmed, subtitle = subtitle.trim(), color = color)
                        habits[index] = updated
                        scope.launch { repository.updateHabit(updated, index) }
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
                Icon(painterResource(R.drawable.ic_refresh_24), contentDescription = "Options")
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
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onEditHabit: (Habit) -> Unit,
    onMoveHabit: (Int, Int) -> Unit,
    onIncrement: (Habit, DayInfo, Int) -> Unit,
    onDecrement: (Habit, DayInfo, Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row {
            Column(
                modifier = Modifier
                    .width(HabitColumnWidth)
                    .border(width = 0.5.dp, color = GridLine)
            ) {
                Spacer(modifier = Modifier.height(44.dp))
                habits.forEachIndexed { index, habit ->
                    HabitNameCell(
                        habit = habit,
                        canMoveUp = index > 0,
                        canMoveDown = index < habits.lastIndex,
                        onEdit = { onEditHabit(habit) },
                        onMoveUp = { onMoveHabit(index, index - 1) },
                        onMoveDown = { onMoveHabit(index, index + 1) }
                    )
                }
            }

            Row(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                days.forEach { day ->
                    Column(
                        modifier = Modifier
                            .width(CellSize)
                            .background(if (day.isToday) TodayHighlight else Color.Transparent)
                    ) {
                        DayHeader(day)
                        habits.forEach { habit ->
                            val key = "${habit.id}:${day.dateKey}"
                            val count = counts[key] ?: 0
                            BeadCell(
                                habitName = habit.name,
                                day = day,
                                count = count,
                                color = habit.color,
                                isToday = day.isToday,
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

@Composable
private fun OptionsDialog(
    themeChoice: AppThemeChoice,
    onThemeChoiceChange: (AppThemeChoice) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeChoice.values().forEach { choice ->
                        if (choice == themeChoice) {
                            Button(onClick = { onThemeChoiceChange(choice) }) { Text(choice.label) }
                        } else {
                            OutlinedButton(onClick = { onThemeChoiceChange(choice) }) { Text(choice.label) }
                        }
                    }
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
