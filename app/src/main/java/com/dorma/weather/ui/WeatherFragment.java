package com.dorma.weather.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dorma.weather.R;
import com.dorma.weather.network.WeatherService;

public class WeatherFragment extends ListFragment {


    public WeatherFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_request) {
            getActivity().startService(new Intent(getActivity(), WeatherService.class));
            return true;
        } else if (id == R.id.action_add) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
