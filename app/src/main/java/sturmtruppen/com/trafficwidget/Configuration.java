package sturmtruppen.com.trafficwidget;

import android.content.Context;

/**
 * Created by Matteo on 14/06/2016.
 */
public class Configuration {

    private String from;
    private String to;
    private String warningTsd;
    private String alertTsd;

    public Configuration() {
    }

    public Configuration(Context context, int appWidgetId) {
        fetchConfiguration(context, appWidgetId);
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getWarningTsd() {
        return warningTsd;
    }

    public String getAlertTsd() {
        return alertTsd;
    }

    public void fetchConfiguration(Context context, int appWidgetId) {
        this.from = TrafficWidgetConfigureActivity.loadFromPref(context, appWidgetId);
        this.to = TrafficWidgetConfigureActivity.loadToPref(context, appWidgetId);
        this.warningTsd = TrafficWidgetConfigureActivity.loadWarningPref(context, appWidgetId);
        this.alertTsd = TrafficWidgetConfigureActivity.loadAlertPref(context, appWidgetId);
    }
}