package ua.parus.pmo.parus8claims.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AutoScrollListView extends ListView implements AutoScrollListPageListener {

    public static final String TAG = AutoScrollListView.class.getSimpleName();
    private View loadingView;
    private LoadingMode loadingMode = LoadingMode.SCROLL_TO_BOTTOM;
    private StopPosition stopPosition = StopPosition.REMAIN_UNCHANGED;
    private boolean loadingViewVisible = false;

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
        this.loadingView = loadingView;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setLoadingMode(LoadingMode loadingMode) {
        this.loadingMode = loadingMode;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setStopPosition(StopPosition stopPosition) {
        this.stopPosition = stopPosition;
    }

    private void addLoadingView(ListView listView, View loadingView) {
        if (listView == null || loadingView == null) {
            return;
        }
        if (!this.loadingViewVisible) {
            if (this.loadingMode == LoadingMode.SCROLL_TO_BOTTOM) {
                listView.addFooterView(loadingView);
            } else {
                listView.addHeaderView(loadingView);
            }
            this.loadingViewVisible = true;
        }
    }

    private void removeLoadingView(ListView listView, View loadingView) {
        if (listView == null || loadingView == null) {
            return;
        }
        if (this.loadingViewVisible) {
            if (this.loadingMode == LoadingMode.SCROLL_TO_BOTTOM) {
                listView.removeFooterView(loadingView);
            } else {
                listView.removeHeaderView(loadingView);
            }
            this.loadingViewVisible = false;
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof AutoScrollListAdapter)) {
            throw new IllegalArgumentException(AutoScrollListAdapter.class.getSimpleName() + " expected");
        }
        AutoScrollListAdapter autoScrollListAdapter = (AutoScrollListAdapter) adapter;
        autoScrollListAdapter.setLoadingMode(this.loadingMode);
        autoScrollListAdapter.setStopPosition(this.stopPosition);
        autoScrollListAdapter.setAutoScrollListPageListener(this);
        this.setOnScrollListener(autoScrollListAdapter);
        View dummy = new View(getContext());
        addLoadingView(AutoScrollListView.this, dummy);
        super.setAdapter(adapter);
        removeLoadingView(AutoScrollListView.this, dummy);
    }

    @Override
    public void onListEnd() {
        removeLoadingView(this, this.loadingView);
    }

    @Override
    public void onHasMore() {
        addLoadingView(AutoScrollListView.this, this.loadingView);
    }

    @Override
    public void onEmptyList(boolean empty) {

    }

    public enum LoadingMode {SCROLL_TO_TOP, SCROLL_TO_BOTTOM}

    public enum StopPosition {START_OF_LIST, END_OF_LIST, REMAIN_UNCHANGED}

}
