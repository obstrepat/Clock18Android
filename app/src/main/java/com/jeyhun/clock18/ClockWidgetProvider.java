package com.jeyhun.clock18;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_TICK = "com.jeyhun.clock18.WIDGET_TICK";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateOne(context, manager, id);
        scheduleNext(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String a = intent.getAction();
        if (ACTION_TICK.equals(a)
                || Intent.ACTION_BOOT_COMPLETED.equals(a)
                || Intent.ACTION_TIME_CHANGED.equals(a)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(a)) {
            updateAll(context);
            scheduleNext(context);
        }
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, ClockWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        for (int id : ids) updateOne(context, manager, id);
        if (ids.length > 0) scheduleNext(context);
    }

    private static void updateOne(Context context, AppWidgetManager manager, int id) {
        Clock18.Parts p = Clock18.now();
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_clock18);
        rv.setTextViewText(R.id.widgetCustomTime, Clock18.hhmm(p));
        String real = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        rv.setTextViewText(R.id.widgetRealTime, "Adi vaxt  " + real);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widgetRoot, pi);
        manager.updateAppWidget(id, rv);
    }

    private static void scheduleNext(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ClockWidgetProvider.class).setAction(ACTION_TICK);
        PendingIntent pi = PendingIntent.getBroadcast(context, 18, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long now = System.currentTimeMillis();
        long next = now + 60_000L;
        am.setAndAllowWhileIdle(AlarmManager.RTC, next, pi);
    }
}
