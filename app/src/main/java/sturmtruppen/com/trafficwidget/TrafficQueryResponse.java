package sturmtruppen.com.trafficwidget;

import java.util.Date;

/**
 * Created by micheletogni on 03/06/16.
 */
public class TrafficQueryResponse {
    public boolean successful;
    public int totalMinutes;
    public Date executionTimeStamp;

    TrafficQueryResponse(){
        successful = false;
    }
}
