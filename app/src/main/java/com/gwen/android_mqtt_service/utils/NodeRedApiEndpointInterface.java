package com.gwen.android_mqtt_service.utils;

import com.gwen.android_mqtt_service.dto.NodeRedMsg;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NodeRedApiEndpointInterface {

    // MQTT
    @POST("mail_to")
    Call<NodeRedMsg> sendMailMQTT(@Body NodeRedMsg payload);

}

