package jp.co.future.androidbase.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import jp.co.future.androidbase.activity.DeviceAdapter;
import jp.co.future.androidbase.util.BleUtil;

/**
 * Created by u-kiyota on 2016/04/09.
 */
public class BlePeriodicService extends BasePeriodicService implements BluetoothAdapter.LeScanCallback{
    /* ログ出力用のタグ */
    private final String TAG = BlePeriodicService.class.getSimpleName();

    /* BLE接続のアダプター */
    private BluetoothAdapter mBluetoothAdapter;

    /* BLEデバイス一覧表示リストのデータを扱うアダプター */
    private DeviceAdapter mDeviceAdapter;

    /* startActivityForResultのためのリクエストコード */
    final int REQUEST_ENABLE_BT = 1;

    /* スキャンしているかどうかのフラグ */
    private boolean mIsScanning;

    /* 非同期処理用のハンドラー */
    private Handler mHandler = new Handler();

    // 10秒後にスキャンを止める用の定数
    private static final long SCAN_PERIOD = 10000;

    /** 常駐を解除したい場合のために，常駐インスタンスを保持 */
    public static BasePeriodicService activeService;

    /* BLEのペリフェラルのアドバタイザー */
    public BluetoothLeAdvertiser advertiser;

    /** フィルター対象のIntent */
    private static final String BLE_CALLBACK_INTENT = "jp.co.future.service.BlePeriodicService";

    @Override
    protected long getIntervalMS() {
        // 設定画面の送信間隔設定項目値を取得
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // ミリセカンズに変換し、返却する
        return 30000L;
    }

    @Override
    protected void execTask() {
        try {
            Log.d(TAG, "バックグランドで非同期処理を実行します。");
            startScan();

        } catch (Exception e) {
            Log.e(TAG, "予期せぬエラーが発生しました。：" + e.toString());
            e.printStackTrace();
        } finally {
            // ロック解除
//            LockUtils.unlock();
            // 次回の実行について計画を立てる
            makeNextPlan();
        }

    }


    @Override
    public void makeNextPlan() {
        this.scheduleNextTime();
    }

    /**
     * もし起動していたら，常駐を解除する
     */
    public static void stopResidentIfActive(Context context) {
        if (activeService != null) {
            activeService.stopResident(context);
        }
    }

     /*
     BLEデバイスのスキャンスタート
     */
    private void startScan() {

        BluetoothManager bluetoothManager = BleUtil.getManager(this);
        if (bluetoothManager != null) {
            Log.d(TAG, "mBluetoothAdapter取得");
            mBluetoothAdapter = bluetoothManager.getAdapter();
            advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        }

         //端末のBluetoothがONになっていない場合、設定をONにする
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show();

            //BLEをONにする画面に飛ばす
            Log.d(TAG, "BLEをONにする");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //finish();
            return;
        }

        if(advertiser != null) {
            // アドバタイズ設定
            AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
            settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            settingBuilder.setConnectable(false);
            settingBuilder.setTimeout(0);
            settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
            AdvertiseSettings settings = settingBuilder.build();

            // アドバタイジングデータ
            AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
            dataBuilder.addServiceUuid(new ParcelUuid(UUID.fromString("00000000-AAAA-BBBB-CCCC-DDDDEEEEFFFF")));
            AdvertiseData advertiseData = dataBuilder.build();
            Log.d(TAG, "anvertiser = " + advertiser);
            //アドバタイズを開始
            advertiser.startAdvertising(settings, advertiseData, new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                }
            });
        }else{
            Log.d(TAG, "この端末はBluetoothAdvertiserに対応していません。");
        }
        if ((mBluetoothAdapter != null) && (!mIsScanning)) {

            // スキャンを止める処理をSCAN_PERIODミリ秒後に入れる
            // スキャン処理はバッテリー消費が大きいため
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, SCAN_PERIOD);

            Log.d(TAG, "スキャンスタート");
            // BLE検索ボタンの表示文言更新
//            Button btnBleSearch = (Button) findViewById(R.id.btn_BleSearch);
//            btnBleSearch.setText("BLEデバイス検索中");
            // 一覧からデバイスを削除
//            mDeviceAdapter.clear();

            mBluetoothAdapter.startLeScan(this);
            mIsScanning = true;
//            setProgressBarIndeterminateVisibility(true);
//            invalidateOptionsMenu();
        }
    }

    /*
    BLEデバイスのスキャンストップ
     */
    private void stopScan() {
        if (mBluetoothAdapter != null) {
            Log.d(TAG, "スキャンストップ");
            // BLE検索ボタンの表示文言更新
//            Button btnBleSearch = (Button) findViewById(R.id.btn_BleSearch);
//            btnBleSearch.setText("BLEデバイス検索");
            mBluetoothAdapter.stopLeScan(this);
        }
        mIsScanning = false;
//        setProgressBarIndeterminateVisibility(false);
//        invalidateOptionsMenu();
    }


    /*
    スキャン結果のコールバック
     */
    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {

        ParcelUuid[] uuids = newDeivce.getUuids();
        String uuid = "";
        if (uuids != null) {
            for (ParcelUuid puuid : uuids) {
                uuid += puuid.toString() + " ";
            }
        }
        String msg = "name=" + newDeivce.getName() + ", bondStatus="
                + newDeivce.getBondState() + ", address="
                + newDeivce.getAddress() + ", type" + newDeivce.getType()
                + ", uuids=" + uuid;
        Log.d("BLEActivity", msg);


            //ScanResult sr = new ScanResult(newDeivce.);
            String result = new String(newScanRecord);
            Log.d("newScanRecord", result);


//        mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
        Log.d(TAG, "Device = " + String.valueOf(newDeivce) + "; Rssi = " + newRssi);
        // INTENTをブロードキャスト
        Intent myBroadcast = new Intent(BLE_CALLBACK_INTENT);
        myBroadcast.putExtra("device", String.valueOf(newDeivce));
        myBroadcast.putExtra("rssi", newRssi);
        sendBroadcast(myBroadcast);

    }



}
