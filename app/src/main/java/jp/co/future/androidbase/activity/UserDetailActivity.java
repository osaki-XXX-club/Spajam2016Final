package jp.co.future.androidbase.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
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

public class UserDetailActivity extends AppCompatActivity implements UserDetailActivityFragment.OnFragmentInteractionListener, ObservableScrollViewCallbacks {

    /**
     * ログ出力用タグ
     */
    private static final String TAG = UserDetailActivity.class.getSimpleName();

    /**
     * クラス名
     */
    private static final String className = UserDetailActivity.class.getName().toString();

    /**
     * バインドするフラグメント
     */
    private UserDetailActivityFragment fragment;

    private Handler mHandler;

    /**
     * トップ画面からの連携用データ
     */
    private String id;

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


        id = getIntent().getStringExtra("id");
    }

    @Override
    public void onPause() {
        super.onPause();
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
    protected void onResume() {

        super.onResume();
        // フッター部にバージョンの設定
        //TextView versionV = (TextView) fragment.getView().findViewById(R.id.txt_version);
        //versionV.setText("Version_" + versionName);

        final String userId = id;

        final int age;
        final String comment;
        if ("ogawatachi".equals(id)) {
            age = 26;
            comment = "最近興味があることはハッカソンです。アイデア、独創性、実装力、プレゼン力と社会人に必要なすべての要素を求められるため非常に刺激になります。趣味は登山で月1ペースで奥多摩に上りに行く生粋の山男です。";
            //写真
            final ImageView imageView = (ImageView) fragment.getView().findViewById(R.id.imgv_user);
            Drawable drawable = getResources().getDrawable(R.drawable.user1);
            imageView.setImageDrawable(drawable);
        } else if ("laqiiz".equals(id)) {
            age = 29;
            comment = "最近ハワイで結婚式を上げました。結婚すると時間がなくなると聞きますが、本当その通りです。まだ子供もいないので、産まれたら大変だろうなと今から戦々恐々としています";
            //写真
            final ImageView imageView = (ImageView) fragment.getView().findViewById(R.id.imgv_user);
            Drawable drawable = getResources().getDrawable(R.drawable.mano);
            imageView.setImageDrawable(drawable);
        } else if ("keigodasu".equals(id)) {
            age = 25;
            comment = "IoTや3Dプリンタなどモノづくりが大好きです。社内で一番CADを使って様々なプロダクトを作成しています。面白いアイデアがあればぜひ一緒にブレストしましょう！";

            //写真
            final ImageView imageView = (ImageView) fragment.getView().findViewById(R.id.imgv_user);
            Drawable drawable = getResources().getDrawable(R.drawable.mano);
            imageView.setImageDrawable(drawable);

        } else if ("sadayuki-matsuno".equals(id)) {
            age = 23;
            comment = "社会人3年目になりました！技術が大好き過ぎて、最近表参道のギークハウス（シェアハウス）に引っ越しました！技術を極める！";

            //写真
            final ImageView imageView = (ImageView) fragment.getView().findViewById(R.id.imgv_user);
            Drawable drawable = getResources().getDrawable(R.drawable.mano);
            imageView.setImageDrawable(drawable);


        } else {
            age = 23;
            comment = "社内のR＆D部隊に転籍しました。しかし技術が好きでもっと高めたいですが、採用・人材育成に関わることが多く、もっと技術領域に深く関わりたい今日このごろです";
        }

        mHandler = new Handler();

        final TextView view1 = (TextView) fragment.getView().findViewById(R.id.txt_username);


        final TextView commentView = (TextView) fragment.getView().findViewById(R.id.txt_comment);
        commentView.setText(comment);

        // Qiita関連
        final TextView version3 = (TextView) fragment.getView().findViewById(R.id.txt_qiita_followees);
        final TextView version4 = (TextView) fragment.getView().findViewById(R.id.txt_qiita_item_count);
        final TextView introduceView = (TextView) fragment.getView().findViewById(R.id.txt_intoroduce);
        final TextView addressView = (TextView) fragment.getView().findViewById(R.id.txt_user_address);

        // リンク
        final TextView qiitaLinkView = (TextView) fragment.getView().findViewById(R.id.txt_link_qiita);
        final TextView githubLinkView = (TextView) fragment.getView().findViewById(R.id.txt_link_github);

        // Github情報の表示
        GitHubClient gitHubClient = ServiceGenerator.createService(GitHubClient.class);
        Call<GithubUser> githubCall = gitHubClient.user(userId);

        githubCall.enqueue(new Callback<GithubUser>() {
            @Override
            public void onResponse(Call<GithubUser> call, Response<GithubUser> response) {
                GithubUser body = response.body();

                TextView view2 = (TextView) fragment.getView().findViewById(R.id.user_company);
                view2.setText(String.valueOf(body.getCompany()));

                TextView viewGitFollowers = (TextView) fragment.getView().findViewById(R.id.txt_github_followers);
                viewGitFollowers.setText(Integer.toString(body.getFollowers()));

                TextView viewGitRepos = (TextView) fragment.getView().findViewById(R.id.txt_github_repos);
                viewGitRepos.setText(Integer.toString(body.getPublicRepos()));

                githubLinkView.setText(body.getHtmlUrl());
            }

            @Override
            public void onFailure(Call<GithubUser> call, Throwable t) {
                t.printStackTrace();
            }
        });


        Request request = new Request.Builder()
                .url("https://qiita.com/api/v2/users/" + userId)
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

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        view1.setText(body.getName() + "(" + age + ")");
                        version3.setText(Integer.toString(body.getFolloweesCount()));
                        version4.setText(Integer.toString(body.getItemsCount()));
                        introduceView.setText(body.getDescription());
                        String location = body.getLocation();
                        addressView.setText(location == null ? "" : location);
                        qiitaLinkView.setText("http://qiita.com/" + userId);
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

                final String contribution = items[0];

                mHandler = new Handler(Looper.getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView version3 = (TextView) fragment.getView().findViewById(R.id.txt_qiita_contribution);
                        version3.setText(contribution);
                    }
                });
            }
        });

        ObservableScrollView listView = (ObservableScrollView) fragment.getView().findViewById(R.id.list);
        listView.setScrollViewCallbacks(this);

    }


}
