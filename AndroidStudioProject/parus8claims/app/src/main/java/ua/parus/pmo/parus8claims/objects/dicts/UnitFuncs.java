package ua.parus.pmo.parus8claims.objects.dicts;

/**
 * Created  by igorgo on 03.05.2015.
 */
public class UnitFuncs {

    public static final String TABLE_NAME = "unitfunc";
    public static final String COLUMN_ID = "id";
    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String COLUMN_UNIT = "unit";
    private static final String COLUMN_UNIT_TYPE = "INTEGER";
    public static final String COLUMN_FUNC = "func";
    private static final String COLUMN_FUNC_TYPE = "TEXT";
    private static final String COMMA_SEP = ", ";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_UNIT + " " + COLUMN_UNIT_TYPE + COMMA_SEP +
                    COLUMN_FUNC + " " + COLUMN_FUNC_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

}
