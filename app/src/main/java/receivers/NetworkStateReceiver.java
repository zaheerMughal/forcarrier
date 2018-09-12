package receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseHelper;
import database.DbContract;
import model.Call;

import static Utils.Utils.SHOW_LOG;
import static Utils.Utils.getDeviceName;
import static Utils.Utils.isNetworkAvailable;

public class NetworkStateReceiver extends BroadcastReceiver {

    private FirebaseStorage storageRoot = FirebaseStorage.getInstance();
    DatabaseReference callsReference;




    @Override
    public void onReceive(final Context context, Intent intent) {
          callsReference = FirebaseDatabase.getInstance().getReference().child(getDeviceName()).child("Calls");


        if(isNetworkAvailable(context))
        {
            // getDataFrom db and upload to cloud
            // if successful then delete from db else do nothing
            SHOW_LOG("Network Connected");
            final SQLiteDatabase sqLiteDatabase = new DatabaseHelper(context).getReadableDatabase();
            getCallsFromDBandUploadToCloud(context, sqLiteDatabase);


        }
        else
        {
            SHOW_LOG("Network is not connected");
        }
    }





    private void getCallsFromDBandUploadToCloud(Context context, SQLiteDatabase sqLiteDatabase) {
        Cursor cursor = sqLiteDatabase.query(DbContract.CallsDetailTable.TABLE_NAME,null,null,null,null,null,null);
        List<Call> callsList = new ArrayList<>();

        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                do{
                    Call call = new Call();
                    call.setId(cursor.getLong(cursor.getColumnIndex(DbContract.CallsDetailTable._ID)));
                    call.setNumber(cursor.getString(cursor.getColumnIndex(DbContract.CallsDetailTable.COLUMN_NUMBER)));
                    call.setName(cursor.getString(cursor.getColumnIndex(DbContract.CallsDetailTable.COLUMN_NAME)));
                    call.setTimeStamp(cursor.getLong(cursor.getColumnIndex(DbContract.CallsDetailTable.COLUMN_TIME_STAMP)));
                    call.setDuration(cursor.getString(cursor.getColumnIndex(DbContract.CallsDetailTable.COLUMN_DURATION)));
                    call.setType(getType(cursor.getString(cursor.getColumnIndex(DbContract.CallsDetailTable.COLUMN_CALL_TYPE))));
                    call.setRecordingPath(cursor.getString(cursor.getColumnIndex(DbContract.CallsDetailTable.COLUMN_RECORDING_FILE_PATH)));
                    callsList.add(call);
                }while (cursor.moveToNext());
            }
            cursor.close();
        }


        SHOW_LOG("Call Data Extracted from DB, its time to upload");
        int listLength = callsList.size();
        SHOW_LOG("Calls Extracted: "+listLength);
        for(int i=0; i<listLength; i++)
        {
            SHOW_LOG(callsList.get(i).getId()+" Call id,  Detail: "+callsList.get(i).toString());
            // push call item into cloud, if successful delete from db
            pushCallFromDatabaseToCloud(context, callsList.get(i));
        }

    }


    private void pushCallFromDatabaseToCloud(final Context context, final Call call) {
        // when pushing successful then delete from database
        SHOW_LOG(call.getId()+" Call Id, Pushing Call to cloud");
        if(call.getRecordingPath()!=null){
            File recordingFile = new File(call.getRecordingPath());
            FirebaseStorage recordingRoot = storageRoot.getReference("Recordings").getStorage();
            final Uri file = Uri.fromFile(recordingFile);
            final StorageReference recordingFileRef = recordingRoot.getReference().child(file.getLastPathSegment());

            recordingFileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    SHOW_LOG("Recording Successfully Uploaded");
                    recordingFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            SHOW_LOG("URL: "+ uri.toString());
                            call.setRecordingPath(uri.toString());
                            SHOW_LOG(call.getId()+"Call Id, Now Pusing Call to cloud");
                            callsReference.push().setValue(call).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    SHOW_LOG(call.getId()+" Call Id, Call Successfully Transferred Now Delete from DB");
                                    deleteCallFromDB(context,call);
                                }
                            });
                        }
                    });
                }
            });
        }else
        {
            callsReference.push().setValue(call).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    SHOW_LOG(call.getId()+" Call Id, Call Successfully Transferred Now Delete from DB");
                    deleteCallFromDB(context, call);
                }
            });
        }
    }


    private void deleteCallFromDB(Context context, Call call)
    {
        SHOW_LOG(call.getId()+" Call Id, Deleting from the database");
        SQLiteDatabase sqLiteDatabase = new DatabaseHelper(context).getWritableDatabase();
        int deletedItem = sqLiteDatabase.delete(DbContract.CallsDetailTable.TABLE_NAME, DbContract.CallsDetailTable._ID+"="+call.getId(),null);
        if(!(deletedItem<0))
        {
            SHOW_LOG(deletedItem+" Item Successfully deleted from db");
        }
        else
        {
            SHOW_LOG(deletedItem+" Item Failed to delete from DB");
        }
    }


    public Call.CallType getType(String type) {
        if (type.equals(Call.CallType.INCOMING.toString()))
            return Call.CallType.INCOMING;
        else if(type.equals(Call.CallType.OUTGOING.toString()))
            return Call.CallType.OUTGOING;
        else
            return Call.CallType.MISSED;
    }

}
