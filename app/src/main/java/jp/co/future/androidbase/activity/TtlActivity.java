package jp.co.future.androidbase.activity;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

import jp.co.future.androidbase.R;

public class TtlActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttl);

        Button btn = (Button)findViewById(R.id.read_manufacturer_name_button);
        btn.setOnClickListener(this);


        // TextToSpeechオブジェクトの生成
        tts = new TextToSpeech(this, this);

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


        speechText();

    }

    @Override
    public void onClick(View v) {
        speechText();
    }

    private void speechText() {

        if (tts.isSpeaking()) {
            // 読み上げ中なら止める
            tts.stop();
        }

        // 読み上げ開始
        tts.speak("hello world,  こんにちは世界", TextToSpeech.QUEUE_FLUSH, null);

    }



}
