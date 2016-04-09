package jp.co.future.androidbase.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import jp.co.future.androidbase.R;
import jp.co.future.androidbase.fragment.BleActivityFragment;
import jp.co.future.androidbase.util.BleUtil;
import jp.co.future.androidbase.util.ScannedDevice;

public class BleActivity extends AppCompatActivity implements BleActivityFragment.OnFragmentInteractionListener, BluetoothAdapter.LeScanCallback {

    /* ログ出力用のタグ */
    private final String TAG = MainActivity.class.getSimpleName();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        init();


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        // 他の画面でBLEをハンドリングするのでコメントアウト
//        if (mBluetoothAdapter.isEnabled()) {
//            Log.d(TAG, "mBluetoothAdapter破棄");
//            mBluetoothAdapter.disable();
//            Toast.makeText(this, "Bluetoothをオフにしました。", Toast.LENGTH_LONG).show();
//        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     初期化処理
     */
    private void init() {

        // BLE check
        // 端末がBLEに対応しているかどうかチェック
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // BT check
        BluetoothManager bluetoothManager = BleUtil.getManager(this);
        if (bluetoothManager != null) {
            Log.d(TAG, "mBluetoothAdapter取得");
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }


        // listviewの初期化
        ListView deviceListView = (ListView) findViewById(R.id.list);
        // データ更新用のアダプターインスタンス化
        mDeviceAdapter = new DeviceAdapter(this, R.layout.listitem_device,
                new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);
        // Listviewの行が押された時のイベント設定
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View view, int position, long id) {
                ScannedDevice item = mDeviceAdapter.getItem(position);
                if (item != null) {
                    Intent intent = new Intent(view.getContext(), DeviceActivity.class);
                    BluetoothDevice selectedDevice = item.getDevice();
                    intent.putExtra(DeviceActivity.EXTRA_BLUETOOTH_DEVICE, selectedDevice);
                    startActivity(intent);

                    // stop before change Activity
                    stopScan();
                }
            }
        });

        stopScan();
    }

    /*
    BLEデバイスのスキャンスタート
     */
    private void startScan() {

        // 端末のBluetoothがONになっていない場合、設定をONにする
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show();

            //BLEをONにする画面に飛ばす
            Log.d(TAG, "BLEをONにする");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //finish();
            return;
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
            Button btnBleSearch = (Button) findViewById(R.id.btn_BleSearch);
            btnBleSearch.setText("BLEデバイス検索中");
            // 一覧からデバイスを削除
            mDeviceAdapter.clear();

            mBluetoothAdapter.startLeScan(this);
            mIsScanning = true;
            setProgressBarIndeterminateVisibility(true);
            invalidateOptionsMenu();
        }
    }

    /*
    BLEデバイスのスキャンストップ
     */
    private void stopScan() {
        if (mBluetoothAdapter != null) {
            Log.d(TAG, "スキャンストップ");
            // BLE検索ボタンの表示文言更新
            Button btnBleSearch = (Button) findViewById(R.id.btn_BleSearch);
            btnBleSearch.setText("BLEデバイス検索");
            mBluetoothAdapter.stopLeScan(this);
        }
        mIsScanning = false;
        setProgressBarIndeterminateVisibility(false);
        invalidateOptionsMenu();
    }

    /*
    スキャン結果のコールバック
     */
    @Override
    public void onLeScan(final BluetoothDevice newDeivce, final int newRssi,
                         final byte[] newScanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // スキャン結果を一覧に追加する
                mDeviceAdapter.update(newDeivce, newRssi, newScanRecord);
            }
        });
    }


    @Override
    public void onBleSearchClicked(View v) {
        // BLE検索ボタンが押された時の処理
        startScan();


    }


}
