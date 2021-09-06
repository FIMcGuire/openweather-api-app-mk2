package com.example.weatherapiapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

//class DatabaseManipulator
public class DatabaseManipulator {
    //string to hold name of database
    private static final String DATABASE_NAME = "savedcities.db";

    //int to declare database version (4)
    private static int DATABASE_VERSION = 1;

    //string to hold table name
    static final String TABLE_NAME = "newtable";

    //context object
    private static Context context;

    //SQLiteDatabase object
    static SQLiteDatabase db;

    //SQLiteStatement object
    private SQLiteStatement insertStmt;

    //string to structure 'Insert' SQL statement
    private static final String INSERT = "insert into " + TABLE_NAME
            + " (name) values (?)";

    //constructor method
    public DatabaseManipulator(Context context)
    {
        DatabaseManipulator.context = context;
        OpenHelper openHelper = new OpenHelper(this.context);
        DatabaseManipulator.db = openHelper.getWritableDatabase();
        this.insertStmt = DatabaseManipulator.db.compileStatement(INSERT);
    }

    //method to insert given string into database
    public long insert(String name)
    {
        this.insertStmt.bindString(1, name);
        return this.insertStmt.executeInsert();
    }

    //method to delete given string from database
    public void deletecity(String city) {
        db.delete(TABLE_NAME, "name = " + "'" + city + "'", null);
    }

    //method to return all strings stored in database
    public List<String[]> selectAll()
    {
        //create list of string arrays list
        List<String[]> list = new ArrayList<>();
        //create cursor object
        Cursor cursor = db.query(TABLE_NAME, new String[]{"id", "name"}, null, null, null, null, "name asc");
        //create int counter
        int x = 0;
        //if cursor.moveToFirst() returns true
        if (cursor.moveToFirst())
        {
            //do while cursor.movetoNext() returns true
            do {
                //create array of Strings bl fill with database entry at index 0 and index 1
                String[] bl = new String[]{cursor.getString(0),
                    cursor.getString(1)};
                //add current array to list
                list.add(bl);
                //increment x
                x++;
            }while (cursor.moveToNext());
        }

        //if statement to determine if cursor is not null and if cursor.isClosed() returns false
        if (cursor != null && !cursor.isClosed())
        {
            //call cursor.close()
            cursor.close();
        }

        //call cursor.close()
        cursor.close();

        //return list
        return list;
    }

    //declare OpenHelper class
    private static class OpenHelper extends SQLiteOpenHelper
    {
        //constructor
        OpenHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db)
        {
            //execute SQL statement
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY, name TEXT)");
        }

        //method to create new database
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            DATABASE_VERSION = newVersion;
            //execute SQL statement
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            //call onCreate() method
            onCreate(db);
        }
    }
}
