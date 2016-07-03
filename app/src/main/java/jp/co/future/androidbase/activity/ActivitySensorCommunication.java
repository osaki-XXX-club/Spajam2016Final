
package jp.co.future.androidbase.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import jp.co.future.androidbase.R;
import jp.co.future.androidbase.activity.base.usingBluetooth.ActivityUsingBluetooth;
import jp.co.future.androidbase.activity.base.view.LinearLayoutDetectableSoftKey;
import jp.co.future.androidbase.log.Logg;
import jp.co.future.androidbase.sensorModule.command.Commander;
import jp.co.future.androidbase.sensorModule.SensorModule;
import jp.co.future.androidbase.sensorModule.command.control.*;
import jp.co.future.androidbase.sensorModule.enums.MeasuringMode;
import jp.co.future.androidbase.sensorModule.enums.MeasuringState;
import jp.co.future.androidbase.sensorModule.enums.Sensor;
import jp.co.future.androidbase.sensorModule.enums.AwakeMode;

import java.util.*;

//import static jp.co.future.androidbase.R.id.setting_item_switch_ambient_light;

/**
 * [JP] 渡された{@code BluetoothDevice}オブジェクトで{@link SensorModule}オブジェクトを生成し、
 * このオブジェクトから通知される様々な受信イベントを表示します。
 *
 * また、様々なボタン押下をトリガーに、センサモジュールに対してBLE通信コマンドを発行したり、
 * 受信したセンサデータのログを記録させます。
 *
 * @see SensorModule
 * @see com.alps.sample.sensorModule.SensorModule.ISensorModule
 * @see com.alps.sample.sensorModule.LatestData
 */
@SuppressLint("NewApi")
public class ActivitySensorCommunication extends ActivityUsingBluetooth {
    public static final int INDEX_MEASURING_MODE_SLOW = 0;
    public static final int INDEX_MEASURING_MODE_FAST = 1;
    public static final int INDEX_MEASURING_MODE_HYBRID = 2;
    public static final int INDEX_MEASURING_MODE_FORCE = 3;

    private final String TAG = getClass().getSimpleName();

    public static final String EXTRAS_DEVICES = "DEVICES";

    private LinearLayoutDetectableSoftKey linearLayoutDetectableSoftKey;
    private RelativeLayout layoutMain;
    private LinearLayout layoutMask;

    private List<SensorModule> sensorModules;

    private Spinner spinnerTargetNode;

    private Button buttonSettingsRead;
    private Button buttonSettingsWrite;
    private Button buttonSleep;
    private Button buttonSyncTimestamp;
    private Button buttonMeasure;
    private Button buttonLog;

    private ScrollView wrapperTextInfoData;
    private TextView textInfoData;
    private ImageView iconBattery;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logg.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // get extra value
        int tagCount = 0;
        final Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(EXTRAS_DEVICES);
        sensorModules = new ArrayList<SensorModule>();
        if (parcelables != null) {
            for (Parcelable parcelable : parcelables) {
                if (parcelable instanceof BluetoothDevice) {
                    SensorModule sensorModule = new SensorModule(this, (BluetoothDevice) parcelable, tagCount, iSensorModule);
                    sensorModules.add(sensorModule);
                    ++tagCount;
                }
            }
        }
        if (tagCount == 0) {
            finish();
        }

        //setContentView(R.layout.activity_sensor_communication);

        // set the view
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        // set the tool-bar to this activity
        //Toolbar mToolbar = (Toolbar) findViewById(R.id.header);
        //mToolbar.setTitle(R.string.app_title);
        //setSupportActionBar(mToolbar);

//        for (SensorModule sensorModule : sensorModules) {
//            adapter.add(sensorModule.getName());
//        }
        for (SensorModule sensorModule : sensorModules) {
            sensorModule.activate();
        }
        try {
            Thread.sleep(15000); //3000ミリ秒Sleepする
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onStart() {
        Logg.d(TAG, "onStart");
        super.onStart();
        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        updateAllViews();
    }

    @Override
    protected void onResume() {
        Logg.d(TAG, "onResume");
        super.onResume();
        final SensorModule sensorModule = getCurrentSensorModule();
        if (sensorModule != null) {
            MeasuringState nextMeasuringState = MeasuringState.Started;
            switch (sensorModule.measuringMode) {
                case Slow:
                case Fast:
                case Hybrid:
                    nextMeasuringState = (sensorModule.measuringState == MeasuringState.Started) ? MeasuringState.Stopped : MeasuringState.Started;
                    break;
                case Force:
                    nextMeasuringState = MeasuringState.Started;
                    break;
            }

//            layoutMask.requestFocus();
//            layoutMask.setVisibility(View.VISIBLE);

            ArrayList<CtrlCmd> commands = new ArrayList<CtrlCmd>();
            commands.add(new CtrlCmdMeasuringState(nextMeasuringState));
            Log.d(TAG,"nextMeasuringState ="+ nextMeasuringState.toString());
            sensorModule.writeSettings(commands, false, new Commander.ICommander() {
                @Override
                public void onBatchFinish() {
                    updateParameters(sensorModule);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            layoutMask.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onPause() {
        Logg.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logg.d(TAG, "onStop");
        super.onStop();
        // clear screen on flag
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        Logg.d(TAG, "onDestroy");
        super.onDestroy();

//        if (sensorModules != null) {
//            for (SensorModule sensorModule : sensorModules) {
//                sensorModule.deactivate();
//            }
//            sensorModules.clear();
//            sensorModules = null;
//        }
    }

    @Override
    protected void onChangeBluetoothState(boolean on) {
        // NOP
    }


    private View.OnClickListener onClickListenerButtonMeasure = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final SensorModule sensorModule = getCurrentSensorModule();
            if (sensorModule != null) {
                MeasuringState nextMeasuringState = MeasuringState.Started;
                Log.d(TAG,"measuringMode:"+sensorModule.measuringMode);
                switch (sensorModule.measuringMode) {
                    case Slow:
                    case Fast:
                    case Hybrid:
                        nextMeasuringState = (sensorModule.measuringState == MeasuringState.Started) ? MeasuringState.Stopped : MeasuringState.Started;
                        break;
                    case Force:
                        nextMeasuringState = MeasuringState.Started;
                        break;
                }

                layoutMask.requestFocus();
                layoutMask.setVisibility(View.VISIBLE);

                ArrayList<CtrlCmd> commands = new ArrayList<CtrlCmd>();
                commands.add(new CtrlCmdMeasuringState(nextMeasuringState));
                Log.d(TAG,"nextMeasuringState ="+ nextMeasuringState.toString());
                sensorModule.writeSettings(commands, false, new Commander.ICommander() {
                    @Override
                    public void onBatchFinish() {
                        updateParameters(sensorModule);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layoutMask.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
            }
        }
    };


    private int getValueFromTextViewResource(int resource, int min, int max, int defaultValue) {
        String text;
        int value = defaultValue;
        TextView tv = (TextView) layoutMain.findViewById(resource);
        if (tv != null) {
            text = tv.getText().toString().trim();
            try {
                value = Integer.parseInt(text);
                if (value < min) {
                    value = defaultValue;
                }
                else if (value > max) {
                    value = defaultValue;
                }
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    private SensorModule getCurrentSensorModule() {
//        int position = spinnerTargetNode.getSelectedItemPosition();
        SensorModule target;
//        if (sensorModules == null) {
//            return null;
//        }
//        if (position < sensorModules.size()) {
//            target = sensorModules.get(position);
//        }
//        else {
//            target = null;
//        }
        target = sensorModules.get(0);
        //Log.d(TAG,"target="+target.getName());
        return target;
    }

    private void updateAllViews() {
        Logg.d(TAG, "updateAllViews");
        SensorModule sensorModule = getCurrentSensorModule();
        //updateButtonLogging(sensorModule);
        updateConnectionStatus(sensorModule);
        updateParameters(sensorModule);
        updateSensorData(sensorModule);
        //updateStatus(sensorModule);
    }

//    private void updateButtonLogging(final SensorModule sensorModule) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (sensorModule != null) {
//                    if (!sensorModule.isLogging()) {
//                        buttonLog.setText(getString(R.string.button_logging_on));
//                    } else {
//                        buttonLog.setText(getString(R.string.button_logging_off));
//                    }
//                }
//            }
//        });
//    }

    private void updateConnectionStatus(final SensorModule sensorModule) {
        Logg.d(TAG, "updateConnectionStatus");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isConnected = false;
                MeasuringState measuringState = MeasuringState.Stopped;
                MeasuringMode mode = MeasuringMode.Slow;

                if (sensorModule != null) {
                    isConnected = sensorModule.isConnected();
                    measuringState = sensorModule.measuringState;
                    mode = sensorModule.measuringMode;
                } else {
                    Logg.d(TAG, "[ERROR] sensorModule == null");
                }

                //layoutMask.setVisibility(isConnected ? View.INVISIBLE : View.VISIBLE);
                switch (mode) {
                    case Slow:
                    case Fast:
                    case Hybrid:
                        //buttonMeasure.setText((measuringState == MeasuringState.Started) ? getString(R.string.button_measure_off) : getString(R.string.button_measure_on));
                        break;
                    case Force:
                        //buttonMeasure.setText(getString(R.string.button_measure_force));
                        break;
                }

                //layoutMask.requestFocus();
            }
        });
    }

    private void updateParameters(final SensorModule sensorModule) {
        Logg.d(TAG, "updateParameters");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv;
                Switch sw;
                Spinner spinner;

                if (sensorModule == null) {
                    Logg.d(TAG, "[ERROR] sensorModule == null");
                    return;
                }

                //spinner = (Spinner) findViewById(R.id.setting_item_edit_measuring_mode);
                int selectionIndex;
                switch (sensorModule.measuringMode) {
                    case Slow:
                        selectionIndex = INDEX_MEASURING_MODE_SLOW;
                        break;
                    case Fast:
                        selectionIndex = INDEX_MEASURING_MODE_FAST;
                        break;
                    case Force:
                        selectionIndex = INDEX_MEASURING_MODE_FORCE;
                        break;
                    case Hybrid:
                        selectionIndex = INDEX_MEASURING_MODE_HYBRID;
                        break;
                    default:
                        selectionIndex = INDEX_MEASURING_MODE_SLOW;
                        break;
                }
                //spinner.setSelection(selectionIndex);
                Set<Sensor> enabledSensors = new LinkedHashSet<Sensor>();
//                tv = (TextView) layoutMain.findViewById(R.id.setting_item_edit_interval_on_mode_slow);
//                tv.setText(String.format("%d", sensorModule.intervalMeasuringOnModeSlow));
//
//                tv = (TextView) layoutMain.findViewById(R.id.setting_item_edit_interval_on_mode_fast);
//                tv.setText(String.format("%d", sensorModule.intervalMeasuringOnModeFast));
//
//                sw = (Switch) layoutMain.findViewById(R.id.setting_item_switch_magnetic);
//                sw.setChecked(sensorModule.enabledSensors.contains(Sensor.Magnetic));
//                sw = (Switch) layoutMain.findViewById(R.id.setting_item_switch_acceleration);
//                sw.setChecked(sensorModule.enabledSensors.contains(Sensor.Acceleration));
                enabledSensors.add(Sensor.Acceleration);
                Log.d("Communication","acceleration"+ sensorModule.enabledSensors.contains(Sensor.Acceleration) );
//                sw = (Switch) layoutMain.findViewById(R.id.setting_item_switch_pressure);
//                sw.setChecked(sensorModule.enabledSensors.contains(Sensor.Pressure));
//                sw = (Switch) layoutMain.findViewById(R.id.setting_item_switch_humidity);
//                sw.setChecked(sensorModule.enabledSensors.contains(Sensor.Humidity));
//                sw = (Switch) layoutMain.findViewById(R.id.setting_item_switch_temperature);
//                sw.setChecked(sensorModule.enabledSensors.contains(Sensor.Temperature));
//                sw = (Switch) layoutMain.findViewById(R.id.setting_item_switch_uv);
//                sw.setChecked(sensorModule.enabledSensors.contains(Sensor.UV));
//                sw = (Switch) layoutMain.findViewById(setting_item_switch_ambient_light);
//                sw.setChecked(sensorModule.enabledSensors.contains(Sensor.AmbientLight));
//
//                tv = (TextView) layoutMain.findViewById(R.id.setting_item_sleep_interval_on_timer_mode);
//                tv.setText(String.format("%d", sensorModule.intervalTimerAwakeLimit));

                int buttonMeasureTitleResource = 0;

                switch (sensorModule.measuringMode) {
                    case Slow:
                    case Fast:
                    case Hybrid: {
                        buttonMeasureTitleResource = (sensorModule.measuringState == MeasuringState.Started) ? R.string.button_measure_off : R.string.button_measure_on;
                        break;
                    }
                    case Force:
                        buttonMeasureTitleResource = R.string.button_measure_force;
                        break;
                }
//                buttonMeasure.setText(getResources().getText(buttonMeasureTitleResource));

//                layoutMask.requestFocus();
            }
        });
    }

//    private void updateStatus(final SensorModule sensorModule) {
//        Logg.d(TAG, "updateStatus : batteryVoltage = %f", sensorModule.latestData.batteryVoltage);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                TextView tv = (TextView) findViewById(R.id.battery_value);
//
//                float batteryVoltage = sensorModule.latestData.batteryVoltage;
//                if (Float.compare(batteryVoltage, 0) > 0) {
//                    tv.setText(String.format("%.2f", batteryVoltage));
//
//                    if (batteryVoltage >= CtrlCmdRequestStatus.DOUBLE_BATTERY_VOLTAGE_LEVEL_5) {
//                        Logg.d(TAG, "level = 5");
//                        iconBattery.setImageResource(R.drawable.battery_5);
//                    }
//                    else if (batteryVoltage >= CtrlCmdRequestStatus.DOUBLE_BATTERY_VOLTAGE_LEVEL_4) {
//                        Logg.d(TAG, "level = 4");
//                        iconBattery.setImageResource(R.drawable.battery_4);
//                    }
//                    else if (batteryVoltage >= CtrlCmdRequestStatus.DOUBLE_BATTERY_VOLTAGE_LEVEL_3) {
//                        Logg.d(TAG, "level = 3");
//                        iconBattery.setImageResource(R.drawable.battery_3);
//                    }
//                    else if (batteryVoltage >= CtrlCmdRequestStatus.DOUBLE_BATTERY_VOLTAGE_LEVEL_2) {
//                        Logg.d(TAG, "level = 2");
//                        iconBattery.setImageResource(R.drawable.battery_2);
//                    }
//                    else {
//                        Logg.d(TAG, "level = 1");
//                        iconBattery.setImageResource(R.drawable.battery_1);
//                    }
//                }
//                else {
//                    Logg.d(TAG, "level = unknown");
//                    tv.setText(getString(R.string.unknown_battery_value));
//                    iconBattery.setImageResource(R.drawable.battery_unknown);
//                }
//            }
//        });
//    }

    private void updateSensorData(final SensorModule sensorModule) {
//        Logg.d(TAG, "updateSensorData");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sensorModule == null) {
                    Logg.d(TAG, "[ERROR] sensorModule == null");
                    return;
                }
                //Log.d(TAG,"updateSensorData:"+sensorModule.enabledSensors);
//                textInfoData.setText(sensorModule.latestData.makeTextForGUI(sensorModule.enabledSensors));
            }
        });
    }

    private SensorModule.ISensorModule iSensorModule = new SensorModule.ISensorModule() {
        @Override
        public void onReadyCommunication(final int tag, final boolean ready) {
            SensorModule sensorModule = getCurrentSensorModule();
            if (sensorModule == null)  {
                return;
            }

            int currentTag = sensorModule.getTag();
            if (currentTag == tag) {
                updateConnectionStatus(sensorModule);

                if (ready) {
                    // If ready is true,
                    // the properties of sensorModule are the newest values.
                    updateParameters(sensorModule);
                    updateSensorData(sensorModule);
                    //updateStatus(sensorModule);
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (sensorModules == null) {
                        return;
                    }

                    try {
                        SensorModule sensorModuleFiredEvent = sensorModules.get(tag);
                        String text = (ready ? "READY : " : "DISCONNECTED : ") + sensorModuleFiredEvent.getName();
                        Toast.makeText(ActivitySensorCommunication.this, text, Toast.LENGTH_SHORT).show();
                    }
                    catch (IndexOutOfBoundsException e) {
                        Logg.d(TAG, "[ERROR] tag = %d, sensorModules.size = %d", tag, sensorModules.size());
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onReceiveNotificationReadingResult(int tag) {
            final SensorModule sensorModule = getCurrentSensorModule();
            int currentTag = sensorModule.getTag();
            if (currentTag == tag) {
                updateParameters(sensorModule);
            }
        }

        @Override
        public void onReceiveNotificationStatus(int tag) {
            final SensorModule sensorModule = getCurrentSensorModule();
            int currentTag = sensorModule.getTag();
            if (currentTag == tag) {
                //updateStatus(sensorModule);
            }
        }

        @Override
        public void onReceiveNack(final int tag) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (sensorModules == null) {
                        return;
                    }

                    try {
                        SensorModule sensorModuleFiredEvent = sensorModules.get(tag);
                        String text = sensorModuleFiredEvent.getName() + " get a NACK!\nSome changes were rejected.";
                        Toast.makeText(ActivitySensorCommunication.this, text, Toast.LENGTH_SHORT).show();
                    }
                    catch (IndexOutOfBoundsException e) {
                        Logg.d(TAG, "[ERROR] tag = %d, sensorModules.size = %d", tag, sensorModules.size());
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onReceiveNotificationSensorData(int tag) {
            final SensorModule sensorModule = getCurrentSensorModule();
            int currentTag = sensorModule.getTag();
            if (currentTag == tag) {
                updateSensorData(sensorModule);
            }
        }
    };
}
