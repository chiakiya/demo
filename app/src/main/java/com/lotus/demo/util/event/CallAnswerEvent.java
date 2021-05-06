package com.lotus.demo.util.event;

public class CallAnswerEvent extends CallEvent {
    private String sdp;

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }

    public CallAnswerEvent(String from, String sdp) {
        super(from);
        this.sdp = sdp;
    }

}
