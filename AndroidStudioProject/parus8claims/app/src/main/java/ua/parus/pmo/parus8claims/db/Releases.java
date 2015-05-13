package ua.parus.pmo.parus8claims.db;

import java.io.Serializable;

/**
 * Created by igor-go on 14.04.2015.
 */
public class Releases implements Serializable {
    private long _rn;
    private String _vesion;
    private String _release;
    private int _builds_cached;

    public long get_rn() {
        return _rn;
    }

    public void set_rn(long _rn) {
        this._rn = _rn;
    }

    public String get_vesion() {
        return _vesion;
    }

    public void set_vesion(String _vesion) {
        this._vesion = _vesion;
    }

    public String get_release() {
        return _release;
    }

    public void set_release(String _release) {
        this._release = _release;
    }

    public int get_builds_cached() {
        return _builds_cached;
    }

    public void set_builds_cached(int _builds_cached) {
        this._builds_cached = _builds_cached;
    }
}
