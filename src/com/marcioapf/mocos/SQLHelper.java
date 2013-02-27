package com.marcioapf.mocos;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class SQLHelper extends SQLiteOpenHelper {

	
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MATERIASDB";
    
    public static final String _ID = BaseColumns._ID;
    private static final String FIELD_NAME = "FIELD_NAME";
    private static final String FIELD_ATRASOS = "FIELD_ATRASOS";
    //private static final String FIELD_ = "FIELD_ATRASOS";
    
    private static final String TABLE_NAME = "dictionary";
    
    
    SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	String sql =
                "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY	 AUTOINCREMENT, " +
                FIELD_NAME + " TEXT NOT NULL, " +
                FIELD_ATRASOS + " INTEGER" +
                ");";
    	
        db.execSQL(sql);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Pesquisar o que fazer aqui
	}
	
	public void insert(String name, int atrasos) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_NAME, name);
		values.put(FIELD_ATRASOS, atrasos);

		db.insertOrThrow(TABLE_NAME, null, values);
	}
	
	public Cursor all(Activity activity) {
		  String[] from = { _ID, FIELD_NAME, FIELD_ATRASOS };
		  String order = FIELD_NAME;

		  SQLiteDatabase db = getReadableDatabase();
		  Cursor cursor = db.query(TABLE_NAME, from, null, null, null, null, order);
		  activity.startManagingCursor(cursor);

		  return cursor;
	}
}
