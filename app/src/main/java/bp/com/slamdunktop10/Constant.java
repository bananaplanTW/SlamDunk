package bp.com.slamdunktop10;

/**
 * Created by daz on 12/12/14.
 */
public class Constant {
    public class Server {
        public static final String INSTANCE = "http://runningarea.bitnamiapp.com";
        public static final String NBA_PORT = "8052";
    }

    public class NBAAPI {
        public static final String TOP10_ENDPOINT = "/getTop10";
        public static final String HIGHLIGHT_ENDPOINT = "/getHighlight";
        public static final String UPDATE_TOP10_AWESOME_BORED_ENDPOINT = "/updateTop10AwesomeBored";
        public static final String UPDATE_HIGHLIGHT_AWESOME_BORED_ENDPOINT = "/updateHighlightAwesomeBored";
        public static final String DATE_IN_EPOCH = "dateInEpoch=";
        public static final String TOP10_URL = Server.INSTANCE + ":" + Server.NBA_PORT + TOP10_ENDPOINT;
        public static final String HIGHLIGHT_URL = Server.INSTANCE + ":" + Server.NBA_PORT + HIGHLIGHT_ENDPOINT;
        public static final String HIGHLIGHT_URL_WITH_DATE = Server.INSTANCE + ":" + Server.NBA_PORT + HIGHLIGHT_ENDPOINT + "?" + DATE_IN_EPOCH;
        public static final String UPDATE_TOP10_AWESOME_BORED_URL = Server.INSTANCE + ":" + Server.NBA_PORT + UPDATE_TOP10_AWESOME_BORED_ENDPOINT;
        public static final String UPDATE_HIGHLIGHT_AWESOME_BORED_URL = Server.INSTANCE + ":" + Server.NBA_PORT + UPDATE_HIGHLIGHT_AWESOME_BORED_ENDPOINT;
    }

    public class NBAVideo {
        public static final int LEVEL1 = 100;
        public static final int LEVEL2 = 200;
        public static final int LEVEL3 = 300;
        public static final int LEVEL4 = 400;
        public static final int LEVEL5 = 1000;
    }
    public static final String VIDEO_STREAMING_URL = "videoStreamingUrl";
    public static final String DAILY_HIGHLIGHTS = "dailyHighlights";
    public static final String DATE = "date";
}
