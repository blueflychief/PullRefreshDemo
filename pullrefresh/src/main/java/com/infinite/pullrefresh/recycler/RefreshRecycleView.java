package com.infinite.pullrefresh.recycler;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.infinite.pullrefresh.R;

import java.lang.ref.WeakReference;

import static android.support.v7.widget.RecyclerView.LayoutManager;
import static com.infinite.pullrefresh.recycler.FooterStatusHandle.TYPE_LOADING_MORE;


/**
 * 可刷新和加载更多的RecycleView, 也可分页，使用时会填充整个父布局
 */
public class RefreshRecycleView extends SwipeRefreshLayout {

    private LoadMoreRecyclerView mRecycleView;
    private OnLoadMoreListener mOnLoadMoreListener;
    private OnRefreshListener mOnRefreshListener;
    private OnFooterClickListener mOnFooterClickListener;
    private boolean mCanLoadMore = true;  //是否能加载更多
    private boolean mLoadMoreEnable = true;  //是否需要加载更多功能
    private boolean mShowFooter = true;      //是否显示Footer
    private View mHeader, mFooter;             // 头部和脚部
    private FooterStatusHandle mFooterStatus;  //Footer状态
    private LayoutManager mLayoutManager;

    public RefreshRecycleView(Context context) {
        this(context, null);
    }

    public RefreshRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRecycleView = new LoadMoreRecyclerView(context, this);
        addView(mRecycleView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorPrimary));
        if (mLayoutManager == null) {
            mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            setLayoutManager(mLayoutManager);
        }
    }


    public LoadMoreRecyclerView getRecycleView() {
        return mRecycleView;
    }


    public void setAdapter(RefreshAdapter adapter) {
        mRecycleView.setAdapter(adapter);
    }

    public RefreshAdapter getAdapter() {
        return (RefreshAdapter) mRecycleView.getAdapter();
    }

    public void setLayoutManager(final LayoutManager manager) {
        mLayoutManager = manager;
        if (mLayoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) mLayoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getAdapter().isFooter(position) || getAdapter().isHeader(position) ?
                            ((GridLayoutManager) mLayoutManager).getSpanCount() : 1;
                }
            });
        }

        mRecycleView.setLayoutManager(mLayoutManager);
        initDefaultFooter();
    }


    public LayoutManager getLayoutManager() {
        return mRecycleView.getLayoutManager();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return !isRefreshing() && super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor) {
        mRecycleView.addItemDecoration(decor);
    }

    /**
     * 是否需要下拉刷新的功能
     *
     * @param refreshEnable
     */
    public void setRefreshEnable(boolean refreshEnable) {
        setEnabled(refreshEnable);
    }

    /**
     * 是否需要加载更多的功能
     *
     * @param loadMoreEnable
     */
    public void setLoadMoreEnable(boolean loadMoreEnable) {
        mLoadMoreEnable = loadMoreEnable;
        if (!loadMoreEnable) {
            mFooter = null;
        } else {
            if (mFooter == null) {
                initDefaultFooter();
            }
        }
    }

    public boolean isLoadMoreEnable() {
        return mLoadMoreEnable;
    }

    /**
     * 设置是否还可以加载更多
     *
     * @param canLoadMore
     */
    public void setCanLoadMore(boolean canLoadMore) {
        mCanLoadMore = canLoadMore;
    }

    /**
     * @return 是否还可以加载更多
     */
    public boolean getCanLoadMore() {
        return mCanLoadMore;
    }


    /**
     * footer的点击事件
     *
     * @param listener
     */
    public void setOnFooterClickListener(OnFooterClickListener listener) {
        mOnFooterClickListener = listener;
    }

    /**
     * 设置下拉刷新的监听
     *
     * @param listener
     */
    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
        super.setOnRefreshListener(mOnRefreshListener);
    }

    /**
     * 设置加载更多监听
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }


    /**
     * 自动刷新
     */
    public void autoRefresh() {
        autoRefresh(0);
    }

    /**
     * 自动刷新
     *
     * @param delay 延时
     */
    public void autoRefresh(int delay) {
        setFooterVisiable(GONE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mRecycleView.mIsLoadMore && !isRefreshing()) {
                    setRefreshing(true);
                    mOnRefreshListener.onRefresh();
                }
            }
        }, delay);
    }

    private void setFooterVisiable(int visiable) {
        if (mFooter != null) {
            mFooter.findViewById(R.id.ll_root).setVisibility(visiable);
        }
    }

    /**
     * 是否还有更多
     *
     * @param hasMore
     */
    public void setFooterHasMore(boolean hasMore) {
        setFooterStatus(hasMore ? FooterStatusHandle.TYPE_PULL_LOAD_MORE : FooterStatusHandle.TYPE_NO_MORE);
    }

    /**
     * 设置Footer显示状态
     */
    public void setFooterStatus(FooterStatusHandle type) {
        if (mRecycleView == null) {
            throw new NullPointerException("-----mRecycleView is null!!!");
        }
        mFooterStatus = type;
        switch (type) {
            case TYPE_PULL_LOAD_MORE:
                mRecycleView.mIsLoadMore = false;
                mCanLoadMore = true;
                break;
            case TYPE_LOADING_MORE:
                mRecycleView.mIsLoadMore = true;
                mCanLoadMore = true;
                break;
            case TYPE_ERROR:
                mRecycleView.mIsLoadMore = false;
                mCanLoadMore = true;
                break;
            case TYPE_NO_MORE:
                mRecycleView.mIsLoadMore = false;
                mCanLoadMore = false;
                break;
        }
        refreshFooter(type);
        this.setRefreshEnable(type != TYPE_LOADING_MORE);
        if (this.isRefreshing()) {
            this.setRefreshing(false);
        }
    }

    private void refreshFooter(FooterStatusHandle type) {
        if (mCanLoadMore) {
            if (getFooter() != null) {
                getFooter().findViewById(R.id.ll_root).setVisibility(VISIBLE);
                if (type == FooterStatusHandle.TYPE_PULL_LOAD_MORE) {
                    getFooter().findViewById(R.id.ll_more).setVisibility(GONE);
                    getFooter().findViewById(R.id.tv_no_more).setVisibility(GONE);
                    getFooter().findViewById(R.id.tv_pull_load_more).setVisibility(VISIBLE);
                    getFooter().findViewById(R.id.tv_error).setVisibility(GONE);
                    return;
                }

                if (type == TYPE_LOADING_MORE) {
                    getFooter().findViewById(R.id.ll_more).setVisibility(VISIBLE);
                    getFooter().findViewById(R.id.tv_no_more).setVisibility(GONE);
                    getFooter().findViewById(R.id.tv_pull_load_more).setVisibility(GONE);
                    getFooter().findViewById(R.id.tv_error).setVisibility(GONE);
                    return;
                }

                if (type == FooterStatusHandle.TYPE_ERROR) {
                    getFooter().findViewById(R.id.ll_more).setVisibility(GONE);
                    getFooter().findViewById(R.id.tv_no_more).setVisibility(GONE);
                    getFooter().findViewById(R.id.tv_pull_load_more).setVisibility(GONE);
                    getFooter().findViewById(R.id.tv_error).setVisibility(VISIBLE);
                }
            }
        } else {
            if (getFooter() != null) {
                getFooter().findViewById(R.id.ll_root).setVisibility(VISIBLE);
                getFooter().findViewById(R.id.ll_more).setVisibility(GONE);
                getFooter().findViewById(R.id.tv_pull_load_more).setVisibility(GONE);
                getFooter().findViewById(R.id.tv_error).setVisibility(GONE);
                if (getShowFooterWithNoMore()) {
                    getFooter().findViewById(R.id.tv_no_more).setVisibility(VISIBLE);
                } else {
                    getFooter().findViewById(R.id.tv_no_more).setVisibility(GONE);
                }
            }
        }
    }


    /**
     * 添加头部
     *
     * @param header 作为头部的布局
     */
    public void setHeader(View header) {
        this.mHeader = header;
    }

    /**
     * 添加头部
     */
    public void setHeader(int resId) {
        this.mHeader = LayoutInflater.from(getContext()).inflate(resId, mRecycleView, false);
    }

    public View getHeader() {
        return mHeader;
    }

    /**
     * 删除头部
     */
    public void removeHeader() {
        this.mHeader = null;
    }

    /**
     * 添加脚部
     */
    public void setFooter(View footer) {
        this.mFooter = footer;
    }


    /**
     * 设置mHeader是否可见
     * @param visible
     */
    public void setHeaderVisible(boolean visible) {
        if (mHeader != null) {
            int oldVisible = mHeader.getVisibility();
            int newVisible = visible ? VISIBLE : GONE;
            if (oldVisible != newVisible) {
                mHeader.setVisibility(newVisible);
                getAdapter().notifyDataSetChanged();
            }
        }
    }

    /**
     * mFooter
     * @param visible
     */
    public void setFooterVisible(boolean visible) {
        if (mFooter != null) {
            int oldVisible = mFooter.getVisibility();
            int newVisible = visible ? VISIBLE : GONE;
            if (oldVisible != newVisible) {
                mFooter.setVisibility(newVisible);
                getAdapter().notifyDataSetChanged();
            }
        }
    }

    /**
     * 添加脚部
     */
    public void setFooter(int resId) {
        this.mFooter = LayoutInflater.from(getContext()).inflate(resId, mRecycleView, false);
        mFooter.findViewById(R.id.tv_error).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnFooterClickListener != null) {
                    setFooterStatus(TYPE_LOADING_MORE);
                    mOnFooterClickListener.onErrorClick();
                }
            }
        });

        mFooter.findViewById(R.id.tv_pull_load_more).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnFooterClickListener != null) {
                    setFooterStatus(TYPE_LOADING_MORE);
                    mOnFooterClickListener.onLoadMoreClick();
                }
            }
        });
    }

    /**
     * 初始化默认的footer
     */
    private void initDefaultFooter() {
        setFooter(R.layout.view_recycler_load_more);
    }

    /**
     * @return 脚部视图
     */
    public View getFooter() {
        return mFooter;
    }


//    //没有更多时是否显示Footer
//    public void setShowFooterWithNoMore(boolean show) {
//        mShowFooter = show;
//    }

    public boolean getShowFooterWithNoMore() {
        return mShowFooter;
    }

    /**
     * @return 获取最后一个可见视图的位置
     */
    public int findLastVisibleItemPosition() {
        LayoutManager manager = mRecycleView.getLayoutManager();
        // 获取最后一个正在显示的View
        if (manager instanceof GridLayoutManager) {
            return ((GridLayoutManager) manager).findLastVisibleItemPosition();
        } else if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) manager).getSpanCount()];
            ((StaggeredGridLayoutManager) manager).findLastVisibleItemPositions(into);
            return findMax(into);
        }
        return -1;
    }

    /**
     * 设置子视图充满一行
     *
     * @param view 子视图
     */
    public void setFullSpan(View view) {
        LayoutManager manager = getLayoutManager();
        // 根据布局设置参数, 使"加载更多"的布局充满一行
        if (manager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams params = new StaggeredGridLayoutManager.LayoutParams(
                    StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT, StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT);
            params.setFullSpan(true);
            view.setLayoutParams(params);
        }
    }

    /**
     * 可分页加载更多的RecyclerView
     */
    public class LoadMoreRecyclerView extends RecyclerView {
        private boolean mIsLoadMore = false;  //是否正在加载更多
        private WeakReference<RefreshRecycleView> mRefreshRecycleView;

        public LoadMoreRecyclerView(Context context, RefreshRecycleView view) {
            super(context);
            mRefreshRecycleView = new WeakReference<>(view);
            post(new Runnable() {
                @Override
                public void run() {
                    if (getBottom() != 0 && getChildAt(findLastVisibleItemPosition()) != null && getBottom() >= getChildAt(findLastVisibleItemPosition()).getBottom()) {
                        // 最后一条正在显示的子视图在RecyclerView的上面, 说明子视图未充满RecyclerView
                        getAdapter().notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onScrollStateChanged(int state) {
            super.onScrollStateChanged(state);
            RefreshRecycleView view = mRefreshRecycleView.get();
            if (view != null) {
                if (state == SCROLL_STATE_IDLE && // 停止滚动
                        view.mCanLoadMore
                        && view.mOnLoadMoreListener != null  // 可以加载更多, 且有加载监听
                        && mIsUp
                        && !mIsLoadMore
                        && view.findLastVisibleItemPosition() == view.getLayoutManager().getItemCount() - 1) { // 滚动到了最后一个子视图
                    if (view.mFooterStatus != FooterStatusHandle.TYPE_ERROR) {
                        if (view.isRefreshing()) {
                            return;
                        }
                        view.setFooterStatus(TYPE_LOADING_MORE);
                        view.mOnLoadMoreListener.onLoadMore(); // 执行加载更多
                    }
                }
            }
        }


        private float mOldY = 0.0f;
        private float mNewY = 0.0f;
        private boolean mIsUp = false;

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mOldY = e.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mNewY = e.getY();
                    //下拉
                    if (mNewY - mOldY >= ViewConfiguration.get(getContext()).getScaledTouchSlop() * 1.0f) {
                        mIsUp = false;
                    }
                    //上拉
                    else if (mOldY - mNewY >= ViewConfiguration.get(getContext()).getScaledTouchSlop() * 1.0f) {
                        mIsUp = true;
                    }
                    mOldY = mNewY;
                    break;

                case MotionEvent.ACTION_UP:
                    mOldY = 0.0f;
                    mNewY = 0.0f;
                    break;
            }
            return super.onTouchEvent(e);
        }
    }


    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
