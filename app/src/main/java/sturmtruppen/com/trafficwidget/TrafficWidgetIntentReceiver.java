package sturmtruppen.com.trafficwidget;

/**
 * Created by Matteo on 31/05/2016.
 */

import sturmtruppen.com.trafficwidget.TrafficWidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TrafficWidgetIntentReceiver extends BroadcastReceiver {
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
}
