package com.marcioapf.mocos;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
	public static final String PREFS_NAME = "MyPrefsFile";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	sharedPrefTable = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    	sqlHelper = new SQLHelper(this);

        setContentView(R.layout.main);

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
    		tvFaltasTotais.setTextColor(Color.DKGRAY);
    	}

    	tvFaltasTotais.setText("Total: " + (float)totalAtrasos/2 + "/" + ((int)Math.ceil((float)0.10f*16*totalAulasSemanais)));
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
        	if(lm.getMateriaTextView() == v) {
        		selected = lm;
        		break;
        	}
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                createEditSubjectDialog().show();
                return true;
            case R.id.remove:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final LinMateria currentSelected = selected;
                builder.setTitle("Remover")
                    .setMessage("Tem certeza que deseja remover \"" + selected.getStrNome() + "\"?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            arrLinMaterias.remove(currentSelected);
                            llMaterias.removeView(currentSelected);
                            sqlHelper.remove(currentSelected.getData().getSqlID());
                            updateTotal();
                        }
                    })
                    .setNegativeButton("Não", null);
                builder.show();
                return true;
            case R.id.check:
                selected.setCheckNeeded(true);
                selected.update();
                return true;
            case R.id.notas:
                Intent intent = new Intent(this, NotasActivity.class);
                intent.putExtra("MateriaID", selected.getData().getSqlID());
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private AlertDialog createEditSubjectDialog() {
        View dialogContent = View.inflate(this, R.layout.edit_dialog, null);
        final TextView etNomeMateria = (TextView) dialogContent
                .findViewById(R.id.nome_materia);
        final TextView etAulasSemanais = (TextView) dialogContent
                .findViewById(R.id.maximo_atrasos);
        final LinMateria currentSelect = selected;

        etNomeMateria.setText(currentSelect.getStrNome());
        etAulasSemanais.setText(Integer.toString(currentSelect.getAulasSemanais()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Edição de Matéria")
            .setView(dialogContent)
            .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    currentSelect.setStrNome(etNomeMateria.getText().toString());
                    currentSelect.setAulasSemanais(
                            Integer.parseInt(etAulasSemanais.getText().toString()));
                    currentSelect.update();
                    sqlHelper.update(currentSelect.getData());
                }
            })
            .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                etNomeMateria.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etNomeMateria, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        return dialog;
    }
}



