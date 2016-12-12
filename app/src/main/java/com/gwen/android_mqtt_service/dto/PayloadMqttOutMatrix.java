package com.gwen.android_mqtt_service.dto;

import java.io.Serializable;

public class PayloadMqttOutMatrix implements Serializable {

    // { "Demo": "Protoypage", "Application": "Web", "Type": "Squelette", "Status": "OK" }

    private String Demo;
    private String Application;
    private String Type;
    private String Status;

    public PayloadMqttOutMatrix() {
    }

    public String getDemo() {
        return Demo;
    }

    public void setDemo(String demo) {
        Demo = demo;
    }

    public String getApplication() {
        return Application;
    }

    public void setApplication(String application) {
        Application = application;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
