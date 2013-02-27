package com.marcioapf.mocos;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class SQLHelper extends SQLiteOpenHelper {

	
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "materias.db";
    
    public static final String _ID = BaseColumns._ID;
    private static final String FIELD_NAME = "FIELD_NAME";
    private static final String FIELD_ATRASOS = "FIELD_ATRASOS";
    private static final String FIELD_AULAS_SEMANAIS = "FIELD_AULAS_SEMANAIS";
    private static final String FIELD_CHECK_NEEDED = "FIELD_CHECK_NEEDED";
    private static final String FIELD_MEMOS1 = "FIELD_MEMOS1";
    private static final String FIELD_MEMOS2 = "FIELD_MEMOS2";
    private static final String FIELD_MEMOS3 = "FIELD_MEMOS3";
    
    private static final String TABLE_NAME = "dictionary";
    
    
    SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	String sql =
                "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FIELD_NAME + " TEXT NOT NULL, " +
                FIELD_ATRASOS + " INTEGER, " +
                FIELD_AULAS_SEMANAIS + " INTEGER, " +
                FIELD_CHECK_NEEDED + " INTEGER, " +
                FIELD_MEMOS1 + " TEXT, " +
                FIELD_MEMOS2 + " TEXT, " +
                FIELD_MEMOS3 + " TEXT" +
                ");";
    	
        db.execSQL(sql);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Pesquisar o que fazer aqui
	}
	
	public long insertAndID(MateriaData mData) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_NAME, mData.getStrNome());
		values.put(FIELD_ATRASOS, mData.getAtrasos());
		values.put(FIELD_AULAS_SEMANAIS, mData.getAulasSemanais());
		values.put(FIELD_CHECK_NEEDED, mData.checkNeeded);
		
		
		//values.put(FIELD_MEMOS1, atrasos);
		//values.put(FIELD_MEMOS2, atrasos);
		//values.put(FIELD_MEMOS3, atrasos);
		
		long id = db.insertOrThrow(TABLE_NAME, null, values);
		
		db.close();
		
		mData.setSqlID(id);
		
		return id;
	}
	
	public void update(MateriaData mData){
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(FIELD_NAME, mData.getStrNome());
		values.put(FIELD_ATRASOS, mData.getAtrasos());
		values.put(FIELD_AULAS_SEMANAIS, mData.getAulasSemanais());
		values.put(FIELD_CHECK_NEEDED, mData.checkNeeded);
		
		db.update(TABLE_NAME, values, _ID+"="+mData.getSqlID(), null);
		
		db.close();
	}
	
	public void remove(long id){
		SQLiteDatabase db = getWritableDatabase();
		
		//TODO: remove temp
		int temp = db.delete(TABLE_NAME, _ID+"="+id, null);
		Log.w("Database", "Tentativa de deletar: " + temp);
		
		db.close();
	}
	
	public Cursor all(Activity activity) {
		  String[] from = { _ID, FIELD_NAME, FIELD_ATRASOS };
		  String order = FIELD_NAME;

		  SQLiteDatabase db = getReadableDatabase();
		  Cursor cursor = db.query(TABLE_NAME, from, null, null, null, null, order);
		  //activity.startManagingCursor(cursor);
		  
		  return cursor;
	}
}
