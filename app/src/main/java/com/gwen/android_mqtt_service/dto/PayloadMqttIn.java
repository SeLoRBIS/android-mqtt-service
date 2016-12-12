package com.gwen.android_mqtt_service.dto;

import java.io.Serializable;

public class PayloadMqttIn implements Serializable {

    private Long timestamp;
    private String nom;
    private String prenom;
    private String email;
    private boolean mail_to;
    private boolean send_to_twitter;
    private boolean is_selfie;
    private boolean is_preview;

    public PayloadMqttIn() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean is_selfie() {
        return is_selfie;
    }

    public void setIs_selfie(boolean is_selfie) {
        this.is_selfie = is_selfie;
    }

    public boolean is_preview() {
        return is_preview;
    }

    public void setIs_preview(boolean is_preview) {
        this.is_preview = is_preview;
    }
}
