package com.enrandomlabs.jasensanders.v1.folio;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;



public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If Restore database Flag is set to true, then restore database
        //and reset flag to false.
        if(Utility.getPrefRestoreBackup(this) && Utility.backupExists()){
            Utility.setPrefRestoreBackup(this, false);
            Utility.restoreDBfromMobileDevice(this);
        }

        //On first run, If This device does not support auto-backup, then attempt to recover
        //database from local storage.
        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (Utility.isFirstRun(this) && Utility.backupExists()) {
                Utility.restoreDBfromMobileDevice(this);
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
