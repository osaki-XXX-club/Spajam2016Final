package jp.co.future.androidbase;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by mano on 2016/04/09.
 */
public class TwiterMain {

    public static void main(String[] args) throws TwitterException {

        System.setProperty("twitter4j.loggerFactory", "twitter4j.NullLoggerFactory");

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("hoi0cyM27KW5ABxmGFe72Zjwe")
                .setOAuthConsumerSecret("pXG0342MExvfr8HVWNpPx4OJgBJ1uNSxfr0rRQzKL4xdliNYDC")
                .setOAuthAccessToken("\t586776783-oq2IeijYTjESmmAZh2Kp0FbTZN5AxkVuaLHWxlFV")
                .setOAuthAccessTokenSecret("F3cHBM0epIvc6WwVBpbF3OdIFeVU3ZJ0nG7sFMcjIltKt");

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        List<Status> statuses = twitter.getHomeTimeline();
        System.out.println("Showing home timeline.");

        for (Status status : statuses) {
            System.out.println(status.getUser().getName() + ":" +
                    status.getText());
        }

    }

}
