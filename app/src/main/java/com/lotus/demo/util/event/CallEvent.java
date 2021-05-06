package com.lotus.demo.util.event;

import org.json.JSONObject;

public class CallEvent {
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public CallEvent(String from) {
        this.from = from;
    }

}
