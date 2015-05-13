package ua.parus.pmo.parus8claims.db;

/**
 * Created by igorgo on 18.04.2015.
 */
class Units {
    private long _id;
    private String _code;
    private String _name;
    private int _apps_cached;
    private int _funcs_cached;


    public String get_code() {
        return _code;
    }

    public void set_code(String _code) {
        this._code = _code;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public int get_apps_cached() {
        return _apps_cached;
    }

    public void set_apps_cached(int _apps_cached) {
        this._apps_cached = _apps_cached;
    }

    public int get_funcs_cached() {
        return _funcs_cached;
    }

    public void set_funcs_cached(int _funcs_cached) {
        this._funcs_cached = _funcs_cached;
    }
}
