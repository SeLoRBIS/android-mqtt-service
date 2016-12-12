package com.gwen.android_mqtt_service.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gwen.android_mqtt_service.R;
import com.gwen.android_mqtt_service.services.MQTTService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar(R.string.activity_home_title);
        TTS.init(getApplicationContext());
    }

    /** Set the toolbar */
    private void setToolbar(@StringRes int activity_home_title){
        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(activity_home_title);
        mToolbar.showOverflowMenu();
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.service_menu, menu);
        return true;
    }

    /** Action lors de l'interaction avec le menu de la toolbar **/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case R.id.activate_service:
                Intent i = new Intent(MainActivity.this, MQTTService.class);
                if (isMyServiceRunning(MQTTService.class)){
                    Log.d(TAG, "Stop the Android MQTTService");
                    stopService(i);
                    item.setTitle(R.string.menu_service_mqtt_on);
                } else {
                    Log.d(TAG, "Start the Android MQTTService");
                    startService(i);
                    item.setTitle(R.string.menu_service_mqtt_off);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check if a service is started
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
