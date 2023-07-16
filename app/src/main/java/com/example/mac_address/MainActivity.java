package com.example.mac_address;

//import androidx.appcompat.app.AppCompatActivity;
//import android.os.Bundle;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    private EditText editTextGridId;
    private Button buttonStartScan;
    private TextView textViewScanResults;

    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextGridId = findViewById(R.id.editTextGridId);
        buttonStartScan = findViewById(R.id.buttonStartScan);
        textViewScanResults = findViewById(R.id.textViewScanResults);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        buttonStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gridId = editTextGridId.getText().toString().trim();
                if (!gridId.isEmpty()) {
                    startWifiScan(gridId);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a Grid ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startWifiScan(String gridId) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
            return;
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null && !scanResults.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ScanResult scanResult : scanResults) {
                String bssid = scanResult.BSSID;
                String wifiName = scanResult.SSID;
                int level = scanResult.level;
                sb.append("BSSID: ").append(bssid).append("; WIFI name: ").append(wifiName)
                        .append("; Level: ").append(level).append("\n");
            }

            String scanResultText = sb.toString().trim();
            textViewScanResults.setText(scanResultText);
        }
    }
}
