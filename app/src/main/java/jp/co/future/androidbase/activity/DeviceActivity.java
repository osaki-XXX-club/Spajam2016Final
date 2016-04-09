/*
 * Copyright (C) 2013 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.future.androidbase.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import jp.co.future.androidbase.R;
import jp.co.future.androidbase.util.BleUtil;
import jp.co.future.androidbase.util.BleUuid;


/**
 * BLEデバイスへのconnect・Service
 * Discoveryを実施し、Characteristicsのread/writeをハンドリングするActivity
 */
public class DeviceActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "BLEDevice";

    public static final String EXTRA_BLUETOOTH_DEVICE = "BT_DEVICE";
    private BluetoothAdapter mBTAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mConnGatt;
    private int mStatus;

    private Button mReadManufacturerNameButton;
    private Button mReadSerialNumberButton;
    private Button mWriteAlertLevelButton;


    private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mStatus = newState;
                mConnGatt.discoverServices();
                Log.i(TAG, "GATT接続");
                Log.i(TAG, "Connected to GATT server.");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mStatus = newState;
                Log.d(TAG, "GATT非接続");
                runOnUiThread(new Runnable() {
                    public void run() {
                        mReadManufacturerNameButton.setEnabled(false);
                        mReadSerialNumberButton.setEnabled(false);
                        mWriteAlertLevelButton.setEnabled(false);
                    }

                    ;
                });
            }
        }

        ;

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "サービス発見イベント");
            // button able
            runOnUiThread(new Runnable() {
                public void run() {
                    //mReadManufacturerNameButton.setEnabled(true);
                    //mReadSerialNumberButton.setEnabled(true);
                    //mWriteAlertLevelButton.setEnabled(true);
                }

                ;
            });


            for (BluetoothGattService service : gatt.getServices()) {
                if ((service == null) || (service.getUuid() == null)) {
                    continue;
                }

                // デバイスのcharacteristicとdescriptorをログに表示
                // どんなデータが飛んできてるかチェック
                Log.i(TAG, "service.getUuid:" + service.getUuid());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    Log.i(TAG, "characteristic UUID:" + characteristic.getUuid());
                    int charaProp = characteristic.getProperties();
                    Log.i(TAG, "characteristic property:" + charaProp);
                    Log.i(TAG, "characteristic property READ:" + (BluetoothGattCharacteristic.PROPERTY_READ <= charaProp));
                    Log.i(TAG, "characteristic property WRITE:" + (BluetoothGattCharacteristic.PROPERTY_WRITE <= charaProp));
                    Log.i(TAG, "value:" + characteristic.getValue());
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        Log.i(TAG, "descriptor UUID:" + descriptor.getUuid());
                        Log.i(TAG, "descriptor value:" + descriptor.getValue());

                    }

//                    Log.i(TAG, "value:" + service.getCharacteristic(UUID.fromString(characteristic.getUuid().toString())));

                }

//                Log.i(TAG, "CHAR_MANUFACTURER_NAME_STRING:" + service.getCharacteristic(UUID.fromString(BleUuid.CHAR_MANUFACTURER_NAME_STRING)));
//                Log.i(TAG, "CHAR_SERIAL_NUMBER_STRING:" + service.getCharacteristic(UUID.fromString(BleUuid.CHAR_SERIAL_NUMBER_STRING)));

                // デバイス情報のサービスの場合
                if (BleUuid.SERVICE_DEVICE_INFORMATION.equalsIgnoreCase(service
                        .getUuid().toString())) {
                    mReadManufacturerNameButton
                            .setTag(service.getCharacteristic(UUID
                                    .fromString(BleUuid.CHAR_DEVICE_NAME_STRING)));
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mReadManufacturerNameButton.setEnabled(true);
                        }

                        ;
                    });
                }
                // デバイス独自設定のサービス
                if (BleUuid.SERVICE_DFREE.equalsIgnoreCase(service
                        .getUuid().toString())) {
                    mReadSerialNumberButton
                            .setTag(service.getCharacteristic(UUID
                                    .fromString(BleUuid.CHAR_DFREE_VALUE)));
                    mWriteAlertLevelButton.setTag(service
                            .getCharacteristic(UUID
                                    .fromString(BleUuid.CHAR_DFREE_VALUE)));
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mReadSerialNumberButton.setEnabled(true);
                            mWriteAlertLevelButton.setEnabled(true);
                        }

                        ;
                    });
                }
//                if (BleUuid.SERVICE_IMMEDIATE_ALERT.equalsIgnoreCase(service
//                        .getUuid().toString())) {
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            mWriteAlertLevelButton.setEnabled(true);
//                        }
//
//                        ;
//                    });
//                    mWriteAlertLevelButton.setTag(service
//                            .getCharacteristic(UUID
//                                    .fromString(BleUuid.CHAR_ALERT_LEVEL)));
//                }
            }

            runOnUiThread(new Runnable() {
                public void run() {
                    setProgressBarIndeterminateVisibility(false);
                }

                ;
            });
        }

        ;

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead 呼ばれた");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (BleUuid.CHAR_DEVICE_NAME_STRING
                        .equalsIgnoreCase(characteristic.getUuid().toString())) {
                    Log.i(TAG, "CHAR_DEVICE_NAME_STRING");
                    final String name = characteristic.getStringValue(0);

                    // 値として何が入ってきているかログに出力
                    Log.i(TAG, "characteristic UUID:" + characteristic.getUuid());
                    Log.i(TAG, "value:" + characteristic.getValue());
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        Log.i(TAG, "descriptor UUID:" + descriptor.getUuid());
                        Log.i(TAG, "descriptor value:" + descriptor.getValue());

                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            mReadManufacturerNameButton.setText(name);
                            setProgressBarIndeterminateVisibility(false);
                        }

                        ;
                    });
                } else if (BleUuid.CHAR_DFREE_VALUE
                        .equalsIgnoreCase(characteristic.getUuid().toString())) {
                    Log.i(TAG, "CHAR_DFREE_VALUE_STRING : " + characteristic.getStringValue(0));
                    Log.i(TAG, "CHAR_DFREE_VALUE_FLOAT : " + characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                    Log.i(TAG, "CHAR_DFREE_VALUE_INT : " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));

                    //final String name = characteristic.getValue().toString();
                    final int val = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);


                    // 値として何が入ってきているかログに出力
                    Log.i(TAG, "characteristic UUID:" + characteristic.getUuid());
                    Log.i(TAG, "value:" + characteristic.getValue());
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        Log.i(TAG, "descriptor UUID:" + descriptor.getUuid());
                        Log.i(TAG, "descriptor value:" + descriptor.getValue());

                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            mReadSerialNumberButton.setText(String.valueOf(val));
                            setProgressBarIndeterminateVisibility(false);
                        }

                        ;
                    });
                }

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicWrite 呼ばれた");
            runOnUiThread(new Runnable() {
                public void run() {
                    setProgressBarIndeterminateVisibility(false);
                }

                ;
            });
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device);

        // state
        mStatus = BluetoothProfile.STATE_DISCONNECTED;
        mReadManufacturerNameButton = (Button) findViewById(R.id.read_manufacturer_name_button);
        mReadManufacturerNameButton.setOnClickListener(this);
        mReadSerialNumberButton = (Button) findViewById(R.id.read_serial_number_button);
        mReadSerialNumberButton.setOnClickListener(this);
        mWriteAlertLevelButton = (Button) findViewById(R.id.write_alert_level_button);
        mWriteAlertLevelButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnGatt != null) {
            if ((mStatus != BluetoothProfile.STATE_DISCONNECTING)
                    && (mStatus != BluetoothProfile.STATE_DISCONNECTED)) {
                mConnGatt.disconnect();
            }
            mConnGatt.close();
            mConnGatt = null;
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.read_manufacturer_name_button) {
            if ((v.getTag() != null)
                    && (v.getTag() instanceof BluetoothGattCharacteristic)) {
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
                        .getTag();
                if (mConnGatt.readCharacteristic(ch)) {
                    Log.i(TAG, "デバイスネームRead");
                    setProgressBarIndeterminateVisibility(true);
                }
            }
        } else if (v.getId() == R.id.read_serial_number_button) {
            if ((v.getTag() != null)
                    && (v.getTag() instanceof BluetoothGattCharacteristic)) {
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
                        .getTag();
                if (mConnGatt.readCharacteristic(ch)) {
                    Log.i(TAG, "値Read");
                    setProgressBarIndeterminateVisibility(true);
                }
            }

        } else if (v.getId() == R.id.write_alert_level_button) {
            if ((v.getTag() != null)
                    && (v.getTag() instanceof BluetoothGattCharacteristic)) {

//                BluetoothManager mBluetoothManager = BleUtil.getManager(this);
//                BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
//                BluetoothDevice mDevice = getBTDeviceExtra();
//                BluetoothGatt mBG = mDevice.connectGatt(this, false, mGattcallback);
//                BluetoothGattService mSVC = mConnGatt.getService(UUID.fromString(BleUuid.SERVICE_DFREE));
//                BluetoothGattCharacteristic mCH = mSVC.getCharacteristic(UUID.fromString(BleUuid.CHAR_DFREE_VALUE));
//                mCH.setValue(new byte[]{(byte) 0x01});
                //mBG.writeCharacteristic(mCH);

                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
                        .getTag();
                ch.setValue(new byte[]{(byte) 0x01});
//                ch.setValue(1,BluetoothGattCharacteristic.FORMAT_UINT32, 0);

                if (mConnGatt.writeCharacteristic(ch)) {
                    Log.i(TAG, "値Write");
                    setProgressBarIndeterminateVisibility(true);
                } else {
                    Log.i(TAG, "値Write失敗");
                }
            }
        }
    }

    private void init() {
        // BLE check
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        // BT check
        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        // check BluetoothDevice
        if (mDevice == null) {
            mDevice = getBTDeviceExtra();
            if (mDevice == null) {
                finish();
                return;
            }
        }

        // button disable
        mReadManufacturerNameButton.setEnabled(false);
        mReadSerialNumberButton.setEnabled(false);
        mWriteAlertLevelButton.setEnabled(false);

        // connect to Gatt
        if ((mConnGatt == null)
                && (mStatus == BluetoothProfile.STATE_DISCONNECTED)) {
            // try to connect
            mConnGatt = mDevice.connectGatt(this, false, mGattcallback);
            mStatus = BluetoothProfile.STATE_CONNECTING;
        } else {
            if (mConnGatt != null) {
                // re-connect and re-discover Services
                mConnGatt.connect();
                mConnGatt.discoverServices();
            } else {
                Log.e(TAG, "state error");
                finish();
                return;
            }
        }
        setProgressBarIndeterminateVisibility(true);
    }

    private BluetoothDevice getBTDeviceExtra() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        }

        return extras.getParcelable(EXTRA_BLUETOOTH_DEVICE);
    }

}
