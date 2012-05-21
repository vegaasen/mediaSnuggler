package com.gtfo.snuggle.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.gtfo.snuggle.model.ServerInformation;
import com.gtfo.snuggle.service.impl.MediaServiceImpl;

/**
 * Main activity (and the only one..?)
 * TODO: Enhance + start on the share music + video stuff..
 *
 * @author vegaasen
 * @since 0.1a
 */

public class MainActivity extends Activity
{
    private static final String TAG = MainActivity.class.getName();
    private ServerInformation serverInformation;
    private NetworkInfo networkInfo;
    private ConnectivityManager connectivityManager;
    private boolean startServerButtonActivated = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        wifiVerification();

        initiateLoggingOfServer();
        
        initiateService();
    }

    /**
     * Application is forced to close, and we will destroy the created services + itself..
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MediaServiceImpl.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.rescan).setIcon(android.R.drawable.ic_menu_search);
        menu.add(0, 1, 0, R.string.dissolve).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                ));
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + Environment.getRootDirectory())
                ));
                Log.i(TAG, "Refreshing content from the external media.");
                break;
            case 1:
                Log.d(TAG, "Stopping the application.");
                finish();
        }
        return false;
    }

    private boolean wifiVerification() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
            startServerButtonActivated = true;
            return true;
        }
        Toast.makeText(this, "Please connect to a WiFi connection", Toast.LENGTH_LONG).show();
        showActivationMenuForWiFi();
        return false;
    }

    private void showActivationMenuForWiFi() {

    }

    /**
     * This method will start the server-service.
     * NOTE: This will both initiate the Pictures-service, the Movie-service, the Song-service and the File-service.
     * todo: make the user determine this themselves via an integrated menu
     */

    private void initiateService() {
        Log.i(TAG, "Starting the main Service");
        startService(new Intent(this, MediaServiceImpl.class));
    }

    private void initiateLoggingOfServer() {
        //not implemented yet
    }

}
