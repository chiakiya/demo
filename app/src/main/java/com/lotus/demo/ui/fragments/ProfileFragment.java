package com.lotus.demo.ui.fragments;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lotus.demo.R;
import com.lotus.demo.data.AppData;
import com.lotus.demo.util.event.OfflineEvent;
import com.lotus.demo.util.event.OnlineEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class ProfileFragment extends BaseFragment {

    private TextView txtOffline;
    private TextView txtUserId;
    private Button btnShareId;

    @Override
    protected int getLayout() {
        return R.layout.fragment_profile;
    }


    @Override
    protected void initViews() {
        txtOffline = (TextView) view.findViewById(R.id.txtOffline);
        txtUserId = (TextView) view.findViewById(R.id.txtUserId);
        btnShareId = (Button) view.findViewById(R.id.btnShareId);
    }

    @Override
    protected void main() {
        if (AppData.isOnline) {
            txtOffline.setVisibility(View.INVISIBLE);
            txtUserId.setText(AppData.currentUser.getClientID());
        } else {
            btnShareId.setEnabled(false);
        }

        btnShareId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share connect id");
                shareIntent.putExtra(Intent.EXTRA_TEXT, txtUserId.getText());
                shareIntent.setType("text/plain");
                startActivity(shareIntent);
            }
        });
    }

    @Subscribe
    public void onOnline(OnlineEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtOffline.setVisibility(View.INVISIBLE);
                txtUserId.setText(AppData.currentUser.getClientID());
                btnShareId.setEnabled(true);
            }
        });
    }

    @Subscribe
    public void onOffline(OfflineEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtOffline.setVisibility(View.VISIBLE);
                txtUserId.setText("-");
                btnShareId.setEnabled(false);
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}