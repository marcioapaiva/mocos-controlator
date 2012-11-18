package com.marcioapf.mocos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends Activity{
	
	EditText etMateria, etAulasSemanais;
	Intent intent;
	Bundle bundle;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        setContentView(R.layout.edit);     
        
        etMateria = (EditText)findViewById(R.id.nome_materia);
        etAulasSemanais = (EditText)findViewById(R.id.maximo_atrasos);
        
        intent = getIntent();
        bundle = intent.getExtras();
        
        etMateria.setText(bundle.getString("strMateria"));
        etAulasSemanais.setText(bundle.getInt("maxAtrasos") + "");
        
		Button btnSave = (Button) findViewById(R.id.btn_save);
		
		btnSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				intent.putExtra("strMateria", etMateria.getText().toString());
				intent.putExtra("maxAtrasos", Integer.parseInt(etAulasSemanais.getText().toString()));
				setResult(RESULT_OK, intent);
				finish();
			}
		});
        
	}
	
	@Override
	public void onPause(){
		super.onPause();
		
	}
//	public void onDestroy(){
//		super.onDestroy();
//	}
}
