package com.dorma.weather.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.dorma.weather.R;
import com.dorma.weather.WeatherApplication;
import com.dorma.weather.db.DatabaseHelper;
import com.dorma.weather.db.WeatherDAO;
import com.dorma.weather.db.WeatherItem;
import com.dorma.weather.network.model.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends Activity implements LoaderManager.LoaderCallbacks<WeatherItem> {

    public static final String WEATHER_ID = "WEATHER_ID";

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private WeatherDAO dao;
    private WeatherItem currentItem;
    private TextView fromTime;
    private TextView toTime;
    private EditText forecast;
    private EditText temperature;
    private CheckBox warm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        dao = new WeatherDAO(this);

        fromTime = (TextView) findViewById(R.id.fromTimeVal);
        toTime = (TextView) findViewById(R.id.toTimeVal);
        forecast = (EditText) findViewById(R.id.forecastVal);
        temperature = (EditText) findViewById(R.id.temperatureVal);
        warm = (CheckBox) findViewById(R.id.warm);

        if (getIntent().hasExtra(WEATHER_ID)) {
            getLoaderManager().initLoader(0, null, this);
        } else {
            fromTime.setText(format.format(new Date()));
            toTime.setText(format.format(new Date()));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        if (!getIntent().hasExtra(WEATHER_ID)) {
            menu.findItem(R.id.delete_action).setEnabled(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save_action:
                if (currentItem == null) {
                    try {
                        dao.insert(
                            new WeatherItem(
                                format.parse(fromTime.getText().toString()).getTime(),
                                format.parse(toTime.getText().toString()).getTime(),
                                forecast.getText().toString(),
                                Float.parseFloat(temperature.getText().toString()),
                                warm.isChecked())
                        );
                        onBackPressed();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    currentItem.warm = warm.isChecked();
                    currentItem.forecast = forecast.getText().toString();
                    currentItem.temperature = Float.parseFloat(temperature.getText().toString());
                    dao.update(currentItem);
                    onBackPressed();
                }
                return true;
            case R.id.delete_action:
                dao.deleteById(getIntent().getLongExtra(WEATHER_ID, -1));
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<WeatherItem> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<WeatherItem>(this) {
            @Override
            public WeatherItem loadInBackground() {
                return dao.getById(getIntent().getLongExtra(WEATHER_ID, -1));
            }

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<WeatherItem> loader, WeatherItem data) {
        if (data != null) {
            currentItem = data;
            fromTime.setText(format.format(new Date(data.startTime)));
            toTime.setText(format.format(new Date(data.endTime)));
            forecast.setText(data.forecast);
            temperature.setText(String.valueOf(data.temperature));
            warm.setChecked(data.warm);
        }
    }

    @Override
    public void onLoaderReset(Loader<WeatherItem> loader) {
    }
}
