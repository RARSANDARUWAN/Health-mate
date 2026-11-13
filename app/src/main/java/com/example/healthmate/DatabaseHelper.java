package com.example.healthmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "health_mate.db";
    private static final int DATABASE_VERSION = 5;

    // Users table and columns
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "u_id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_AGE = "age";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // Health records table and columns
    public static final String TABLE_RECORDS = "health_records";
    public static final String COL_REC_ID = "id";
    public static final String COL_REC_USER_ID = "u_id";
    public static final String COL_DATE = "date";
    public static final String COL_STEPS = "steps";
    public static final String COL_CALORIES = "calories";
    public static final String COL_WATER = "water";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersQuery = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULL_NAME + " TEXT, "
                + COL_AGE + " INTEGER, "
                + COL_EMAIL + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersQuery);

        // Create health records table
        String createRecordsQuery = "CREATE TABLE " + TABLE_RECORDS + " ("
                + COL_REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REC_USER_ID + " INTEGER, "
                + COL_DATE + " TEXT, "
                + COL_STEPS + " INTEGER, "
                + COL_CALORIES + " INTEGER, "
                + COL_WATER + " INTEGER, "
                + "FOREIGN KEY(" + COL_REC_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createRecordsQuery);

        // Add default test data
        addDummyUser(db);
        addDummyRecords(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables if database version updates
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Insert a default user for testing
    private void addDummyUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, "RAR Sandaruwan");
        values.put(COL_AGE, 23);
        values.put(COL_EMAIL, "sandaruwan@example.com");
        values.put(COL_PASSWORD, "123456");
        db.insert(TABLE_USERS, null, values);
    }

    // Insert 7 days  records
    private void addDummyRecords(SQLiteDatabase db) {
        String[] dates = {
                "2026-06-01", "2026-06-02", "2026-06-03",
                "2026-06-04", "2026-06-05", "2026-06-06", "2026-06-07"
        };
        int[] steps = {6500, 7200, 8100, 5400, 9000, 7800, 8500};
        int[] calories = {350, 390, 450, 300, 480, 410, 430};
        int[] water = {1800, 2000, 2200, 1500, 2500, 2100, 2000};

        for (int i = 0; i < dates.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COL_REC_USER_ID, 1);
            values.put(COL_DATE, dates[i]);
            values.put(COL_STEPS, steps[i]);
            values.put(COL_CALORIES, calories[i]);
            values.put(COL_WATER, water[i]);
            db.insert(TABLE_RECORDS, null, values);
        }
    }

    // Save a new user account
    public boolean registerUser(String fullName, int age, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_AGE, age);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Verify user login details
    public boolean checkUserLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    // Fetch all health records sorted by date
    public Cursor getAllHealthRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " DESC";
        return db.rawQuery(query, null);
    }
}
