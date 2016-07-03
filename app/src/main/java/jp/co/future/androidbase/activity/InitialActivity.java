package jp.co.future.androidbase.activity;

/**
 * Created by itaru on 2016/07/03.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Types.BoomType;
import com.nightonke.boommenu.Types.ButtonType;
import com.nightonke.boommenu.Types.PlaceType;
import com.nightonke.boommenu.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import jp.co.future.androidbase.Orientation;
import jp.co.future.androidbase.R;
import jp.co.future.androidbase.adapter.InitialAdapter;
import jp.co.future.androidbase.model.InitialModel;
import jp.co.future.androidbase.model.TimeLineModel;
// other imports and package statement are omitted

public class InitialActivity extends AppCompatActivity
        implements ObservableScrollViewCallbacks, TextToSpeech.OnInitListener {

    /**
     * ログ出力用タグ
     */
    private static final String TAG = InitialActivity.class.getSimpleName();


    public final static String TAG_ORIENTATION = "orientation";

    private BoomMenuButton boomMenuButton;

    //firebase
    private DatabaseReference myRef;

    private TextToSpeech tts;

    private String listName[] = {"SPAJAM2016 FINAL", "くまがや温泉会議", "さいたま温泉会議", "ソフトバンク温泉会議",
            "ふじつう温泉会議", "コロプラ温泉会議", "ドワンゴ温泉会議", "DeNA温泉会議", "アイスタイル温泉会議",
            "おおさき温泉会議", "ごたんだ温泉会議", "ヘリテージ温泉会議",
            "おおさき温泉会議", "ごたんだ温泉会議", "ヘリテージ温泉会議"};
    private int imgName[] = {R.drawable.spajam, R.drawable.pho_slide_conference_02, R.drawable.iiyama, R.drawable.onsen
            , R.drawable.pho_slide_conference_02, R.drawable.onsen, R.drawable.pho_slide_conference_02, R.drawable.iiyama
            , R.drawable.pho_slide_conference_02, R.drawable.onsen, R.drawable.pho_slide_conference_02, R.drawable.iiyama
            , R.drawable.pho_slide_conference_02, R.drawable.iiyama, R.drawable.onsen};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        ObservableListView listView = (ObservableListView) findViewById(R.id.list);
        listView.setScrollViewCallbacks(this);

        // TODO These are dummy. Populate your data here.
        ArrayList<InitialModel> items = new ArrayList<>();
        for (int i = 0; i < listName.length; i++) {
            InitialModel model = new InitialModel();
            model.setName(listName[i]);
            model.setImg(imgName[i]);
            items.add(model);
        }


        InitialAdapter adapter = new InitialAdapter(this);
        adapter.setInitialList(items);
        listView.setAdapter(adapter);

        // アイテムクリック時ののイベントを追加
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view, int pos, long id) {

                // 選択アイテムを取得
                ListView listView = (ListView) parent;
                InitialModel item = (InitialModel) listView.getItemAtPosition(pos);

                // firebaseにデータを登録（サンプル）
                // myRef.setValue("テストメッセージ");


                //タイムライン画面遷移
                Intent intent = new Intent(getApplicationContext(), TimelineActivity.class);
                intent.putExtra(TAG_ORIENTATION, Orientation.vertical);
                startActivity(intent);
            }
        });

        // firebaseのセットアップ
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        // Read from the database
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d(TAG, "Value is: " + value);
//                // speechText("あ");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });

        // TextToSpeechオブジェクトの生成
        tts = new TextToSpeech(this, this);

    }

    @Override
    public void onInit(int status) {
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


    }

    @Override
    protected void onResume() {
        super.onResume();

        //メニューボタン
        boomMenuButton = (BoomMenuButton) findViewById(R.id.boom);

        //各メニューが押された時の処理
        boomMenuButton.setOnSubButtonClickListener(new BoomMenuButton.OnSubButtonClickListener() {
            @Override
            public void onClick(int buttonIndex) {
                // return the index of the sub button clicked
                if (0 == buttonIndex) {
                    // BLEの処理？ デバイス検索
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);


                } else if (1 == buttonIndex) {
                    // TODO タッチ画面
                    //タイムライン画面遷移
                    Intent intent = new Intent(getApplicationContext(), CensorActivity.class);
                    startActivity(intent);

                } else if (2 == buttonIndex) {
                    // ここは使わない
                }
            }
        });
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
    public void onScrollChanged(int scrollY, boolean firstScroll,
                                boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = getSupportActionBar();
        if (scrollState == ScrollState.UP) {
            if (ab.isShowing()) {
                ab.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!ab.isShowing()) {
                ab.show();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //メニューボタン作成
        Drawable[] subButtonDrawables = new Drawable[2];
        int[] drawablesResource = new int[]{
                R.drawable.ic_bluetooth_searching_white_48dp,
//                R.drawable.ic_people_white_48dp,
                R.drawable.ic_touch_app_white_48dp
        };
        for (int i = 0; i < 2; i++)
            subButtonDrawables[i] = ContextCompat.getDrawable(this, drawablesResource[i]);

        String[] subButtonTexts = new String[]{"専用デバイス検索", "タッチモード"};

        int[][] subButtonColors = new int[2][2];
        for (int i = 0; i < 2; i++) {
            subButtonColors[i][1] = ContextCompat.getColor(this, R.color.colorPrimary);
            subButtonColors[i][0] = Util.getInstance().getPressedColor(subButtonColors[i][1]);
        }

        boomMenuButton.init(
                subButtonDrawables, // The drawables of images of sub buttons. Can not be null.
                subButtonTexts,     // The texts of sub buttons, ok to be null.
                subButtonColors,    // The colors of sub buttons, including pressed-state and normal-state.
                ButtonType.HAM,     // The button type.
                BoomType.PARABOLA_2,  // The boom type.
                PlaceType.HAM_2_1,  // The place type.
                null,               // Ease type to move the sub buttons when showing.
                null,               // Ease type to scale the sub buttons when showing.
                null,               // Ease type to rotate the sub buttons when showing.
                null,               // Ease type to move the sub buttons when dismissing.
                null,               // Ease type to scale the sub buttons when dismissing.
                null,               // Ease type to rotate the sub buttons when dismissing.
                null                // Rotation degree.
        );

        boomMenuButton.setTextViewColor(ContextCompat.getColor(this, R.color.white));
        boomMenuButton.setSubButtonShadowOffset(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2));

    }

    private void speechText(String word) {

//        Intent i = getIntent();
//        String word = i.getStringExtra("word");

        if (tts.isSpeaking()) {
            // 読み上げ中なら止める
            tts.stop();
        }

        // TODO 音量調整

//        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        float amStreamSystemMaxVol = am.getStreamMaxVolume(am.STREAM_SYSTEM);
//        float amStreamSystemVol = am.getStreamVolume(am.STREAM_SYSTEM);
//        float amStreamSystemRatio = amStreamSystemVol / amStreamSystemMaxVol;
//        Log.d("Volume", "amStreamSystemMaxVol:" + amStreamSystemMaxVol + " amStreamSystemVol:" + amStreamSystemVol + " amStreamSystemRatio:" + amStreamSystemRatio);
        HashMap<String, String> params = new HashMap<String, String>();
//        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(amStreamSystemRatio));

        // 読み上げ開始
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, params);

    }


}