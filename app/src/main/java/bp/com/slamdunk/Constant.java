package bp.com.slamdunk;

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
        public static final String UPDATE_TOP10_AWESOME_BORED_ENDPOINT = "/updateTop10AwesomeBored";
        public static final String TOP10_URL = Server.INSTANCE + ":" + Server.NBA_PORT + TOP10_ENDPOINT;
        public static final String UPDATE_TOP10_AWESOME_BORED_URL = Server.INSTANCE + ":" + Server.NBA_PORT + UPDATE_TOP10_AWESOME_BORED_ENDPOINT;
    }
    public static final String VIDEO_STREAMING_URL = "videoStreamingUrl";
}
