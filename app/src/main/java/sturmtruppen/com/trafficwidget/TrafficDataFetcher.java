package sturmtruppen.com.trafficwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by Matteo on 03/06/2016.
 */
public class TrafficDataFetcher extends AsyncTask<Void, Void, TrafficQueryResponse> {

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
    protected TrafficQueryResponse doInBackground(Void... params) {

        return GetDistance("","");
    }

    /**
     * Metodo che aggiorna la visualizzazione con i dati ritornati dal doInBackground
     *
     * @param response
     */
    @Override
    protected void onPostExecute(TrafficQueryResponse response) {
        manUpdateWidget(this.contest, response);
    }

    private void manUpdateWidget(Context context, String newTimeStamp) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        views.setTextViewText(R.id.widgettext, newTimeStamp);
        //REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
        views.setOnClickPendingIntent(R.id.btnRefresh, TrafficWidget.buildRefreshPendingIntent(context));

        pushWidgetUpdate(context.getApplicationContext(), views);
    }

    private void manUpdateWidget(Context context, TrafficQueryResponse response) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.traffic_widget);
        views.setTextViewText(R.id.widgettext, response.executionTimeStamp.toString());
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

    private TrafficQueryResponse GetDistance(String src, String dest) {

        TrafficQueryResponse queryResponse = new TrafficQueryResponse();

        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/distancematrix/json?");
        urlString.append("origin=" + src );//Origine
        urlString.append("&destination=" + dest );//Destinazione
        urlString.append("&departure_time=now");//Partenza immediata
        urlString.append("&key=");
        Log.d("xxx", "URL=" + urlString.toString());

        // get the JSON And parse it to get the directions data.
        HttpURLConnection urlConnection = null;
        URL url = null;

        try {
            url = new URL(urlString.toString());

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

            JSONObject firstElement = elements.getJSONObject(0);
            //Log.d("JSON","legs: "+legs.toString());

            JSONObject duration = firstElement.getJSONObject("duration");
            //Log.d("JSON","steps: "+steps.toString());

            queryResponse.totalMinutes = duration.getInt("value")/60;
            queryResponse.executionTimeStamp = new Date();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return queryResponse;
    }

}
