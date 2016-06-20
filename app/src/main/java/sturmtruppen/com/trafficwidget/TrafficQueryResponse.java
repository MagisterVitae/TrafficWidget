package sturmtruppen.com.trafficwidget;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;


/**
 * Created by micheletogni on 03/06/16.
 */
public class TrafficQueryResponse {
    public boolean successful;
    public int totalSeconds;
    public Date executionTimeStamp;
    public int btnColor;
    public boolean reversedPath;
    public boolean connectivityAvaliable;

    TrafficQueryResponse() {
        successful = false;
        reversedPath = false;
        connectivityAvaliable = true;
    }

    public String FormattedDuration() {

        return String.format("%02d:%02d",
                TimeUnit.SECONDS.toHours(totalSeconds),
                TimeUnit.SECONDS.toMinutes(totalSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(totalSeconds)));

    }
}
