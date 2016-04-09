package jp.co.future.androidbase.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.txusballesteros.SnakeView;

import java.util.ArrayList;

import jp.co.future.androidbase.R;
import jp.co.future.androidbase.fragment.UserDetailActivityFragment;

public class UserDetailActivity extends AppCompatActivity implements UserDetailActivityFragment.OnFragmentInteractionListener ,ObservableScrollViewCallbacks {


    /** ログ出力用タグ */
    private static final String TAG = UserDetailActivity.class.getSimpleName();

    /** クラス名 */
    private static final String className = UserDetailActivity.class.getName().toString();

    /** バインドするフラグメント */
    private UserDetailActivityFragment fragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetail);

        if (savedInstanceState == null) {
            // フラグメントを生成
            fragment = new UserDetailActivityFragment();
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
        //TextView versionV = (TextView) fragment.getView().findViewById(R.id.txt_version);
        //versionV.setText("Version_" + versionName);

        ObservableScrollView listView = (ObservableScrollView) fragment.getView().findViewById(R.id.list);
        listView.setScrollViewCallbacks(this);




    }

    @Override
    public void onPause() {
        super.onPause();
    }


//    @Override
//    public void onAaClicked(View v) {
//
//
//    }

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





}
