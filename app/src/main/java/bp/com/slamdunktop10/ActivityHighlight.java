package bp.com.slamdunktop10;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by daz on 12/16/14.
 */
public class ActivityHighlight extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener{
    private final String TAG = "ActivityHighlight";
    private Long dateInEpoch;
    private LayoutInflater inflater;
    private Calendar calendar;

    private Intent intent;
    private TextView dateTextView;
    private TextView highlightTextView;
    private ListView highlightListView;
    private ImageView reloadImageView;

    ArrayList<NBAHighlight> nbaHighlightArrayList;
    NBADailyHighlightListAdapter nbaDailyHighlightListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.activity_hightlight);
        inflater = getLayoutInflater();
        calendar = Calendar.getInstance();
        intent   = getIntent();

        initViews();
        setViews();
    }

    @Override
    public void onPause () {
        uploadAwesomeBored();
        super.onPause();
    }

    private void initViews () {
        reloadImageView   = (ImageView) findViewById(R.id.reload);
        dateTextView      = (TextView) findViewById(R.id.highlight_date);
        highlightTextView = (TextView) findViewById(R.id.highlight_title);
        highlightListView = (ListView) findViewById(R.id.nba_highlight_list);
    }

    private void setViews () {

        //setting day string
        dateInEpoch = intent.getExtras().getLong(Constant.DATE);
        calendar.setTimeInMillis(dateInEpoch);
        String dayString = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH);
        dateTextView.setText(dayString);

        nbaHighlightArrayList = new ArrayList<NBAHighlight>();
        nbaDailyHighlightListAdapter = new NBADailyHighlightListAdapter(this, R.layout.nba_video_template);
        highlightListView.setAdapter(nbaDailyHighlightListAdapter);
        highlightListView.setOnItemClickListener(this);

        reloadImageView.setOnClickListener(this);

        Typeface font = Typeface.createFromAsset(getAssets(), "futura-medium.ttf");
        dateTextView.setTypeface(font);
        highlightTextView.setTypeface(font);

        parseHighlight(intent.getExtras().getString(Constant.DAILY_HIGHLIGHTS));

    }

    private void parseHighlight (String highlightJSONString) {
        try {
            JSONArray highlightsJSONArray = new JSONArray(highlightJSONString);
            JSONObject highlightJSONObject;
            String urlAddress, title;
            int length = highlightsJSONArray.length();
            int id, bored, awesome;

            nbaHighlightArrayList.clear();
            for (int i = 0; i < length; i ++) {
                highlightJSONObject = highlightsJSONArray.getJSONObject(i);
                id              = highlightJSONObject.getInt("id");
                title           = highlightJSONObject.getString("title");
                urlAddress      = highlightJSONObject.getString("url");
                bored           = highlightJSONObject.getInt("bored");
                awesome         = highlightJSONObject.getInt("awesome");

                nbaHighlightArrayList.add(new NBAHighlight(id, urlAddress,bored, awesome, title));
            }
            nbaDailyHighlightListAdapter.setNbaDailyHighlightArrayList(nbaHighlightArrayList);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadHighlight () {
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
            String url = Constant.NBAAPI.HIGHLIGHT_URL_WITH_DATE + dateInEpoch.toString();
            Log.d(TAG, dateInEpoch.toString());
            String top10Lists = getRequest.execute(url).get();
            Log.d(TAG, top10Lists);

            parseHighlight(top10Lists);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }/* catch (JSONException e) {
            e.printStackTrace();
        }*/
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

            JSONArray highlightListJSONArray = new JSONArray();
            JSONObject highlightJSONObject;
            int length = nbaHighlightArrayList.size();

            for (int i = 0; i < length; i ++) {
                NBAHighlight nbaHighlight = nbaHighlightArrayList.get(i);
                if (nbaHighlight.addBored > 0 || nbaHighlight.addAwesome > 0) {
                    highlightJSONObject = new JSONObject();
                    highlightJSONObject.put("id", nbaHighlight.id);
                    highlightJSONObject.put("addBored", nbaHighlight.addBored);
                    highlightJSONObject.put("addAwesome", nbaHighlight.addAwesome);
                    highlightListJSONArray.put(highlightJSONObject);
                }
            }
            Log.d(TAG, highlightListJSONArray.toString());
            RestfulUtility.PostRequest postRequest = new RestfulUtility.PostRequest(highlightListJSONArray.toString());
            postRequest.execute(Constant.NBAAPI.UPDATE_HIGHLIGHT_AWESOME_BORED_URL).get();

            for (int i = 0; i < length; i ++) {
                NBAHighlight nbaHighlight = nbaHighlightArrayList.get(i);
                nbaHighlight.addBored = 0;
                nbaHighlight.addAwesome = 0;
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
                loadHighlight();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
        if (adapterView.getAdapter() == nbaDailyHighlightListAdapter) {
            NBADailyHighlightListAdapter.NBAHighlightViewTag nbaVideoInfoViewTag = (NBADailyHighlightListAdapter.NBAHighlightViewTag) view.getTag();
            Intent intent = new Intent(this, ActivityPlayVideo.class);
            intent.putExtra(Constant.VIDEO_STREAMING_URL, nbaVideoInfoViewTag.urlAddress);
            startActivity(intent);
        }
    }

    public class NBADailyHighlightListAdapter extends ArrayAdapter<NBAHighlight> implements View.OnClickListener{
        ArrayList<NBAHighlight> nbaHighlightArrayList;
        public NBADailyHighlightListAdapter(Context context, int resource) {
            super(context, resource);
            nbaHighlightArrayList = new ArrayList<NBAHighlight>();
        }

        public void setNbaDailyHighlightArrayList (ArrayList<NBAHighlight> nbaDailyHighlightArrayList) {
            this.nbaHighlightArrayList = nbaDailyHighlightArrayList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount () {
            return nbaHighlightArrayList.size();
        }

        @Override
        public NBAHighlight getItem (int i) {
            return nbaHighlightArrayList.get(i);
        }

        @Override
        public View getView (int i, View view, ViewGroup viewGroup) {
            // setting bored and awesome
            Integer bored = getItem(i).bored;
            Integer awesome = getItem(i).awesome;

            // setting url address
            String urlAddress = getItem(i).urlAddress;

            // setting title
            String title = getItem(i).title;

            // setting date string
            NBAHighlightViewTag nbaHighlightViewTag;

            if (view == null) {
                view = inflater.inflate(R.layout.nba_video_template, null);
                TextView titleTextView         = (TextView) view.findViewById(R.id.title);
                TextView awesomeNumberTextView = (TextView) view.findViewById(R.id.awesome_number);
                TextView boredNumberTextView   = (TextView) view.findViewById(R.id.bored_number);
                RelativeLayout boredButton     = (RelativeLayout) view.findViewById(R.id.bored);
                RelativeLayout awesomeButton   = (RelativeLayout) view.findViewById(R.id.awesome);
                ImageView boredIcon            = (ImageView) view.findViewById(R.id.bored_icon);
                ImageView awesomeIcon          = (ImageView) view.findViewById(R.id.awesome_icon);

                boredButton.setOnClickListener(this);
                awesomeButton.setOnClickListener(this);

                nbaHighlightViewTag = new NBAHighlightViewTag(urlAddress, titleTextView, awesomeNumberTextView, boredNumberTextView, awesomeButton, boredButton, awesomeIcon, boredIcon);
                view.setTag(nbaHighlightViewTag);
            } else {
                nbaHighlightViewTag = (NBAHighlightViewTag) view.getTag();
                nbaHighlightViewTag.urlAddress = urlAddress;
            }
            nbaHighlightViewTag.titleTextView.setText(title);
            nbaHighlightViewTag.awesomeNumberTextView.setText(awesome.toString());
            nbaHighlightViewTag.boredNumberTextView.setText(bored.toString());

            nbaHighlightViewTag.awesomeButton.setTag(i);
            nbaHighlightViewTag.boredButton.setTag(i);

            if (bored < Constant.NBAVideo.LEVEL1) {
                nbaHighlightViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l1));
            } else if (bored >= Constant.NBAVideo.LEVEL1 && bored < Constant.NBAVideo.LEVEL2) {
                nbaHighlightViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l2));
            } else if (bored >= Constant.NBAVideo.LEVEL2 && bored < Constant.NBAVideo.LEVEL3) {
                nbaHighlightViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l3));
            } else if (bored >= Constant.NBAVideo.LEVEL3 && bored < Constant.NBAVideo.LEVEL4) {
                nbaHighlightViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l4));
            } else if (bored >= Constant.NBAVideo.LEVEL4 && bored < Constant.NBAVideo.LEVEL5) {
                nbaHighlightViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_max));
            } else if (bored >= Constant.NBAVideo.LEVEL5) {
                nbaHighlightViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_blow));
            }

            if (awesome < Constant.NBAVideo.LEVEL1) {
                nbaHighlightViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l1));
            } else if (awesome >= Constant.NBAVideo.LEVEL1 && awesome < Constant.NBAVideo.LEVEL2) {
                nbaHighlightViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l2));
            } else if (awesome >= Constant.NBAVideo.LEVEL2 && awesome < Constant.NBAVideo.LEVEL3) {
                nbaHighlightViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l3));
            } else if (awesome >= Constant.NBAVideo.LEVEL3 && awesome < Constant.NBAVideo.LEVEL4) {
                nbaHighlightViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l4));
            } else if (awesome >= Constant.NBAVideo.LEVEL4 && awesome < Constant.NBAVideo.LEVEL5) {
                nbaHighlightViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_max));
            } else if (awesome >= Constant.NBAVideo.LEVEL5) {
                nbaHighlightViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_blow));
            }

            return view;
        }

        public class NBAHighlightViewTag {
            String urlAddress;
            TextView titleTextView;
            TextView awesomeNumberTextView;
            TextView boredNumberTextView;
            RelativeLayout awesomeButton, boredButton;
            ImageView awesomeIcon, boredIcon;
            public NBAHighlightViewTag(String urlAddress,
                                       TextView titleTextView,
                                       TextView awesomeNumberTextView, TextView boredNumberTextView,
                                       RelativeLayout awesomeButton, RelativeLayout boredButton,
                                       ImageView awesomeIcon, ImageView boredIcon) {
                this.urlAddress = urlAddress;
                this.titleTextView = titleTextView;
                this.awesomeNumberTextView = awesomeNumberTextView;
                this.boredNumberTextView = boredNumberTextView;
                this.awesomeButton = awesomeButton;
                this.boredButton = boredButton;
                this.awesomeIcon = awesomeIcon;
                this.boredIcon = boredIcon;
            }
        }

        @Override
        public void onClick(View view) {

            NBAHighlight nbaHighlight;
            int index;
            Tracker appTracker = ((Top10Application) getApplication()).getTracker(Top10Application.TrackerName.APP_TRACKER);
            appTracker.setScreenName("Highlight");
            appTracker.enableAdvertisingIdCollection(true);

            switch (view.getId()) {
                case R.id.awesome:
                    index = (Integer) view.getTag();
                    nbaHighlight = getItem(index);
                    nbaHighlight.awesome ++;
                    nbaHighlight.addAwesome ++;
                    Log.d(TAG, nbaHighlight.awesome.toString());

                    TextView awesomeNumber = (TextView) view.findViewById(R.id.awesome_number);
                    ImageView awesomeIcon  = (ImageView) view.findViewById(R.id.awesome_icon);

                    awesomeNumber.setText(nbaHighlight.awesome.toString());

                    if (nbaHighlight.awesome < Constant.NBAVideo.LEVEL1) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l1));
                    } else if (nbaHighlight.awesome >= Constant.NBAVideo.LEVEL1 && nbaHighlight.awesome < Constant.NBAVideo.LEVEL2) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l2));
                    } else if (nbaHighlight.awesome >= Constant.NBAVideo.LEVEL2 && nbaHighlight.awesome < Constant.NBAVideo.LEVEL3) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l3));
                    } else if (nbaHighlight.awesome >= Constant.NBAVideo.LEVEL3 && nbaHighlight.awesome < Constant.NBAVideo.LEVEL4) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l4));
                    } else if (nbaHighlight.awesome >= Constant.NBAVideo.LEVEL4 && nbaHighlight.awesome < Constant.NBAVideo.LEVEL5) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_max));
                    } else if (nbaHighlight.awesome >= Constant.NBAVideo.LEVEL5) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_blow));
                    }



                    appTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Preference")
                            .setAction("Awesome")
                            .build());
                    break;
                case R.id.bored:
                    index = (Integer) view.getTag();
                    nbaHighlight = getItem(index);
                    nbaHighlight.bored ++;
                    nbaHighlight.addBored ++;

                    TextView boredNumber = (TextView) view.findViewById(R.id.bored_number);
                    ImageView boredIcon = (ImageView) view.findViewById(R.id.bored_icon);

                    boredNumber.setText(nbaHighlight.bored.toString());

                    if (nbaHighlight.bored < Constant.NBAVideo.LEVEL1) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l1));
                    } else if (nbaHighlight.bored >= Constant.NBAVideo.LEVEL1 && nbaHighlight.bored < Constant.NBAVideo.LEVEL2) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l2));
                    } else if (nbaHighlight.bored >= Constant.NBAVideo.LEVEL2 && nbaHighlight.bored < Constant.NBAVideo.LEVEL3) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l3));
                    } else if (nbaHighlight.bored >= Constant.NBAVideo.LEVEL3 && nbaHighlight.bored < Constant.NBAVideo.LEVEL4) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l4));
                    } else if (nbaHighlight.bored >= Constant.NBAVideo.LEVEL4 && nbaHighlight.bored < Constant.NBAVideo.LEVEL5) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_max));
                    } else if (nbaHighlight.bored >= Constant.NBAVideo.LEVEL5) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_blow));
                    }

                    appTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Preference")
                            .setAction("Boring")
                            .build());
                    break;
            }
        }
    }
}
