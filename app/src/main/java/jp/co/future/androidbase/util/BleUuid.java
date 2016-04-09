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
package jp.co.future.androidbase.util;

/**
 * BLE UUID Strings
 */
public class BleUuid {
    // 1800は、アクセスプロファイル。デバイス名など
    // BLEの規定で定められている値
    // https://www.bluetooth.org/ja-jp/specification/assigned-numbers/generic-attribute-profile
    public static final String SERVICE_DEVICE_INFORMATION = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_DEVICE_NAME_STRING = "00002a00-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = "00002a04-0000-1000-8000-00805f9b34fb";

    // 1801始まりはサービスの状態変化通知サービス？ これは多分使わない？
    public static final String SERVICE_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb";

    // 00000000始まりはデバイス独自のサービス
    public static final String SERVICE_DFREE = "00000000-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_DFREE_VALUE = "00000000-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    // 1802 Immediate Alert
    public static final String SERVICE_IMMEDIATE_ALERT = "00001802-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb";
    // StickNFindではCHAR_ALERT_LEVELに0x01をWriteすると光り、0x02では音が鳴り、0x03では光って鳴る。

    // 180F Battery Service
    //public static final String SERVICE_BATTERY_SERVICE = "0000180F-0000-1000-8000-00805f9b34fb";
    //public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";
}
