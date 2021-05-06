package com.lotus.demo.util.event;

import org.webrtc.IceCandidate;

public class CallIceCandidateEvent extends CallEvent {
    private IceCandidate iceCandidate;

    public IceCandidate getIceCandidate() {
        return iceCandidate;
    }

    public void setIceCandidate(IceCandidate iceCandidate) {
        this.iceCandidate = iceCandidate;
    }

    public CallIceCandidateEvent(String from, IceCandidate iceCandidate) {
        super(from);
        this.iceCandidate = iceCandidate;
    }
}
