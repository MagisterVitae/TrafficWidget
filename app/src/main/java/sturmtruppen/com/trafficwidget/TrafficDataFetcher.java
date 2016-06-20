package sturmtruppen.com.trafficwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
public class TrafficDataFetcher extends AsyncTask<Configuration, Void, TrafficQueryResponse> {

    private Context context;
    public static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private static String APIKEY = "AIzaSyAmp0DJnQmf09u0G2Z4UtbArFIPhu_dLOA";
    private static boolean manualRefresh;

    // Costruttore per test con time stamp
    public TrafficDataFetcher(Context context) {
        this.context = context;
    }

    /**
     * Metodo che esegue le operazioni onerose in un thread separato
     *
     * @param conf
     * @return
     */
    @Override
    protected TrafficQueryResponse doInBackground(Configuration... conf) {
        // Valuta se il refresh è manuale
        manualRefresh = conf[0].isManualRefresh();

        // Verifica connessione internet
        if (!Utils.isNetworkOnline(context)) {
            TrafficQueryResponse queryResponse = new TrafficQueryResponse();
            queryResponse.connectivityAvaliable = false;
            return queryResponse;
        }

        return GetDistance(conf);
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

        // Se non è disponibile la connessione a internet
        if (!response.connectivityAvaliable) {
            if (manualRefresh)
                Toast.makeText(context, "Network unavailable!", Toast.LENGTH_LONG).show();
            views.setTextViewText(R.id.widgettext, "N.A.");
            views.setInt(R.id.btnRefresh, "setBackgroundColor", Utils.CYAN);
            pushWidgetUpdate(context.getApplicationContext(), views);
            return;
        }

        views.setTextViewText(R.id.widgettext, response.FormattedDuration());
        views.setInt(R.id.btnRefresh, "setBackgroundColor", response.btnColor);
        if (response.btnColor == Utils.RED)
            Utils.sendNotification(context);

        if (response.successful && manualRefresh)
            Toast.makeText(context, "ETA: " + response.FormattedDuration(), Toast.LENGTH_SHORT).show();
        if (response.reversedPath && manualRefresh)
            Toast.makeText(context, "Reversed path", Toast.LENGTH_SHORT).show();
        if (!response.successful && manualRefresh)
            Toast.makeText(context, "Data retrieval failure!", Toast.LENGTH_SHORT).show();

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
     * @param confArray
     * @return
     */
    private TrafficQueryResponse GetDistance(Configuration[] confArray) {
        Configuration conf = confArray[0];
        TrafficQueryResponse queryResponse = new TrafficQueryResponse();

        // reverse path evaluation
        String tempFrom = conf.getFrom();
        if (Utils.reversePath(conf.getTimeReverse())) {
            conf.setFrom(conf.getTo());
            conf.setTo(tempFrom);
            queryResponse.reversedPath = true;
        }

        String urlString = Uri.parse("https://maps.googleapis.com/maps/api/distancematrix/json?").buildUpon()
                .appendQueryParameter("origins", conf.getFrom())
                .appendQueryParameter("destinations", conf.getTo())
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
            queryResponse.btnColor = Utils.getBtnColor(duration.getInt("value"), conf.getWarningTsd(), conf.getAlertTsd());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResponse;
    }


}
