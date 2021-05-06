package com.lotus.demo.ui.activities;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lotus.demo.R;
import com.lotus.demo.constant.SocketEvents;
import com.lotus.demo.data.AppData;
import com.lotus.demo.ui.fragments.BaseFragment;
import com.lotus.demo.ui.fragments.ConnectFragment;
import com.lotus.demo.ui.fragments.ProfileFragment;
import com.lotus.demo.ui.fragments.SettingFragment;
import com.lotus.demo.util.event.CallMakeEvent;
import com.lotus.demo.util.socket.SocketUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigation;

    private ActionBar toolbar;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        toolbar = getSupportActionBar();
    }

    @Override
    protected void main() {
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        openFragment(new ProfileFragment());
        toolbar.setTitle(getResources().getString(R.string.profile_fragment_title));

        // Connect to signaling server
        SocketUtils.connectToSignallingServer();
    }

    public void openFragment(BaseFragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_profile:
                            toolbar.setTitle(getResources().getString(R.string.profile_fragment_title));
                            openFragment(new ProfileFragment());
                            return true;
                        case R.id.navigation_connect:
                            toolbar.setTitle(getResources().getString(R.string.connect_fragment_title));
                            openFragment(new ConnectFragment());
                            return true;
                        case R.id.navigation_setting:
                            toolbar.setTitle(getResources().getString(R.string.setting_fragment_title));
                            openFragment(new SettingFragment());
                            return true;
                    }
                    return false;
                }
            };

    @Override
    protected void onDestroy() {
        SocketUtils.disconnect();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onCallMake(CallMakeEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Call make");

                // When user is calling with another, reject call
                if (AppData.isCalling){
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("to", event.getFrom());
                        obj.put("reason", "On another call!");
                    } catch (JSONException e) {
                        obj = new JSONObject();
                    }
                    SocketUtils.emit(SocketEvents.CALL_REJECT, obj);
                    return;
                }

                // Open InCallActivity
                Intent intent = new Intent(MainActivity.this, InCallActivity.class);
                intent.putExtra("from", event.getFrom());
                openActivity(intent, false);
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