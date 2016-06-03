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
    public static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    private static String from;
    private static String to;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

       /* CharSequence widgetText = TrafficWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);*/

        CharSequence confFrom = TrafficWidgetConfigureActivity.loadFromPref(context, appWidgetId);
        CharSequence confTo = TrafficWidgetConfigureActivity.loadToPref(context, appWidgetId);
        from = confFrom.toString();
        to = confTo.toString();

        String currentTime = formatter.format(new Date());
        String strWidgetText = currentTime;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        views.setTextViewText(R.id.widgettext, "[" + String.valueOf(appWidgetId) + "]" + strWidgetText);

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

        //Toast.makeText(context, "updateAppWidget(): " + String.valueOf(appWidgetId) + "\n" + strWidgetText, Toast.LENGTH_LONG).show();
        Toast.makeText(context, "From: " + from + "\n" + "To: " + to, Toast.LENGTH_LONG).show();

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
        String action = intent.getAction();
        if (intent.getAction().equals(TrafficWidget.ACTION_WIDGET_REFRESH)) {
            //manUpdateWidget(context);

            // Aggiornamento widget mediante task
            TrafficDataFetcher dataFetcher = new TrafficDataFetcher(context);
            String[] destinations = {from, to};
            dataFetcher.execute(destinations);

            Toast.makeText(context, "From: " + from + "\n" + "To: " + to, Toast.LENGTH_LONG).show();
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

