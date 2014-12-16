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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ActivityTop10Main extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener{
    private final String TAG = "Top10";
    private ArrayList<NBAVideoInfo> nbaVideoInfoArrayList;
    private ArrayList<NBADailyHighlight> nbaDailyHighlightsArrayList;

    //TextView top10TitleTextView;
    RadioGroup top10HighlightRadioGroup;
    RadioButton top10RadioButton;
    RadioButton highlightRadioButton;

    ListView nbaTop10ListView;
    ListView nbaDailyHighlightListView;

    ImageView reloadImageView;
    LayoutInflater inflater;
    NBAVideoListAdapter nbaVideoListAdapter;
    NBADailyHighlightListAdapter nbaDailyHighlightListAdapter;
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
        top10HighlightRadioGroup  = (RadioGroup) findViewById(R.id.top10_highlight);
        top10RadioButton          = (RadioButton) findViewById(R.id.top10_title);
        highlightRadioButton      = (RadioButton) findViewById(R.id.highlight_title);
        //top10TitleTextView        = (TextView) findViewById(R.id.top10_title);
        nbaTop10ListView          = (ListView) findViewById(R.id.nba_top10_list);
        nbaDailyHighlightListView = (ListView) findViewById(R.id.nba_highlight_list);
        reloadImageView           = (ImageView) findViewById(R.id.reload);
    }

    private void setViews () {


        nbaVideoInfoArrayList = new ArrayList<NBAVideoInfo>();
        nbaVideoListAdapter = new NBAVideoListAdapter(this, R.layout.nba_video_template);
        nbaTop10ListView.setAdapter(nbaVideoListAdapter);
        nbaTop10ListView.setOnItemClickListener(this);

        nbaDailyHighlightsArrayList = new ArrayList<NBADailyHighlight>();
        nbaDailyHighlightListAdapter = new NBADailyHighlightListAdapter(this, R.layout.highlight_daily_template);
        nbaDailyHighlightListView.setAdapter(nbaDailyHighlightListAdapter);
        nbaDailyHighlightListView.setOnItemClickListener(this);

        reloadImageView.setOnClickListener(this);

        Typeface font = Typeface.createFromAsset(getAssets(), "futura-medium.ttf");
        top10RadioButton.setTypeface(font);
        highlightRadioButton.setTypeface(font);
        top10HighlightRadioGroup.setOnCheckedChangeListener(this);
        //top10TitleTextView.setTypeface(font);
        loadTop10();
        loadHighlight();
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
            long dateInEpoch;

            nbaVideoInfoArrayList.clear();
            for (int i = 0; i < length; i ++) {
                top10JSONObject = top10ListJSONArray.getJSONObject(i);
                id              = top10JSONObject.getInt("id");
                urlAddress      = top10JSONObject.getString("url");
                bored           = top10JSONObject.getInt("bored");
                awesome         = top10JSONObject.getInt("awesome");
                dateInEpoch     = top10JSONObject.getLong("dateInEpoch");
                nbaVideoInfoArrayList.add(new NBAVideoInfo(id, urlAddress,bored, awesome, dateInEpoch));
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
            String highlightLists = getRequest.execute(Constant.NBAAPI.HIGHLIGHT_URL).get();
            JSONArray highlightListsJSONArray = new JSONArray(highlightLists);
            JSONObject highlightListsJSONObject;
            JSONArray highlights;
            int length = highlightListsJSONArray.length();

            nbaDailyHighlightsArrayList.clear();
            for (int i = 0; i < length; i ++) {
                highlightListsJSONObject = highlightListsJSONArray.getJSONObject(i);
                highlights = highlightListsJSONObject.getJSONArray("highlights");

                calendar.setTimeInMillis(highlightListsJSONObject.getLong("dateInEpoch"));
                nbaDailyHighlightsArrayList.add(new NBADailyHighlight(highlightListsJSONObject.getLong("dateInEpoch"), highlights));
            }
            nbaDailyHighlightListAdapter.setNbaDailyHighlightArrayList(nbaDailyHighlightsArrayList);

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
        } else if (adapterView.getAdapter() == nbaDailyHighlightListAdapter) {
            NBADailyHighlight nbaDailyHighlight = nbaDailyHighlightListAdapter.getItem(pos);
            Log.d(TAG, " " + pos);
            Intent intent = new Intent(this, ActivityHighlight.class);
            intent.putExtra(Constant.DATE, nbaDailyHighlight.dateInEpoch);
            intent.putExtra(Constant.DAILY_HIGHLIGHTS, nbaDailyHighlight.highlights.toString());
            startActivity(intent);
        }
    }

    @Override
    public void onPause () {
        uploadAwesomeBored();
        super.onPause();
    }

    @Override
    public void onResume () {
        super.onResume();
        loadTop10();
        loadHighlight();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        int id = radioGroup.getCheckedRadioButtonId();
        switch (radioGroup.getId()) {
            case R.id.top10_highlight:
                if (id == R.id.top10_title) {
                    nbaTop10ListView.setVisibility(View.VISIBLE);
                    nbaDailyHighlightListView.setVisibility(View.INVISIBLE);
                } else if (id == R.id.highlight_title) {
                    nbaTop10ListView.setVisibility(View.INVISIBLE);
                    nbaDailyHighlightListView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public class NBADailyHighlightListAdapter extends ArrayAdapter<NBADailyHighlight> implements View.OnClickListener{
        ArrayList<NBADailyHighlight> nbaDailyHighlightArrayList;
        public NBADailyHighlightListAdapter(Context context, int resource) {
            super(context, resource);
            nbaDailyHighlightArrayList = new ArrayList<NBADailyHighlight>();
        }

        public void setNbaDailyHighlightArrayList (ArrayList<NBADailyHighlight> nbaDailyHighlightArrayList) {
            this.nbaDailyHighlightArrayList = nbaDailyHighlightArrayList;
        }

        @Override
        public int getCount () {
            return nbaDailyHighlightArrayList.size();
        }

        @Override
        public NBADailyHighlight getItem (int i) {
            return nbaDailyHighlightArrayList.get(i);
        }

        @Override
        public View getView (int i, View view, ViewGroup viewGroup) {
            NBAHighlightViewTag nbaHighlightViewTag;
            calendar.setTimeInMillis(getItem(i).dateInEpoch);
            String dateString = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH);

            if (view == null) {
                view = inflater.inflate(R.layout.highlight_daily_template, null);
                TextView dateTextView = (TextView) view.findViewById(R.id.date);

                nbaHighlightViewTag = new NBAHighlightViewTag(dateTextView);
                view.setTag(nbaHighlightViewTag);
            } else {
                nbaHighlightViewTag = (NBAHighlightViewTag) view.getTag();
            }

            nbaHighlightViewTag.dateTextView.setText(dateString);

            return view;
        }

        public class NBAHighlightViewTag {
            TextView dateTextView;
            public NBAHighlightViewTag (TextView dateTextView) {
                this.dateTextView = dateTextView;
            }
        }

        @Override
        public void onClick(View view) {

        }
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

            calendar.setTimeInMillis(getItem(i).dateInEpoch);

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
                ImageView boredIcon            = (ImageView) view.findViewById(R.id.bored_icon);
                ImageView awesomeIcon          = (ImageView) view.findViewById(R.id.awesome_icon);

                boredButton.setOnClickListener(this);
                awesomeButton.setOnClickListener(this);

                nbaVideoInfoViewTag = new NBAVideoInfoViewTag(urlAddress, dateTextView, awesomeNumberTextView, boredNumberTextView, awesomeButton, boredButton, awesomeIcon, boredIcon);
                view.setTag(nbaVideoInfoViewTag);
            } else {
                nbaVideoInfoViewTag = (NBAVideoInfoViewTag) view.getTag();
                nbaVideoInfoViewTag.urlAddress = urlAddress;
                //nbaVideoInfoViewTag.date = date;
            }
            nbaVideoInfoViewTag.dateTextView.setText(dateString);
            nbaVideoInfoViewTag.awesomeNumberTextView.setText(awesome.toString());
            nbaVideoInfoViewTag.boredNumberTextView.setText(bored.toString());

            nbaVideoInfoViewTag.awesomeButton.setTag(i);
            nbaVideoInfoViewTag.boredButton.setTag(i);

            if (bored < Constant.NBAVideo.LEVEL1) {
                nbaVideoInfoViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l1));
            } else if (bored >= Constant.NBAVideo.LEVEL1 && bored < Constant.NBAVideo.LEVEL2) {
                nbaVideoInfoViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l2));
            } else if (bored >= Constant.NBAVideo.LEVEL2 && bored < Constant.NBAVideo.LEVEL3) {
                nbaVideoInfoViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l3));
            } else if (bored >= Constant.NBAVideo.LEVEL3 && bored < Constant.NBAVideo.LEVEL4) {
                nbaVideoInfoViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l4));
            } else if (bored >= Constant.NBAVideo.LEVEL4 && bored < Constant.NBAVideo.LEVEL5) {
                nbaVideoInfoViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_max));
            } else if (bored >= Constant.NBAVideo.LEVEL5) {
                nbaVideoInfoViewTag.boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_blow));
            }

            if (awesome < Constant.NBAVideo.LEVEL1) {
                nbaVideoInfoViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l1));
            } else if (awesome >= Constant.NBAVideo.LEVEL1 && awesome < Constant.NBAVideo.LEVEL2) {
                nbaVideoInfoViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l2));
            } else if (awesome >= Constant.NBAVideo.LEVEL2 && awesome < Constant.NBAVideo.LEVEL3) {
                nbaVideoInfoViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l3));
            } else if (awesome >= Constant.NBAVideo.LEVEL3 && awesome < Constant.NBAVideo.LEVEL4) {
                nbaVideoInfoViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l4));
            } else if (awesome >= Constant.NBAVideo.LEVEL4 && awesome < Constant.NBAVideo.LEVEL5) {
                nbaVideoInfoViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_max));
            } else if (awesome >= Constant.NBAVideo.LEVEL5) {
                nbaVideoInfoViewTag.awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_blow));
            }

            return view;
        }

        @Override
        public void onClick(View view) {
            NBAVideoInfo nbaVideoInfo;
            int index;
            Tracker appTracker = ((Top10Application) getApplication()).getTracker(Top10Application.TrackerName.APP_TRACKER);
            appTracker.setScreenName("Home");
            appTracker.enableAdvertisingIdCollection(true);

            switch (view.getId()) {
                case R.id.awesome:
                    index = (Integer) view.getTag();
                    nbaVideoInfo = getItem(index);
                    nbaVideoInfo.awesome ++;
                    nbaVideoInfo.addAwesome ++;
                    Log.d(TAG, nbaVideoInfo.awesome.toString());

                    TextView awesomeNumber = (TextView) view.findViewById(R.id.awesome_number);
                    ImageView awesomeIcon  = (ImageView) view.findViewById(R.id.awesome_icon);

                    awesomeNumber.setText(nbaVideoInfo.awesome.toString());

                    if (nbaVideoInfo.awesome < Constant.NBAVideo.LEVEL1) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l1));
                    } else if (nbaVideoInfo.awesome >= Constant.NBAVideo.LEVEL1 && nbaVideoInfo.awesome < Constant.NBAVideo.LEVEL2) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l2));
                    } else if (nbaVideoInfo.awesome >= Constant.NBAVideo.LEVEL2 && nbaVideoInfo.awesome < Constant.NBAVideo.LEVEL3) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l3));
                    } else if (nbaVideoInfo.awesome >= Constant.NBAVideo.LEVEL3 && nbaVideoInfo.awesome < Constant.NBAVideo.LEVEL4) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_l4));
                    } else if (nbaVideoInfo.awesome >= Constant.NBAVideo.LEVEL4 && nbaVideoInfo.awesome < Constant.NBAVideo.LEVEL5) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_max));
                    } else if (nbaVideoInfo.awesome >= Constant.NBAVideo.LEVEL5) {
                        awesomeIcon.setBackground(getResources().getDrawable(R.drawable.awesome_button_blow));
                    }



                    appTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Preference")
                            .setAction("Awesome")
                            .build());
                    break;
                case R.id.bored:
                    index = (Integer) view.getTag();
                    nbaVideoInfo = getItem(index);
                    nbaVideoInfo.bored ++;
                    nbaVideoInfo.addBored ++;

                    TextView boredNumber = (TextView) view.findViewById(R.id.bored_number);
                    ImageView boredIcon = (ImageView) view.findViewById(R.id.bored_icon);

                    boredNumber.setText(nbaVideoInfo.bored.toString());

                    if (nbaVideoInfo.bored < Constant.NBAVideo.LEVEL1) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l1));
                    } else if (nbaVideoInfo.bored >= Constant.NBAVideo.LEVEL1 && nbaVideoInfo.bored < Constant.NBAVideo.LEVEL2) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l2));
                    } else if (nbaVideoInfo.bored >= Constant.NBAVideo.LEVEL2 && nbaVideoInfo.bored < Constant.NBAVideo.LEVEL3) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l3));
                    } else if (nbaVideoInfo.bored >= Constant.NBAVideo.LEVEL3 && nbaVideoInfo.bored < Constant.NBAVideo.LEVEL4) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_l4));
                    } else if (nbaVideoInfo.bored >= Constant.NBAVideo.LEVEL4 && nbaVideoInfo.bored < Constant.NBAVideo.LEVEL5) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_max));
                    } else if (nbaVideoInfo.bored >= Constant.NBAVideo.LEVEL5) {
                        boredIcon.setBackground(getResources().getDrawable(R.drawable.bored_button_blow));
                    }

                    appTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Preference")
                            .setAction("Boring")
                            .build());
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
            ImageView awesomeIcon, boredIcon;
            public NBAVideoInfoViewTag(String urlAddress,
                                       TextView dateTextView,
                                       TextView awesomeNumberTextView, TextView boredNumberTextView,
                                       RelativeLayout awesomeButton, RelativeLayout boredButton,
                                       ImageView awesomeIcon, ImageView boredIcon) {
                this.urlAddress = urlAddress;
                this.date = date;
                this.dateTextView = dateTextView;
                this.awesomeNumberTextView = awesomeNumberTextView;
                this.boredNumberTextView = boredNumberTextView;
                this.awesomeButton = awesomeButton;
                this.boredButton = boredButton;
                this.awesomeIcon = awesomeIcon;
                this.boredIcon = boredIcon;
            }
        }
    }
}
