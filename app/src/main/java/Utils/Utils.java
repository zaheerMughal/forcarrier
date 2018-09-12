package Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Utils {
    private static MediaRecorder recorder;
    private static boolean recordstarted = false;
    private static File outputFile;


    public static void SHOW_LOG(String message)
    {
        Log.i("696969",message); // make this line comment to disable all app logs
    }

    public static String retrieveContactName(Context context, String number) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        String contactName = "unknown";
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if(!cursor.isClosed()) {
                cursor.close();
            }

            //Log.e(TAG, "Contact Name: " + contactName);
        }
        return contactName;
    }

    public static void startRecording(Context context) {
        //Log.e("recording", "started");
        File audioFile = null;
        File sampleDir = new File(context.getFilesDir(), "/.aa");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        String file_name = "rec";
        try {
            audioFile = File.createTempFile(file_name, ".amr", sampleDir);
            outputFile = audioFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager!=null) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
        }


        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION|MediaRecorder.AudioSource.VOICE_DOWNLINK
                |MediaRecorder.AudioSource.VOICE_UPLINK|MediaRecorder.AudioSource.VOICE_CALL|MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(outputFile.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        recorder.start();
        recordstarted = true;
    }

    public static File stopRecording() {
        if (recordstarted) {
            recorder.stop();
            recordstarted = false;
            return outputFile;
        }
        return null;
    }

    public static String getDeviceName() {
        return Build.MODEL;

    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            SHOW_LOG("File Successfully Moved from temp to local");
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}
