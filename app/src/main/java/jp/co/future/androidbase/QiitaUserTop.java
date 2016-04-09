package jp.co.future.androidbase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by mano on 2016/04/10.
 */
public class QiitaUserTop {

    public UserActivity scrape(String userName) {

        UserActivity userActivity = new UserActivity();

        try {
            Document dom = Jsoup.connect("http://qiita.com/" + "laqiiz")
                    .get();

            String[] items = dom.select(".row .userActivityChart_stats")
                    .select(".userActivityChart_statCount")
                    .html().split("\n");

            userActivity.contribution = items[0];
            userActivity.followers = items[1];
            userActivity.item = items[2];

        } catch (IOException e) {
            e.printStackTrace();
        }

        return userActivity;

    }


    static class UserActivity {

        public String contribution;
        public String followers;
        public String item;

    }
}
