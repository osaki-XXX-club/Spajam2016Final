package jp.co.future.androidbase;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mano on 2016/04/09.
 */
public class ServiceGenerator {

    public static final String GITHUB_API_BASE_URL = "https://api.github.com/";
    public static final String QIITA_API_BASE_URL = "https://qiita.com/api/v2/";

    private static Map<String, Retrofit> pool = new HashMap<>();

    static {
        Retrofit retrofit1 = new Retrofit.Builder()
                .baseUrl(GITHUB_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl(QIITA_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        pool.put(GitHubClient.class.getSimpleName(), retrofit1);
        pool.put(QiitaClient.class.getSimpleName(), retrofit2);

    }


    public static <S> S createService(Class<S> serviceClass) {
        Retrofit builder = pool.get(serviceClass.getSimpleName());
        return builder.create(serviceClass);
    }


}