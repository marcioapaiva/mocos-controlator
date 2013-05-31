package com.marcioapf.mocos.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class SQLHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "materias.db";

	private static final String _ID = BaseColumns._ID;
    private static final String FIELD_NAME = "FIELD_NAME";
    private static final String FIELD_PROFESSOR_NAME = "FIELD_PROFESSOR_NAME";
	private static final String FIELD_DELAYS = "FIELD_ATRASOS";
	private static final String FIELD_WEEKLY_CLASSES = "FIELD_AULAS_SEMANAIS";
	private static final String FIELD_CHECK_NEEDED = "FIELD_CHECK_NEEDED";
	private static final String FIELD_MEMOS1 = "FIELD_MEMOS1";
	private static final String FIELD_MEMOS2 = "FIELD_MEMOS2";
	private static final String FIELD_MEMOS3 = "FIELD_MEMOS3";

	private static final String TABLE_NAME = "mocosTable";

	public SQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql =
				"CREATE TABLE " + TABLE_NAME + " (" +
						_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FIELD_NAME + " TEXT NOT NULL, " +
                        FIELD_PROFESSOR_NAME + " TEXT, " +
                        FIELD_DELAYS + " INTEGER, " +
                        FIELD_WEEKLY_CLASSES + " INTEGER, " +
						FIELD_CHECK_NEEDED + " INTEGER, " +
						FIELD_MEMOS1 + " TEXT, " +
						FIELD_MEMOS2 + " TEXT, " +
						FIELD_MEMOS3 + " TEXT" +
						");";

		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME +
                       " ADD COLUMN " + FIELD_PROFESSOR_NAME + " TEXT");
        }
	}

	public long insertAndID(SubjectData mData) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
        values.put(FIELD_NAME, mData.getName());
        values.put(FIELD_PROFESSOR_NAME, mData.getProfessorName());
		values.put(FIELD_DELAYS, mData.getDelays());
		values.put(FIELD_WEEKLY_CLASSES, mData.getWeeklyClasses());
		values.put(FIELD_CHECK_NEEDED, mData.isCheckNeeded());

		//values.put(FIELD_MEMOS1, atrasos);
		//values.put(FIELD_MEMOS2, atrasos);
		//values.put(FIELD_MEMOS3, atrasos);

		long id = db.insertOrThrow(TABLE_NAME, null, values);

		db.close();
		mData.setSqlID(id);

		return id;
	}

	public void update(SubjectData mData){
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_NAME, mData.getName());
        values.put(FIELD_PROFESSOR_NAME, mData.getProfessorName());
		values.put(FIELD_DELAYS, mData.getDelays());
		values.put(FIELD_WEEKLY_CLASSES, mData.getWeeklyClasses());
		values.put(FIELD_CHECK_NEEDED, mData.isCheckNeeded());

		db.update(TABLE_NAME, values, _ID+"="+mData.getSqlID(), null);

		db.close();
	}

	public void remove(long id){
		SQLiteDatabase db = getWritableDatabase();

		//TODO: remove temp
		int temp = db.delete(TABLE_NAME, _ID+"="+id, null);

		db.close();
	}
	public SubjectData retrieveMateriaDataById(long id){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, _ID+"="+id, null, null, null, _ID);

		cursor.moveToNext();
        SubjectData mdaux = subjectDataFromCursor(cursor);

		db.close();
		return mdaux;
	}

	public ArrayList<SubjectData> retrieveAllMateriaData() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, _ID);

		ArrayList<SubjectData> subjectDataList = new ArrayList<SubjectData>();
		while (cursor.moveToNext())
			subjectDataList.add(subjectDataFromCursor(cursor));

		db.close();
		return subjectDataList;
	}

	public SubjectMemo retrieveMateriaMemosByID(long id){
		String[] from = {FIELD_MEMOS1, FIELD_MEMOS2, FIELD_MEMOS3};

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, from, _ID+"="+id, null, null, null, null);

		String str1, str2, str3;

		//Deve haver exatamente um resultado.
		//TODO: Verificar isso e emitir uma exceção caso não ocorra

		cursor.moveToNext();
		str1 = cursor.getString(0);
		str2 = cursor.getString(1);
		str3 = cursor.getString(2);

		db.close();
		return new SubjectMemo(str1, str2, str3, id);
	}

	public void updateMemos(SubjectMemo mMemos) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_MEMOS1, mMemos.get1stBimSum());
		values.put(FIELD_MEMOS2, mMemos.get2ndBimSum());
		values.put(FIELD_MEMOS3, mMemos.getExam());

		db.update(TABLE_NAME, values, _ID+"="+mMemos.getSqlID(), null);

		db.close();
	}

    private static SubjectData subjectDataFromCursor(Cursor cursor) {
        SubjectData data = new SubjectData();

        data.setSqlID(cursor.getInt(cursor.getColumnIndex(_ID)));
        data.setName(cursor.getString(cursor.getColumnIndex(FIELD_NAME)));
        data.setProfessorName(cursor.getString(cursor.getColumnIndex(FIELD_PROFESSOR_NAME)));
        data.setDelays(cursor.getInt(cursor.getColumnIndex(FIELD_DELAYS)));
        data.setWeeklyClasses(cursor.getInt(cursor.getColumnIndex(FIELD_WEEKLY_CLASSES)));
        data.setCheckNeeded(cursor.getInt(cursor.getColumnIndex(FIELD_CHECK_NEEDED)) != 0);

        return data;
    }
}
