package com.shenhua.onewaycalendar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.time.Duration

/**
 *
 * @since 2025/1/7
 * @author 倒挂草(daoguacao fuxh@igancao.com)
 */
class DownloadWork(private val ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    companion object {
        private val uniqueWorkName = CalendarAppWidgetProvider::class.java.simpleName

        fun enqueue(context: Context) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = PeriodicWorkRequestBuilder<DownloadWork>(
                Duration.ofMinutes(15)
            )
            manager.enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.UPDATE,
                requestBuilder.build()
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        ctx.getSharedPreferences("app", Context.MODE_PRIVATE).edit {
            putLong("last_update", System.currentTimeMillis())
        }
        val (url, file) = ctx.source()
        if (file.exists()) {
            return Result.success()
        }
        val req = Request.Builder().url(url).build()
        OkHttpClient().newCall(req).execute().use { res ->
            if (res.isSuccessful) {
                res.body?.bytes()?.run {
                    val bitmap = BitmapFactory.decodeByteArray(this, 0, size, null)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                }
                val glanceIds =
                    GlanceAppWidgetManager(ctx).getGlanceIds(CalendarWidget::class.java)
                return try {
                    for (glanceId in glanceIds) {
                        CalendarWidget().update(ctx, glanceId)
                    }
                    Result.success()
                } catch (e: Exception) {
                    Result.failure()
                }
            } else {
                return Result.failure()
            }
        }
    }
}