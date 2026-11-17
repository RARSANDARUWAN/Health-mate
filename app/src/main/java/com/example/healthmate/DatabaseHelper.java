package com.example.healthmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "health_mate.db";
    private static final int DATABASE_VERSION = 10;

    // users table schema column definitions
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "u_id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_AGE = "age";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_JOINED_DATE = "joined_date";

    // health records table schema column definitions
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
        // create user profile accounts table
        String createUsersQuery = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULL_NAME + " TEXT, "
                + COL_AGE + " INTEGER, "
                + COL_EMAIL + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT, "
                + COL_JOINED_DATE + " TEXT)";
        db.execSQL(createUsersQuery);

        // create health metrics logs table with foreign key reference
        String createRecordsQuery = "CREATE TABLE " + TABLE_RECORDS + " ("
                + COL_REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REC_USER_ID + " INTEGER, "
                + COL_DATE + " TEXT, "
                + COL_STEPS + " INTEGER, "
                + COL_CALORIES + " INTEGER, "
                + COL_WATER + " INTEGER, "
                + "FOREIGN KEY(" + COL_REC_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createRecordsQuery);

        // seed initial demo records and user profile data
        addDummyUser(db);
        addDummyRecords(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop existing tables and rebuild database schema
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // insert default pre-populated demo user record
    private void addDummyUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, "RAR Sandaruwan");
        values.put(COL_AGE, 23);
        values.put(COL_EMAIL, "sandaruwan@example.com");
        values.put(COL_PASSWORD, "123456");
        values.put(COL_JOINED_DATE, "October 2026");
        db.insert(TABLE_USERS, null, values);
    }

    // insert initial sample timeline health records for testing charts and stats
    private void addDummyRecords(SQLiteDatabase db) {
        String[] timelineDates = {
                "2026-06-01", "2026-06-02", "2026-06-03",
                "2026-06-04", "2026-06-05", "2026-06-06", "2026-06-07"
        };
        int[] sampleSteps = {6500, 7200, 8100, 5400, 9000, 7800, 8500};
        int[] sampleCalories = {350, 390, 450, 300, 480, 410, 430};
        int[] sampleWater = {1800, 2000, 2200, 1500, 2500, 2100, 2000};

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

    // register a new user profile into sqlite database
    public boolean registerUser(String fullName, int age, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_AGE, age);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);

        String currentMonthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
        values.put(COL_JOINED_DATE, currentMonthYear);

        long resultId = db.insert(TABLE_USERS, null, values);
        return resultId != -1;
    }

    // verify user login credentials against database records
    public boolean checkUserLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?", new String[]{email, password});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    // query and retrieve unique user id associated with email address
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            cursor.close();
        }
        return userId;
    }

    // fetch full user profile details cursor by user id
    public Cursor getUserProfileById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    // count total number of health logs created by specific user
    public int getTotalLogsCountByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECORDS + " WHERE " + COL_REC_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    // update user profile name and age details in database
    public boolean updateUserProfile(int userId, String fullName, int age) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, fullName);
        values.put(COL_AGE, age);

        int rows = db.update(TABLE_USERS, values, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // fetch all health records ordered descending by date
    public Cursor getAllHealthRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " DESC", null);
    }

    // fetch all health records ordered ascending for weekly chart rendering
    public Cursor getWeeklyChartData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " ASC", null);
    }

    // query a specific health log record by exact date string
    public Cursor getRecordByDate(String targetDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECORDS + " WHERE " + COL_DATE + " = ?", new String[]{targetDate});
    }

    // check if a health record already exists for the given date
    public boolean checkRecordExistsByDate(String targetDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECORDS + " WHERE " + COL_DATE + " = ?", new String[]{targetDate});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // insert a new daily health record entry into database
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

    // fetch top 5 most recent health records for dashboard display
    public Cursor getRecentHealthRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " DESC LIMIT 5", null);
    }

    // delete a specific health record by its primary key id
    public boolean deleteRecordById(int recordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_RECORDS, COL_REC_ID + " = ?", new String[]{String.valueOf(recordId)});
        return rows > 0;
    }

    // update existing health log metrics by record id
    public boolean updateHealthRecord(int recordId, int steps, int calories, int water) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_WATER, water);

        int rows = db.update(TABLE_RECORDS, values, COL_REC_ID + " = ?", new String[]{String.valueOf(recordId)});
        return rows > 0;
    }
}