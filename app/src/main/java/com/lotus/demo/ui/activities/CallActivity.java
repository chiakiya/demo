package com.lotus.demo.ui.activities;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lotus.demo.R;
import com.lotus.demo.constant.SocketEvents;
import com.lotus.demo.data.AppData;
import com.lotus.demo.util.common.SimpleSdpObserver;
import com.lotus.demo.util.event.CallAnswerEvent;
import com.lotus.demo.util.event.CallDisconnectEvent;
import com.lotus.demo.util.event.CallFinishEvent;
import com.lotus.demo.util.event.CallIceCandidateEvent;
import com.lotus.demo.util.event.CallOfferEvent;
import com.lotus.demo.util.socket.SocketUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;

public class CallActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final int VIDEO_RESOLUTION_WIDTH = 1280;
    public static final int VIDEO_RESOLUTION_HEIGHT = 1280;
    public static final int FPS = 30;
    private static final int RC_CALL = 111;
    private static final String TAG = "CallActivity";

//    private SurfaceViewRenderer surfaceViewParter;
    private SurfaceViewRenderer surfaceViewMe;
    private FloatingActionButton btnEndCall;

    private EglBase rootEglBase;
    private PeerConnection peerConnection;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;
    private AudioSource audioSource;
    private VideoSource videoSource;
    private AudioTrack localAudioTrack;
    private VideoCapturer videoCapturer;

    private MediaConstraints audioConstraints;
    private String from;

    private MediaStream mediaStream;

    private Boolean isDisconnect = false;

    private AudioManager audioManager;


    @Override
    protected int getLayout() {
        return R.layout.activity_call;
    }

    @Override
    protected void initViews() {
        Intent intent = getIntent();
        from = intent.getStringExtra("from");
//        surfaceViewParter = findViewById(R.id.surfaceViewParter);
        surfaceViewMe = findViewById(R.id.surfaceViewMe);
        btnEndCall = (FloatingActionButton) findViewById(R.id.btnEndCall);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CALL)
    @Override
    protected void main() {
        // Require permissions
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            startCall();
        } else {
            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }


        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    // Finish call
    private void finishCall() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("to", from);
        } catch (JSONException e) {
            obj = new JSONObject();
        }
        SocketUtils.emit(SocketEvents.CALL_FINISH, obj);
    }

    private void startStreamingVideo() {
        mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);

        acceptCall();

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }


    private void startCall() {
        initializeSurfaceViews();
        initializePeerConnectionFactory();
        createVideoTrackFromCameraAndShowIt();
        initializePeerConnections();
        startStreamingVideo();
    }

    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();
//        surfaceViewParter.init(rootEglBase.getEglBaseContext(), null);
//        surfaceViewParter.setEnableHardwareScaler(true);
//        surfaceViewParter.setMirror(true);

        surfaceViewMe.init(rootEglBase.getEglBaseContext(), null);
        surfaceViewMe.setEnableHardwareScaler(true);
        surfaceViewMe.setMirror(true);
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    private void createVideoTrackFromCameraAndShowIt() {
        audioConstraints = new MediaConstraints();
        videoCapturer = createVideoCapturer();
        videoSource = factory.createVideoSource(videoCapturer);
        videoSource.adaptOutputFormat(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrackFromCamera.setEnabled(true);
        videoTrackFromCamera.addRenderer(new VideoRenderer(surfaceViewMe));

        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private void initializePeerConnections() {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
                if(iceConnectionState.equals(PeerConnection.IceConnectionState.DISCONNECTED)) {
                    finish();
                }
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");

                JSONObject obj = new JSONObject();
                try {
                    obj.put("to", from);
                    JSONObject candidate = new JSONObject();
                    candidate.put("sdpMid", iceCandidate.sdpMid);
                    candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                    candidate.put("sdp", iceCandidate.sdp);
                    obj.put("candidate", candidate);
                } catch (JSONException e) {
                    obj = new JSONObject();
                }
                SocketUtils.emit(SocketEvents.CALL_ICE_CANDIDATE, obj);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
//                VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(true);
//                remoteVideoTrack.setEnabled(true);
//                remoteVideoTrack.addRenderer(new VideoRenderer(surfaceViewParter));
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        peerConnection = factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);

    }

    private void acceptCall() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("to", from);
        } catch (JSONException e) {
            obj = new JSONObject();
        }
        SocketUtils.emit(SocketEvents.CALL_ACCEPT, obj);
        AppData.isCalling = true;
    }


    private void doCall() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                try {
                    JSONObject obj = new JSONObject();
                    JSONObject offer = new JSONObject();
                    offer.put("type", "offer");
                    offer.put("sdp", sessionDescription.description);
                    obj.put("to", from);
                    obj.put("offer", offer);
                    SocketUtils.emit(SocketEvents.CALL_OFFER, obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
    }

    @Subscribe
    public void onCallDisconnect(CallDisconnectEvent event) {
        Log.d(TAG, "Call disconnect");
        isDisconnect = true;
        finish();
    }

    @Subscribe
    public void onCallOffer(CallOfferEvent event) {
        Log.d(TAG, "Call offer");
        SessionDescription sessionDescription = new SessionDescription(OFFER, event.getSdp());
        peerConnection.setRemoteDescription(new SimpleSdpObserver(), sessionDescription);

        Log.d(TAG, "create answer");
        MediaConstraints mediaConstraints = new MediaConstraints();
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "create answer success");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                try {
                    JSONObject obj = new JSONObject();
                    JSONObject offer = new JSONObject();
                    offer.put("type", "answer");
                    offer.put("sdp", sessionDescription.description);
                    obj.put("to", from);
                    obj.put("answer", offer);
                    SocketUtils.emit(SocketEvents.CALL_ANSWER, obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCreateFailure(String s) {
                Log.d(TAG, "create answer fail: " + s);
                super.onCreateFailure(s);
            }
        }, mediaConstraints);
    }

    @Subscribe
    public void onCallAnswer(CallAnswerEvent event) {
        Log.d(TAG, "Call answer");
        peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, event.getSdp()));
        doCall();
    }

    @Subscribe
    public void onCallFinish(CallFinishEvent event) {
        Log.d(TAG, "Call finish");
        isDisconnect = true;
        finish();
    }

    @Subscribe
    public void onCallIceCandidate(CallIceCandidateEvent event) {
        Log.d(TAG, "Call Ice Candidate" + event);
        peerConnection.addIceCandidate(event.getIceCandidate());
    }

    @Override
    protected void onDestroy() {
        try {
            AppData.isCalling = false;

            if (!isDisconnect){
                finishCall();
            }

            if (videoCapturer != null) {
                videoCapturer.stopCapture();
                videoCapturer.dispose();
            }

            if (peerConnection != null) {
                peerConnection.dispose();
            }
            if (factory != null) {
                factory.dispose();
            }

            if (surfaceViewMe != null) {
                surfaceViewMe.release();
            }
//            if (surfaceViewParter != null) {
//                surfaceViewParter.release();
//            }

        } catch (Exception e) {
            Log.d(TAG, "ERROR DESTROY");
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "Permission denied");
        finish();
    }
}
