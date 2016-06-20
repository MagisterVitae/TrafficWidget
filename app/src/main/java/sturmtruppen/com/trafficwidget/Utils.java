package sturmtruppen.com.trafficwidget;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Matteo on 20/06/2016.
 */
public class Utils {

    /**
     * Definizioni di costanti
     */
    public static int GREEN = Color.GREEN;
    public static int YELLOW = Color.YELLOW;
    public static int RED = Color.RED;
    public static int CYAN = Color.CYAN;

    /**
     * Metodo per la determinazione del colore del widget
     *
     * @param duration
     * @param warningTsd
     * @param alertTsd
     * @return
     */
    public static int getBtnColor(int duration, String warningTsd, String alertTsd) {
        int warningSecTsd = Integer.parseInt(warningTsd) * 60;
        int alertSecTsd = Integer.parseInt(alertTsd) * 60;

        if (duration >= alertSecTsd)
            return RED;
        if (duration >= warningSecTsd)
            return YELLOW;
        else
            return GREEN;
    }

    /**
     * Metodo usato per inviare una nuova notifica
     *
     * @param context
     */
    public static void sendNotification(Context context) {
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_madonna)
                        .setContentTitle("Traffic Widget")
                        .setContentText("ETA above alert threshold!!!")
                        .setContentIntent(pendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    /**
     * Metodo che verifica se invertire il punto di partenza con quello di destinazione
     *
     * @param reverseTime
     * @return
     */
    public static boolean reversePath(String reverseTime) {
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        String[] revTimeArr = reverseTime.split(":");
        String[] currTimeArr = currentTime.split(":");

        // se ora corrente è successiva a ora di soglia, inverti
        if (Integer.parseInt(currTimeArr[0]) > Integer.parseInt(revTimeArr[0]))
            return true;
            // se stessa ora, confronta i minuti
        else if (Integer.parseInt(currTimeArr[0]) == Integer.parseInt(revTimeArr[0])
                && Integer.parseInt(currTimeArr[1]) > Integer.parseInt(revTimeArr[1]))
            return true;
            // non invertire
        else
            return false;
    }

    /**
     * Metodo che verifica la disponibilità della connessione internet
     *
     * @param context
     * @return
     */
    public static boolean isNetworkOnline(Context context) {
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
