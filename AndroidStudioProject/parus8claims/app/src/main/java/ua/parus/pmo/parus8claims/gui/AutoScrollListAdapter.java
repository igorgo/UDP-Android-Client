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
    private boolean canScroll = false;
    // Флаг enable/disable клик на строке
    private boolean rowEnabled = true;
    private AutoScrollListView.LoadingMode loadingMode;
    private AutoScrollListView.StopPosition stopPosition;
    private AutoScrollListPageListener autoScrollListPageListener;

    protected void lock() {
        this.canScroll = false;
    }

    void unlock() {
        this.canScroll = true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRowEnabled(boolean rowEnabled) {
        this.rowEnabled = rowEnabled;
    }

    public void setLoadingMode(AutoScrollListView.LoadingMode loadingMode) {
        this.loadingMode = loadingMode;
    }

    @SuppressWarnings("UnusedDeclaration")
    public AutoScrollListView.StopPosition getStopPosition() {
        return this.stopPosition;
    }

    public void setStopPosition(AutoScrollListView.StopPosition stopPosition) {
        this.stopPosition = stopPosition;
    }

    public void setAutoScrollListPageListener(AutoScrollListPageListener autoScrollListPageListener) {
        this.autoScrollListPageListener = autoScrollListPageListener;
    }

    protected abstract void onScrollNext();

    protected abstract View getAutoScrollListView(int position, View convertView, ViewGroup parent);

    @Override
    public boolean isEnabled(int position) {
        return this.rowEnabled;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view instanceof AutoScrollListView) {
            if (this.loadingMode == AutoScrollListView.LoadingMode.SCROLL_TO_TOP && firstVisibleItem == 0 && this.canScroll) {
                onScrollNext();
            }
            if (this.loadingMode == AutoScrollListView.LoadingMode.SCROLL_TO_BOTTOM && firstVisibleItem + visibleItemCount - 1 == getCount() && this.canScroll) {
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
        if (this.autoScrollListPageListener != null) {
            this.autoScrollListPageListener.onListEnd();
        }
    }

    protected void notifyHasMore() {
        unlock();
        if (this.autoScrollListPageListener != null) {
            this.autoScrollListPageListener.onHasMore();
        }
    }

    protected void notifyEmptyList(boolean empty) {
        if (this.autoScrollListPageListener != null) {
            this.autoScrollListPageListener.onEmptyList(empty);
        }
    }

}
