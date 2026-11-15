package com.example.healthmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "health_mate.db";
    private static final int DATABASE_VERSION = 6;

    // user table schema definitions
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "u_id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_AGE = "age";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // health metrics table schema definitions
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
        // build user profile table
        String createUsersQuery = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULL_NAME + " TEXT, "
                + COL_AGE + " INTEGER, "
                + COL_EMAIL + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersQuery);

        // build health logs table with foreign key constraint
        String createRecordsQuery = "CREATE TABLE " + TABLE_RECORDS + " ("
                + COL_REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REC_USER_ID + " INTEGER, "
                + COL_DATE + " TEXT, "
                + COL_STEPS + " INTEGER, "
                + COL_CALORIES + " INTEGER, "
                + COL_WATER + " INTEGER, "
                + "FOREIGN KEY(" + COL_REC_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createRecordsQuery);

        // populate initial sample seed records
        addDummyUser(db);
        addDummyRecords(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop legacy tables and trigger re-creation
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void addDummyUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, "RAR Sandaruwan");
        values.put(COL_AGE, 23);
        values.put(COL_EMAIL, "sandaruwan@example.com");
        values.put(COL_PASSWORD, "123456");
        db.insert(TABLE_USERS, null, values);
    }

    private void addDummyRecords(SQLiteDatabase db) {
        String[] timelineDates = {
                "2026-06-01", "2026-06-02", "2026-06-03",
                "2026-06-04", "2026-06-05", "2026-06-06", "2026-06-07"
        };
        int[] sampleSteps = {6500, 7200, 8100, 5400, 9000, 7800, 8500};
        int[] sampleCalories = {350, 390, 450, 300, 480, 410, 430};
        int[] sampleWater = {1800, 2000, 2200, 1500, 2500, 2100, 2000};

        // loop and seed default sample weekly metrics
        for (int i = 0; i < timelineDates.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COL_REC_USER_ID, 1);
            values.put(COL_DATE, timelineDates[i]);
            values.put(COL_STEPS, sampleSteps[i]);
            values.put(COL_CALORIES, sampleCalories[i]);
            values.put(COL_WATER, sampleWater[i]);
            db.insert(TABLE_RECORDS, null, values);
        }
    }

    public boolean registerUser(String fullName, int age, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_AGE, age);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);

        long resultId = db.insert(TABLE_USERS, null, values);
        return resultId != -1;
    }

    public boolean checkUserLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        boolean isValidUser = cursor.getCount() > 0;
        cursor.close();
        return isValidUser;
    }

    public Cursor getAllHealthRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        // fetch all records sorted by most recent date first
        String query = "SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " DESC";
        return db.rawQuery(query, null);
    }

    public Cursor getWeeklyChartData() {
        SQLiteDatabase db = this.getReadableDatabase();
        // query records in ascending order for dashboard chart rendering
        String query = "SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " ASC";
        return db.rawQuery(query, null);
    }

    public Cursor getRecordByDate(String targetDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_RECORDS + " WHERE " + COL_DATE + " = ?";
        return db.rawQuery(query, new String[]{targetDate});
    }

    public boolean checkRecordExistsByDate(String targetDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_RECORDS + " WHERE " + COL_DATE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{targetDate});
        boolean isExisting = cursor.getCount() > 0;
        cursor.close();
        return isExisting;
    }

    public boolean insertHealthRecord(int userId, String date, int steps, int calories, int water) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REC_USER_ID, userId);
        values.put(COL_DATE, date);
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);

        long insertId = db.insert(TABLE_RECORDS, null, values);
        return insertId != -1;
    }

    public Cursor getRecentHealthRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        // fetch top 5 latest logs for recent activity list
        String query = "SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " DESC LIMIT 5";
        return db.rawQuery(query, null);
    }

    public boolean deleteRecordById(int recordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int affectedRows = db.delete(TABLE_RECORDS, COL_REC_ID + " = ?", new String[]{String.valueOf(recordId)});
        return affectedRows > 0;
    }

    public boolean updateHealthRecord(int recordId, int steps, int calories, int water) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);

        int affectedRows = db.update(TABLE_RECORDS, values, COL_REC_ID + " = ?", new String[]{String.valueOf(recordId)});
        return affectedRows > 0;
    }
}