package jp.co.future.androidbase;

import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by mano on 2016/04/09.
 */
public interface QiitaClient {
    @GET("/users/{user}")
    QiitaUser user(
            @Path("user") String owner
    );

}

