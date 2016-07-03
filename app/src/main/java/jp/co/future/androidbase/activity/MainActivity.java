package jp.co.future.androidbase.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Types.BoomType;
import com.nightonke.boommenu.Types.ButtonType;
import com.nightonke.boommenu.Types.PlaceType;
import com.nightonke.boommenu.Util;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Random;

import jp.co.future.androidbase.Orientation;
import jp.co.future.androidbase.R;
import jp.co.future.androidbase.fragment.MainActivityFragment;
import jp.co.future.androidbase.service.BlePeriodicService;
import jp.co.future.androidbase.util.BleUtil;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnFragmentInteractionListener {


    /**
     * ログ出力用タグ
     */
    private static final String TAG = MainActivity.class.getSimpleName();


    /**
     * クラス名
     */
    private static final String className = MainActivity.class.getName().toString();

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

    private static final BlePeriodicService blePeriodicService = new BlePeriodicService();

    private static String device;
    private static int rssi;

    /**
     * ブロードキャストレシーバ
     */
    private BroadcastReceiver bleReceiver;

    /**
     * フィルター対象のIntent
     */
    private static final String BLE_CALLBACK_INTENT = "jp.co.future.service.BlePeriodicService";


    /**
     * バインドするフラグメント
     */
    private MainActivityFragment fragment;

    private ImageView foundDevice1;
    private ImageView foundDevice2;
    private ImageView foundDevice3;
    private ImageView foundDevice4;


    private boolean blesearch;

    private RippleBackground rippleBackground;


    //firebase
    private DatabaseReference messageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // フラグメントを生成
            fragment = new MainActivityFragment();
            // フラグメントをアクティビティに追加する FragmentTransaction を利用する
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, fragment, "fragment");
            transaction.commit();
        }

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        init();

        // bleを受診した時のレシーバ
        bleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 前画面からパラメータを取得する
                device = intent.getStringExtra("device");
                rssi = intent.getIntExtra("rssi", 0);
                Log.d(TAG, "device =" + device);

                //デバイスIDで誰のBLEか判定する
                //if ("6A:5F:CD:4A:C4:AA".equalsIgnoreCase(device)) {//検証機
                if ("5F:0E:66:9B:34:F2".equalsIgnoreCase(device) || "6A:5F:CD:4A:C4:AA".equalsIgnoreCase(device)) {//淳平さん

                    //小川
                    if (!foundDevice1.isShown()) {
                        foundDevice(foundDevice1);
                    }


                } else if ("2".equalsIgnoreCase(device)) {
                    //真野
                    if (!foundDevice2.isShown()) {
                        foundDevice(foundDevice2);
                    }
                } else if ("98:4F:EE:0F:75:1F".equalsIgnoreCase(device)) {
                    //Arudino
                    if (!foundDevice3.isShown()) {
                        foundDevice(foundDevice3);
                    }
                } else if ("28:A1:83:31:16:B6".equalsIgnoreCase(device)) {
                    //タグ
                    if (!foundDevice4.isShown()) {
                        foundDevice(foundDevice4);
                    }
                }
            }
        };

        // firebaseのセットアップ
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        messageRef = database.getReference("message");

        // Read from the database
//        messageRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = (String) dataSnapshot.getValue();
//                Log.d("Firebase", "Value is: " + value);
//
////                TimeLineModel model = new TimeLineModel();
////                model.setName(value);
////                model.setAge(100);
////                TimelineActivity.getmDataList().add(model);
////                TimelineActivity.getmTimeLineAdapter().notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w("Firebase", "Failed to read value.", error.toException());
//            }
//        });
    }

    @Override
    protected void onResume() {

        super.onResume();

        // レシーバの登録
        registerReceiver(bleReceiver, new IntentFilter(BLE_CALLBACK_INTENT));

        //BLEサーチ
        rippleBackground = (RippleBackground) findViewById(R.id.content);


        foundDevice1 = (ImageView) findViewById(R.id.foundDevice1);
        foundDevice2 = (ImageView) findViewById(R.id.foundDevice2);
        foundDevice3 = (ImageView) findViewById(R.id.foundDevice3);
        foundDevice4 = (ImageView) findViewById(R.id.foundDevice4);


    }

    private void foundDevice(ImageView foundDevice) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        foundDevice.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) foundDevice.getLayoutParams();
        Random rnd = new Random();

        int leftDp = 0;
        int topDp = 0;
        int rightDp = 0;
        int bottomDp = 0;

        rightDp = rnd.nextInt(250) - 50;
        bottomDp = rnd.nextInt(250) - 50;
        leftDp = rnd.nextInt(500) + 50 + 400;
        topDp = rnd.nextInt(800) + 500;

        Log.d(TAG, "左：" + leftDp);
        Log.d(TAG, "上：" + topDp);

        if (400 <= topDp && topDp <= 500) {
            topDp += 250;
        } else if (500 < topDp && topDp <= 550) {
            topDp += 150;
        } else if (550 < topDp && topDp <= 650) {
            topDp += 100;
        }

        param.setMargins(leftDp, topDp, 0, 0);


        foundDevice.setLayoutParams(param);
        animatorSet.start();
    }


    @Override
    public void onPause() {
        super.onPause();

        // ブロードキャストレシーバの解除
        unregisterReceiver(bleReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO BLE止める
        blePeriodicService.stopResident(getApplicationContext());

        Intent intent = new Intent(this, BlePeriodicService.class);

        // サービスの停止
        stopService(intent);


    }

    @Override
    public void onFoundDeviceClicked(View v) {
        //IDごとに遷移
        // 画面へ遷移
        // インテントのインスタンス生成
        Intent intent = new Intent(this, UserDetailActivity.class);
        if (v.getId() == foundDevice1.getId()) {
            intent.putExtra("id", "ogawatachi");
        }
        if (v.getId() == foundDevice2.getId()) {
            intent.putExtra("id", "laqiiz");
        }
        if (v.getId() == foundDevice3.getId()) {
            intent.putExtra("id", "keigodasu");
        }
        if (v.getId() == foundDevice4.getId()) {
            intent.putExtra("id", "sadayuki-matsuno");
        }


        //intent.putExtra("id", "laqiiz");
//        intent.putExtra("subMode", CommonKbnConst.VAL_MODE_HAITATU);
//        intent.putExtra("clear", true);
        // 次画面のアクティビティ起動
        startActivity(intent);

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

        //TODO BLE止める
        blePeriodicService.stopResident(getApplicationContext());

        // BT check
        BluetoothManager bluetoothManager = BleUtil.getManager(this);
        if (bluetoothManager != null) {
            Log.d(TAG, "mBluetoothAdapter取得");
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }


    }


    @Override
    public void onCenterImageClicked(View v) {
        if (blesearch) {
            rippleBackground.stopRippleAnimation();
            blesearch = false;
            foundDevice1.setVisibility(View.INVISIBLE);
            foundDevice2.setVisibility(View.INVISIBLE);
            foundDevice3.setVisibility(View.INVISIBLE);
            foundDevice4.setVisibility(View.INVISIBLE);

            //TODO BLE止める
            blePeriodicService.stopResident(v.getContext());

        } else {

            blesearch = true;

            foundDevice1.setVisibility(View.INVISIBLE);
            foundDevice2.setVisibility(View.INVISIBLE);
            foundDevice3.setVisibility(View.INVISIBLE);
            foundDevice4.setVisibility(View.INVISIBLE);

            //TODO BLEスタート
            blePeriodicService.startResident(v.getContext());

            rippleBackground.startRippleAnimation();
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    foundDevice(foundDevice1);
//                }
//            }, 3000);

        }

    }


    //    @Override
//    public void onAaClicked(View v) {
//        // 画面へ遷移
//        // インテントのインスタンス生成
//        Intent intent = new Intent(this, UserDetailActivity.class);
////        intent.putExtra("mode", CommonKbnConst.VAL_MODE_HAITATU);
////        intent.putExtra("subMode", CommonKbnConst.VAL_MODE_HAITATU);
////        intent.putExtra("clear", true);
//        // 次画面のアクティビティ起動
//        startActivity(intent);
//
//    }
//
//
//
//    @Override
//    public void onSetteiClicked(View v) {
//        Intent intent = new Intent(this, BleActivity.class);
//        startActivity(intent);
////        // インテントのインスタンス生成
////        Intent intent = new Intent(this, FugaActivity.class);
////        // インテントに値を設定
////        Bundle bundle = new Bundle();
////        bundle.putString("displayMode", "normal");
////        intent.putExtras(bundle);
////        // 次画面のアクティビティ起動
////        startActivity(intent);
//    }
//
//    @Override
//    public void onKaijoClicked(View v) {
//
//    }

}
