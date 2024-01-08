package com.example.mac_address;


import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.PointF;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mac_address.ZoomableImageView;

import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;

import android.provider.Settings;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.CountDownTimer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private DatabaseHelper dbHelper;
    private EditText editTextBSSID;
    private TextView textViewScanResults;
    private String floorNum = "1";
    private WifiManager wifiManager;

    private Button buttonCheckDatabase;
    private Button buttonExtractDatabase;
    private Button buttonStartScan;
    private Button buttonEraseDatabase;

    private ZoomableImageView zoomableImageView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextBSSID = findViewById(R.id.editTextBSSID);
        buttonStartScan = findViewById(R.id.buttonStartScan);
        buttonCheckDatabase = findViewById(R.id.buttonCheckDatabase);
        buttonEraseDatabase = findViewById(R.id.buttonEraseDatabase);
        textViewScanResults = findViewById(R.id.textViewScanResults);
        zoomableImageView = findViewById(R.id.zoomableImageView);



        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        buttonStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PointF imagePoint = zoomableImageView.getimagePoint();

                startWifiScan(floorNum,imagePoint);

                //Toast.makeText(MainActivity.this, "Please enter a Grid ID", Toast.LENGTH_SHORT).show();

            }
        });



        buttonCheckDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bssId = editTextBSSID.getText().toString().trim();
                String databaseContents = dbHelper.getSpecDataAsString(floorNum,bssId);
                textViewScanResults.setText(databaseContents);

            }
        });

        buttonExtractDatabase = findViewById(R.id.buttonExtractDatabase);
        buttonExtractDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extractDbFile();
            }
        });



        buttonEraseDatabase.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Start a 3-second countdown timer
                new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        // Optional: Update UI to show remaining time
                    }

                    public void onFinish() {
                        // Erase the database after 3 seconds
                        dbHelper.eraseDatabase();
                        Toast.makeText(MainActivity.this, "Database erased", Toast.LENGTH_SHORT).show();
                    }
                }.start();
                return true; // Return true to indicate that the callback consumed the long click
            }
        });

        dbHelper = new DatabaseHelper(this);


        zoomableImageView.setImageResource(R.drawable.bahen_1stfloor);


        //if press second button
        setupButton(R.id.button1, R.drawable.bahen_1stfloor, zoomableImageView);
        setupButton(R.id.button2, R.drawable.bahen_2ndfloor, zoomableImageView);
        setupButton(R.id.button3, R.drawable.bahen_3rdfloor, zoomableImageView);
        setupButton(R.id.button5, R.drawable.bahen_5thfloor, zoomableImageView);
        setupButton(R.id.button7, R.drawable.bahen_7thfloor, zoomableImageView);
        setupButton(R.id.button8, R.drawable.bahen_8thfloor, zoomableImageView);
        setupButton(R.id.buttonB, R.drawable.bahen_basement, zoomableImageView);

    }

    private void setupButton(int buttonId, final int imageResourceId, ZoomableImageView zoomableImageView) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomableImageView.setImageResource(imageResourceId);
                String resourceName = getResources().getResourceEntryName(button.getId());

                // Extract the character after "button"
                if(resourceName.startsWith("button")) {
                    floorNum = resourceName.substring("button".length());
                    // Do something with the extracted string (floorIdentifier)
                    updateBluePoints();
                }
            }
        });
    }


    private void startWifiScan(String floorNum, PointF imagePoint) {
        String imagePointX = Float.toString(imagePoint.x);
        String imagePointY = Float.toString(imagePoint.y);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
            return;
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null && !scanResults.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
            for (ScanResult scanResult : scanResults) {
                String bssid = scanResult.BSSID;
                String wifiName = scanResult.SSID;
                int level = scanResult.level;
                if(wifiName.equals("UofT")) {
                    Log.d("DatabaseHelper","inserted");
                    dbHelper.insertData(bssid, imagePointX,imagePointY, floorNum, String.valueOf(level),wifiName);
                }
                //dbHelper.insertData(bssid, imagePointX,imagePointY, floorNum, String.valueOf(level),wifiName);
//                sb.append("BSSID: ").append(bssid).append("; WIFI name: ").append(wifiName)
//                        .append("; Level: ").append(level).append("\n");
            }

            String databaseContents = dbHelper.getSpecDataAsString(floorNum,"");
            textViewScanResults.setText(databaseContents);
        }else {
            textViewScanResults.setText("No Wi-Fi scan results available.");
        }
        updateBluePoints();
    }

    private void updateBluePoints() {
        List<String> uniqueCoordinates = dbHelper.getUniqueImagePixelCoordinates(floorNum);
        List<PointF> points = new ArrayList<>();

        if (!uniqueCoordinates.isEmpty() && !uniqueCoordinates.get(0).equals("No unique coordinates found.")) {
            for (String coord : uniqueCoordinates) {
                String[] parts = coord.split(", ");
                float x = Float.parseFloat(parts[0].substring(3)); // Extract x after "X: "
                float y = Float.parseFloat(parts[1].substring(3)); // Extract y after "Y: "
                points.add(new PointF(x, y));
            }
        }

        zoomableImageView.setUniquePoints(points); // This will either set points or an empty list
    }


    private void extractDbFile(){

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);

        boolean success = exportDatabase("scanResult.db");
        if(success){
            Log.d("ExtractDB", "Extraction succeed" );
            Toast.makeText(MainActivity.this, "Exported", Toast.LENGTH_SHORT).show();
        }else{
            Log.d("ExtractDB", "Extraction failed" );
        }

    }

    public boolean exportDatabase(String filename) {
        Log.d("DatabaseHelper", "EnterExportDatabase");
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/x-sqlite3");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
            if (uri == null) {
                Log.e("DatabaseHelper", "Failed to create new MediaStore record.");
                return false;
            }

            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
                 FileChannel src = new FileInputStream(new File(dbHelper.getReadableDatabase().getPath())).getChannel();
                 FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor());
                 FileChannel dst = outputStream.getChannel()) {

                dst.transferFrom(src, 0, src.size());
                Log.d("DatabaseHelper", "Database exported to " + uri.toString());
                return true;

            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error exporting database: " + e.getMessage());
                getContentResolver().delete(uri, null, null);
                return false;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error exporting database: " + e.getMessage());
            return false;
        }
    }

}
