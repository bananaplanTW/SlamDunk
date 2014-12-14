package bp.com.slamdunk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class ActivityTop10Main extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener{
    private final String TAG = "Top10";
    private ArrayList<NBAVideoInfo> nbaVideoInfoArrayList;

    ListView nbaTop10ListView;
    ImageView reloadImageView;
    LayoutInflater inflater;
    NBAVideoListAdapter nbaVideoListAdapter;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10_main);
        inflater = getLayoutInflater();
        calendar = Calendar.getInstance();

        initViews();
        setViews();
    }

    private void initViews () {
        getActionBar().hide();
        nbaTop10ListView = (ListView) findViewById(R.id.nba_top10_list);
        reloadImageView = (ImageView) findViewById(R.id.reload);
    }

    private void setViews () {
        nbaVideoInfoArrayList = new ArrayList<NBAVideoInfo>();
        nbaVideoListAdapter = new NBAVideoListAdapter(this, R.layout.nba_video_template);
        nbaTop10ListView.setAdapter(nbaVideoListAdapter);
        nbaTop10ListView.setOnItemClickListener(this);
        reloadImageView.setOnClickListener(this);

        loadTop10();
    }

    private void loadTop10 () {
        if (!CheckNetworkStatus.isConnected(this)){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.check_network_status)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //do nothing
                }
            });
            alertDialog.show();
            return;
        }

        RestfulUtility.GetRequest getRequest = new RestfulUtility.GetRequest();
        try {
            String top10Lists = getRequest.execute(Constant.NBAAPI.TOP10_URL).get();
            JSONArray top10ListJSONArray = new JSONArray(top10Lists);
            JSONObject top10JSONObject;
            String urlAddress;
            int length = top10ListJSONArray.length();
            int id, bored, awesome;

            nbaVideoInfoArrayList.clear();
            for (int i = 0; i < length; i ++) {
                top10JSONObject = top10ListJSONArray.getJSONObject(i);
                id              = top10JSONObject.getInt("id");
                urlAddress      = top10JSONObject.getString("url");
                bored           = top10JSONObject.getInt("bored");
                awesome         = top10JSONObject.getInt("awesome");
                calendar.setTimeInMillis(top10JSONObject.getLong("dateInEpoch"));
                nbaVideoInfoArrayList.add(new NBAVideoInfo(id, urlAddress,bored, awesome, calendar.getTime()));
            }

            nbaVideoListAdapter.setNbaVideoInfoArrayList(nbaVideoInfoArrayList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadAwesomeBored () {
        if (!CheckNetworkStatus.isConnected(this)){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.check_network_status)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });
            alertDialog.show();
            return;
        }

        try {

            JSONArray top10ListJSONArray = new JSONArray();
            JSONObject top10JSONObject;
            int length = nbaVideoInfoArrayList.size();

            for (int i = 0; i < length; i ++) {
                NBAVideoInfo nbaVideoInfo = nbaVideoInfoArrayList.get(i);
                if (nbaVideoInfo.addBored > 0 || nbaVideoInfo.addAwesome > 0) {
                    top10JSONObject = new JSONObject();
                    top10JSONObject.put("id", nbaVideoInfo.id);
                    top10JSONObject.put("addBored", nbaVideoInfo.addBored);
                    top10JSONObject.put("addAwesome", nbaVideoInfo.addAwesome);
                    top10ListJSONArray.put(top10JSONObject);
                }
            }
            Log.d(TAG, top10ListJSONArray.toString());
            RestfulUtility.PostRequest postRequest = new RestfulUtility.PostRequest(top10ListJSONArray.toString());
            postRequest.execute(Constant.NBAAPI.UPDATE_TOP10_AWESOME_BORED_URL).get();

            for (int i = 0; i < length; i ++) {
                NBAVideoInfo nbaVideoInfo = nbaVideoInfoArrayList.get(i);
                nbaVideoInfo.addBored = 0;
                nbaVideoInfo.addAwesome = 0;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reload:
                uploadAwesomeBored();
                loadTop10();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
        if (adapterView.getAdapter() == nbaVideoListAdapter) {
            NBAVideoListAdapter.NBAVideoInfoViewTag nbaVideoInfoViewTag = (NBAVideoListAdapter.NBAVideoInfoViewTag) view.getTag();
            Log.d(TAG, nbaVideoInfoViewTag.urlAddress);
            Intent intent = new Intent(this, ActivityPlayVideo.class);
            intent.putExtra(Constant.VIDEO_STREAMING_URL, nbaVideoInfoViewTag.urlAddress);
            startActivity(intent);
        }
    }

    @Override
    public void onPause () {
        uploadAwesomeBored();
        super.onPause();
    }

    public class NBAVideoListAdapter extends ArrayAdapter<NBAVideoInfo> implements View.OnClickListener{
        ArrayList<NBAVideoInfo> nbaVideoInfoArrayList;

        public NBAVideoListAdapter(Context context, int resource) {
            super(context, resource);
            nbaVideoInfoArrayList = new ArrayList<NBAVideoInfo>();
        }

        public void setNbaVideoInfoArrayList (ArrayList<NBAVideoInfo> nbaVideoInfoArrayList) {
            this.nbaVideoInfoArrayList = nbaVideoInfoArrayList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount () {
            return nbaVideoInfoArrayList.size();
        }

        @Override
        public NBAVideoInfo getItem (int i) {
            return nbaVideoInfoArrayList.get(i);
        }

        @Override
        public View getView (int i, View view, ViewGroup viewGroup) {

            Date date = getItem(i).date;
            calendar.setTime(date);

            Integer bored = getItem(i).bored;
            Integer awesome = getItem(i).awesome;

            String urlAddress = getItem(i).urlAddress;
            String dateString = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH);
            NBAVideoInfoViewTag nbaVideoInfoViewTag;
            Log.d(TAG, dateString);


            if (view == null) {
                view = inflater.inflate(R.layout.nba_video_template, null);
                TextView dateTextView          = (TextView) view.findViewById(R.id.date);
                TextView awesomeNumberTextView = (TextView) view.findViewById(R.id.awesome_number);
                TextView boredNumberTextView   = (TextView) view.findViewById(R.id.bored_number);
                RelativeLayout boredButton     = (RelativeLayout) view.findViewById(R.id.bored);
                RelativeLayout awesomeButton   = (RelativeLayout) view.findViewById(R.id.awesome);

                boredButton.setOnClickListener(this);
                awesomeButton.setOnClickListener(this);

                nbaVideoInfoViewTag = new NBAVideoInfoViewTag(urlAddress, date, dateTextView, awesomeNumberTextView, boredNumberTextView, awesomeButton, boredButton);
                view.setTag(nbaVideoInfoViewTag);
            } else {
                nbaVideoInfoViewTag = (NBAVideoInfoViewTag) view.getTag();
                nbaVideoInfoViewTag.urlAddress = urlAddress;
                nbaVideoInfoViewTag.date = date;
            }
            nbaVideoInfoViewTag.dateTextView.setText(dateString);
            nbaVideoInfoViewTag.awesomeNumberTextView.setText(awesome.toString());
            nbaVideoInfoViewTag.boredNumberTextView.setText(bored.toString());

            nbaVideoInfoViewTag.awesomeButton.setTag(i);
            nbaVideoInfoViewTag.boredButton.setTag(i);

            return view;
        }

        @Override
        public void onClick(View view) {
            NBAVideoInfo nbaVideoInfo;
            int index;

            switch (view.getId()) {
                case R.id.awesome:
                    index = (Integer) view.getTag();
                    nbaVideoInfo = getItem(index);
                    nbaVideoInfo.awesome ++;
                    nbaVideoInfo.addAwesome ++;
                    Log.d(TAG, nbaVideoInfo.awesome.toString());

                    TextView awesomeNumber = (TextView) view.findViewById(R.id.awesome_number);
                    awesomeNumber.setText(nbaVideoInfo.awesome.toString());
                    break;
                case R.id.bored:
                    index = (Integer) view.getTag();
                    nbaVideoInfo = getItem(index);
                    nbaVideoInfo.bored ++;
                    nbaVideoInfo.addBored ++;

                    TextView boredNumber = (TextView) view.findViewById(R.id.bored_number);
                    boredNumber.setText(nbaVideoInfo.bored.toString());
                    break;
            }
        }

        public class NBAVideoInfoViewTag {
            String urlAddress;
            Date date;
            TextView dateTextView;
            TextView awesomeNumberTextView;
            TextView boredNumberTextView;
            RelativeLayout awesomeButton, boredButton;
            public NBAVideoInfoViewTag(String urlAddress,
                                       Date date, TextView dateTextView,
                                       TextView awesomeNumberTextView, TextView boredNumberTextView,
                                       RelativeLayout awesomeButton, RelativeLayout boredButton) {
                this.urlAddress = urlAddress;
                this.date = date;
                this.dateTextView = dateTextView;
                this.awesomeNumberTextView = awesomeNumberTextView;
                this.boredNumberTextView = boredNumberTextView;
                this.awesomeButton = awesomeButton;
                this.boredButton = boredButton;
            }
        }
    }
}
