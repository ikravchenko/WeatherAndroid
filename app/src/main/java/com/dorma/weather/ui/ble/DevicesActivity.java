package com.dorma.weather.ui.ble;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dorma.weather.R;

public class DevicesActivity extends ListActivity {

    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;

    private Handler handler;
    private LeDeviceListAdapter leDeviceListAdapter;

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    leDeviceListAdapter.add(device);
                }
            });
        }
    };
    private MenuItem scanMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(getMainLooper());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        leDeviceListAdapter = new LeDeviceListAdapter(this);
        leDeviceListAdapter.setNotifyOnChange(true);
        setListAdapter(leDeviceListAdapter);
        initBLEAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(DeviceDetailsActivity.DEVICE_EXTRA, (BluetoothDevice)l.getItemAtPosition(position));
        startActivity(intent);
        scanLeDevice(false);
    }

    private void initBLEAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (scanning) {
                        scanning = false;
                        updateScanTitle();
                        bluetoothAdapter.stopLeScan(leScanCallback);
                    }
                }
            }, SCAN_PERIOD);

            scanning = true;
            updateScanTitle();
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            scanning = false;
            updateScanTitle();
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private void updateScanTitle() {
        scanMenu.setTitle(scanning ? "Stop" : "Scan");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                initBLEAdapter();
            } else {
                Toast.makeText(this, "Please activate Bluetooth", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);
        scanMenu = menu.findItem(R.id.scan_action);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_action:
                scanLeDevice(!scanning);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Displays found devices list
     */
    class LeDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
        public LeDeviceListAdapter(Context context) {
            super(context, android.R.layout.two_line_list_item, android.R.id.text1);
        }

        @Override
        public void add(BluetoothDevice object) {
            if (getPosition(object) == -1) {
                super.add(object);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            ((TextView)view.findViewById(android.R.id.text1)).setText(getItem(position).getName());
            ((TextView)view.findViewById(android.R.id.text2)).setText(getItem(position).getAddress());
            return view;
        }
    }
}
