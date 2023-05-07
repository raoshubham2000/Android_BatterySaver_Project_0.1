package com.example.beta_31;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView batteryPercentageTextView = findViewById(R.id.batteryPercentageTextView);
        Button saveBatteryButton = findViewById(R.id.saveBatteryButton);

        // Set the click listener for the Save Battery button
        saveBatteryButton.setOnClickListener(v -> {
            // Reduce brightness

            // Turn off data, Bluetooth, and GPS
            disableData();
            disableBluetooth();
            disableGPS();
            reduceBrightness();


        });

        // Get the current battery percentage and update the UI
        int batteryPercentage = getBatteryPercentage();
        batteryPercentageTextView.setText(getString(R.string.battery_percentage, batteryPercentage));
    }
    private static final int REQUEST_SYSTEM_ALERT_WINDOW_PERMISSION = 2;
    private void reduceBrightness() {
        // Reduce the screen brightness
        // Note: Requires the SYSTEM_ALERT_WINDOW permission in AndroidManifest.xml
        if (!Settings.canDrawOverlays(this)) {
            // Request the necessary permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW_PERMISSION);
        } else {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = 0.5f; // Adjust the value as needed (0.0f - 1.0f)
            getWindow().setAttributes(layoutParams);
        }
    }

    private static final int REQUEST_DATA_PERMISSIONS = 1;



    private void disableData() {
        // Disable mobile data
        // Note: Requires the ACCESS_NETWORK_STATE and CHANGE_NETWORK_STATE permissions in AndroidManifest.xml
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
                NetworkRequest request = builder.build();
                connectivityManager.unregisterNetworkCallback(dataCallback);
                connectivityManager.requestNetwork(request, dataCallback);
            }
        } else {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CHANGE_NETWORK_STATE}, REQUEST_DATA_PERMISSIONS);
        }
    }

    private final ConnectivityManager.NetworkCallback dataCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.bindProcessToNetwork(network);
            }
        }

        @Override
        public void onLost(Network network) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.bindProcessToNetwork(null);
            }
        }
    };

    // Handle permission request results

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_DATA_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, disable data
                disableData();
            } else {
                Toast.makeText(this, "Permission denied. Cannot disable mobile data.", Toast.LENGTH_SHORT).show();            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void disableBluetooth() {
        // Disable Bluetooth
        // Note: Requires the BLUETOOTH and BLUETOOTH_ADMIN permissions in AndroidManifest.xml
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        } else {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 0);
        }
    }

    private void disableGPS() {
        // Disable GPS
        // Note: Requires the ACCESS_FINE_LOCATION permission in AndroidManifest.xml
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // Handle location updates if needed
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // Handle GPS status changes if needed
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // Handle GPS provider enabled if needed
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // Handle GPS provider disabled if needed
                    }
                };
                locationManager.removeUpdates(locationListener);
            }
        } else {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }


    private int getBatteryPercentage() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, intentFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPercentage = (level / (float) scale) * 100;
        return Math.round(batteryPercentage);
    }
}