package com.habitbeads.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HabitEntryDao {
    @Query("SELECT * FROM habit_entries")
    suspend fun getAllEntries(): List<HabitEntryEntity>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND dateKey = :dateKey LIMIT 1")
    suspend fun getEntry(habitId: Int, dateKey: String): HabitEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: HabitEntryEntity)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun clearEntry(habitId: Int, dateKey: String)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId")
    suspend fun clearEntriesForHabit(habitId: Int)

    @Query("DELETE FROM habit_entries")
    suspend fun clearEntries()
}
