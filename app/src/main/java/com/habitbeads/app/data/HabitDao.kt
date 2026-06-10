package com.habitbeads.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY displayOrder ASC, id ASC")
    suspend fun getActiveHabits(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Int): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveHabit(id: Int, updatedAt: Long)

    @Query("DELETE FROM habits")
    suspend fun clearHabits()
}
