package es.elixir.bsc.elixibilitas.model.metrics;

import java.time.ZonedDateTime;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class HomePageHistoryRecord {
    
    private int responseCode;
    private ZonedDateTime time;
    
    @JsonbProperty("code")
    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
    
    @JsonbProperty("time")
    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }
}
