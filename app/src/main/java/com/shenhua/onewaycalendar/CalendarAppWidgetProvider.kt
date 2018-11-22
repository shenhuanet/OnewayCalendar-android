package com.shenhua.onewaycalendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.text.SimpleDateFormat
import java.util.*

/**
 * 690 × 1000 jpg
 * Created by shenhua on 2018/11/22.
 *
 * @author shenhua
 * Email shenhuanet@126.com
 */
class CalendarAppWidgetProvider : AppWidgetProvider() {

    /**
     * 处理视图更新
     *
     * @param context          context
     * @param appWidgetManager appWidgetManager
     * @param appWidgetIds     appWidgetIds
     */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val remoteViews = RemoteViews(context.packageName, R.layout.calendar_appwidget)
        val refreshIntent = Intent()
        refreshIntent.action = ACTION_REFRESH
        refreshIntent.component = ComponentName(context, CalendarAppWidgetProvider::class.java)
        remoteViews.setOnClickPendingIntent(
            R.id.action_refresh,
            PendingIntent.getBroadcast(context, 0, refreshIntent, 0)
        )

        val downloadIntent = Intent()
        downloadIntent.action = ACTION_SAVE
        downloadIntent.component = ComponentName(context, CalendarAppWidgetProvider::class.java)
        remoteViews.setOnClickPendingIntent(
            R.id.action_download,
            PendingIntent.getBroadcast(context, 0, downloadIntent, 0)
        )
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    /**
     * 处理点击事件
     *
     * @param context context
     * @param intent  intent
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (ACTION_REFRESH == intent.action) {
            refresh(context)
        } else if (ACTION_SAVE == intent.action) {
            Toast.makeText(context, "敬请期待", Toast.LENGTH_SHORT).show()
        } else {
            // android.appwidget.action.APPWIDGET_UPDATE
            refresh(context)
        }
    }

    private fun refresh(context: Context) {
        val remoteViews = RemoteViews(context.packageName, R.layout.calendar_appwidget)
        Glide.with(context).asBitmap()
            .load("http://img.owspace.com/Public/uploads/Download/" + today() + ".jpg")
            .into(object : SimpleTarget<Bitmap>() {
                /**
                 * The method that will be called when the resource load has finished.
                 *
                 * @param resource the loaded resource.
                 */
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    try {
                        remoteViews.setImageViewBitmap(R.id.image, resource)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val componentName = ComponentName(context, CalendarAppWidgetProvider::class.java)
                    val awm = AppWidgetManager.getInstance(context.applicationContext)
                    awm.updateAppWidget(componentName, remoteViews)
                }
            })
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy/MMdd", Locale.CHINA).format(Date())
    }

    companion object {

        private const val ACTION_REFRESH = "com.shenhua.onewaycalendar.action.REFRESH"
        private const val ACTION_SAVE = "com.shenhua.onewaycalendar.action.SAVE"
    }
}
