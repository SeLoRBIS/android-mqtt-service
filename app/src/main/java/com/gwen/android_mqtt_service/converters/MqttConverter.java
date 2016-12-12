package com.gwen.android_mqtt_service.converters;

import android.util.Log;

import com.google.gson.Gson;
import com.gwen.android_mqtt_service.dto.PayloadMqttIn;
import com.gwen.android_mqtt_service.dto.PayloadMqttOutMatrix;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

public class MqttConverter {

    private static final String TAG = MqttConverter.class.getName();

    public static PayloadMqttIn convertMsgToMqttPayloadObject(MqttMessage msg) throws JSONException {

        Log.d(TAG, "Debut de conversion du message MQTT");
        // {"email":"email@email.com","mail_to":true,"nom":"NOM","prenom":"Prenom","send_to_twitter":false,"timestamp":1478253870746,"is_selfie":false}
        PayloadMqttIn payload = new PayloadMqttIn();
        Gson gson = new Gson();
        try {
            payload = gson.fromJson(new String(msg.getPayload(), "utf-8"), PayloadMqttIn.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // TODO supprimer en PROD
        payload.setTimestamp(System.currentTimeMillis());
        Log.d(TAG, "Fin de conversion du message MQTT");

        return payload;
    }

    public static String convertPayloadObjectToMqtt(PayloadMqttOutMatrix msg) throws JSONException {

        Log.d(TAG, "Debut de conversion en Json");

        String payload = "";
        Gson gson = new Gson();

        payload = gson.toJson(msg, PayloadMqttOutMatrix.class);

        Log.d(TAG, "Fin de conversion en Json");

        return payload;
    }

}
