package com.habitbeads.app.data

import androidx.room.Entity

@Entity(tableName = "habit_entries", primaryKeys = ["habitId", "dateKey"])
data class HabitEntryEntity(
    val habitId: Int,
    val dateKey: String,
    val count: Int,
    val updatedAt: Long
)
