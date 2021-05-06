package com.lotus.demo.ui.fragments;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lotus.demo.R;
import com.lotus.demo.data.AppData;
import com.lotus.demo.ui.activities.CallActivity;
import com.lotus.demo.ui.activities.InCallActivity;
import com.lotus.demo.util.event.OfflineEvent;
import com.lotus.demo.util.event.OnlineEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static io.socket.client.Socket.EVENT_CONNECT;


public class ConnectFragment  extends BaseFragment {

    @Override
    protected int getLayout() {
        return R.layout.fragment_connect;
    }

    private Button btnConnect;
    private TextView txtOffline;


    @Override
    protected void initViews() {
        btnConnect = (Button) view.findViewById(R.id.btnConnect);
        txtOffline = (TextView) view.findViewById(R.id.txtOffline);
    }

    @Override
    protected void main() {

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        if (AppData.isOnline){
            txtOffline.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe
    public void onOnlineEvent(OnlineEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtOffline.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Subscribe
    public void onOfflineEvent(OfflineEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtOffline.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}