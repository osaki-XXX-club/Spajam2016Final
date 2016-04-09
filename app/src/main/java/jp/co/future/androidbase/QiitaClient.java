package jp.co.future.androidbase;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.Call;

/**
 * Created by mano on 2016/04/09.
 */
public interface QiitaClient {
    @GET("/users/{user}")
    Call<QiitaUser> user(
            @Path("user") String user
    );

}

