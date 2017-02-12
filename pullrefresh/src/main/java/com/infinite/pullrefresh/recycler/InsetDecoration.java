package com.infinite.pullrefresh.recycler;

/**
 * Created by Administrator on 10/19/2016.
 */

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.infinite.pullrefresh.R;


/**
 * 为每个子项提供页边距
 */
public class InsetDecoration extends RecyclerView.ItemDecoration {

    private int mInsetMargin;

    public InsetDecoration(Context context) {
        super();
        mInsetMargin = context.getResources().getDimensionPixelOffset(R.dimen.dp2);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // 对子视图的所有 4 个边界应用计算得出的页边距
        outRect.set(mInsetMargin, mInsetMargin, mInsetMargin, mInsetMargin);
    }
}