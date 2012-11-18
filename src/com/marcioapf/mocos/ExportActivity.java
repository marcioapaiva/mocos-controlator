package com.marcioapf.mocos;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ExportActivity extends Activity implements OnClickListener{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.export_activity);
		
		EditText etFilePath = (EditText) findViewById(R.id.etExportFilePath);
		etFilePath.setText(DatabaseDealer.getDefaultImportExportPath());
		
		
		((Button) findViewById(R.id.btn_export)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_import)).setOnClickListener(this);
	}

	public void onClick(View v) {
		DatabaseDealer dbDealer = new DatabaseDealer();
		switch(v.getId()){
		case R.id.btn_export:
			try {
				dbDealer.dataExport(dbDealer.restoreData());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case R.id.btn_import:
			try {
				dbDealer.dataImport();
				setResult(WelcomeActivity.ACTIVITY_RESULT_IMPORT_MADE);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//SharedPreferences table = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
	        //Log.e("there we go", table.getInt("numMaterias", 0) + " ");
			break;
		}
			
	}
}
