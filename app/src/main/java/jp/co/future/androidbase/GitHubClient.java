package jp.co.future.androidbase;

import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by mano on 2016/04/09.
 */
public interface GitHubClient {
    @GET("/users/{user}")
    GithubUser user(
            @Path("user") String user
    );

}

