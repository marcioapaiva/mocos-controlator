package com.marcioapf.mocos;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class NotasActivity extends Activity {
	
	int TEMP = 10;
	EditText et1bim, et2bim, etExame;
	MateriaMemos mMemos;
	DatabaseDealer dbDealer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notas);
		
		et1bim = (EditText)findViewById(R.id.et1bim);
		et2bim = (EditText)findViewById(R.id.et2bim);
		etExame = (EditText)findViewById(R.id.etExame);
		
		dbDealer = new DatabaseDealer();
		mMemos = dbDealer.getMemos(TEMP);
		
		et1bim.setText(mMemos.getS1bim());
		et2bim.setText(mMemos.getS2bim());
		etExame.setText(mMemos.getsExame());
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		DatabaseDealer dbDealer = new DatabaseDealer();
		mMemos.setAll(et1bim.getText().toString(), et2bim.getText().toString(),
							etExame.getText().toString());
		dbDealer.addMemos(mMemos, TEMP);
	}

}