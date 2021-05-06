package com.lotus.demo.model;

import com.google.gson.annotations.SerializedName;

public class Client {
    @SerializedName("clientID")
    private String clientID;

    @SerializedName("socketID")
    private String socketID;


    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getSocketID() {
        return socketID;
    }

    public void setSocketID(String socketID) {
        this.socketID = socketID;
    }
}
