package sturmtruppen.com.trafficwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TrafficWidgetConfigureActivity TrafficWidgetConfigureActivity}
 */
public class TrafficWidget extends AppWidgetProvider {

    public static String ACTION_WIDGET_CONFIGURE = "ConfigureWidget";
    public static String ACTION_WIDGET_REFRESH = "sturmtruppen.com.trafficwidget.MANUAL_REFRESH";
    private static boolean manualRefresh = false;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);

        // Caricamento configurazioni
        Configuration conf = new Configuration(context, appWidgetId);
        if (manualRefresh) {
            conf.setManualRefresh(true);
            manualRefresh = false;
        }

        // Aggiornamento widget mediante task
        TrafficDataFetcher dataFetcher = new TrafficDataFetcher(context);
        dataFetcher.execute(conf);

        // Sets up the settings button to open the configuration activity
        Intent configIntent = new Intent(context, TrafficWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        views.setOnClickPendingIntent(R.id.btnConfig, configPendingIntent);
        configIntent.setAction(ACTION_WIDGET_CONFIGURE + Integer.toString(appWidgetId));

        // Sets up the refresh button to update the widget view
        Intent refreshIntent = new Intent(context, TrafficWidget.class);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent refreshPendingIntent = buildRefreshPendingIntent(context);
        views.setOnClickPendingIntent(R.id.btnRefresh, refreshPendingIntent);
        refreshIntent.setAction(ACTION_WIDGET_REFRESH);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        int i = 1;
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            i++;
        }

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            TrafficWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
        TrafficWidgetConfigureActivity.deleteAllPrefs(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(TrafficWidget.ACTION_WIDGET_REFRESH)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), TrafficWidget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            manualRefresh = true;
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }


    /**
     * Metodo per costruire un pending intent per il refresh manuale del widget
     *
     * @param context
     * @return
     */
    public static PendingIntent buildRefreshPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_WIDGET_REFRESH);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}

