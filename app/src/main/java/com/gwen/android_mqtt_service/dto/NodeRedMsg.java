package com.gwen.android_mqtt_service.dto;

import java.io.Serializable;

public class NodeRedMsg implements Serializable {

    private String from;
    private String to;
    private String subject;
    private String body;
    private String filename;
    private String payload;
    private boolean mail_to;
    private boolean send_to_twitter;

    public NodeRedMsg() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isSend_to_twitter() {
        return send_to_twitter;
    }

    public void setSend_to_twitter(boolean send_to_twitter) {
        this.send_to_twitter = send_to_twitter;
    }

    public boolean isMail_to() {
        return mail_to;
    }

    public void setMail_to(boolean mail_to) {
        this.mail_to = mail_to;
    }
}
