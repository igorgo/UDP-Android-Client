package ua.parus.pmo.parus8claims.db;

/**
 * Created by igorgo on 11.04.2015.
 * http://kylewbanks.com/blog/Tutorial-Implementing-a-Client-Side-Cache-using-the-SQLite-Database-on-Android-and-SQLiteOpenHelper
 */
class Applist {

    private String _code;
    private String _name;
    private long _id;


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
}
