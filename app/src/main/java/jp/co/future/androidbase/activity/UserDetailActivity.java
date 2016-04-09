package jp.co.future.androidbase.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import jp.co.future.androidbase.GitHubClient;
import jp.co.future.androidbase.GithubUser;
import jp.co.future.androidbase.QiitaUser;
import jp.co.future.androidbase.R;
import jp.co.future.androidbase.ServiceGenerator;
import jp.co.future.androidbase.fragment.UserDetailActivityFragment;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserDetailActivity extends AppCompatActivity implements UserDetailActivityFragment.OnFragmentInteractionListener ,ObservableScrollViewCallbacks {


    /** ログ出力用タグ */
    private static final String TAG = UserDetailActivity.class.getSimpleName();

    /** クラス名 */
    private static final String className = UserDetailActivity.class.getName().toString();

    /** バインドするフラグメント */
    private UserDetailActivityFragment fragment;

    private Handler mHandler;


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


        final String userId = "laqiiz";
        final UserDetailActivity activity = this;

        // Github情報の表示
        GitHubClient gitHubClient = ServiceGenerator.createService(GitHubClient.class);
        Call<GithubUser> githubCall = gitHubClient.user(userId);

        githubCall.enqueue(new Callback<GithubUser>() {
            @Override
            public void onResponse(Call<GithubUser> call, Response<GithubUser> response) {
                GithubUser body = response.body();
                TextView version2 = (TextView) fragment.getView().findViewById(R.id.txt_intoroduce);
                version2.setText("Version_" + body.getName() + "," + body.getCompany());
            }

            @Override
            public void onFailure(Call<GithubUser> call, Throwable t) {
                t.printStackTrace();
            }
        });


// JSONパースエラーになるので、生でOKHttpClientを使う
//        QiitaClient qiitaClient = ServiceGenerator.createService(QiitaClient.class);
//        Call<QiitaUser> qiitaCall = qiitaClient.user(userId);
//        qiitaCall.enqueue(new Callback<QiitaUser>() {
//            @Override
//            public void onResponse(Call<QiitaUser> call, Response<QiitaUser> response) {
//                QiitaUser body = response.body();
//                TextView version3 = (TextView) fragment.getView().findViewById(R.id.txt_intoroduce3);
//                version3.setText("Version_" + body.getName() + "," + body.getDescription());
//            }
//
//            @Override
//            public void onFailure(Call<QiitaUser> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });


        Request request = new Request.Builder()
                .url("https://qiita.com/api/v2/users/laqiiz")
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String res = response.body().string();

                Gson gson = new Gson();
                final QiitaUser body = gson.fromJson(res, QiitaUser.class);
                mHandler = new Handler(Looper.getMainLooper());

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView version3 = (TextView) fragment.getView().findViewById(R.id.txt_intoroduce3);
                        version3.setText("Version_" + body.getName() + "," + body.getDescription());
                    }
                });
            }
        });


        Request request2 = new Request.Builder()
                .url("http://qiita.com/" + userId)
                .get()
                .build();

        OkHttpClient client2 = new OkHttpClient();
        client2.newCall(request2).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String res = response.body().string();

                Document dom = Jsoup.parse(res);

                String[] items = dom.select(".row .userActivityChart_stats")
                        .select(".userActivityChart_statCount")
                        .html().split("\n");

                Log.d(TAG, res);

                final String contribution = items[0];

                mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView version3 = (TextView) fragment.getView().findViewById(R.id.txt_intoroduce3);
                        version3.setText("contribution=" + contribution);
                    }
                });
            }
        });

//        AsyncTask<Void, Void, String> asyncTask = new AsyncScrapingTask().execute();
//        String contribution = null;
//        try {
//            contribution = asyncTask.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

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
