package jp.co.future.androidbase.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.txusballesteros.SnakeView;

import jp.co.future.androidbase.R;
import jp.co.future.androidbase.fragment.MainActivityFragment;
import jp.co.future.androidbase.fragment.SpiderActivityFragment;

public class SpiderActivity extends AppCompatActivity implements SpiderActivityFragment.OnFragmentInteractionListener {


    /** ログ出力用タグ */
    private static final String TAG = SpiderActivity.class.getSimpleName();

    /** クラス名 */
    private static final String className = SpiderActivity.class.getName().toString();

    /** バインドするフラグメント */
    private SpiderActivityFragment fragment;

    /** トランザクションID */
    private static String ikkatuTranId;

    private SnakeView snakeView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spider);

        if (savedInstanceState == null) {
            // フラグメントを生成
            fragment = new SpiderActivityFragment();
            // フラグメントをアクティビティに追加する FragmentTransaction を利用する
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, fragment, "fragment");
            transaction.commit();
        }


    }

    @Override
    protected void onResume() {

        super.onResume();
        // フッター部にバージョンの設定
        TextView versionV = (TextView) fragment.getView().findViewById(R.id.txt_version);
        //versionV.setText("Version_" + versionName);

        snakeView = (SnakeView)findViewById(R.id.snake);
        snakeView.setMinValue(0);
        snakeView.setMaxValue(1000);


    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onAaClicked(View v) {

        snakeView.addValue(100);
    }




}
