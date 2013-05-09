package com.marcioapf.mocos;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WelcomeActivity extends Activity {
	
	LinearLayout llMaterias;//, llPrincipal;
	Button btnAdicionar, btnExportar;
	ArrayList<LinMateria> arrLinMaterias;
	LinMateria selected = null;
	TextView tvFaltasTotais;
    int totalAulasSemanais = 0;
    int totalAtrasos = 0;
	public static SharedPreferences sharedPrefTable;
	SQLHelper sqlHelper;
    
	public static final int ACTIVITY_REQUEST_EDIT = 1;
	public static final int ACTIVITY_REQUEST_IMPORT_EXPORT = 2;
	public static final int ACTIVITY_RESULT_IMPORT_MADE = 2;
	public static final int ACTIVITY_REQUEST_SETTINGS = 2;
	public static final String PREFS_NAME = "MyPrefsFile";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	
    	sharedPrefTable = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    	sqlHelper = new SQLHelper(this);
    	
        setContentView(R.layout.welcome_activity);        
        
        llMaterias = (LinearLayout) findViewById(R.id.llmaterias);
        tvFaltasTotais = (TextView) findViewById(R.id.tvFaltasTotais);
        btnAdicionar = (Button) findViewById(R.id.btnNovaMateria);
        
		btnAdicionar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LinMateria aux = new LinMateria(WelcomeActivity.this, "Nova", 4, false);
				arrLinMaterias.add(aux);
				llMaterias.addView(aux);
				sqlHelper.insertAndID(aux.getData());
            	try{
            		Intent intent = new Intent(WelcomeActivity.this, Class.forName("com.marcioapf.mocos.EditActivity"));
            		intent.putExtra("strMateria", aux.getStrNome());
            		intent.putExtra("maxAtrasos", aux.getAulasSemanais());
            		startActivityForResult(intent, ACTIVITY_REQUEST_EDIT);
            	}
            	catch(ClassNotFoundException e){
            		e.printStackTrace();
            	}
				updateTotal();
			}
		});
    	
		
    	arrLinMaterias = new ArrayList<LinMateria>();
    	
    	ArrayList<MateriaData> materiasData = sqlHelper.retrieveAllMateriaData();
    	for (MateriaData mData : materiasData){
    		arrLinMaterias.add(new LinMateria(this, mData));
    	}
    	
        for (LinMateria lm : arrLinMaterias)
	        llMaterias.addView(lm);
        
        //Atualiza a contagem do total de faltas
        updateTotal();
		
    }
    
    protected void updateTotal() {
    	totalAulasSemanais = 0;
    	totalAtrasos = 0;
    	
    	for (LinMateria materia : arrLinMaterias){
    		totalAulasSemanais += materia.getAulasSemanais();
    		totalAtrasos += materia.getAtrasos();
    	}
    	
    	if((int)(2*Math.ceil((float)0.10f*16*totalAulasSemanais)) - totalAtrasos <= 0.2f*(int)Math.ceil((float)0.10f*16*totalAulasSemanais)){
    		tvFaltasTotais.setTextColor(Color.RED);
    	}
    	else {
    		tvFaltasTotais.setTextColor(Color.LTGRAY);
    	}
    		
    	tvFaltasTotais.setText((float)totalAtrasos/2 + "/" + ((int)Math.ceil((float)0.10f*16*totalAulasSemanais)));
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	
    }
    
    protected void onPause() {
    	super.onPause();
    	//Retirar somente a data de cada matéria
    	ArrayList<MateriaData> materiasData = new ArrayList<MateriaData>();
    	for (LinMateria lm : arrLinMaterias){
    		materiasData.add(lm.getData());
    		sqlHelper.update(lm.getData());
    	}
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        for (LinMateria lm : arrLinMaterias){
        	if(lm.linMatTop.tvMateria == v) {
        		selected = lm;
        		break;
        	}
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:            	
            	try{
            		Intent intent = new Intent(this, Class.forName("com.marcioapf.mocos.EditActivity"));
            		intent.putExtra("strMateria", selected.getStrNome());
            		intent.putExtra("maxAtrasos", selected.getAulasSemanais());
            		startActivityForResult(intent, ACTIVITY_REQUEST_EDIT);
            	}
            	catch(ClassNotFoundException e){
            		e.printStackTrace();
            	}
                return true;
            case R.id.remove:
            	arrLinMaterias.remove(selected);
            	llMaterias.removeView(selected);
            	sqlHelper.remove(selected.getData().getSqlID());
            	updateTotal();
                return true;
            case R.id.check:
            	selected.setCheckNeeded(true);
            	selected.update();
            	return true;
            case R.id.notas:
				try {
					Intent intent = new Intent(this, Class.forName("com.marcioapf.mocos.NotasActivity"));
					intent.putExtra("MateriaID", selected.getData().getSqlID());
					startActivity(intent);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
            	return true;
            default:
            	return super.onContextItemSelected(item);
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch(requestCode){	
	    	case ACTIVITY_REQUEST_EDIT:
	    		if(resultCode == RESULT_OK){
	    			selected.setStrNome(data.getExtras().getString("strMateria"));
	    			selected.setAulasSemanais(data.getExtras().getInt("maxAtrasos"));
	    			selected.update();
	    			sqlHelper.update(selected.getData());
	    		}
	    		break;
    	}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.options_menu, menu);
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    	case R.id.dropbox_sync:
    		dropboxSync();
    		return true;
    	case R.id.settings:
    		openSettingsActivity();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

	private void dropboxSync() {
		// TODO Auto-generated method stub
		Log.w("dropboxSync()", "Method was called, but isn't implemented yet");
		
	}

	private void openSettingsActivity() {
		Intent intent;
		try {
			intent = new Intent(this, Class.forName("com.marcioapf.mocos.SettingsActivity"));
			startActivityForResult(intent, ACTIVITY_REQUEST_SETTINGS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}



