package com.gwen.android_mqtt_service.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.gwen.android_mqtt_service.activities.PhotoActivity;
import com.gwen.android_mqtt_service.activities.TTS;
import com.gwen.android_mqtt_service.constants.Constants;
import com.gwen.android_mqtt_service.converters.MqttConverter;
import com.gwen.android_mqtt_service.dto.PayloadMqttIn;
import com.gwen.android_mqtt_service.utils.AssetsPropertyReader;
import com.gwen.android_mqtt_service.utils.Utils;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Date;

public class MQTTService extends Service {

    private static final String TAG = MQTTService.class.getName();
    private static boolean hasWifi = false;
    private static boolean hasMmobile = false;
    private ConnectivityManager mConnMan;
    private volatile IMqttAsyncClient mqttClient;
    private MQTTBroadcastReceiver mqttBroadcastReceiver;

    class MQTTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            IMqttToken token;
            boolean hasConnectivity = false;
            boolean hasChanged = false;
            NetworkInfo infos[] = mConnMan.getAllNetworkInfo();

            for (int i = 0; i < infos.length; i++){
                if (infos[i].getTypeName().equalsIgnoreCase("MOBILE")){
                    if((infos[i].isConnected() != hasMmobile)){
                        hasChanged = true;
                        hasMmobile = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                } else if ( infos[i].getTypeName().equalsIgnoreCase("WIFI") ){
                    if((infos[i].isConnected() != hasWifi)){
                        hasChanged = true;
                        hasWifi = infos[i].isConnected();
                    }
                    Log.d(TAG, infos[i].getTypeName() + " is " + infos[i].isConnected());
                }
            }

            hasConnectivity = hasMmobile || hasWifi;
            Log.v(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - "+(mqttClient == null || !mqttClient.isConnected()));
            if (hasConnectivity && hasChanged && (mqttClient == null || !mqttClient.isConnected())) {
                connect();
            } else if (!hasConnectivity && mqttClient != null && mqttClient.isConnected()) {
                Log.d(TAG, "doDisconnect()");
                try {
                    token = mqttClient.disconnect();
                    token.waitForCompletion(1000);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public class MQTTBinder extends Binder {
        public MQTTService getService(){
            return MQTTService.this;
        }
    }

    @Override
    public void onCreate() {
        IntentFilter intentf = new IntentFilter();
        intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mqttBroadcastReceiver = new MQTTBroadcastReceiver();
        registerReceiver(mqttBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        android.os.Debug.waitForDebugger();
        super.onConfigurationChanged(newConfig);
    }

    private MqttConnectOptions setOptions(){
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setUserName(AssetsPropertyReader.getInstance(this).getProperty("mqtt.username"));
        opts.setPassword(AssetsPropertyReader.getInstance(this).getProperty("mqtt.pwd").toCharArray());
        opts.setCleanSession(true);
        return opts;
    }

    private void connect(){
        IMqttToken token;
        try {
            Log.d(TAG, "Client MQTT creation ");
            mqttClient = new MqttAsyncClient(AssetsPropertyReader.getInstance(this).getProperty("mqtt.wss_uri"), Constants.MQTT_CLIENT_ID, new MemoryPersistence());
            Log.d(TAG, "Broker Connection : Username=" + setOptions().getUserName() + " , CleanSession=" + setOptions().isCleanSession());
            token = mqttClient.connect(setOptions());
            token.waitForCompletion(Constants.MQTT_TIMEOUT);
            Log.d(TAG, "Callback event");
            Log.d(TAG, "Topic Subscription");
            token = mqttClient.subscribe(AssetsPropertyReader.getInstance(this).getProperty("mqtt.topic.greater"), 0);
            token.waitForCompletion(Constants.MQTT_TIMEOUT);
            // token = mqttClient.subscribe(AssetsPropertyReader.getInstance(this).getProperty("mqtt.topic.test"), 0);
            // token.waitForCompletion(Constants.MQTT_TIMEOUT);
            mqttClient.setCallback(new MqttEventCallback());
        } catch (MqttSecurityException e) {
            Log.d(TAG,"MqttSecurityException - " + e.getMessage());
        } catch (MqttException e) {
            checkError(e);
        }
    }

    public void publish(String topic, String msg){
        Log.d(TAG, "doConnect()");
        IMqttToken token;
        try {
            Log.d(TAG, "Client MQTT creation ");
            mqttClient = new MqttAsyncClient(AssetsPropertyReader.getInstance(this).getProperty("mqtt.wss_uri"),  Constants.MQTT_CLIENT_ID, new MemoryPersistence());
            Log.d(TAG, "Broker Connection : Username=" + setOptions().getUserName() + " , CleanSession=" + setOptions().isCleanSession());
            token = mqttClient.connect(setOptions());
            token.waitForCompletion(Constants.MQTT_TIMEOUT);
            Log.d(TAG, "Callback event");
            mqttClient.setCallback(new MqttEventCallback());
            Log.d(TAG, "Topic Subscription");
            MqttMessage message = new MqttMessage(Constants.MQTT_KEEP_ALIVE_MESSAGE);
            message.setQos(Constants.MQTT_QOS);
            message.setPayload(msg.getBytes());
            mqttClient.publish(topic, message);
            token.waitForCompletion(Constants.MQTT_TIMEOUT);
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            checkError(e);
        }
    }

    private void checkError(MqttException e){
        switch (e.getReasonCode()) {
            case MqttException.REASON_CODE_CLIENT_EXCEPTION:
                Log.e(TAG, "Error " + MqttException.REASON_CODE_CLIENT_EXCEPTION + " - Code client exception : " + e.getMessage());
                break;
            case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                Log.e(TAG, "Error " + MqttException.REASON_CODE_BROKER_UNAVAILABLE + " - Code broker unavailable : " + e.getMessage());
                break;
            case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                Log.e(TAG, "Error " + MqttException.REASON_CODE_CLIENT_TIMEOUT + " - Client timeout : " + e.getMessage());
                connect();
                break;
            case MqttException.REASON_CODE_CONNECTION_LOST:
                Log.e(TAG, "Error " + MqttException.REASON_CODE_CONNECTION_LOST + " - Connection lost : " + e.getMessage());
                break;
            case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                Log.e(TAG, "Error " + MqttException.REASON_CODE_SERVER_CONNECT_ERROR + " - Server connect error : " + e.getMessage());
                e.printStackTrace();
                break;
            case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                Log.e(TAG, "Error " + MqttException.REASON_CODE_FAILED_AUTHENTICATION + " - Code Failed Authentification : " + e.getMessage());
                break;
            default:
                Log.e(TAG, "Error :" + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand()");
        connect();
        return START_STICKY;
    }

    private class MqttEventCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable arg0) {
            Toast.makeText(getApplicationContext(), "MQTT - Connection lost", Toast.LENGTH_SHORT).show();
            connect();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            Toast.makeText(getApplicationContext(), "MQTT - Delivery complete", Toast.LENGTH_SHORT).show();
        }

        @Override
        @SuppressLint("NewApi")
        public void messageArrived(String topic, final MqttMessage msg) throws Exception {

            if (topic.equals(AssetsPropertyReader.getInstance(getApplicationContext()).getProperty("mqtt.topic.test"))) {
                Handler h = new Handler(getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        TTS.speak(msg.toString());
                    }
                });
            } else {

                Log.i(TAG, "Message arrived from topic : " + topic);

                final PayloadMqttIn payload = MqttConverter.convertMsgToMqttPayloadObject(msg);

                Log.d(TAG, "Message date : " + new Date(payload.getTimestamp()));

                // On verifie la validite du message
                if (Utils.isDateValide(payload.getTimestamp())) {
                    if (payload.is_preview()) {
                        Log.d(TAG, "Message date valid : " + new Date(payload.getTimestamp()));
                        Handler h = new Handler(getMainLooper());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent launchIntent = new Intent(MQTTService.this, PhotoActivity.class);
                                launchIntent.putExtra(Constants.MQTT_MESSAGE, payload);
                                if (Build.VERSION.SDK_INT >= 11) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                }
                                startActivity(launchIntent);
                                Log.d(TAG, "MQTT Message : " + new String(msg.getPayload()));
                                // Toast.makeText(getApplicationContext(), "MQTT Message:\n" + new String(msg.getPayload()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.d(TAG, "Message date valid : " + new Date(payload.getTimestamp()));
                        Handler h = new Handler(getMainLooper());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent launchIntent = new Intent(MQTTService.this, PhotoActivity.class);
                                launchIntent.putExtra(Constants.MQTT_MESSAGE, payload);
                                if (Build.VERSION.SDK_INT >= 11) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                }
                                startActivity(launchIntent);
                                Log.d(TAG, "MQTT Message : " + new String(msg.getPayload()));
                                // Toast.makeText(getApplicationContext(), "MQTT Message:\n" + new String(msg.getPayload()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else {
                    Log.d(TAG, "Message date invalid : " + new Date(payload.getTimestamp()));
                    Toast.makeText(getApplicationContext(), "Message date invalid : " + new Date(payload.getTimestamp()), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stop the MQTTService");
        try {
            Log.d(TAG, "Deconnexion du client MQTT");
            unregisterReceiver(mqttBroadcastReceiver);
            mqttClient.disconnect();
            Log.d(TAG, "Désenregistrement du BroadcastReceiver MQTT");

        } catch (MqttException e) {
            Log.e(TAG, "Message date invalid - onDestroy : " + e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called");
        return null;
    }

}
