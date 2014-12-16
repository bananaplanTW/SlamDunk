package bp.com.slamdunktop10;

import java.util.Date;

/**
 * Created by daz on 12/16/14.
 */
public class NBAHighlight {
    public Integer id;
    public String urlAddress, title;
    public Date date;
    public Integer bored, awesome, addBored, addAwesome, views;

    public NBAHighlight(Integer id, String urlAddress, Integer bored, Integer awesome, String title) {
        this.id = id;
        this.urlAddress = urlAddress;
        this.title = title;
        this.bored = bored;
        this.awesome = awesome;
        this.addBored = 0;
        this.addAwesome = 0;
    }

    public void setViews (Integer views){
        this.views = views;
    }
}
