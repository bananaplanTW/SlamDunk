package bp.com.slamdunktop10;

import java.util.Date;

/**
 * Created by daz on 12/12/14.
 */
public class NBAVideoInfo {
    public Integer id;
    public String urlAddress, title;
    public Long dateInEpoch;
    public Integer bored, awesome, addBored, addAwesome, views;

    public NBAVideoInfo(Integer id, String urlAddress, Integer bored, Integer awesome, Long dateInEpoch) {
        this.id = id;
        this.urlAddress = urlAddress;
        this.dateInEpoch = dateInEpoch;
        this.bored = bored;
        this.awesome = awesome;
        this.addBored = 0;
        this.addAwesome = 0;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public void setViews (Integer views){
        this.views = views;
    }
}
