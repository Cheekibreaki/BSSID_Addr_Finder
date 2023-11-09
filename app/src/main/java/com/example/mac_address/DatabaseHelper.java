package com.example.mac_address;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log; // Import the Log class
import android.database.Cursor;
import java.io.File;
import android.provider.MediaStore;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.FileOutputStream;




public class DatabaseHelper extends SQLiteOpenHelper{
    // Table Name
    public static final String TABLE_NAME = "SIGNAL_STRENGTH";

    // Table columns
    public static final String _ID = "_id";
    public static final String BSSID = "bssid";
    public static final String LEVEL = "level";
    public static final String GRIDID = "gridid";
    public static final String WIFINAME = "wifiname";
    // Database Information
    static final String DB_NAME = "SIGNAL_STRENGTH_DETECT.DB";

    // database version
    static final int DB_VERSION = 6;



    // Creating table query
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BSSID + " TEXT NOT NULL, "
            + WIFINAME + " TEXT NOT NULL, "
            + GRIDID + " TEXT NOT NULL, "
            + LEVEL + " TEXT, "
            + "UNIQUE (" + GRIDID + ", " + BSSID + "));"; // Combined UNIQUE constraint
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertData(String bssid, String gridId, String level, String wifiname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BSSID, bssid);
        contentValues.put(GRIDID, gridId);
        contentValues.put(LEVEL, level);
        contentValues.put(WIFINAME, wifiname);
        // Using INSERT OR IGNORE to avoid duplicate entries
        long rowId = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        // Check if insertion was successful
        if (rowId != -1) {
            Log.d("DatabaseHelper", "Data inserted: GridID = " + gridId + ", BSSID = " + bssid);
        } else {
            Log.d("DatabaseHelper", "Insertion failed for GridID = " + gridId);
        }
    }

    public String getAllDataAsString() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        StringBuilder stringBuilder = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                String bssid = cursor.getString(cursor.getColumnIndex(BSSID));
                String gridId = cursor.getString(cursor.getColumnIndex(GRIDID));
                String level = cursor.getString(cursor.getColumnIndex(LEVEL));
                String wifiName = cursor.getString(cursor.getColumnIndex(WIFINAME));

                stringBuilder.append("BSSID: ").append(bssid)
                        .append(", GRIDID: ").append(gridId)
                        .append(", LEVEL: ").append(level)
                        .append(", WIFI NAME: ").append(wifiName)
                        .append("\n");
            } while (cursor.moveToNext());
        } else {
            stringBuilder.append("No data in the database.");
        }
        cursor.close();

        return stringBuilder.toString();
    }




//    public boolean exportDatabase(String folderpath, String outputPath) {
//        Log.d("DatabaseHelper", "EnterExportDatabase");
//        try {
////            File sd = Environment.getExternalStorageDirectory();
////            if (sd.canWrite()) {
//                String currentDBPath = getReadableDatabase().getPath();
//                File currentDB = new File(currentDBPath);
//                File backupDB = new File(folderpath, outputPath);
//
//                if (currentDB.exists()) {
//                    try (FileChannel src = new FileInputStream(currentDB).getChannel();
//                         FileChannel dst = new FileOutputStream(backupDB).getChannel()) {
//                        dst.transferFrom(src, 0, src.size());
//                        Log.d("DatabaseHelper", "Database exported to " + backupDB.getPath());
//                        return true;
//                    }
//                }else{
//                    Log.d("DatabaseHelper", "Can not find currentDB" + currentDB.getPath());
//                }
////            }else{
////                Log.e("DatabaseHelper", "Can not write External: " );
////            }
//        } catch (Exception e) {
//            Log.e("DatabaseHelper", "Error exporting database: " + e.getMessage());
//        }
//        return false;
//    }




}
