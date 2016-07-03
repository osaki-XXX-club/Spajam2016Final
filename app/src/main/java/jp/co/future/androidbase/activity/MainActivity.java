package jp.co.future.androidbase.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Types.BoomType;
import com.nightonke.boommenu.Types.ButtonType;
import com.nightonke.boommenu.Types.PlaceType;
import com.nightonke.boommenu.Util;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jp.co.future.androidbase.R;
import jp.co.future.androidbase.activity.base.usingBluetooth.ActivityUsingBluetooth;
import jp.co.future.androidbase.fragment.MainActivityFragment;
import jp.co.future.androidbase.service.BlePeriodicService;
import jp.co.future.androidbase.util.BleUtil;
import android.view.View.OnClickListener;


public class MainActivity extends ActivityUsingBluetooth implements MainActivityFragment.OnFragmentInteractionListener,Runnable {


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

    private BoomMenuButton boomMenuButton;

    private boolean blesearch;

    private RippleBackground rippleBackground;

    //firebase
    private DatabaseReference messageRef;

    //ble alpus
    private ListView listFoundBLEDevices;
    private ArrayAdapter<BLEDevice> adapterFoundBLEDevices;
    private LinkedHashMap<String, BLEDevice> foundBLEDevices;
    public static final int DELAY_MILLIS_UPDATE_SCAN_RESULTS_FIRST = 10000;
    public static final int DELAY_MILLIS_UPDATE_SCAN_RESULTS_INTERVAL = 500;
    // define splash hold time
    private final long SPLASH_HOLD_TIME = 1000L;
    // define request code
    private final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final boolean BLE_DEVICE_FILTERING_ENABLE = true;
    public static final String BLE_DEVICE_NAME_FILTERING_REGULAR_EXPRESSION = "^SNM.*";

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

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        init();

//spajam-finalでは不使用のためコメントアウト
//        // bleを受診した時のレシーバ
//        bleReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // 前画面からパラメータを取得する
//                device = intent.getStringExtra("device");
//                rssi = intent.getIntExtra("rssi", 0);
//                Log.d(TAG, "device =" + device);
//
//                //デバイスIDで誰のBLEか判定する
//                //if ("6A:5F:CD:4A:C4:AA".equalsIgnoreCase(device)) {//検証機
//                if ("5F:0E:66:9B:34:F2".equalsIgnoreCase(device) || "6A:5F:CD:4A:C4:AA".equalsIgnoreCase(device)) {//淳平さん
//
//                    //小川
//                    if (!foundDevice1.isShown()) {
//                        foundDevice(foundDevice1);
//                    }
//
//
//                } else if ("2".equalsIgnoreCase(device)) {
//                    //真野
//                    if (!foundDevice2.isShown()) {
//                        foundDevice(foundDevice2);
//                    }
//                } else if ("98:4F:EE:0F:75:1F".equalsIgnoreCase(device)) {
//                    //Arudino
//                    if (!foundDevice3.isShown()) {
//                        foundDevice(foundDevice3);
//                    }
//                } else if ("28:A1:83:31:16:B6".equalsIgnoreCase(device)) {
//                    //タグ
//                    if (!foundDevice4.isShown()) {
//                        foundDevice(foundDevice4);
//                    }
//                }
//            }
//        };

        // firebaseのセットアップ
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        messageRef =  database.getReference("message");

        // firebaseにデータを登録（サンプル）
        messageRef.setValue("テストメッセージ");

        // Read from the database
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = (String) dataSnapshot.getValue();
                Log.d("Firebase", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });



        // ble connect to alplus
        // get bluetooth adapter
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        boolean isFeature = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (!isFeature || adapter == null) {
            // show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.msg_bluetooth_useless));
            builder.setNegativeButton(R.string.dialog_button_cancel, null);
            builder.setPositiveButton(R.string.dialog_button_ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.setCancelable(false);
            builder.show();
        } else {
            if (adapter.isEnabled()) {
                // show splash
                Handler handler = new Handler();
                handler.postDelayed(this, SPLASH_HOLD_TIME);
            } else {
                // show dialog
                Intent intentStart = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentStart, REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        // レシーバの登録
        //registerReceiver(bleReceiver, new IntentFilter(BLE_CALLBACK_INTENT));


        //メニューボタン
        boomMenuButton = (BoomMenuButton) findViewById(R.id.boom);

        //各メニューが押された時の処理
        boomMenuButton.setOnSubButtonClickListener(new BoomMenuButton.OnSubButtonClickListener() {
            @Override
            public void onClick(int buttonIndex) {
                // return the index of the sub button clicked
                if (0 == buttonIndex) {
                    //BLE切り替え
                    if (blesearch) {
                        rippleBackground.stopRippleAnimation();
                        blesearch = false;

                        foundDevice1.setVisibility(View.INVISIBLE);
                        foundDevice2.setVisibility(View.INVISIBLE);
                        foundDevice3.setVisibility(View.INVISIBLE);
                        foundDevice4.setVisibility(View.INVISIBLE);


                        //TODO BLE止める
                        blePeriodicService.stopResident(getApplicationContext());
                    } else {
                        rippleBackground.startRippleAnimation();
                        blesearch = true;

                        foundDevice1.setVisibility(View.INVISIBLE);
                        foundDevice2.setVisibility(View.INVISIBLE);
                        foundDevice3.setVisibility(View.INVISIBLE);
                        foundDevice4.setVisibility(View.INVISIBLE);

                        //TODO BLEスタート
                        blePeriodicService.startResident(getApplicationContext());
                    }


                } else if (1 == buttonIndex) {
                    //マッチング画面遷移
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    long[] pattern = {3000, 1000, 2000, 5000, 3000, 1000}; // OFF/ON/OFF/ON...
                    vibrator.vibrate(pattern, -1);
                } else if (2 == buttonIndex) {
                    //設定画面遷移
                }
            }
        });


        //BLEサーチ
        rippleBackground = (RippleBackground) findViewById(R.id.content);


        foundDevice1 = (ImageView) findViewById(R.id.foundDevice1);
        foundDevice2 = (ImageView) findViewById(R.id.foundDevice2);
        foundDevice3 = (ImageView) findViewById(R.id.foundDevice3);
        foundDevice4 = (ImageView) findViewById(R.id.foundDevice4);

        // ble alpus
        listFoundBLEDevices = (ListView) findViewById(R.id.list);
        initializeScanList();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        toggleScanning(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
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
        topDp = rnd.nextInt(800)+ 500;

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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //メニューボタン作成
        Drawable[] subButtonDrawables = new Drawable[3];
        int[] drawablesResource = new int[]{
                R.drawable.ic_bluetooth_searching_white_48dp,
                R.drawable.ic_people_white_48dp,
                R.drawable.ic_settings_white_48dp
        };
        for (int i = 0; i < 3; i++)
            subButtonDrawables[i] = ContextCompat.getDrawable(this, drawablesResource[i]);

        String[] subButtonTexts = new String[]{"検索切替", "マッチング", "設定"};

        int[][] subButtonColors = new int[3][2];
        for (int i = 0; i < 3; i++) {
            subButtonColors[i][1] = ContextCompat.getColor(this, R.color.colorPrimary);
            subButtonColors[i][0] = Util.getInstance().getPressedColor(subButtonColors[i][1]);
        }

        boomMenuButton.init(
                subButtonDrawables, // The drawables of images of sub buttons. Can not be null.
                subButtonTexts,     // The texts of sub buttons, ok to be null.
                subButtonColors,    // The colors of sub buttons, including pressed-state and normal-state.
                ButtonType.CIRCLE,     // The button type.
                BoomType.PARABOLA_2,  // The boom type.
                PlaceType.CIRCLE_3_4,  // The place type.
                null,               // Ease type to move the sub buttons when showing.
                null,               // Ease type to scale the sub buttons when showing.
                null,               // Ease type to rotate the sub buttons when showing.
                null,               // Ease type to move the sub buttons when dismissing.
                null,               // Ease type to scale the sub buttons when dismissing.
                null,               // Ease type to rotate the sub buttons when dismissing.
                null                // Rotation degree.
        );

        boomMenuButton.setTextViewColor(ContextCompat.getColor(this, R.color.black));
        boomMenuButton.setSubButtonShadowOffset(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2));

    }


    @Override
    public void onPause() {
        super.onPause();

        // ブロードキャストレシーバの解除
//        unregisterReceiver(bleReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO BLE止める
        //blePeriodicService.stopResident(getApplicationContext());

        //Intent intent = new Intent(this, BlePeriodicService.class);

        // サービスの停止
        //stopService(intent);


    }

    @Override
    protected void onChangeBluetoothState(boolean on) {
        toggleScanning(on);
    }

//    @Override
//    public void onFoundDeviceClicked(View v) {
//        //IDごとに遷移
//        // 画面へ遷移
//        // インテントのインスタンス生成
//        Intent intent = new Intent(this, UserDetailActivity.class);
//        if (v.getId() == foundDevice1.getId()) {
//            intent.putExtra("id", "ogawatachi");
//        }
//        if (v.getId() == foundDevice2.getId()) {
//            intent.putExtra("id", "laqiiz");
//        }
//        if (v.getId() == foundDevice3.getId()) {
//            intent.putExtra("id", "keigodasu");
//        }
//        if (v.getId() == foundDevice4.getId()) {
//            intent.putExtra("id", "sadayuki-matsuno");
//        }
//
//
//        //intent.putExtra("id", "laqiiz");
////        intent.putExtra("subMode", CommonKbnConst.VAL_MODE_HAITATU);
////        intent.putExtra("clear", true);
//        // 次画面のアクティビティ起動
//        startActivity(intent);
//
//    }

    @Override
    protected void onFoundBLEDevice(BLEDevice bleDevice) {
        //Log.d("BLE","onFoundBLEDevice: bleDevice ="+bleDevice.toString());
        String name = bleDevice.getName();
        //Log.d("BLE","onFoundBLEDevice: name ="+name);

        if (BLE_DEVICE_FILTERING_ENABLE) {
            if (!name.matches(BLE_DEVICE_NAME_FILTERING_REGULAR_EXPRESSION)) {
                return;
            }
        }
        Log.d("BLE","アルプス見つけたよー。："+name);
        synchronized (foundBLEDevices) {
            foundBLEDevices.put(bleDevice.getAddress(), bleDevice);
            Log.d("BLE","foundBLEDevices = "+foundBLEDevices);
        }
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

//spajam-finalでは不使用のためコメントアウト
//        //TODO BLE止める
//        blePeriodicService.stopResident(getApplicationContext());
//
//        // BT check
//        BluetoothManager bluetoothManager = BleUtil.getManager(this);
//        if (bluetoothManager != null) {
//            Log.d(TAG, "mBluetoothAdapter取得");
//            mBluetoothAdapter = bluetoothManager.getAdapter();
//        }
//
//        if (mBluetoothAdapter.getScanMode() !=
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }


    }


    @Override
    public void onFoundDeviceClicked(View v) {

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
    @Override
    public void onBUttonSelectClicked(View v) {
        List<Parcelable> parcelableList = new ArrayList<Parcelable>();
        //set view
        SparseBooleanArray array = listFoundBLEDevices.getCheckedItemPositions();
        for (int i = 0; i < foundBLEDevices.size(); i++) {
            boolean checked = array.get(i);
            if (checked) {
                BLEDevice bleDevice = adapterFoundBLEDevices.getItem(i);
                BluetoothDevice bluetoothDevice = bleDevice.getBluetoothDevice();
                parcelableList.add(bluetoothDevice);

                //Log.d("BLE", "position : %d (%s)", i, bluetoothDevice);
            }
        }

        int size = parcelableList.size();
        if ((0 < size) && (size <= 4)) {
            toggleScanning(false);
            connectToTarget(parcelableList);
        } else {
            Toast.makeText(MainActivity.this, "Please select the sensor modules less than 4.", Toast.LENGTH_SHORT).show();
        }

    }
    private void connectToTarget(List<Parcelable> parcelableList) {
        int size = parcelableList.size();
        if (size > 0) {
            final Parcelable parcelables [] = new Parcelable[parcelableList.size()];
            parcelableList.toArray(parcelables);

            String body = getString(R.string.connect_to_following_devices);

            for (Parcelable parcelable : parcelables) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) parcelable;
                //Log.d(TAG, "putExtra : %s", bluetoothDevice);
                body += String.format("\n%s (%s)", (bluetoothDevice.getName()==null)?getString(R.string.no_device_name):bluetoothDevice.getName(), bluetoothDevice.getAddress());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(body);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapterFoundBLEDevices.clear();

                    Intent intent = new Intent(getApplicationContext(), ActivitySensorCommunication.class);
                    intent.putExtra(ActivitySensorCommunication.EXTRAS_DEVICES, parcelables);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.dialog_button_cancel, null);
            builder.setCancelable(false);
            builder.show();
        }
    }
    private void initializeScanList() {
        Log.d("BLE","initializeScanList");
        foundBLEDevices = new LinkedHashMap<String, BLEDevice>();
        adapterFoundBLEDevices = new AdapterBLEDevice(getApplicationContext(), R.layout.custom_row, foundBLEDevices);
        listFoundBLEDevices.setAdapter(adapterFoundBLEDevices);
    }

    protected void toggleScanning(final boolean enable) {
        updateLoopHandler.removeCallbacksAndMessages(null);

        invalidateOptionsMenu();

        enableScanning(enable);

        if (enable) {
            //Log.d("BLE","enable ="+enable);
            // Start runnable-loop to update ListView at fixed intervals
            updateLoopHandler.postDelayed(runnableLoopingUpdate, DELAY_MILLIS_UPDATE_SCAN_RESULTS_FIRST);
        }
    }
    private Handler updateLoopHandler = new Handler(Looper.getMainLooper());
    private Runnable runnableLoopingUpdate = new Runnable() {
        @Override
        public void run() {
            //Log.d("BLE","isScanning ="+isScanning);

            if (isScanning) {
                synchronized (foundBLEDevices) {
                    if (adapterFoundBLEDevices != null) {
                        //Log.d("BLE","runnableLoopingUpdate");
                        adapterFoundBLEDevices.clear();
                        adapterFoundBLEDevices.addAll(foundBLEDevices.values());
                        for(String i: foundBLEDevices.keySet() ){
                            Log.d("BLEhoge",i);
                        }

                        adapterFoundBLEDevices.notifyDataSetChanged();
                    }
                }

                updateLoopHandler.postDelayed(this, DELAY_MILLIS_UPDATE_SCAN_RESULTS_INTERVAL);
            }
        }
    };

    @Override
    public void run() {

    }
}
