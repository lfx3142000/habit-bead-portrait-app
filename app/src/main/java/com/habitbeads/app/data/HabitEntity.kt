package com.habitbeads.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val subtitle: String,
    val colorArgb: Int,
    val target: Int,
    val displayOrder: Int,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
