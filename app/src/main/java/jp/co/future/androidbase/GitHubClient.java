package jp.co.future.androidbase;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.Call;

/**
 * Created by mano on 2016/04/09.
 */
public interface GitHubClient {
    @GET("/users/{user}")
    Call<GithubUser> user(
            @Path("user") String user
    );

}

