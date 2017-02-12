package com.infinite.pullrefresh.extras;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.infinite.pullrefresh.R;
import com.infinite.pullrefresh.layout.BaseHeaderView;
import com.infinite.pullrefresh.layout.FlingLayout;
import com.infinite.pullrefresh.support.utils.AnimUtil;
import com.nineoldandroids.view.ViewHelper;


public class NormalHeaderView extends BaseHeaderView {
    private TextView textView;
    private View tagImg;
    private View progress;
    private View stateImg;


    public NormalHeaderView(Context context) {
        this(context, null);
    }

    public NormalHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NormalHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_header_normal, this, true);
        textView = (TextView) findViewById(R.id.text);
        tagImg = findViewById(R.id.tag);
        progress = findViewById(R.id.progress);
        stateImg = findViewById(R.id.state);
    }

    @Override
    protected void onStateChange(int state) {
        if (textView == null || tagImg == null || progress == null || stateImg == null) {
            return;
        }
        stateImg.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.VISIBLE);
        tagImg.setVisibility(View.VISIBLE);
        ViewHelper.setAlpha(textView, 1);
        ViewHelper.setAlpha(tagImg, 1);
        ViewHelper.setTranslationY(stateImg, 0);
        ViewHelper.setTranslationY(progress, 0);
        switch (state) {
            case NONE:
                break;
            case PULLING:
                textView.setText(R.string.stringPullLoadMore);
                AnimUtil.startRotation(tagImg, 0);
                break;
            case LOOSENT_O_REFRESH:
                textView.setText(R.string.stringReleaseLoadMore);
                AnimUtil.startRotation(tagImg, 180);
                break;
            case REFRESHING:
                textView.setText(R.string.stringLoading);
                AnimUtil.startShow(progress, 0.1f, 400, 200);
                AnimUtil.startHide(textView);
                AnimUtil.startHide(tagImg);
                break;
            case REFRESH_CLONE:
                AnimUtil.startFromY(stateImg, -2 * stateImg.getHeight());
                AnimUtil.startToY(progress, 2 * progress.getHeight());
                stateImg.setVisibility(View.VISIBLE);
                progress.setVisibility(View.VISIBLE);
                textView.setVisibility(View.INVISIBLE);
                tagImg.setVisibility(View.INVISIBLE);
                textView.setText(R.string.stringLoaded);
                break;

        }

    }

    @Override
    public float getSpanHeight() {
        return getHeight();
    }


    @Override
    public int getLayoutType() {
        return FlingLayout.LAYOUT_NORMAL;
    }
}
