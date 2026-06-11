package com.habitbeads.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.habitbeads.app.data.DatabaseProvider
import com.habitbeads.app.data.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val WidgetActionAdd = "com.habitbeads.portrait.widget.ADD"
private const val WidgetExtraHabitId = "habit_id"

class TodayHabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        HabitWidgetUpdater.updateToday(context, manager, appWidgetIds)
    }
}

class SingleHabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        HabitWidgetUpdater.updateSingleHabit(context, manager, appWidgetIds)
    }
}

class WeeklyStripWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        HabitWidgetUpdater.updateWeeklyStrip(context, manager, appWidgetIds)
    }
}

class HabitWidgetActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WidgetActionAdd) return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val habitId = intent.getIntExtra(WidgetExtraHabitId, -1)
            if (habitId > 0) {
                val repository = repository(context)
                val todayKey = todayKey()
                val count = repository.loadCounts()["$habitId:$todayKey"] ?: 0
                repository.saveCount(habitId, todayKey, (count + 1).coerceAtMost(9))
            }
            HabitWidgetUpdater.updateAll(context)
            result.finish()
        }
    }
}

object HabitWidgetUpdater {
    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        updateToday(
            context,
            manager,
            manager.getAppWidgetIds(ComponentName(context, TodayHabitWidgetProvider::class.java))
        )
        updateSingleHabit(
            context,
            manager,
            manager.getAppWidgetIds(ComponentName(context, SingleHabitWidgetProvider::class.java))
        )
        updateWeeklyStrip(
            context,
            manager,
            manager.getAppWidgetIds(ComponentName(context, WeeklyStripWidgetProvider::class.java))
        )
    }

    fun updateToday(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            val data = loadWidgetData(context)
            widgetIds.forEach { id ->
                manager.updateAppWidget(id, buildTodayWidget(context, data))
            }
        }
    }

    fun updateSingleHabit(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            val data = loadWidgetData(context)
            widgetIds.forEach { id ->
                manager.updateAppWidget(id, buildSingleHabitWidget(context, data))
            }
        }
    }

    fun updateWeeklyStrip(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            val data = loadWidgetData(context)
            widgetIds.forEach { id ->
                manager.updateAppWidget(id, buildWeeklyStripWidget(context, data))
            }
        }
    }
}

private data class WidgetPalette(
    val background: Int,
    val text: Int,
    val mutedText: Int
)

private data class WidgetData(
    val habits: List<Habit>,
    val counts: Map<String, Int>,
    val days: List<DayInfo>,
    val palette: WidgetPalette
)

private suspend fun loadWidgetData(context: Context): WidgetData {
    val repository = repository(context)
    val themeChoice = AppThemeChoice.values().firstOrNull { it.name == loadSavedThemeName(context) } ?: AppThemeChoice.Warm
    return WidgetData(
        habits = repository.loadHabits(),
        counts = repository.loadCounts(),
        days = recentDays().takeLast(7),
        palette = widgetPalette(themeChoice)
    )
}

private fun buildTodayWidget(context: Context, data: WidgetData): RemoteViews {
    val views = baseWidget(context, "Today", "Tap a habit to add a bead", data.palette)
    val todayKey = todayKey()
    val habits = data.habits.take(5)
    if (habits.isEmpty()) {
        addMessageRow(context, views, "Open Habit Beads to add habits", data.palette)
    } else {
        habits.forEach { habit ->
            val count = data.counts["${habit.id}:$todayKey"] ?: 0
            addHabitRow(context, views, habit, count, data.palette, allowQuickAdd = true)
        }
    }
    return views
}

private fun buildSingleHabitWidget(context: Context, data: WidgetData): RemoteViews {
    val habit = data.habits.firstOrNull()
    val views = baseWidget(
        context,
        habit?.name ?: "Single Habit",
        habit?.subtitle?.takeIf { it.isNotBlank() } ?: "Tap to add today's bead",
        data.palette
    )
    if (habit == null) {
        addMessageRow(context, views, "Open Habit Beads to add a habit", data.palette)
    } else {
        val count = data.counts["${habit.id}:${todayKey()}"] ?: 0
        addHabitRow(context, views, habit, count, data.palette, allowQuickAdd = true)
    }
    return views
}

private fun buildWeeklyStripWidget(context: Context, data: WidgetData): RemoteViews {
    val views = baseWidget(context, "Weekly Strip", "Last 7 days", data.palette)
    val habits = data.habits.take(3)
    if (habits.isEmpty()) {
        addMessageRow(context, views, "Open Habit Beads to add habits", data.palette)
    } else {
        habits.forEach { habit ->
            val beadText = data.days.joinToString(" ") { day ->
                val count = data.counts["${habit.id}:${day.dateKey}"] ?: 0
                if (count > 0) "*" else "o"
            }
            addWeeklyRow(context, views, habit.name, beadText, habit.color.toArgb(), data.palette)
        }
    }
    return views
}

private fun baseWidget(context: Context, title: String, subtitle: String, palette: WidgetPalette): RemoteViews {
    return RemoteViews(context.packageName, R.layout.widget_habit_beads).apply {
        removeAllViews(R.id.widget_body)
        setTextViewText(R.id.widget_title, title)
        setTextViewText(R.id.widget_subtitle, subtitle)
        setTextColor(R.id.widget_title, palette.text)
        setTextColor(R.id.widget_subtitle, palette.mutedText)
        setInt(R.id.widget_root, "setBackgroundColor", palette.background)
        setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))
    }
}

private fun addHabitRow(
    context: Context,
    parent: RemoteViews,
    habit: Habit,
    count: Int,
    palette: WidgetPalette,
    allowQuickAdd: Boolean
) {
    val row = RemoteViews(context.packageName, R.layout.widget_habit_row).apply {
        setTextViewText(R.id.widget_row_name, habit.name)
        setTextViewText(R.id.widget_row_value, if (count > 0) "* $count" else "o")
        setTextColor(R.id.widget_row_name, palette.text)
        setTextColor(R.id.widget_row_value, if (count > 0) habit.color.toArgb() else palette.mutedText)
        if (allowQuickAdd) {
            setOnClickPendingIntent(R.id.widget_row_root, addBeadIntent(context, habit.id))
        }
    }
    parent.addView(R.id.widget_body, row)
}

private fun addWeeklyRow(context: Context, parent: RemoteViews, name: String, beads: String, beadColor: Int, palette: WidgetPalette) {
    val row = RemoteViews(context.packageName, R.layout.widget_habit_row).apply {
        setTextViewText(R.id.widget_row_name, name)
        setTextViewText(R.id.widget_row_value, beads)
        setTextColor(R.id.widget_row_name, palette.text)
        setTextColor(R.id.widget_row_value, beadColor)
        setOnClickPendingIntent(R.id.widget_row_root, openAppIntent(context))
    }
    parent.addView(R.id.widget_body, row)
}

private fun addMessageRow(context: Context, parent: RemoteViews, message: String, palette: WidgetPalette) {
    val row = RemoteViews(context.packageName, R.layout.widget_habit_row).apply {
        setTextViewText(R.id.widget_row_name, message)
        setTextViewText(R.id.widget_row_value, "")
        setTextColor(R.id.widget_row_name, palette.mutedText)
    }
    parent.addView(R.id.widget_body, row)
}

private fun openAppIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java)
    return PendingIntent.getActivity(
        context,
        100,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun addBeadIntent(context: Context, habitId: Int): PendingIntent {
    val intent = Intent(context, HabitWidgetActionReceiver::class.java).apply {
        action = WidgetActionAdd
        putExtra(WidgetExtraHabitId, habitId)
    }
    return PendingIntent.getBroadcast(
        context,
        200 + habitId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun repository(context: Context): HabitRepository {
    val database = DatabaseProvider.getDatabase(context.applicationContext)
    return HabitRepository(database.habitDao(), database.habitEntryDao())
}

private fun todayKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
}

private fun widgetPalette(choice: AppThemeChoice): WidgetPalette {
    return when (choice) {
        AppThemeChoice.Warm -> WidgetPalette(0xFFFFF4E8.toInt(), 0xFF241F1B.toInt(), 0xFF665A50.toInt())
        AppThemeChoice.Ocean -> WidgetPalette(0xFFEFF8FA.toInt(), 0xFF172126.toInt(), 0xFF52656D.toInt())
        AppThemeChoice.Forest -> WidgetPalette(0xFFF2F7EA.toInt(), 0xFF202217.toInt(), 0xFF59614B.toInt())
        AppThemeChoice.Grape -> WidgetPalette(0xFFF7F0FA.toInt(), 0xFF251F24.toInt(), 0xFF655A68.toInt())
    }
}
