package com.shenhua.onewaycalendar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import coil.imageLoader
import coil.request.ImageRequest
import com.shenhua.onewaycalendar.theme.GlanceTheme
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

/**
 * 690 × 1000 jpg
 * Created by shenhua on 2018/11/22.
 *
 * @author shenhua
 * Email shenhuanet@126.com
 */
class CalendarAppWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = CalendarWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        DownloadWork.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        DownloadWork.cancel(context)
    }
}

class CalendarWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.background)
                    .appWidgetBackgroundCornerRadius()
            ) {
                Image(
                    provider = ImageProvider(imageBitmap(LocalContext.current)),
                    contentDescription = "",
                    modifier = GlanceModifier.fillMaxHeight()
                        .appWidgetBackgroundCornerRadius()
                        .clickable(actionRunCallback<RefreshAction>())
                )
            }
        }
    }

    private fun imageBitmap(context: Context): Bitmap {
        val (url, file) = context.source()
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.path)
        }
        return BitmapFactory.decodeResource(context.resources, R.drawable.img)
    }

    private fun handleBitmap(file: File, bitmap: Bitmap) {
        val tintColor = randomColor()
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                val color = bitmap.getPixel(i, j)
                val r = (color shr 16) and 0xff
                val g = (color shr 8) and 0xff
                val b = color and 0xff
                val hold = 50
                if (r < hold && g < hold && b < hold) {
                    bitmap.setPixel(i, j, tintColor)
                }
            }
        }
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
    }

    private fun randomColor(): Int {
        val colors = listOf(
            "#ffc3ab", "#a9ccff", "#fbd1f1", "#ffc8e8", "#fb88a7", "#f7656b", "#aa92d4"
        )
        return Color.parseColor(colors[Random.nextInt(colors.size)])
    }
}

fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        cornerRadius(16.dp)
    }
    return this
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context, glanceId: GlanceId, parameters: ActionParameters
    ) {
//        context.download(glanceId)
    }
}

suspend fun Context.download(glanceId: GlanceId) {
    val (url, file) = source()
    if (file.exists()) {
        updateAppWidgetState(this, glanceId) { it.clear() }
        CalendarWidget().update(this, glanceId)
        return
    }
    val result = suspendCoroutine {
        ImageRequest.Builder(this)
            .data(url)
            .listener { _, result ->
                val drawable = result.drawable
                if (drawable is BitmapDrawable) {
                    FileOutputStream(file).use { out ->
                        drawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                }
                it.resume(true)
            }
            .build()
            .run { context.imageLoader.enqueue(this) }
    }
    if (result) {
        updateAppWidgetState(this, glanceId) { it.clear() }
        CalendarWidget().update(this, glanceId)
    }
}

fun Context.source(): Pair<String, File> {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val date = sdf.format(Date())
    val file = File(filesDir, "images/${date}.jpg")
    val suffix = "${date.substring(0, 4)}/${date.substring(4)}.jpg"
    val url = "https://img.owspace.com/Public/uploads/Download/${suffix}"
    return url to file
}