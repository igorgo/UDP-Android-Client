package ua.parus.pmo.parus8claims.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by igor-go on 20.04.2015.
 * ua.parus.pmo.parus8claims.gui
 */
public class AutoScrollListView extends ListView implements AutoScrollListPageListener {

    public static final String TAG = AutoScrollListView.class.getSimpleName();
    private View mLoadingView;
    private LoadingMode mLoadingMode = LoadingMode.SCROLL_TO_BOTTOM;
    private StopPosition mStopPosition = StopPosition.REMAIN_UNCHANGED;
    private boolean mLoadingViewVisible = false;

    public AutoScrollListView(Context context) {
        super(context);
    }

    public AutoScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLoadingView(View loadingView) {
        this.mLoadingView = loadingView;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setLoadingMode(LoadingMode loadingMode) {
        this.mLoadingMode = loadingMode;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setStopPosition(StopPosition stopPosition) {
        this.mStopPosition = stopPosition;
    }

    private void addLoadingView(ListView listView, View loadingView) {
        if (listView == null || loadingView == null) {
            return;
        }
        if (!mLoadingViewVisible) {
            if (mLoadingMode == LoadingMode.SCROLL_TO_BOTTOM) {
                listView.addFooterView(loadingView);
            } else {
                listView.addHeaderView(loadingView);
            }
            mLoadingViewVisible = true;
        }
    }

    private void removeLoadingView(ListView listView, View loadingView) {
        if (listView == null || loadingView == null) {
            return;
        }
        if (mLoadingViewVisible) {
            if (mLoadingMode == LoadingMode.SCROLL_TO_BOTTOM) {
                listView.removeFooterView(loadingView);
            } else {
                listView.removeHeaderView(loadingView);
            }
            mLoadingViewVisible = false;
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof AutoScrollListAdapter)) {
            throw new IllegalArgumentException(AutoScrollListAdapter.class.getSimpleName() + " expected");
        }
        AutoScrollListAdapter autoScrollListAdapter = (AutoScrollListAdapter) adapter;
        autoScrollListAdapter.setLoadingMode(mLoadingMode);
        autoScrollListAdapter.setStopPosition(mStopPosition);
        autoScrollListAdapter.setAutoScrollListPageListener(this);
        this.setOnScrollListener(autoScrollListAdapter);
        View dummy = new View(getContext());
        addLoadingView(AutoScrollListView.this, dummy);
        super.setAdapter(adapter);
        removeLoadingView(AutoScrollListView.this, dummy);
    }

    @Override
    public void onListEnd() {
        removeLoadingView(this, mLoadingView);
    }

    @Override
    public void onHasMore() {
        addLoadingView(AutoScrollListView.this, mLoadingView);
    }

    public static enum LoadingMode {SCROLL_TO_TOP, SCROLL_TO_BOTTOM}

    public static enum StopPosition {START_OF_LIST, END_OF_LIST, REMAIN_UNCHANGED}

}
