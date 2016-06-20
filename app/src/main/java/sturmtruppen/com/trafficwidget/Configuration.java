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
    private String timeReverse;
    private boolean manualRefresh;

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

    public String getTimeReverse() {
        return timeReverse;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setWarningTsd(String warningTsd) {
        this.warningTsd = warningTsd;
    }

    public void setAlertTsd(String alertTsd) {
        this.alertTsd = alertTsd;
    }

    public void setTimeReverse(String timeReverse) {
        this.timeReverse = timeReverse;
    }

    public boolean isManualRefresh() {
        return manualRefresh;
    }

    public void setManualRefresh(boolean manualRefresh) {
        this.manualRefresh = manualRefresh;
    }

    public void fetchConfiguration(Context context, int appWidgetId) {
        this.from = TrafficWidgetConfigureActivity.loadFromPref(context, appWidgetId);
        this.to = TrafficWidgetConfigureActivity.loadToPref(context, appWidgetId);
        this.warningTsd = TrafficWidgetConfigureActivity.loadWarningPref(context, appWidgetId);
        this.alertTsd = TrafficWidgetConfigureActivity.loadAlertPref(context, appWidgetId);
        this.timeReverse = TrafficWidgetConfigureActivity.loadTimeReversePref(context, appWidgetId);
    }
}
