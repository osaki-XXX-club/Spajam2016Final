package jp.co.future.androidbase;

import com.squareup.;

import java.util.HashMap;
import java.util.Map;


import okhttp3.OkHttpClient;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by mano on 2016/04/09.
 */
public class ServiceGenerator {

    public static final String GITHUB_API_BASE_URL = "https://api.github.com/";
    public static final String QIITA_API_BASE_URL = "https://qiita.com/api/v2/";

    private static Map<String, RestAdapter.Builder> pool = new HashMap<>();

    static {
        RestAdapter.Builder githubBuilder = new RestAdapter.Builder()
                .setEndpoint(GITHUB_API_BASE_URL)
                .setClient(new OkClient(new OkHttpClient()));
        RestAdapter.Builder qiitaBuilder = new RestAdapter.Builder()
                .setEndpoint(QIITA_API_BASE_URL)
                .setClient(new OkClient(new OkHttpClient()));

        pool.put(GitHubClient.class.getSimpleName(), githubBuilder);
        pool.put(QiitaClient.class.getSimpleName(), qiitaBuilder);

    }


    public static <S> S createService(Class<S> serviceClass) {
        RestAdapter.Builder builder = pool.get(serviceClass.getSimpleName());
        RestAdapter adapter = builder.build();
        return adapter.create(serviceClass);
    }


}