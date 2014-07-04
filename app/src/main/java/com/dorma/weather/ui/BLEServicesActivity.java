package com.dorma.weather.ui;

import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.UUID;

public class BLEServicesActivity extends ListActivity {

    public final static UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");

    private static final UUID HR_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final String DEVICE_EXTRA = "DEVICE_EXTRA";
    private BluetoothDevice device;
    private BluetoothGattCallback callback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.addAll(gatt.getServices());
                }
            });
        }

    };
    private BluetoothGatt bluetoothGatt;
    private ArrayAdapter<BluetoothGattService> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(DEVICE_EXTRA)) {
            onBackPressed();
            return;
        }

        device = getIntent().getParcelableExtra(DEVICE_EXTRA);
        adapter = new ArrayAdapter<BluetoothGattService>(this, android.R.layout.simple_list_item_1, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                if (getItem(position).getUuid().equals(HR_SERVICE_UUID)) {
                    view.setText("Heart Rate");
                } else {
                    view.setText(getItem(position).getUuid().toString());
                }
                return view;
            }
        };
        adapter.setNotifyOnChange(true);
        setListAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothGatt = device.connectGatt(this, true, callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }
    }
}
