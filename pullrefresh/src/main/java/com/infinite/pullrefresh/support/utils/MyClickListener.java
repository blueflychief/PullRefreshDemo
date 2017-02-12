package com.infinite.pullrefresh.support.utils;

import android.view.View;

import java.util.Calendar;

/**
 * 防止快速点击
 * Created by Administrator on 9/15/2016.
 */
public abstract class MyClickListener implements View.OnClickListener {
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onMyClick(v);
        }
    }

    protected abstract void onMyClick(View v);
}
