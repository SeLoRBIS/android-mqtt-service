package com.gwen.android_mqtt_service.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.gwen.android_mqtt_service.activities.MainActivity;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class RuntimePermissionRequestActivity extends Activity {

    private static final String TAG = RuntimePermissionRequestActivity.class.getName();
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private String menuItem;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        } else {
            startCamActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamActivity();
                } else {
                    Toast.makeText(this, "Sorry, Please grant camera permission.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void startCamActivity(){

        Intent intent = new Intent(this, MainActivity.class);

        this.startActivity(intent);
        ActivityCompat.finishAfterTransition(this);

    }
}
