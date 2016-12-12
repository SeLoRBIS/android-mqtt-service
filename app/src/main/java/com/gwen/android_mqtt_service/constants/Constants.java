package com.gwen.android_mqtt_service.constants;

import android.content.Context;

import com.gwen.android_mqtt_service.utils.AssetsPropertyReader;

import java.util.UUID;

public class Constants {

    private static Context context;

    /** MQTT */
    /** Constants Client Id */
    public static final String MQTT_CLIENT_ID = "ntdc-" + UUID.randomUUID().toString();
    /** Constants Timeout */
    public static final int MQTT_TIMEOUT = 10000;
    /** Constants QOS */
    public static final int MQTT_QOS = 1;
    /** Constants Keep Aive Message */
    public static final byte[] MQTT_KEEP_ALIVE_MESSAGE = { 0 };
    /** Constants Statut OK */
    public static final String MQTT_STATUT_OK = "OK";
    /** Constants Statut KO */
    public static final String MQTT_STATUT_KO = "KO";

    /** NODE-RED */
    /** User Node-Red */
    public static final String MQTT_NODE_RED_USERNAME = AssetsPropertyReader.getInstance(context).getProperty("mqtt.nodered.username");
    /** Pwd Node-Red */
    public static final String MQTT_NODE_RED_PWD = AssetsPropertyReader.getInstance(context).getProperty("mqtt.nodered.pwd");
    /** Expediteur de l'envoi de photo Node-Red */
    public static final String MQTT_MAIL_FROM = AssetsPropertyReader.getInstance(context).getProperty("mqtt.nodered.mail.from");

    public static final String MQTT_MESSAGE = "mqtt_message";

    /** Validite du message en secondes */
    public static final int MQTT_MSG_VALIDITE = 60;
    /** Timeout values */
    public static final int OKHTTP_CONNECT_TIMEOUT = 60;
    public static final int OKHTTP_READ_TIMEOUT = 60;
    public static final int OKHTTP_WRITE_TIMEOUT = 60;

    /**
     * retourne l'url de l'API REST Node-Red
     * @return url
     */
    public static String getNodeRedUrlWS() {
        return AssetsPropertyReader.getInstance(context).getProperty("mqtt.nodered.url.ws");
    }

}
