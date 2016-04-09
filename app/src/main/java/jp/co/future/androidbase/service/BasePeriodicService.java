package jp.co.future.androidbase.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

/**
 * 常駐型サービスの基底クラス。
 *
 * @author ogawa itaru
 *
 */
public abstract class BasePeriodicService extends Service {
    /** ログ出力用タグ */
    private static final String TAG = BasePeriodicService.class.getSimpleName();

    /** サービスの定期実行の間隔をミリ秒で指定。 処理が終了してから次に呼ばれるまでの時間。 */
    protected abstract long getIntervalMS();

    /** 定期実行したいタスクの中身（１回分） タスクの実行が完了したら，次回の実行計画を立てること。 */
    protected abstract void execTask();

    /** 次回の実行計画を立てる。 */
    protected abstract void makeNextPlan();

    // ---------- 必須メンバ -----------

    protected final IBinder binder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ---------- サービスのライフサイクル -----------

    @Override
    public void onStart(Intent intent, int startId) {

        // サービス起動時の処理。
        // サービス起動中に呼ぶと複数回コールされ得る。しかし二重起動はしない
        // @see http://d.hatena.ne.jp/rso/20110911

        super.onStart(intent, startId);

        // タスクを実行
        execTask();

        // NOTE: ここで次回の実行計画を逐次的にコールしていない理由は，
        // タスクが非同期の場合があるから。
    }

    /**
     * 常駐を開始
     */
    public BasePeriodicService startResident(Context context) {
        Log.d(TAG, "startResident");
        Intent intent = new Intent(context, this.getClass());
        intent.putExtra("type", "start");
        context.startService(intent);

        return this;
    }

    /**
     * サービスの次回の起動を予約
     */
    public void scheduleNextTime() {

        long now = System.currentTimeMillis();

        // アラームをセット
        PendingIntent alarmSender = PendingIntent.getService(this, 0, new Intent(this, this.getClass()), 0);
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, now + getIntervalMS(), alarmSender);
        // 次回登録が完了
        Log.d(TAG, "次回実行時間:" + getIntervalMS() / 1000 / 60 + "分後");

    }

    /**
     * サービスの定期実行を解除し，サービスを停止
     */
    public void stopResident(Context context) {
        try {
            // サービス名を指定
            Intent intent = new Intent(context, this.getClass());

            // アラームを解除
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, // ここを-1にすると解除に成功しない
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);

            // サービス自体を停止
            stopSelf();
        } catch (Exception e) {
            Log.d(TAG, "stopResidentに失敗：" + e.toString());
        }

    }

}
