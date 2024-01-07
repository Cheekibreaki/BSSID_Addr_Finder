package com.example.mac_address;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log; // Import the Log class
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper{
    // Table Name
    public static final String TABLE_NAME = "SIGNAL_STRENGTH";

    // Table columns
    public static final String _ID = "_id";
    public static final String BSSID = "bssid";
    public static final String LEVEL = "level";
    public static final String IMAGEPIXELX = "imagepixelx";
    public static final String IMAGEPIXELY = "imagepixely";
    public static final String FLOORNUM = "floornum";
    public static final String WIFINAME = "wifiname";
    // Database Information
    static final String DB_NAME = "SIGNAL_STRENGTH_DETECT.DB";

    // database version
    static final int DB_VERSION = 7;



    // Creating table query
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BSSID + " TEXT NOT NULL, "
            + WIFINAME + " TEXT NOT NULL, "
            + IMAGEPIXELX + " TEXT NOT NULL, "
            + IMAGEPIXELY+ " TEXT NOT NULL, "
            + FLOORNUM + " TEXT NOT NULL, "
            + LEVEL + " TEXT, "
            + "UNIQUE (" + IMAGEPIXELX + "," + IMAGEPIXELY + ", " + BSSID + "," + FLOORNUM + "));"; // Combined UNIQUE constraint
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

    public void insertData(String bssid, String imagePixelX, String imagePixelY, String floornum, String level, String wifiname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BSSID, bssid);
        contentValues.put(IMAGEPIXELX, imagePixelX);
        contentValues.put(IMAGEPIXELY, imagePixelY);
        contentValues.put(FLOORNUM, floornum);
        contentValues.put(LEVEL, level);
        contentValues.put(WIFINAME, wifiname);
        // Using INSERT OR IGNORE to avoid duplicate entries
        long rowId = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        // Check if insertion was successful
        if (rowId != -1) {
            Log.d("DatabaseHelper", "Data inserted: imagePixelX = " + imagePixelX + ", imagePixelY =" + imagePixelY + ", BSSID = " + bssid);
        } else {
            Log.d("DatabaseHelper", "Insertion failed");
        }
    }

    public void eraseDatabase(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public String getSpecDataAsString(String floornumArg, String _BSSID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        List<String> selectionArgs = new ArrayList<>();

        if (!_BSSID.isEmpty() && !floornumArg.isEmpty()) {
            query = "SELECT * FROM " + TABLE_NAME + " WHERE BSSID = ? AND FLOORNUM = ?";
            selectionArgs.add(_BSSID);
            selectionArgs.add(floornumArg);
        } else if (!_BSSID.isEmpty()) {
            query = "SELECT * FROM " + TABLE_NAME + " WHERE BSSID = ?";
            selectionArgs.add(_BSSID);
        } else if (!floornumArg.isEmpty()) {
            query = "SELECT * FROM " + TABLE_NAME + " WHERE FLOORNUM = ?";
            selectionArgs.add(floornumArg);
        } else {
            query = "SELECT * FROM " + TABLE_NAME;
        }
        query += " ORDER BY BSSID ASC";
        Cursor cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));

        StringBuilder stringBuilder = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                String bssid = cursor.getString(cursor.getColumnIndex(BSSID));
                String imagePixelX = cursor.getString(cursor.getColumnIndex(IMAGEPIXELX));
                String imagePixelY = cursor.getString(cursor.getColumnIndex(IMAGEPIXELY));
                String floornum = cursor.getString(cursor.getColumnIndex(FLOORNUM));
                String level = cursor.getString(cursor.getColumnIndex(LEVEL));

                stringBuilder.append("BSSID: ").append(bssid)
                        .append(", X: ").append(imagePixelX)
                        .append(", Y: ").append(imagePixelY)
                        .append(", FLOORNUM: ").append(floornum)
                        .append(", LEVEL: ").append(level)
                        .append("\n");
            } while (cursor.moveToNext());
        } else {
            stringBuilder.append("No data in the database.");
        }
        cursor.close();

        return stringBuilder.toString();
    }

    public List<String> getUniqueImagePixelCoordinates() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> uniqueCoordinates = new ArrayList<>();

        String query = "SELECT DISTINCT " + IMAGEPIXELX + ", " + IMAGEPIXELY + " FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String imagePixelX = cursor.getString(cursor.getColumnIndex(IMAGEPIXELX));
                String imagePixelY = cursor.getString(cursor.getColumnIndex(IMAGEPIXELY));

                String coordinatePair = "X: " + imagePixelX + ", Y: " + imagePixelY;
                uniqueCoordinates.add(coordinatePair);
            } while (cursor.moveToNext());
        } else {
            uniqueCoordinates.add("No unique coordinates found.");
        }
        cursor.close();

        return uniqueCoordinates;
    }



}
