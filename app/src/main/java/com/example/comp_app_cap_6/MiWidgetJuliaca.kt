package com.example.comp_app_cap_6

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MiWidgetJuliaca : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.mi_widget_juliaca)
    val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    views.setTextViewText(R.id.widget_actualizado, "Actualizado $hora")

    val abrirApp = PendingIntent.getActivity(
        context,
        0,
        Intent(context, CuatroComponentesActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_abrir_app, abrirApp)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
