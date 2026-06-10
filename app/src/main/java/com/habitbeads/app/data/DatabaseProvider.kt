package com.habitbeads.app.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var database: HabitBeadsDatabase? = null

    fun getDatabase(context: Context): HabitBeadsDatabase {
        val existing = database
        if (existing != null) return existing

        return synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                HabitBeadsDatabase::class.java,
                "habit_beads.db"
            ).build().also { database = it }
        }
    }
}
