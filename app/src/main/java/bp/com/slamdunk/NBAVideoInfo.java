package bp.com.slamdunk;

import java.util.Date;

/**
 * Created by daz on 12/12/14.
 */
public class NBAVideoInfo {
    public Integer id;
    public String urlAddress;
    public Date date;
    public Integer bored, awesome, addBored, addAwesome;

    public NBAVideoInfo(Integer id, String urlAddress, Integer bored, Integer awesome, Date date) {
        this.id = id;
        this.urlAddress = urlAddress;
        this.date = date;
        this.bored = bored;
        this.awesome = awesome;
        this.addBored = 0;
        this.addAwesome = 0;
    }
}
