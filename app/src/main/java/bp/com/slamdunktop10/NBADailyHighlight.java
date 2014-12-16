package bp.com.slamdunktop10;

import org.json.JSONArray;

import java.util.Date;

/**
 * Created by daz on 12/16/14.
 */
public class NBADailyHighlight {
    public Long dateInEpoch;
    public JSONArray highlights;

    public NBADailyHighlight(Long dateInEpoch, JSONArray highlights) {
        this.dateInEpoch = dateInEpoch;
        this.highlights = highlights;
    }
}
