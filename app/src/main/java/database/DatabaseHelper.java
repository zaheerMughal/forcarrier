package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "Calls.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_CALLS_DETAIL_TABLE = "CREATE TABLE "+DbContract.CallsDetailTable.TABLE_NAME+
                "(" +
                DbContract.CallsDetailTable._ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                DbContract.CallsDetailTable.COLUMN_NUMBER+" TEXT," +
                DbContract.CallsDetailTable.COLUMN_NAME +" TEXT, " +
                DbContract.CallsDetailTable.COLUMN_TIME_STAMP +" INTEGER, " +
                DbContract.CallsDetailTable.COLUMN_DURATION +" TEXT, " +
                DbContract.CallsDetailTable.COLUMN_CALL_TYPE+" TEXT," +
                DbContract.CallsDetailTable.COLUMN_RECORDING_FILE_PATH+" TEXT" +
                ");";


        db.execSQL(CREATE_CALLS_DETAIL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String DROP_CALLS_DETAIL_TABLE = "DROP TABLE IF EXISTS "+ DbContract.CallsDetailTable.TABLE_NAME;
        db.execSQL(DROP_CALLS_DETAIL_TABLE);
        onCreate(db);
    }
}
