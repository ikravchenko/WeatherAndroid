package com.dorma.weather.ui.ble;

import android.app.Activity;
import android.app.ListFragment;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dorma.weather.R;

import java.util.List;
import java.util.UUID;

public class CharachteristicsFragment extends ListFragment {

    static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    static final UUID HR_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
    private static final String UUID_EXTRA = "UUID_EXTRA";
    static final int FIRST_BITMASK = 0x01;
    private BluetoothGatt bluetoothGatt;
    private UUID serviceUuid;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((ArrayAdapter) getListAdapter()).notifyDataSetChanged();
        }
    };

    public static CharachteristicsFragment newInstance(UUID serviceUuid) {
        CharachteristicsFragment f = new CharachteristicsFragment();
        Bundle b = new Bundle();
        b.putSerializable(UUID_EXTRA, serviceUuid);
        f.setArguments(b);

        return f;
    }

    public CharachteristicsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.charachteristics, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_read) {
            readHRLocationIfAvailable();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bluetoothGatt = ((DeviceDetailsActivity) activity).getBluetoothGatt();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(DeviceDetailsActivity.ON_CHARACTERISTIC_CHANGED_ACTION));
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serviceUuid = (UUID) getArguments().get(UUID_EXTRA);
        final ArrayAdapter<BluetoothGattCharacteristic> adapter = new ArrayAdapter<BluetoothGattCharacteristic>(getActivity(), android.R.layout.two_line_list_item, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(16, 16, 16, 16);
                TextView title = (TextView) view.findViewById(android.R.id.text1);
                TextView value = (TextView) view.findViewById(android.R.id.text2);

                BluetoothGattCharacteristic c = getItem(position);
                final UUID uuid = c.getUuid();
                if (uuid.equals(HR_CHARACTERISTIC_UUID)) {
                    title.setText("Heart Sensor");
                    if (c.getValue() != null && c.getValue().length >= 1) {
                        value.setText("Value: " + readHRValue(c));
                    }
                } else if (uuid.equals(HR_SENSOR_LOCATION_CHARACTERISTIC_UUID)) {
                    title.setText("Sensor Location");
                    if (c.getValue() != null && c.getValue().length >= 1) {
                        value.setText("Location: " + getBodySensorPosition(c.getValue()[0]));
                    }
                } else {
                    title.setText(uuid.toString());
                    value.setText("");
                }
                return view;
            }
        };
        adapter.setNotifyOnChange(true);
        setListAdapter(adapter);
        if (bluetoothGatt != null) {
            List<BluetoothGattCharacteristic> characteristics = bluetoothGatt.getService(serviceUuid).getCharacteristics();
            adapter.addAll(characteristics);
            setNotifyHRValueIfAvailable(true);
        }
    }

    private void readHRLocationIfAvailable() {
        BluetoothGattCharacteristic hrLocation = bluetoothGatt.getService(serviceUuid).getCharacteristic(HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
        if (hrLocation != null) {
            bluetoothGatt.readCharacteristic(hrLocation);
        }
    }

    private void setNotifyHRValueIfAvailable(boolean notify) {
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(serviceUuid).getCharacteristic(HR_CHARACTERISTIC_UUID);
        if (characteristic == null) {
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, notify);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setNotifyHRValueIfAvailable(false);
    }

    /**
     * This method will decode and return Heart rate sensor position on body
     */
    private String getBodySensorPosition(byte bodySensorPositionValue) {
        if (bodySensorPositionValue == 0x00)
            return "Other";
        else if (bodySensorPositionValue == 0x01)
            return "Chest";
        else if (bodySensorPositionValue == 0x02)
            return "Wrist";
        else if (bodySensorPositionValue == 0x03)
            return "Finger";
        else if (bodySensorPositionValue == 0x04)
            return "Hand";
        else if (bodySensorPositionValue == 0x05)
            return "Ear Lobe";
        else if (bodySensorPositionValue == 0x06)
            return "Foot";
        return "reserved for future use";
    }

    private int readHRValue(BluetoothGattCharacteristic characteristic) {
        if (isHeartRateInUINT16(characteristic.getValue()[0])) {
            return characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
        } else {
            return characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        }
    }

    /**
     * This method will check if Heart rate value is in 8 bits or 16 bits
     */
    private boolean isHeartRateInUINT16(byte value) {
        if ((value & FIRST_BITMASK) != 0)
            return true;
        return false;
    }
}
