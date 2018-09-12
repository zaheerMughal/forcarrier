package database;

import android.net.Uri;
import android.provider.BaseColumns;

public class DbContract {

    public static final String AUTHORITY = "database";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+AUTHORITY);

    private DbContract(){}


    public static class CallsDetailTable implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(CallsDetailTable.TABLE_NAME).build();

        public static final String TABLE_NAME = "CallsDetail"; // path to this table
        public static final String COLUMN_NUMBER = "Number";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_TIME_STAMP = "TimeStamp";
        public static final String COLUMN_DURATION = "Duration";
        public static final String COLUMN_CALL_TYPE = "CallType";
        public static final String COLUMN_RECORDING_FILE_PATH = "RecordingFilePath";
    }



}
