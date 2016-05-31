package sturmtruppen.com.trafficwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TrafficWidgetConfigureActivity TrafficWidgetConfigureActivity}
 */
public class TrafficWidget extends AppWidgetProvider {

    public static String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";
    public static String ACTION_WIDGET_REFRESH = "sturmtruppen.com.trafficwidget.MANUAL_REFRESH";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

       /* CharSequence widgetText = TrafficWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);*/


        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy  hh:mm:ss a");
        String currentTime = formatter.format(new Date());
        String strWidgetText = currentTime;

        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        updateViews.setTextViewText(R.id.widgettext, "[" + String.valueOf(appWidgetId) + "]" + strWidgetText);

        // Sets up the settings button to open the configuration activity
        Intent configIntent = new Intent(context, TrafficWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.btnConfig, configPendingIntent);
        configIntent.setAction(ACTION_WIDGET_CONFIGURE + Integer.toString(appWidgetId));

        Intent refreshIntent = new Intent(context, TrafficWidgetIntentReceiver.class);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent refreshPendingIntent = buildRefreshPendingIntent(context);
        updateViews.setOnClickPendingIntent(R.id.btnRefresh, refreshPendingIntent);
        refreshIntent.setAction(ACTION_WIDGET_REFRESH);

        appWidgetManager.updateAppWidget(appWidgetId, updateViews);

        Toast.makeText(context, "updateAppWidget(): " + String.valueOf(appWidgetId) + "\n" + strWidgetText, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        int i = 1;
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);

            /*String currentTime = formatter.format(new Date());
            strWidgetText = strWidgetText + "\n" + currentTime;

            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
            updateViews.setTextViewText(R.id.widgettext, strWidgetText);
            appWidgetManager.updateAppWidget(appWidgetIds, updateViews);

            super.onUpdate(context, appWidgetManager, appWidgetIds);
            Toast.makeText(context, "onUpdate()", Toast.LENGTH_LONG).show();*/

            RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
            updateViews.setOnClickPendingIntent(R.id.btnRefresh, buildRefreshPendingIntent(context));

            updateAppWidget(context, appWidgetManager, appWidgetId);
            Toast.makeText(context, "onUpdate(): " + String.valueOf(i) + " : " + String.valueOf(appWidgetId), Toast.LENGTH_LONG).show();
            i++;
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            TrafficWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
        Toast.makeText(context, "onDeleted()", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Toast.makeText(context, "onEnabled()", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Toast.makeText(context, "onDisabled()", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(TrafficWidget.ACTION_WIDGET_REFRESH)) {
            manUpdateWidget(context);
        }
    }

    private void manUpdateWidget(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        //remoteViews.setImageViewResource(R.id.widget_image, getImageToSet());

        //REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
        remoteViews.setOnClickPendingIntent(R.id.btnRefresh, TrafficWidget.buildRefreshPendingIntent(context));

        TrafficWidget.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
    }

    public static PendingIntent buildRefreshPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_WIDGET_REFRESH);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, TrafficWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }


}

