package jp.co.future.androidbase.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.future.androidbase.R;
import jp.co.future.androidbase.model.TouchInputModel;

public class CensorActivity extends Activity {

    /*
     * 画面
     */
    private TextView touchValues;
    private TextView hiraganaValues;
    private TextView confirmValues;
    private TextView wordValues;


    /**
     * タッチリスト: 定期実行スレッドからもアクセスされるので同期化が必要
     */
    private List<TouchInputModel> touchList = Collections.synchronizedList(new ArrayList<TouchInputModel>());

    /**
     * 入力されたひらがなリスト（確定はされていない）
     */
    private List<TouchInputModel> inputStreamList = new ArrayList<>();

    /** 入力が確定した「文字」のリスト */
    private List<TouchInputModel> confirmCharList = new ArrayList<>();

    /*
     * ひらがなリスト
     */
    private static final char[] r01 = {'あ', 'い', 'う', 'え', 'お'};
    private static final char[] r02 = {'か', 'き', 'く', 'け', 'こ'};
    private static final char[] r03 = {'さ', 'し', 'す', 'せ', 'そ'};
    private static final char[] r04 = {'た', 'ち', 'つ', 'て', 'と'};
    private static final char[] r05 = {'な', 'に', 'ぬ', 'ね', 'の'};
    private static final char[] r06 = {'は', 'ひ', 'ふ', 'へ', 'ほ'};
    private static final char[] r07 = {'ま', 'み', 'む', 'め', 'も'};
    private static final char[] r08 = {'や', 'い', 'ゆ', 'え', 'よ'};
    private static final char[] r09 = {'わ', 'い', 'う', 'え', 'を'};
    private static final char[] r10 = {'ら', 'り', 'る', 'れ', 'ろ'};
    private static final char[] r11 = {'ん', 'ー', 'っ', '！', '？'};
    private static final char[][] HIRAGANA_MAP = {r01, r02, r03, r04, r05, r06, r07, r08, r09, r10, r11};

    /**
     * 入力から何秒間経過すると、文字確定するかのしきい値[秒]
     */
    private static final double INPUT_TERM_THRESHOLD = 0.4;
    /**
     * 入力から何秒間通過すると、テキスト確定するかのしきい値[秒]
     */
    private static final double LINE_BREAK_THRESHOLD = 3.0;

    /** 最後に入力した時間（テキスト確定処理で利用） */
    private long lastInputTime = 0;

    /**
     * 最大のタッチ本数
     */
    private int maxPointerCount = 0;

    /*
     * 定期実行
     * タッチ入力を定期的に集約してサーバに送信する
     */
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_censor);

        touchValues = (TextView) findViewById(R.id.touchText);
        hiraganaValues = (TextView) findViewById(R.id.hiragana);
        confirmValues = (TextView) findViewById(R.id.confirmchar);
        wordValues = (TextView) findViewById(R.id.word);

        // 定期実行処理
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (confirmCharList.size() > 0) {
                            long now = new Date().getTime();
                            double term = (double) (((double) now - (double) lastInputTime) / (double) 1000);

                            if (term >= LINE_BREAK_THRESHOLD) {
                                // しきい値を超えたらリスト集約
                                char[] chars = new char[confirmCharList.size()];
                                for (int i = 0; i < confirmCharList.size(); i++) {
                                    TouchInputModel touchInput = confirmCharList.get(i);
                                    chars[i] = touchInput.getCharset();
                                }

                                // いまあるやつは新規生成
                                inputStreamList = Collections.synchronizedList(new ArrayList<TouchInputModel>());

                                wordValues.setText("確定されたテキスト（送信）：" + new String(chars));

                                // 確定したらまた新規生成
                                confirmCharList = new ArrayList<TouchInputModel>();
                            }
                        }

                        //TODO 本当はロック処理が必要
                        // ロックオブジェクトを作ってsynchronizedする必要

                        List<TouchInputModel> list = inputStreamList;

                        if (list.size() == 0) {
                            // 未入力の場合はスキップ
                            return;
                        }

                        // 最後の入力時間を取得
                        TouchInputModel lastTouchInput = list.get(list.size() - 1);

                        long inputTime = lastTouchInput.getInputTime();
                        long now = new Date().getTime();
                        double term = (double) (((double) now - (double) inputTime) / (double) 1000);

                        Log.d("□term□", String.valueOf(term));

                        if (term >= INPUT_TERM_THRESHOLD) {
                            // しきい値を超えたらリスト集約

                            // いまあるやつは新規生成
                            inputStreamList = Collections.synchronizedList(new ArrayList<TouchInputModel>());

                            confirmValues.setText("確定された文字：" + lastTouchInput.getCharset());

                            confirmCharList.add(lastTouchInput);

                            // しきい値を超えたらリスト集約
                            char[] chars = new char[confirmCharList.size()];
                            for (int i = 0; i < confirmCharList.size(); i++) {
                                TouchInputModel touchInput = confirmCharList.get(i);
                                chars[i] = touchInput.getCharset();
                            }
                            wordValues.setText("確定されたテキスト：" + new String(chars));
                        }

                    }
                });
            }

        }, 1000, 100); // 実行したい間隔(ミリ秒)

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float pointX = event.getX();
        float pointY = event.getY();
        int count = event.getPointerCount();

        String s = "TouchEventX:" + pointX + ",Y:" + pointY + ",count:" + count;
        touchValues.setText(s);


        if (count > maxPointerCount) {
            // 指を話した瞬間の本数だと意図しないので、最大を保つ
            maxPointerCount = count;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            // すべてのタッチが離れた時に呼ばれる

            TouchInputModel touchInput = new TouchInputModel();
            touchInput.setPointX(pointX);
            touchInput.setPointY(pointY);
            touchInput.setPointCount(count);
            touchInput.setInputTime(new Date().getTime());

            double term = 0;
            if (touchList.size() > 0) {
                // 最後の入力時間を取得
                TouchInputModel lastTouchInput = touchList.get(touchList.size() - 1);

                long inputTime = lastTouchInput.getInputTime();
                long now = new Date().getTime();
                term = (double) (((double) now - (double) inputTime) / (double) 1000);
            }

            if (term >= INPUT_TERM_THRESHOLD) {
                // 時間経過から次の文字入力と判定する使用
                touchList = Collections.synchronizedList(new ArrayList<TouchInputModel>());
            }

            touchList.add(touchInput);

            Log.d("onTouchEvent", "リストに追加されました[" + touchList.toString() + "]");

            char hiragana = HIRAGANA_MAP[(touchList.size() - 1) % (HIRAGANA_MAP.length)][(maxPointerCount - 1) % 5];

            hiraganaValues.setText("判定された文字：" + hiragana);

            // 入力された文字を追加
            touchInput.setCharset(hiragana);
            inputStreamList.add(touchInput);

            // 0本に戻しておく
            maxPointerCount = 0;

            // 最後の入力時間も保持しておく
            lastInputTime = touchInput.getInputTime();

        }

        return true;
    }

    /**
     * 定期実行スレッドをアプリ終了時に殺すためのの処理
     */
    @Override
    protected void onPause() {
        super.onPause();
        //[注] なぜか速攻でスレッドをキャンセルされるのでコメントアウト
//        if (mTimer != null) {
//            mTimer.cancel();
//            mTimer = null;
//        }
    }

}
