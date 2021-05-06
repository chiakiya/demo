package com.lotus.demo.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author LienBT
 * Create on 09/08/2020
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getLayout();

    protected abstract void initViews();

    protected abstract void main();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        initViews();
        main();
    }

    /**
     * Open an activity
     * @param intent - New activity
     * @param isFinish - Close current activity
     */
    protected void openActivity(Intent intent, boolean isFinish){
        startActivity(intent);
        if (isFinish){
            finish();
        }
    }

}
