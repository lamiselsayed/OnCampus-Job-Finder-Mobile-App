package com.cmp354.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseConnector {
    private SQLiteDatabase database;
    private String name;
    private DatabaseOpenHelper databaseOpenHelper;

    public DatabaseConnector(Context context, String name) {
        this.name = name.split("\\s+")[0];
        databaseOpenHelper = new DatabaseOpenHelper(context, "jobsWishlist" + name, null, 1);
    }

    public void open() throws SQLException {
        database = databaseOpenHelper.getWritableDatabase();
    }

    public void close() {
        if(database != null)
            database.close();
    }

    // inserts job into the student's wishlist
    public void insertJobWishlist(String title, String type, float salary, String duration, String desc) {
        ContentValues insertJob = new ContentValues();
        insertJob.put("title", title);
        insertJob.put("type", type);
        insertJob.put("salary", salary);
        insertJob.put("duration", duration);
        insertJob.put("description", desc);

        open();
        database.insert("jobsWishlist" + name, null, insertJob);
        close();
    }

    // removes job from the student's wishlist
    public void deleteJobWishlist(long id) {
        open();
        database.delete("jobsWishlist" + name, "_id = " + id, null);
        close();
    }

    public Cursor getAllJobsinWishlist() {
        return database.query("jobsWishlist" + name, null, null, null,
                null, null, "title");
                // order alphabetically based on job title
    }

    public boolean checkIfExists(String _title, String _type, float _salary, String _duration, String _desc) {
        String checkQuery = "SELECT * FROM jobsWishlist" + name + " WHERE title = '" + _title + "' and type = '" + _type
                + "' and salary = " + _salary + " and duration = '" + _duration + "' and description = '"+ _desc + "'";
        open();
        Cursor check = database.rawQuery(checkQuery, null);
        if(check.getCount() == 0) {
            check.close();
            close();
            return false;
        }
        else {
            check.close();
            close();
            return true;
        }
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper {
        public DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // query to create a new table named jobsWishlist
            String createQuery = "CREATE TABLE jobsWishlist" + name +
                    "(_id integer primary key autoincrement," +
                    "title TEXT, type TEXT, salary float, duration TEXT, " +
                    "description TEXT);";
            db.execSQL(createQuery); // execute the query
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
    }
}
