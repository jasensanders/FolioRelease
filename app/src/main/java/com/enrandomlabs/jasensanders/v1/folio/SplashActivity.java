/*
 Copyright 2017 Jasen Sanders (EnRandomLabs).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.enrandomlabs.jasensanders.v1.folio;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jasen Sanders on 10/11/2016.
 * A simple {@link AppCompatActivity} subclass used a Launcher for the MainActivity.
 * This SplashActivity shows a pretty splash screen while MainActivity loads.
 */

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
