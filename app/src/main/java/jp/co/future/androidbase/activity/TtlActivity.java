package jp.co.future.androidbase.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Locale;

import jp.co.future.androidbase.R;

public class TtlActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_ttl);

        // Button btn = (Button)findViewById(R.id.read_manufacturer_name_button);
        // btn.setOnClickListener(this);


        // TextToSpeechオブジェクトの生成
        tts = new TextToSpeech(this, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        int amStreamMusicVol = am.getStreamVolume(am.STREAM_SYSTEM);
//        Log.d("hoge", String.valueOf(am.getStreamVolume(am.STREAM_SYSTEM)));
//        Log.d("hoge", String.valueOf(am.getStreamVolume(am.STREAM_MUSIC)));
//        Log.d("hoge", String.valueOf(am.getStreamVolume(am.STREAM_VOICE_CALL)));
//        Log.d("hoge", String.valueOf(am.getStreamVolume(am.STREAM_ALARM)));
//        Log.d("hoge", String.valueOf(am.getStreamVolume(am.STREAM_DTMF)));
//        Log.d("hoge", String.valueOf(am.getStreamVolume(am.STREAM_NOTIFICATION)));
//        Log.d("hoge", String.valueOf(am.getStreamVolume(am.STREAM_RING)));
//        am.setStreamVolume(am.STREAM_SYSTEM, amStreamMusicVol, 0);

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

        Intent i = getIntent();
        String word = i.getStringExtra("word");

        if (tts.isSpeaking()) {
            // 読み上げ中なら止める
            tts.stop();
        }

        // TODO 音量調整

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float amStreamSystemMaxVol = am.getStreamMaxVolume(am.STREAM_SYSTEM);
        float amStreamSystemVol = am.getStreamVolume(am.STREAM_SYSTEM);
        float amStreamSyttemRatio = amStreamSystemVol / amStreamSystemMaxVol;
        Log.d("Volume","amStreamSystemMaxVol:"+amStreamSystemMaxVol+" amStreamSystemVol:"+amStreamSystemVol+" amStreamSyttemRatio:"+amStreamSyttemRatio);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(amStreamSyttemRatio));

        // 読み上げ開始
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, params);

    }


}
