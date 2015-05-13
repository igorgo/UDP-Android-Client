package ua.parus.pmo.parus8claims.gui;

/**
 * Created by igor-go on 20.04.2015.
 * ua.parus.pmo.parus8claims.gui
 */
public interface AutoScrollListPageListener {
    public abstract void onListEnd();

    public abstract void onHasMore();

    public abstract void onEmptyList(boolean empty);
}
