package sturmtruppen.com.trafficwidget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Matteo on 03/06/2016.
 */
public class TrafficDataFetcher extends AsyncTask<String[], Void, TrafficQueryResponse> {

    private Context context;
    public static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private static String APIKEY = "AIzaSyAmp0DJnQmf09u0G2Z4UtbArFIPhu_dLOA";
    private static int GREEN = Color.GREEN;
    private static int YELLOW = Color.YELLOW;
    private static int RED = Color.RED;

    // Costruttore per test con time stamp
    public TrafficDataFetcher(Context context) {
        this.context = context;
    }

    /**
     * Metodo che esegue le operazioni onerose in un thread separato
     *
     * @param params
     * @return
     */
    @Override
    protected TrafficQueryResponse doInBackground(String[]... params) {
        return GetDistance(params[0][0], params[0][1], params[0][2], params[0][3]);
    }

    /**
     * Metodo che aggiorna la visualizzazione con i dati ritornati dal doInBackground
     *
     * @param response
     */
    @Override
    protected void onPostExecute(TrafficQueryResponse response) {
        manUpdateWidget(this.context, response);
    }

    /**
     * [TEST] Metodo per la formattazione dei dati da visualizzare sul widget
     *
     * @param context
     * @param newTimeStamp
     */
    private void manUpdateWidget(Context context, String newTimeStamp) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        views.setTextViewText(R.id.widgettext, newTimeStamp);
        //REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
        views.setOnClickPendingIntent(R.id.btnRefresh, TrafficWidget.buildRefreshPendingIntent(context));

        pushWidgetUpdate(context.getApplicationContext(), views);
    }

    /**
     * Metodo per la formattazione dei dati da visualizzare sul widget
     * @param context
     * @param response
     */
    private void manUpdateWidget(Context context, TrafficQueryResponse response) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);

        views.setTextViewText(R.id.widgettext, response.FormattedDuration());
        views.setInt(R.id.btnRefresh, "setBackgroundColor", response.btnColor);
        if (response.btnColor == RED)
            sendNotification(context);

        if (response.successful)
            Toast.makeText(context, "ETA: " + response.FormattedDuration(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, "Data retrieval failure!", Toast.LENGTH_LONG).show();

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

    /**
     * Metodo che interroga le API Google per stabilire il tempo di percorrenza
     * @param src
     * @param dest
     * @return
     */
    private TrafficQueryResponse GetDistance(String src, String dest, String warningTsd, String alertTsd) {

        TrafficQueryResponse queryResponse = new TrafficQueryResponse();

        String urlString = Uri.parse("https://maps.googleapis.com/maps/api/distancematrix/json?").buildUpon()
                .appendQueryParameter("origins", src)
                .appendQueryParameter("destinations", dest)
                .appendQueryParameter("departure_time", "now")
                .appendQueryParameter("key", APIKEY)
                .build().toString();

        // get the JSON And parse it to get the directions data.
        HttpURLConnection urlConnection = null;
        URL url = null;

        try {
            url = new URL(urlString);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();

            InputStream inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

            String temp, response = "";
            while ((temp = bReader.readLine()) != null) {
                response += temp;
            }
            //Close the reader, stream & connection
            bReader.close();
            inStream.close();
            urlConnection.disconnect();

            Log.d("Risposta:",response);
            //Sortout JSONresponse
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            JSONArray rows = object.getJSONArray("rows");
            //Log.d("JSON","array: "+array.toString());

            //Routes is a combination of objects and arrays
            JSONObject firstRow = rows.getJSONObject(0);
            //Log.d("JSON","rows: "+routes.toString());

            JSONArray elements = firstRow.getJSONArray("elements");
            //Log.d("JSON","summary: "+summary);

            //Seleziona tragitto più breve
            int elementIndex = 0;
            int distance = Integer.MAX_VALUE;
            for (int i = 0; i < elements.length(); i++) {
                if (elements.getJSONObject(elementIndex).getJSONObject("distance").getInt("value") < distance) {
                    distance = elements.getJSONObject(elementIndex).getJSONObject("distance").getInt("value");
                    elementIndex = i;
                }
            }

            JSONObject firstElement = elements.getJSONObject(elementIndex);
            //Log.d("JSON","legs: "+legs.toString());

            JSONObject duration = firstElement.getJSONObject("duration_in_traffic");
            //Log.d("JSON","steps: "+steps.toString());

            queryResponse.totalSeconds = duration.getInt("value");
            queryResponse.executionTimeStamp = new Date();
            queryResponse.successful = true;
            queryResponse.btnColor = getBtnColor(duration.getInt("value"), warningTsd, alertTsd);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResponse;
    }

    /**
     * Metodo per la determinazione del colore del widget
     *
     * @param duration
     * @param warningTsd
     * @param alertTsd
     * @return
     */
    private int getBtnColor(int duration, String warningTsd, String alertTsd) {
        int warningSecTsd = Integer.parseInt(warningTsd) * 60;
        int alertSecTsd = Integer.parseInt(alertTsd) * 60;

        if (duration >= alertSecTsd)
            return RED;
        if (duration >= warningSecTsd)
            return YELLOW;
        else
            return GREEN;
    }

    private void sendNotification(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_madonna)
                        .setContentTitle("Traffic Widget")
                        .setContentText("ETA above alert threshold!!!");

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
