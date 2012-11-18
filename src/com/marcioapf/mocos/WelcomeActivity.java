package com.marcioapf.mocos;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
	private DatabaseDealer dbDealer;
	public static SharedPreferences sharedPrefTable;
    
	public static final int ACTIVITY_REQUEST_EDIT = 1;
	public static final int ACTIVITY_REQUEST_IMPORT_EXPORT = 2;
	public static final int ACTIVITY_RESULT_IMPORT_MADE = 2;
	public static final String PREFS_NAME = "MyPrefsFile";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	
    	sharedPrefTable = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

    	
        setContentView(R.layout.main);        
        
//        llPrincipal = (LinearLayout) findViewById(R.id.llPrincipal); 
        llMaterias = (LinearLayout) findViewById(R.id.llmaterias);        
        tvFaltasTotais = (TextView) findViewById(R.id.tvFaltasTotais);
        btnAdicionar = (Button) findViewById(R.id.btnNovaMateria);
        btnExportar = (Button) findViewById(R.id.btnImportarExportar);
        
		btnAdicionar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LinMateria aux = new LinMateria(WelcomeActivity.this, "Nova", 4, true);
				arrLinMaterias.add(aux);
				llMaterias.addView(aux, arrLinMaterias.size());
				updateTotal();
			}
		});
		
		btnExportar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(WelcomeActivity.this, ExportActivity.class);
				startActivityForResult(intent, ACTIVITY_REQUEST_IMPORT_EXPORT);
			}
		});
		
    	
        //Lê as matérias a partir do SharedPreferences,
        //cria uma LinMateria para cada uma delas,
        //e as armazena em arrLinMaterias.
        //Além disso, adiciona as LinMaterias ao LinearLayout
    	dbDealer= new DatabaseDealer();
    	arrLinMaterias = new ArrayList<LinMateria>();
    	
    	ArrayList<MateriaData> materiasData = dbDealer.restoreData();
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
    	
//    	for (int i=0; i<materias.size();i++){
//    		llmaterias.removeViewAt(1);
//    	}
//    	
//    	materias = new ArrayList<LinMateria>();
//    	ArrayList<MateriaData> materiasData = dbDealer.restoreData();
//    	
//    	for (MateriaData mData : materiasData){
//    		materias.add(new LinMateria(this, mData));
//    	}
//    	
//        for (int i=0; i< materias.size(); i++)
//	        llmaterias.addView(materias.get(i), i+1);
// 
////    	
//    	for (LinMateria lm : arrLinMaterias)
//    		lm.update();
//
//    	updateTotal();
    }
    
    protected void onPause() {
    	super.onPause();
    	//Retirar somente a data de cada matéria
    	ArrayList<MateriaData> materiasData = new ArrayList<MateriaData>();
    	for (LinMateria lm : arrLinMaterias){
    		materiasData.add(lm.getData());
    	}
    	dbDealer.save(materiasData);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        //System.out.println("Texto selecionado: " + ((TextView)v).getText());
        for (LinMateria lm : arrLinMaterias){
        	if(lm.linMatTop.tvMateria == v) {
        		selected = lm;
        		//System.out.println("Selected: " + lm.getStrNome() + " " + lm.getAulasSemanais());
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
            	updateTotal();
                return true;
            case R.id.check:
            	selected.setCheckNeeded(true);
            	selected.update();
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
	    		}
	    		break;
	    	case ACTIVITY_REQUEST_IMPORT_EXPORT:
	    		if(resultCode == ACTIVITY_RESULT_IMPORT_MADE){
	    			//Neste caso, precisamos reconstruir todos os LinMaterias, 
	    			//pois foi realizada uma importação de novos dados
	    			
	    			//Deletando as views originais do linearlayout
	    			llMaterias.removeAllViews();
	    			
	    			arrLinMaterias = new ArrayList<LinMateria>();
	    	    	
	    	    	ArrayList<MateriaData> materiasData = dbDealer.restoreData();
	    	    	for (MateriaData mData : materiasData){
	    	    		arrLinMaterias.add(new LinMateria(this, mData));
	    	    	}
	    	    	
	    	        for (LinMateria lm : arrLinMaterias)
	    		        llMaterias.addView(lm);
	    	        
	    	        //Atualiza a contagem do total de faltas
	    	        updateTotal();
	    		}
    	}

    }

}



