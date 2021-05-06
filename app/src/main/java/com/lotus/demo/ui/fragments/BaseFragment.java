package com.lotus.demo.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    protected View view;

    protected abstract int getLayout();

    protected abstract void initViews();

    protected abstract void main();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(getLayout(), container, false);
        initViews();
        main();
        return view;
    }

    /**
     * Open an activity
     * @param intent - New activity
     */
    protected void openActivity(Intent intent){
        startActivity(intent);
    }
}
