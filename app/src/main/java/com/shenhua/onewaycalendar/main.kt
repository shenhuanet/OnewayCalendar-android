package com.shenhua.onewaycalendar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
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
                )
            }
        }
    }

    private fun imageBitmap(context: Context): Bitmap {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = sdf.format(Date())
        val file = File(context.filesDir, "images/${date}.jpg")
        if (!file.exists()) {
            val suffix = "${date.substring(0, 4)}/${date.substring(4)}.jpg"
            val url = "http://img.owspace.com/Public/uploads/Download/${suffix}"
            ImageRequest.Builder(context)
                .data(url)
                .listener { _, result ->
                    val drawable = result.drawable
                    if (drawable is BitmapDrawable) {
                        FileOutputStream(file).use { out ->
                            drawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                        }
                        context.sendBroadcast(Intent("android.appwidget.action.APPWIDGET_UPDATE"))
                    }
                }
                .build()
                .run { context.imageLoader.enqueue(this) }
            val lastFile =
                File(context.filesDir, "images/${sdf.format(Date().time - 86400000)}.jpg")
            return BitmapFactory.decodeFile(lastFile.path)
        } else {
            return BitmapFactory.decodeFile(file.path)
        }
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