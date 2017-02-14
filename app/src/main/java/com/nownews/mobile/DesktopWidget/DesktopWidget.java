package com.nownews.mobile.DesktopWidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.nownews.R;

public class DesktopWidget extends AppWidgetProvider {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_desktop_widget);
        ComponentName comp = new ComponentName(context, DesktopWidget.class);
        appWidgetManager.updateAppWidget(comp, views);

    }

}
