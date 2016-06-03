package sturmtruppen.com.trafficwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by Matteo on 03/06/2016.
 */
public class TrafficDataFetcher extends AsyncTask<Void, Void, String> {

    private Context contest;
    public static SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");

    // Costruttore per test con time stamp
    public TrafficDataFetcher(Context context) {
        this.contest = context;
    }

    /**
     * Metodo che esegue le operazioni onerose in un thread separato
     *
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(Void... params) {
        String currentTime = formatter.format(new Date());
        return currentTime;
    }

    /**
     * Metodo che aggiorna la visualizzazione con i dati ritornati dal doInBackground
     *
     * @param newTimeStamp
     */
    @Override
    protected void onPostExecute(String newTimeStamp) {
        manUpdateWidget(this.contest, newTimeStamp);
    }

    private void manUpdateWidget(Context context, String newTimeStamp) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        views.setTextViewText(R.id.widgettext, newTimeStamp);
        //REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
        views.setOnClickPendingIntent(R.id.btnRefresh, TrafficWidget.buildRefreshPendingIntent(context));

        pushWidgetUpdate(context.getApplicationContext(), views);
    }

    /**
     * Metodo che aggiorna la view del widget
     *
     * @param context
     * @param remoteViews
     */
    private void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, TrafficWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }


}
