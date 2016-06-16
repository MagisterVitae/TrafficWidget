package sturmtruppen.com.trafficwidget;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Matteo on 16/06/2016.
 */
public class DistanceMatrixParams {
    private String from;
    private String to;
    private String warningTsd;
    private String alertTsd;
    private Configuration conf;
    private boolean reversedPath;

    public DistanceMatrixParams(Configuration conf) {
        this.conf = conf;
        generateParams();
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

    public boolean isReversedPath() {
        return reversedPath;
    }

    private void generateParams() {
        if (reversePath(conf.getTimeReverse())) {
            this.from = conf.getTo();
            this.to = conf.getFrom();
            this.reversedPath = true;
        } else {
            this.from = conf.getFrom();
            this.to = conf.getTo();
            this.reversedPath = false;
        }
        this.warningTsd = conf.getWarningTsd();
        this.alertTsd = conf.getAlertTsd();
    }

    private boolean reversePath(String reverseTime) {
        String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        String[] revTimeArr = reverseTime.split(":");
        String[] currTimeArr = currentTime.split(":");

        if (Integer.parseInt(currTimeArr[0]) > Integer.parseInt(revTimeArr[0]))
            return true;
        else if (Integer.parseInt(currTimeArr[1]) > Integer.parseInt(revTimeArr[1]))
            return true;
        else
            return false;
    }
}
