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
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_DATA_PERMISSIONS = 1;
    private ConnectivityManager.NetworkCallback dataCallback;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 3;


    private TextView batteryPercentageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryPercentageTextView = findViewById(R.id.batteryPercentageTextView);
        Button saveBatteryButton = findViewById(R.id.saveBatteryButton);

        saveBatteryButton.setOnClickListener(v -> saveBattery());
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int batteryPercentage = getBatteryPercentage();

        updateBatteryPercentage(batteryPercentage);
    }

    private void saveBattery() {
        disableData();
        disableBluetooth();
        disableGPS();
        reduceBrightness();

    }

    private void reduceBrightness() {
        // Reduce the screen brightness
        // Note: Requires the SYSTEM_ALERT_WINDOW permission in AndroidManifest.xml
        if (!Settings.canDrawOverlays(this)) {
            // Request the necessary permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            requestManageOverlayPermission.launch(intent);
        } else {
            try {
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.screenBrightness = 0.5f; // Adjust the value as needed (0.0f - 1.0f)
                getWindow().setAttributes(layoutParams);

                Toast.makeText(this, "Screen brightness reduced", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to reduce brightness", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Error reducing brightness: " + e.getMessage());
            }
        }
    }

    private final ActivityResultLauncher<Intent> requestManageOverlayPermission = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Overlay permission granted, retry reducing brightness
                    reduceBrightness();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot reduce brightness.", Toast.LENGTH_SHORT).show();
                }
            }
    );



    private void disableData() {
        // Disable mobile data
        // Note: Requires the ACCESS_NETWORK_STATE and CHANGE_NETWORK_STATE permissions in AndroidManifest.xml
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                if (dataCallback != null) {
                    connectivityManager.unregisterNetworkCallback(dataCallback);
                }

                dataCallback = new ConnectivityManager.NetworkCallback() {
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

                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
                NetworkRequest request = builder.build();
                connectivityManager.registerNetworkCallback(request, dataCallback);
            }
        } else {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CHANGE_NETWORK_STATE}, REQUEST_DATA_PERMISSIONS);
        }
    }

    private void disableBluetooth() {
        // Check if Bluetooth is supported on the device
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable Bluetooth
        // Note: Requires the BLUETOOTH and BLUETOOTH_ADMIN permissions in AndroidManifest.xml
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        } else {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, 0);
        }
    }

    private void disableGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    private int getBatteryPercentage() {

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, intentFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPercentage = (level / (float) scale) * 100;
        return Math.round(batteryPercentage);
    }

    private void updateBatteryPercentage(int percentage) {
        batteryPercentageTextView.setText(getString(R.string.battery_percentage, percentage));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_DATA_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                disableData();
            } else {
                Toast.makeText(this, "Permission denied. Cannot disable mobile data.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                disableBluetooth();
            } else {
                Toast.makeText(this, "Permission denied. Cannot disable Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


