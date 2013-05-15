package com.marcioapf.mocos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.marcioapf.mocos.data.MateriaMemos;
import com.marcioapf.mocos.data.SQLHelper;

public class NotasActivity extends Activity {
	
	long materiaID = 10;
	EditText et1bim, et2bim, etExame;
	TextView tvNomeMateria;
	MateriaMemos mMemos;
	SQLHelper sqlHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notas);
		
		
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		materiaID = bundle.getLong("MateriaID", -1);
		
		sqlHelper = new SQLHelper(this);
		
		tvNomeMateria = (TextView)findViewById(R.id.tvNomeMateria);
		et1bim = (EditText)findViewById(R.id.et1bim);
		et2bim = (EditText)findViewById(R.id.et2bim);
		etExame = (EditText)findViewById(R.id.etExame);
		
		mMemos = sqlHelper.retrieveMateriaMemosByID(materiaID);
		
		String strNomeMateria = sqlHelper.retrieveMateriaDataById(materiaID).getStrNome();
		tvNomeMateria.setText(strNomeMateria + " - Notas");
		et1bim.setText(mMemos.getS1bim());
		et2bim.setText(mMemos.getS2bim());
		etExame.setText(mMemos.getSExame());
	}
	
	@Override
	protected void onPause(){
		super.onPause();

		mMemos.setAll(et1bim.getText().toString(), et2bim.getText().toString(),
							etExame.getText().toString());
		sqlHelper.updateMemos(mMemos);
	}

}