package com.habitbeads.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
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
private const val WidgetActionSubtract = "com.habitbeads.portrait.widget.SUBTRACT"
private const val WidgetExtraHabitId = "habit_id"

class TodayHabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        HabitWidgetUpdater.updateToday(context, manager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetConfigStore.clear(context, it) }
    }
}

class SingleHabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        HabitWidgetUpdater.updateSingleHabit(context, manager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetConfigStore.clear(context, it) }
    }
}

class WeeklyStripWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        HabitWidgetUpdater.updateWeeklyStrip(context, manager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetConfigStore.clear(context, it) }
    }
}

class HabitWidgetActionReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WidgetActionAdd && intent.action != WidgetActionSubtract) return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val habitId = intent.getIntExtra(WidgetExtraHabitId, -1)
            if (habitId > 0) {
                val repository = repository(context)
                val todayKey = todayKey()
                val count = repository.loadCounts()["$habitId:$todayKey"] ?: 0
                val nextCount = if (intent.action == WidgetActionSubtract) {
                    (count - 1).coerceAtLeast(0)
                } else {
                    (count + 1).coerceAtMost(9)
                }
                repository.saveCount(habitId, todayKey, nextCount)
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
                manager.updateAppWidget(id, buildTodayWidget(context, data, id))
            }
        }
    }

    fun updateSingleHabit(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            val data = loadWidgetData(context)
            widgetIds.forEach { id ->
                manager.updateAppWidget(id, buildSingleHabitWidget(context, data, id))
            }
        }
    }

    fun updateWeeklyStrip(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        if (widgetIds.isEmpty()) return
        CoroutineScope(Dispatchers.IO).launch {
            val data = loadWidgetData(context)
            widgetIds.forEach { id ->
                manager.updateAppWidget(id, buildWeeklyStripWidget(context, data, id))
            }
        }
    }

    suspend fun updateConfiguredWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val data = loadWidgetData(context)
        val providerClassName = manager.getAppWidgetInfo(widgetId)?.provider?.className.orEmpty()
        val views = when {
            providerClassName.endsWith(TodayHabitWidgetProvider::class.java.simpleName) -> buildTodayWidget(context, data, widgetId)
            providerClassName.endsWith(SingleHabitWidgetProvider::class.java.simpleName) -> buildSingleHabitWidget(context, data, widgetId)
            providerClassName.endsWith(WeeklyStripWidgetProvider::class.java.simpleName) -> buildWeeklyStripWidget(context, data, widgetId)
            else -> buildTodayWidget(context, data, widgetId)
        }
        manager.updateAppWidget(widgetId, views)
    }
}

class HabitWidgetConfigActivity : Activity() {
    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedHabitId: Int = -1
    private var opacityPercent: Int = 92
    private var isDefaultSettings: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent?.extras?.getInt(EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        isDefaultSettings = widgetId == AppWidgetManager.INVALID_APPWIDGET_ID
        if (!isDefaultSettings) {
            setResult(RESULT_CANCELED)
        }

        opacityPercent = WidgetConfigStore.loadOpacity(this, widgetId)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(0xFFFFF8F0.toInt())
        }
        val title = TextView(this).apply {
            text = if (isDefaultSettings) "Default widget options" else "Widget options"
            textSize = 22f
            setTextColor(0xFF241F1B.toInt())
        }
        val subtitle = TextView(this).apply {
            text = if (isDefaultSettings) {
                "These settings apply to newly added widgets and widgets without custom settings."
            } else {
                "These settings apply to this widget."
            }
            textSize = 13f
            setTextColor(0xFF665A50.toInt())
            setPadding(0, 6, 0, 8)
        }
        val habitLabel = TextView(this).apply {
            text = "Primary habit"
            textSize = 14f
            setTextColor(0xFF665A50.toInt())
            setPadding(0, 28, 0, 8)
        }
        val habitSpinner = Spinner(this)
        val opacityLabel = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF665A50.toInt())
            setPadding(0, 28, 0, 8)
        }
        val opacitySeek = SeekBar(this).apply {
            max = WidgetOpacityChoice.values().lastIndex
            progress = WidgetOpacityChoice.nearest(opacityPercent).ordinal
        }
        fun updateOpacityLabel() {
            val choice = WidgetOpacityChoice.nearest(opacityPercent)
            opacityLabel.text = "Background: ${choice.label} (${choice.opacityPercent}%)"
        }
        opacitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                opacityPercent = WidgetOpacityChoice.values()[progress].opacityPercent
                updateOpacityLabel()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        updateOpacityLabel()

        val save = Button(this).apply {
            text = if (isDefaultSettings) "Save defaults" else "Save widget"
            setOnClickListener {
                isEnabled = false
                WidgetConfigStore.save(this@HabitWidgetConfigActivity, widgetId, opacityPercent, selectedHabitId)
                CoroutineScope(Dispatchers.IO).launch {
                    val manager = AppWidgetManager.getInstance(this@HabitWidgetConfigActivity)
                    if (!isDefaultSettings) {
                        HabitWidgetUpdater.updateConfiguredWidget(this@HabitWidgetConfigActivity, manager, widgetId)
                    }
                    HabitWidgetUpdater.updateAll(this@HabitWidgetConfigActivity)
                    runOnUiThread {
                        if (!isDefaultSettings) {
                            val result = Intent().putExtra(EXTRA_APPWIDGET_ID, widgetId)
                            setResult(RESULT_OK, result)
                        }
                        finish()
                    }
                }
            }
        }

        root.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(subtitle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(habitLabel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(habitSpinner, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(opacityLabel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(opacitySeek, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(save, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = 36
        })
        setContentView(root)

        CoroutineScope(Dispatchers.IO).launch {
            val habits = repository(this@HabitWidgetConfigActivity).loadHabits()
            val names = habits.map { it.name }.ifEmpty { listOf("First habit") }
            val ids = habits.map { it.id }.ifEmpty { listOf(-1) }
            selectedHabitId = WidgetConfigStore.loadHabitId(this@HabitWidgetConfigActivity, widgetId)
                .takeIf { it in ids } ?: ids.first()
            runOnUiThread {
                habitSpinner.adapter = ArrayAdapter(
                    this@HabitWidgetConfigActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    names
                )
                habitSpinner.setSelection(ids.indexOf(selectedHabitId).coerceAtLeast(0))
                habitSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                        selectedHabitId = ids.getOrElse(position) { -1 }
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
                }
            }
        }
    }
}

private enum class WidgetOpacityChoice(val label: String, val opacityPercent: Int) {
    VeryTransparent("Very transparent", 35),
    Translucent("Translucent", 60),
    Soft("Soft", 82),
    Solid("Solid", 100);

    companion object {
        fun nearest(opacityPercent: Int): WidgetOpacityChoice {
            return values().minByOrNull { kotlin.math.abs(it.opacityPercent - opacityPercent) } ?: Soft
        }
    }
}

private data class WidgetPalette(
    val background: Int,
    val text: Int,
    val mutedText: Int,
    val rule: Int,
    val accent: Int
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

private fun buildTodayWidget(context: Context, data: WidgetData, widgetId: Int): RemoteViews {
    val views = baseWidget(context, "Today", "Tap a habit to add a bead", data.palette, widgetId)
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

private fun buildSingleHabitWidget(context: Context, data: WidgetData, widgetId: Int): RemoteViews {
    val selectedHabitId = WidgetConfigStore.loadHabitId(context, widgetId)
    val habit = data.habits.firstOrNull { it.id == selectedHabitId } ?: data.habits.firstOrNull()
    val opacity = WidgetConfigStore.loadOpacity(context, widgetId)
    return RemoteViews(context.packageName, R.layout.widget_single_habit).apply {
        setInt(R.id.widget_single_root, "setBackgroundColor", withOpacity(data.palette.background, opacity))
        setTextColor(R.id.widget_single_title, data.palette.text)
        setTextColor(R.id.widget_single_subtitle, data.palette.mutedText)
        if (habit == null) {
            setTextViewText(R.id.widget_single_title, "Habit Beads")
            setTextViewText(R.id.widget_single_subtitle, "Open app to add habits")
            setImageViewBitmap(R.id.widget_single_bead, beadBitmap(data.palette.accent, 0, sizePx = 88))
            setViewVisibility(R.id.widget_single_decrement, View.INVISIBLE)
            setOnClickPendingIntent(R.id.widget_single_root, openAppIntent(context))
        } else {
            val count = data.counts["${habit.id}:${todayKey()}"] ?: 0
            setTextViewText(R.id.widget_single_title, habit.name)
            setTextViewText(R.id.widget_single_subtitle, count.takeIf { it > 0 }?.let { "$it beads today" } ?: "Tap bead to add")
            setImageViewBitmap(R.id.widget_single_bead, beadBitmap(habit.color.toArgb(), count, sizePx = 88))
            setTextColor(R.id.widget_single_decrement, data.palette.text)
            setViewVisibility(R.id.widget_single_decrement, if (count > 0) View.VISIBLE else View.INVISIBLE)
            setOnClickPendingIntent(R.id.widget_single_root, openAppIntent(context))
            setOnClickPendingIntent(R.id.widget_single_bead, addBeadIntent(context, habit.id))
            setOnClickPendingIntent(R.id.widget_single_decrement, subtractBeadIntent(context, habit.id))
        }
    }
}

private fun buildWeeklyStripWidget(context: Context, data: WidgetData, widgetId: Int): RemoteViews {
    val views = baseWidget(context, "Weekly Strip", "Last 7 days", data.palette, widgetId)
    val habits = data.habits.take(3)
    if (habits.isEmpty()) {
        addMessageRow(context, views, "Open Habit Beads to add habits", data.palette)
    } else {
        habits.forEach { habit ->
            val counts = data.days.map { day ->
                data.counts["${habit.id}:${day.dateKey}"] ?: 0
            }
            addWeeklyRow(context, views, habit.name, counts, habit.color.toArgb(), data.palette)
        }
    }
    return views
}

private fun baseWidget(context: Context, title: String, subtitle: String, palette: WidgetPalette, widgetId: Int): RemoteViews {
    val opacity = WidgetConfigStore.loadOpacity(context, widgetId)
    return RemoteViews(context.packageName, R.layout.widget_habit_beads).apply {
        removeAllViews(R.id.widget_body)
        setImageViewBitmap(R.id.widget_header_icon, beadBitmap(palette.accent, 1, sizePx = 48))
        setTextViewText(R.id.widget_title, title)
        setTextViewText(R.id.widget_subtitle, subtitle)
        setTextColor(R.id.widget_title, palette.text)
        setTextColor(R.id.widget_subtitle, palette.mutedText)
        setInt(R.id.widget_root, "setBackgroundColor", withOpacity(palette.background, opacity))
        setInt(R.id.widget_header_rule, "setBackgroundColor", palette.rule)
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
        setViewVisibility(R.id.widget_row_bead, View.VISIBLE)
        setViewVisibility(R.id.widget_row_strip, View.GONE)
        setViewVisibility(R.id.widget_row_value, View.VISIBLE)
        setImageViewBitmap(R.id.widget_row_bead, beadBitmap(habit.color.toArgb(), count, sizePx = 42))
        setTextViewText(R.id.widget_row_name, habit.name)
        setTextViewText(R.id.widget_row_value, count.takeIf { it > 0 }?.toString() ?: "")
        setTextColor(R.id.widget_row_name, palette.text)
        setTextColor(R.id.widget_row_value, if (count > 0) habit.color.toArgb() else palette.mutedText)
        if (allowQuickAdd) {
            setOnClickPendingIntent(R.id.widget_row_root, addBeadIntent(context, habit.id))
        }
    }
    parent.addView(R.id.widget_body, row)
}

private fun addWeeklyRow(context: Context, parent: RemoteViews, name: String, counts: List<Int>, beadColor: Int, palette: WidgetPalette) {
    val row = RemoteViews(context.packageName, R.layout.widget_habit_row).apply {
        setViewVisibility(R.id.widget_row_bead, View.VISIBLE)
        setViewVisibility(R.id.widget_row_value, View.GONE)
        setViewVisibility(R.id.widget_row_strip, View.VISIBLE)
        setImageViewBitmap(R.id.widget_row_bead, beadBitmap(beadColor, 1, sizePx = 42))
        setTextViewText(R.id.widget_row_name, name)
        setImageViewBitmap(R.id.widget_row_strip, weeklyStripBitmap(beadColor, counts))
        setTextColor(R.id.widget_row_name, palette.text)
        setOnClickPendingIntent(R.id.widget_row_root, openAppIntent(context))
    }
    parent.addView(R.id.widget_body, row)
}

private fun addMessageRow(context: Context, parent: RemoteViews, message: String, palette: WidgetPalette) {
    val row = RemoteViews(context.packageName, R.layout.widget_habit_row).apply {
        setViewVisibility(R.id.widget_row_bead, View.GONE)
        setViewVisibility(R.id.widget_row_strip, View.GONE)
        setViewVisibility(R.id.widget_row_value, View.GONE)
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
        setPackage(context.packageName)
        data = Uri.parse("habitbeads://widget/add/$habitId")
        putExtra(WidgetExtraHabitId, habitId)
    }
    return PendingIntent.getBroadcast(
        context,
        200 + habitId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun subtractBeadIntent(context: Context, habitId: Int): PendingIntent {
    val intent = Intent(context, HabitWidgetActionReceiver::class.java).apply {
        action = WidgetActionSubtract
        setPackage(context.packageName)
        data = Uri.parse("habitbeads://widget/subtract/$habitId")
        putExtra(WidgetExtraHabitId, habitId)
    }
    return PendingIntent.getBroadcast(
        context,
        habitId + 20_000,
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
        AppThemeChoice.Warm -> WidgetPalette(0xFFFFF4E8.toInt(), 0xFF241F1B.toInt(), 0xFF665A50.toInt(), 0xFFE8D8C6.toInt(), 0xFF4F7E8A.toInt())
        AppThemeChoice.Ocean -> WidgetPalette(0xFFEFF8FA.toInt(), 0xFF172126.toInt(), 0xFF52656D.toInt(), 0xFFD6E7EC.toInt(), 0xFF4E7F92.toInt())
        AppThemeChoice.Forest -> WidgetPalette(0xFFF2F7EA.toInt(), 0xFF202217.toInt(), 0xFF59614B.toInt(), 0xFFDDE7D2.toInt(), 0xFF5E7D5B.toInt())
        AppThemeChoice.Grape -> WidgetPalette(0xFFF7F0FA.toInt(), 0xFF251F24.toInt(), 0xFF655A68.toInt(), 0xFFE5D8EA.toInt(), 0xFF756A86.toInt())
    }
}

private fun beadBitmap(baseArgb: Int, count: Int, sizePx: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val inset = sizePx * 0.12f
    val radius = (sizePx / 2f) - inset
    val center = sizePx / 2f

    if (count <= 0) {
        paint.style = Paint.Style.FILL
        paint.color = 0xFFF0F3F8.toInt()
        canvas.drawCircle(center, center, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = sizePx * 0.08f
        paint.color = 0xFFBFC7D4.toInt()
        canvas.drawCircle(center, center, radius - paint.strokeWidth / 2f, paint)
        return bitmap
    }

    paint.style = Paint.Style.FILL
    paint.color = widgetBeadColor(baseArgb, count)
    canvas.drawCircle(center, center, radius, paint)
    paint.color = 0x44FFFFFF
    canvas.drawOval(
        RectF(sizePx * 0.25f, sizePx * 0.18f, sizePx * 0.57f, sizePx * 0.42f),
        paint
    )
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = sizePx * 0.04f
    paint.color = 0x22000000
    canvas.drawCircle(center, center, radius - paint.strokeWidth / 2f, paint)
    return bitmap
}

private fun weeklyStripBitmap(baseArgb: Int, counts: List<Int>): Bitmap {
    val width = 168
    val height = 38
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val radius = 8.5f
    val gap = (width - radius * 2f * 7f) / 8f
    counts.take(7).forEachIndexed { index, count ->
        val cx = gap + radius + index * (radius * 2f + gap)
        val cy = height / 2f
        if (count > 0) {
            paint.style = Paint.Style.FILL
            paint.color = widgetBeadColor(baseArgb, count)
            canvas.drawCircle(cx, cy, radius, paint)
        } else {
            paint.style = Paint.Style.FILL
            paint.color = 0xFFF0F3F8.toInt()
            canvas.drawCircle(cx, cy, radius, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = 0xFFBFC7D4.toInt()
            canvas.drawCircle(cx, cy, radius - 1f, paint)
        }
    }
    return bitmap
}

private fun widgetBeadColor(baseArgb: Int, count: Int): Int {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(baseArgb, hsv)
    val level = count.coerceIn(1, 9)
    val progress = (level - 1) / 8f
    hsv[0] = (hsv[0] + (progress * 54f) + 360f) % 360f
    hsv[1] = (0.18f + progress * 0.66f).coerceIn(0.18f, 0.84f)
    hsv[2] = (0.99f - progress * 0.21f).coerceIn(0.78f, 0.99f)
    return AndroidColor.HSVToColor(hsv)
}

private object WidgetConfigStore {
    private const val Name = "habit_widget_config"
    private const val OpacityPrefix = "opacity_"
    private const val HabitPrefix = "habit_"
    private const val DefaultWidgetId = -1

    fun save(context: Context, widgetId: Int, opacityPercent: Int, habitId: Int) {
        val normalizedWidgetId = normalizeWidgetId(widgetId)
        context.getSharedPreferences(Name, Context.MODE_PRIVATE)
            .edit()
            .putInt("$OpacityPrefix$normalizedWidgetId", WidgetOpacityChoice.nearest(opacityPercent).opacityPercent)
            .putInt("$HabitPrefix$normalizedWidgetId", habitId)
            .apply()
    }

    fun loadOpacity(context: Context, widgetId: Int): Int {
        val normalizedWidgetId = normalizeWidgetId(widgetId)
        val prefs = context.getSharedPreferences(Name, Context.MODE_PRIVATE)
        return when {
            prefs.contains("$OpacityPrefix$normalizedWidgetId") -> prefs.getInt("$OpacityPrefix$normalizedWidgetId", 82)
            prefs.contains("$OpacityPrefix$DefaultWidgetId") -> prefs.getInt("$OpacityPrefix$DefaultWidgetId", 82)
            else -> 82
        }
    }

    fun loadHabitId(context: Context, widgetId: Int): Int {
        val normalizedWidgetId = normalizeWidgetId(widgetId)
        val prefs = context.getSharedPreferences(Name, Context.MODE_PRIVATE)
        return when {
            prefs.contains("$HabitPrefix$normalizedWidgetId") -> prefs.getInt("$HabitPrefix$normalizedWidgetId", -1)
            prefs.contains("$HabitPrefix$DefaultWidgetId") -> prefs.getInt("$HabitPrefix$DefaultWidgetId", -1)
            else -> -1
        }
    }

    fun clear(context: Context, widgetId: Int) {
        context.getSharedPreferences(Name, Context.MODE_PRIVATE)
            .edit()
            .remove("$OpacityPrefix$widgetId")
            .remove("$HabitPrefix$widgetId")
            .apply()
    }

    private fun normalizeWidgetId(widgetId: Int): Int {
        return if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) DefaultWidgetId else widgetId
    }
}

private fun withOpacity(rgb: Int, opacityPercent: Int): Int {
    val alpha = (opacityPercent.coerceIn(20, 100) * 255 / 100) and 0xFF
    return (alpha shl 24) or (rgb and 0x00FFFFFF)
}
