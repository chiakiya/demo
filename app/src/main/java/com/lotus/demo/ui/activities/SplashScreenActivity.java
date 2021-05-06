package com.lotus.demo.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import com.lotus.demo.R;

public class SplashScreenActivity extends BaseActivity {

    private ProgressBar myProgessBar;
    private int mProgressStatus = 0;
    private final int timeSleep = 50;
    private Handler handler = new Handler();

    @Override
    protected int getLayout() {
        return R.layout.activity_splash_screen;
    }

    @Override
    protected void initViews() {
        myProgessBar = (ProgressBar) findViewById(R.id.progressBar1);
    }

    @Override
    protected void main() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus < 100) {
                    mProgressStatus += 10;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myProgessBar.setProgress(mProgressStatus);
                        }
                    });
                    try {
                        Thread.sleep(timeSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                openActivity(new Intent(getApplicationContext(),MainActivity.class),true);
            }
        }).start();
    }

}