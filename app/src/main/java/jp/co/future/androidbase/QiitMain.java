package jp.co.future.androidbase;

/**
 * Created by mano on 2016/04/09.
 */
public class QiitMain {

    public static void main(String[] args) {

        QiitaClient client = ServiceGenerator.createService(QiitaClient.class);

        QiitaUser qiitaUser = client.user("laqiiz");

        System.out.println(qiitaUser.getName() + qiitaUser.getDescription());

        QiitaUserTop qiitaUserTop = new QiitaUserTop();
        QiitaUserTop.UserActivity qiitaActivity = qiitaUserTop.scrape("laqiiz");
        String contribution = qiitaActivity.contribution;

        System.out.println(contribution);

    }
}

