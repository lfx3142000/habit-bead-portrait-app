package com.habitbeads.app.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.habitbeads.app.Habit
import com.habitbeads.app.defaultHabits

class HabitRepository(
    private val habitDao: HabitDao,
    private val entryDao: HabitEntryDao
) {
    suspend fun loadHabits(): List<Habit> {
        val habits = habitDao.getActiveHabits().map { it.toHabit() }
        if (habits.isNotEmpty()) return habits

        val defaults = defaultHabits()
        defaults.forEachIndexed { index, habit ->
            habitDao.insertHabit(habit.toEntity(displayOrder = index))
        }
        return habitDao.getActiveHabits().map { it.toHabit() }
    }

    suspend fun loadCounts(): Map<String, Int> {
        return entryDao.getAllEntries()
            .filter { it.count > 0 }
            .associate { "${it.habitId}:${it.dateKey}" to it.count }
    }

    suspend fun addHabit(name: String, subtitle: String, color: Color, displayOrder: Int): Habit {
        val now = System.currentTimeMillis()
        val id = habitDao.insertHabit(
            HabitEntity(
                name = name,
                subtitle = subtitle,
                colorArgb = color.toArgb(),
                target = 1,
                displayOrder = displayOrder,
                isArchived = false,
                createdAt = now,
                updatedAt = now
            )
        ).toInt()
        return Habit(id = id, name = name, subtitle = subtitle, color = color, target = 1)
    }

    suspend fun updateHabit(habit: Habit, displayOrder: Int) {
        val existing = habitDao.getHabitById(habit.id)
        val now = System.currentTimeMillis()
        habitDao.updateHabit(
            HabitEntity(
                id = habit.id,
                name = habit.name,
                subtitle = habit.subtitle,
                colorArgb = habit.color.toArgb(),
                target = habit.target,
                displayOrder = displayOrder,
                isArchived = existing?.isArchived ?: false,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )
    }

    suspend fun saveHabitOrder(habits: List<Habit>) {
        habits.forEachIndexed { index, habit -> updateHabit(habit, index) }
    }

    suspend fun archiveHabit(habitId: Int) {
        habitDao.archiveHabit(habitId, System.currentTimeMillis())
        entryDao.clearEntriesForHabit(habitId)
    }

    suspend fun saveCount(habitId: Int, dateKey: String, count: Int) {
        val normalized = count.coerceIn(0, 9)
        if (normalized == 0) {
            entryDao.clearEntry(habitId, dateKey)
            return
        }
        entryDao.upsertEntry(
            HabitEntryEntity(
                habitId = habitId,
                dateKey = dateKey,
                count = normalized,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetToDefaults() {
        entryDao.clearEntries()
        habitDao.clearHabits()
        defaultHabits().forEachIndexed { index, habit ->
            habitDao.insertHabit(habit.toEntity(displayOrder = index))
        }
    }
}

private fun HabitEntity.toHabit(): Habit {
    return Habit(
        id = id,
        name = name,
        subtitle = subtitle,
        color = Color(colorArgb),
        target = target
    )
}

private fun Habit.toEntity(displayOrder: Int): HabitEntity {
    val now = System.currentTimeMillis()
    return HabitEntity(
        id = id,
        name = name,
        subtitle = subtitle,
        colorArgb = color.toArgb(),
        target = target,
        displayOrder = displayOrder,
        isArchived = false,
        createdAt = now,
        updatedAt = now
    )
}
