package com.habitbeads.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, HabitEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HabitBeadsDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao
}
