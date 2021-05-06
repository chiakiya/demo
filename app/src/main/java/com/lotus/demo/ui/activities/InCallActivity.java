package com.lotus.demo.ui.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lotus.demo.R;
import com.lotus.demo.constant.SocketEvents;
import com.lotus.demo.util.event.CallCancelEvent;
import com.lotus.demo.util.socket.SocketUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

public class InCallActivity extends BaseActivity {

    private static final String TAG = "InCallActivity";

    private TextView txtFrom;
    private MediaPlayer mMediaPlayer;
    private FloatingActionButton btnReject;
    private FloatingActionButton btnAccept;
    private String from;
    private Boolean isPartnerCanceled = false;
    private Boolean isAccepted = false;

    @Override
    protected int getLayout() {
        return R.layout.activity_in_call;
    }

    @Override
    protected void initViews() {
        Intent intent = getIntent();
        from = intent.getStringExtra("from");
        txtFrom = (TextView) findViewById(R.id.txtFrom);
        btnReject = (FloatingActionButton) findViewById(R.id.btnReject);
        btnAccept = (FloatingActionButton) findViewById(R.id.btnAccept);
        txtFrom.setText(from);
//        FloatingActionButton fab = findViewById(R.id.fab);
    }

    @Override
    protected void main() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.callsound);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectCall();
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answerCall();
            }
        });
    }

    private void  rejectCall(){
        finish();
    }

    private void answerCall(){
        Intent intent = new Intent(InCallActivity.this, CallActivity.class);
        intent.putExtra("from", from);
        isAccepted = true;
        openActivity(intent, true);
    }

    @Override
    protected void onDestroy() {
        if (!isPartnerCanceled && !isAccepted){
            JSONObject obj = new JSONObject();
            try {
                obj.put("to", from);
            } catch (JSONException e) {
                obj = new JSONObject();
            }
            SocketUtils.emit(SocketEvents.CALL_REJECT, obj);
        }

        EventBus.getDefault().unregister(this);
        mMediaPlayer.stop();
        super.onDestroy();
    }

    @Subscribe
    public void onCallCancel(CallCancelEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Call cancel");
                isPartnerCanceled = true;
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }
}