package jp.co.future.androidbase.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jp.co.future.androidbase.Orientation;
import jp.co.future.androidbase.R;
import jp.co.future.androidbase.adapter.TimeLineAdapter;
import jp.co.future.androidbase.model.TimeLineModel;

public class TimelineActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    /**
     * ログ出力用タグ
     */
    private static final String TAG = TimelineActivity.class.getSimpleName();

    private TextToSpeech tts;


    private RecyclerView mRecyclerView;

    private static TimeLineAdapter mTimeLineAdapter;

    private static List<TimeLineModel> mDataList = new ArrayList<>();

    private Orientation mOrientation;

    //firebase
    private DatabaseReference myRef;
    private ValueEventListener eventL;

    private Button sentBtn;

    private boolean sayFlg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mOrientation = (Orientation) getIntent().getSerializableExtra(InitialActivity.TAG_ORIENTATION);

        if (mOrientation == Orientation.horizontal) {
            setTitle("タイムライン");
        } else {
            setTitle("タイムライン");
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(getLinearLayoutManager());
        mRecyclerView.setHasFixedSize(true);

        // TextToSpeechオブジェクトの生成
        tts = new TextToSpeech(this, this);

        sentBtn = (Button) findViewById(R.id.sent_button);
        sentBtn.setOnClickListener(this);


        initView();
    }

    private LinearLayoutManager getLinearLayoutManager() {

        if (mOrientation == Orientation.horizontal) {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            return linearLayoutManager;
        } else {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            return linearLayoutManager;
        }

    }

    private void initView() {
        Log.d(TAG, "init");


//        for (int i = 0; i < 20; i++) {
//            TimeLineModel model = new TimeLineModel();
//            model.setName("Random" + i);
//            model.setAge(i);
//            mDataList.add(model);
//        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");


        mTimeLineAdapter = new TimeLineAdapter(mDataList, mOrientation);
        mRecyclerView.setAdapter(mTimeLineAdapter);


    }

    @Override
    public void onInit(int status) {
        sayFlg = false;
        Log.d(TAG, "onInit");
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.JAPAN;
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);
            } else {
                Log.d("", "Error SetLocale");
            }
        } else {
            Log.d("", "Error Init");
        }

        eventL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);


                // TODO バイブレーションパターン
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long[] pattern = {100, 1000, 100, 2000}; // OFF/ON/OFF/ON...
                //long[] pattern = {100, 100}; // OFF/ON/OFF/ON...
                vibrator.vibrate(pattern, -1);

                if(sayFlg){
                    speechText(value);
                }else{
                    sayFlg = true;
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };

        //myRef.removeEventListener(eventL);
        // Read from the database
        myRef.addValueEventListener(eventL);


    }

    @Override
    protected void onPause() {
        super.onPause();
        myRef.removeEventListener(eventL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != tts) {
            // TextToSpeechのリソースを解放する
            tts.shutdown();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Menu
        switch (item.getItemId()) {
            //When home is clicked
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        if (mOrientation != null)
            savedInstanceState.putSerializable(InitialActivity.TAG_ORIENTATION, mOrientation);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(InitialActivity.TAG_ORIENTATION)) {
                mOrientation = (Orientation) savedInstanceState.getSerializable(InitialActivity.TAG_ORIENTATION);
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void speechText(String word) {

//        Intent i = getIntent();
//        String word = i.getStringExtra("word");

        if (tts.isSpeaking()) {
            // 読み上げ中なら止める
            tts.stop();
        }

        TimeLineModel model = new TimeLineModel();
        model.setName(word);
        model.setAge(100);
        mDataList.add(model);
        mTimeLineAdapter.notifyDataSetChanged();

        // TODO 音量調整

//        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        float amStreamSystemMaxVol = am.getStreamMaxVolume(am.STREAM_SYSTEM);
//        float amStreamSystemVol = am.getStreamVolume(am.STREAM_SYSTEM);
//        float amStreamSystemRatio = amStreamSystemVol / amStreamSystemMaxVol;
//        Log.d("Volume", "amStreamSystemMaxVol:" + amStreamSystemMaxVol + " amStreamSystemVol:" + amStreamSystemVol + " amStreamSystemRatio:" + amStreamSystemRatio);
        HashMap<String, String> params = new HashMap<String, String>();
//        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(amStreamSystemRatio));

        // 読み上げ開始
        //AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //int amStreamSystemMaxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
        // 音量を設定
        //am.setStreamVolume(AudioManager.STREAM_MUSIC, amStreamSystemMaxVol, 0);
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, params);

    }

    @Override
    public void onClick(View v) {
        EditText txt = (EditText) findViewById(R.id.timeline_input);
        Log.d(TAG, txt.getText().toString());

        // firebaseにデータを登録（サンプル）
        myRef.setValue(txt.getText().toString());

    }

    public static TimeLineAdapter getmTimeLineAdapter() {
        return mTimeLineAdapter;
    }

    public static List<TimeLineModel> getmDataList() {
        return mDataList;
    }

}

