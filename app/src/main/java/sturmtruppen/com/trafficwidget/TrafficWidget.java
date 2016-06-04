package sturmtruppen.com.trafficwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    //public static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    private static String from;
    private static String to;
    private static String warningTsd;
    private static String alertTsd;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);

        // Caricamento configurazioni
        CharSequence confFrom = TrafficWidgetConfigureActivity.loadFromPref(context, appWidgetId);
        CharSequence confTo = TrafficWidgetConfigureActivity.loadToPref(context, appWidgetId);
        CharSequence confWarningTsd = TrafficWidgetConfigureActivity.loadWarningPref(context, appWidgetId);
        CharSequence confAlertTsd = TrafficWidgetConfigureActivity.loadAlertPref(context, appWidgetId);
        from = confFrom.toString();
        to = confTo.toString();
        warningTsd = confWarningTsd.toString();
        alertTsd = confAlertTsd.toString();

        // Aggiornamento widget mediante task
        TrafficDataFetcher dataFetcher = new TrafficDataFetcher(context);
        String[] params = {from, to, warningTsd, alertTsd};
        dataFetcher.execute(params);

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

        // Toast.makeText(context, "ETA: ", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        int i = 1;
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {

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
        if (intent.getAction().equals(TrafficWidget.ACTION_WIDGET_REFRESH)) {
            if (isNetworkOnline(context)) {
                // Aggiornamento widget mediante task
                TrafficDataFetcher dataFetcher = new TrafficDataFetcher(context);
                String[] params = {from, to, warningTsd, alertTsd};
                dataFetcher.execute(params);

                //Toast.makeText(context, "From: " + from + "\n" + "To: " + to, Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(context, "Network unavailable!", Toast.LENGTH_LONG).show();
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

    private boolean isNetworkOnline(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }


}

