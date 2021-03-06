package com.dorma.weather.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.dorma.weather.R;
import com.dorma.weather.WeatherApplication;
import com.dorma.weather.db.DatabaseHelper;
import com.dorma.weather.db.WeatherDAO;
import com.dorma.weather.network.WeatherService;
import com.dorma.weather.network.model.Time;

import java.util.Date;

public class WeatherFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private WeatherAdapter adapter;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getLoaderManager().restartLoader(0, null, WeatherFragment.this);
        }
    };

    public WeatherFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter(WeatherDAO.WEATHER_UPDATE_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new WeatherAdapter(getActivity(), null);
        setListAdapter(adapter);
        getListView().setClickable(true);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(DetailActivity.WEATHER_ID, id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_request) {
            getActivity().startService(new Intent(getActivity(), WeatherService.class));
            return true;
        } else if (id == R.id.action_add) {
            startActivity(new Intent(getActivity(), DetailActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                Cursor cursor = ((WeatherApplication) getActivity().getApplication()).getDbHelper().getReadableDatabase().query(
                        DatabaseHelper.TABLE_NAME,
                        new String[]{"ROWID _id, *"},
                        null,
                        null,
                        null,
                        null,
                        DatabaseHelper.FROM_TIME + " ASC");
                cursor.getCount();
                return cursor;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    class WeatherAdapter extends CursorAdapter {

        public WeatherAdapter(Context context, Cursor c) {
            super(context, c, true);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(getActivity()).inflate(R.layout.weather_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            long from = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.FROM_TIME));
            long to = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.TO_TIME));
            ((TextView)view.findViewById(R.id.time)).setText("From: " + DateFormat.format("dd MMM hh:mm", new Date(from)) + ", To: " + DateFormat.format("dd MMM hh:mm", new Date(to)));

            String forecast = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FORECAST));
            float t = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.TEMPERATURE));
            ((TextView)view.findViewById(R.id.forecast)).setText(forecast + ", t: " + t);
            boolean warm = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.WARM)) > 0;
            ((CheckBox)view.findViewById(R.id.checkBox)).setChecked(warm);
        }
    }
}
