package com.dorma.weather.ui.ble;

import android.app.Activity;
import android.app.ListFragment;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.UUID;

public class ServicesFragment extends ListFragment {
    public final static UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private ArrayAdapter<BluetoothGattService> adapter;
    private BluetoothGatt bluetoothGatt;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bluetoothGatt = ((DeviceDetailsActivity) activity).getBluetoothGatt();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ArrayAdapter<BluetoothGattService>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                if (getItem(position).getUuid().equals(ServicesFragment.HR_SERVICE_UUID)) {
                    view.setText("Heart Rate");
                } else {
                    view.setText(getItem(position).getUuid().toString());
                }
                return view;
            }
        };
        adapter.setNotifyOnChange(true);
        setListAdapter(adapter);
        if (bluetoothGatt != null) {
            adapter.addAll(bluetoothGatt.getServices());
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (bluetoothGatt != null) {
            UUID uuid = adapter.getItem(position).getUuid();
            CharachteristicsFragment f = CharachteristicsFragment.newInstance(uuid);
            getFragmentManager().beginTransaction()
                    .replace(DeviceDetailsActivity.ID ,f, "CHARACHTERISTCS")
                    .addToBackStack(null)
                    .commit();
        }
    }
}
