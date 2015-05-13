package ua.parus.pmo.parus8claims.gui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

/**
 * Created by igor-go on 20.04.2015.
 * ua.parus.pmo.parus8claims.gui
 */
public abstract class AutoScrollListAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    protected static final String TAG = AutoScrollListAdapter.class.getSimpleName();

    // Блокировка для предотвращения другого события скроллинга
    private boolean mCanScroll = false;
    // Флаг enable/disable клик на строке
    private boolean mRowEnabled = true;
    private AutoScrollListView.LoadingMode mLoadingMode;
    private AutoScrollListView.StopPosition mStopPosition;
    private AutoScrollListPageListener mAutoScrollListPageListener;

    protected void lock() {
        mCanScroll = false;
    }

    void unlock() {
        mCanScroll = true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRowEnabled(boolean rowEnabled) {
        this.mRowEnabled = rowEnabled;
    }

    public void setLoadingMode(AutoScrollListView.LoadingMode loadingMode) {
        this.mLoadingMode = loadingMode;
    }

    @SuppressWarnings("UnusedDeclaration")
    public AutoScrollListView.StopPosition getStopPosition() {
        return mStopPosition;
    }

    public void setStopPosition(AutoScrollListView.StopPosition stopPosition) {
        this.mStopPosition = stopPosition;
    }

    public void setAutoScrollListPageListener(AutoScrollListPageListener autoScrollListPageListener) {
        this.mAutoScrollListPageListener = autoScrollListPageListener;
    }

    protected abstract void onScrollNext();

    protected abstract View getAutoScrollListView(int position, View convertView, ViewGroup parent);

    @Override
    public boolean isEnabled(int position) {
        return mRowEnabled;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view instanceof AutoScrollListView) {
            if (mLoadingMode == AutoScrollListView.LoadingMode.SCROLL_TO_TOP && firstVisibleItem == 0 && mCanScroll) {
                onScrollNext();
            }
            if (mLoadingMode == AutoScrollListView.LoadingMode.SCROLL_TO_BOTTOM && firstVisibleItem + visibleItemCount - 1 == getCount() && mCanScroll) {
                onScrollNext();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // глушим
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getAutoScrollListView(position, convertView, parent);
    }

    protected void notifyEndOfList() {
        lock();
        if (mAutoScrollListPageListener != null) {
            mAutoScrollListPageListener.onListEnd();
        }
    }

    protected void notifyHasMore() {
        unlock();
        if (mAutoScrollListPageListener != null) {
            mAutoScrollListPageListener.onHasMore();
        }
    }

}
