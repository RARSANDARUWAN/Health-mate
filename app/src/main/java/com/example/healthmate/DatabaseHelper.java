package com.example.healthmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "health_mate.db";
    private static final int DATABASE_VERSION = 5;

    // table user schemas
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "u_id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_AGE = "age";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // table health records schemas
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
        // create user accounts table
        String createUsersQuery = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULL_NAME + " TEXT, "
                + COL_AGE + " INTEGER, "
                + COL_EMAIL + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersQuery);

        // create health metrics tracking table with foreign key reference
        String createRecordsQuery = "CREATE TABLE " + TABLE_RECORDS + " ("
                + COL_REC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REC_USER_ID + " INTEGER, "
                + COL_DATE + " TEXT, "
                + COL_STEPS + " INTEGER, "
                + COL_CALORIES + " INTEGER, "
                + COL_WATER + " INTEGER, "
                + "FOREIGN KEY(" + COL_REC_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";
        db.execSQL(createRecordsQuery);

        // seed initial mock data for testing and chart rendering
        addDummyUser(db);
        addDummyRecords(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop old tables and recreate on schema version change
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
        // mock timeline dates for dashboard charts and history logs
        String[] dates = {
                "2026-06-01", "2026-06-02", "2026-06-03",
                "2026-06-04", "2026-06-05", "2026-06-06", "2026-06-07"
        };
        int[] steps = {6500, 7200, 8100, 5400, 9000, 7800, 8500};
        int[] calories = {350, 390, 450, 300, 480, 410, 430};
        int[] water = {1800, 2000, 2200, 1500, 2500, 2100, 2000};

        // insert 7 days of sample metrics linked to default user id 1
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

    public boolean checkUserLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    public Cursor getAllHealthRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        // fetch all records sorted by latest date first for history view
        String query = "SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " DESC";
        return db.rawQuery(query, null);
    }

    public Cursor getWeeklyChartData() {
        SQLiteDatabase db = this.getReadableDatabase();
        // fetch chronological sequence of last 7 days data for dashboard bar charts
        String query = "SELECT * FROM " + TABLE_RECORDS + " ORDER BY " + COL_DATE + " ASC LIMIT 7";
        return db.rawQuery(query, null);
    }
}