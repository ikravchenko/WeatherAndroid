package com.dorma.weather.ui.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class DeviceDetailsActivity extends Activity {

    public static final String DEVICE_EXTRA = "DEVICE_EXTRA";

    static final int ID = 12345;
    public static final String ON_CHARACTERISTIC_CHANGED_ACTION = "onCharacteristicChanged";

    private BluetoothDevice device;
    private BluetoothGattCallback callback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(getClass().getSimpleName(), "onConnectionStateChange");
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Log.i(getClass().getSimpleName(), "onServicesDiscovered");
            super.onServicesDiscovered(gatt, status);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().beginTransaction().replace(ID, new ServicesFragment()).commit();
                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i(getClass().getSimpleName(), "onCharacteristicChanged");
            super.onCharacteristicChanged(gatt, characteristic);
            Intent intent = new Intent(ON_CHARACTERISTIC_CHANGED_ACTION);
            LocalBroadcastManager.getInstance(DeviceDetailsActivity.this).sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(getClass().getSimpleName(), "onCharacteristicRead");
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    };
    private BluetoothGatt bluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout view = new FrameLayout(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setId(ID);
        setContentView(view);

        if (!getIntent().hasExtra(DEVICE_EXTRA)) {
            onBackPressed();
            return;
        }

        device = getIntent().getParcelableExtra(DEVICE_EXTRA);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(device.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothGatt = device.connectGatt(this, false, callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
}
