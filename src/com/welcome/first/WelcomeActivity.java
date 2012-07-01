package com.welcome.first;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
	
	LinearLayout llmaterias;
	ArrayList<LinMateria> materias;
	LinMateria selected = null;
	TextView tvFaltasTotais;
    int totalAulasSemanais = 0;
    int totalAtrasos = 0;
    
	public static final String PREFS_NAME = "MyPrefsFile";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//TODO: testes:
    	this.getActionBar().setDisplayShowHomeEnabled(false);
    	this.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        
        llmaterias = (LinearLayout) findViewById(R.id.llmaterias);        
        tvFaltasTotais = (TextView) findViewById(R.id.tvFaltasTotais);
        materias = new ArrayList<LinMateria>();
        
        SharedPreferences table = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int numMaterias = table.getInt("numMaterias", 0);
        
        
        String nome;
        int aulasSemanais;
        int atrasos;
        boolean checkNeeded;

        
        
        for (int i=0; i< numMaterias; i++){
        	nome = table.getString("nomemateria"+i, "erro");
        	aulasSemanais = table.getInt("maxatrasosmateria"+i, 90);
        	atrasos = table.getInt("atrasosmateria"+i, 0);
        	checkNeeded = table.getBoolean("checkneededmateria"+i, false);
        	
        	materias.add(i, new LinMateria(this, nome, aulasSemanais, checkNeeded));
        	materias.get(i).setAtrasos(atrasos);
        	materias.get(i).update();
        }
        
        update();
        
        
        for (int i=0; i< materias.size(); i++)
	        llmaterias.addView(materias.get(i), i+1);
        
        Button tempAdicionar = new Button(this);
        tempAdicionar.setText("Nova Matéria");
        
        llmaterias.addView(tempAdicionar);
        
        
        final Context contextremovelater= this;
        
		tempAdicionar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LinMateria aux = new LinMateria(contextremovelater, "Nova", 4, true);
				materias.add(aux);
				llmaterias.addView(aux, materias.size());
			}
		});
    }
    
    protected void update() {
    	totalAulasSemanais = 0;
    	totalAtrasos = 0;
    	
    	for (LinMateria materia : materias){
    		totalAulasSemanais += materia.aulasSemanais;
    		totalAtrasos += materia.atrasos;
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
    	//
        //btn.setText(txtViewTitle.getText());
    	
        //tv.setText("Hello " + tv.getText());
    }
    protected void onPause() {
    	super.onPause();
        SharedPreferences table = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = table.edit();
        editor.putInt("numMaterias", materias.size());
        for (int i=0; i<materias.size(); i++){
        	editor.putString("nomemateria"+i, materias.get(i).strMateria);
        	editor.putInt("maxatrasosmateria"+i, materias.get(i).aulasSemanais);
        	editor.putInt("atrasosmateria"+i, materias.get(i).atrasos);
        	editor.putBoolean("checkneededmateria"+i, materias.get(i).checkNeeded);
        }
        editor.commit();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        for (LinMateria lm : materias){
        	if(lm.strMateria == ((TextView)v).getText()) {
        		selected = lm;
        		break;
        	}
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:            	
            	try{
            		Intent intent = new Intent(this, Class.forName("com.welcome.first.EditActivity"));
            		intent.putExtra("strMateria", selected.strMateria);
            		intent.putExtra("maxAtrasos", selected.aulasSemanais);
            		startActivityForResult(intent, 1);
            	}
            	catch(ClassNotFoundException e){
            	}
                return true;
            case R.id.remove:
            	materias.remove(selected);
            	llmaterias.removeView(selected);
            	update();
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
//    	System.out.println("enter");
    	if(requestCode == 1){
    		if(resultCode == RESULT_OK){
    			selected.strMateria = data.getExtras().getString("strMateria");
    			selected.aulasSemanais = data.getExtras().getInt("maxAtrasos");
    			selected.update();
    			update();
    		}
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
}



