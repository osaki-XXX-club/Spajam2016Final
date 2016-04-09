package jp.co.future.androidbase;


/**
 * Created by mano on 2016/04/09.
 */
public class GithubMain {

    public static void main(String... args) {

        GitHubClient client = ServiceGenerator.createService(GitHubClient.class);
//        GithubUser user = client.user("laqiiz");
        GithubUser user = client.user("kotakanbe");


        System.out.println(user.getName() + "," + user.getFollowers() + "," + user.getFollowing()) ;


    }


}
