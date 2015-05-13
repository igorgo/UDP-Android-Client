package ua.parus.pmo.parus8claims.db;

import java.io.Serializable;

/**
 * Created by igor-go on 15.04.2015.
 * ua.parus.pmo.parus8claims.db
 */
public class Builds implements Serializable {
    private long _rn;
    private long _prn;
    private String _bcode;
    private String _bdate;
    private String _displayName;

    public long get_rn() {
        return _rn;
    }

    public void set_rn(long _rn) {
        this._rn = _rn;
    }

    public long get_prn() {
        return _prn;
    }

    public void set_prn(long _prn) {
        this._prn = _prn;
    }

    public String get_bcode() {
        return _bcode;
    }

    public void set_bcode(String _bcode) {
        this._bcode = _bcode;
    }

    public String get_bdate() {
        return _bdate;
    }

    public void set_bdate(String _bdate) {
        this._bdate = _bdate;
    }

    public String get_displayName() {
        return _displayName;
    }

    public void set_displayName(String _displayName) {
        this._displayName = _displayName;
    }
}
