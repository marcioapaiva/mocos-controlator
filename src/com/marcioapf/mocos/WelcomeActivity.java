package com.marcioapf.mocos;

import java.util.ArrayList;
import java.util.List;

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
import android.view.animation.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.nineoldandroids.animation.*;
import com.nineoldandroids.util.Property;
import com.nineoldandroids.view.ViewHelper;

public class WelcomeActivity extends Activity {

    ScrollView scrollView;
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

        scrollView = (ScrollView) findViewById(R.id.scrllvwNo1);
        llMaterias = (LinearLayout) findViewById(R.id.llmaterias);
        tvFaltasTotais = (TextView) findViewById(R.id.tvFaltasTotais);
        btnAdicionar = (Button) findViewById(R.id.btnNovaMateria);

		btnAdicionar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final LinMateria materia = new LinMateria(WelcomeActivity.this, "Nova", 4, false);

                final ObjectAnimator btnAnimator = ObjectAnimator.ofFloat(btnAdicionar, "alpha", 0);
                btnAnimator.setDuration(500);
                btnAnimator.start();
                createEditSubjectDialog(materia, new Runnable() {
				    @Override
				    public void run() {
				        arrLinMaterias.add(materia);
				        llMaterias.addView(materia);
				        sqlHelper.insertAndID(materia.getData());
                        int initTranslation = - getWindowManager().getDefaultDisplay().getWidth();
                        ViewHelper.setTranslationX(materia, initTranslation);
                        Animator mtrAnimator = ObjectAnimator.ofFloat(materia, "translationX", 0);
                        mtrAnimator.setDuration(800);
                        mtrAnimator.setInterpolator(new DecelerateInterpolator(3.2f));
                        btnAnimator.setFloatValues(1);
                        
                        AnimatorSet set = new AnimatorSet();
                        set.play(mtrAnimator).after(700);
                        set.play(btnAnimator).after(1100);
                        set.start();
				    }
				}, new Runnable() {
                        @Override
                        public void run() {
                            btnAnimator.setFloatValues(1);
                            btnAnimator.start();
                        }
                    }).show();
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
        selected = (LinMateria) v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final LinMateria currentSelected = selected;
        switch (item.getItemId()) {
            case R.id.edit:
                createEditSubjectDialog(currentSelected, new Runnable() {
                    @Override
                    public void run() {
                        sqlHelper.update(currentSelected.getData());
                    }
                }).show();
                return true;
            case R.id.remove:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Remover")
                    .setMessage("Tem certeza que deseja remover \"" + selected.getStrNome() + "\"?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            animateSubjectOut(currentSelected);
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

    @Override
    public void onBackPressed() {
        AnimatorSet animatorSet = animateAll(0, getWindowManager().getDefaultDisplay().getWidth());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateAll(1, -getWindowManager().getDefaultDisplay().getWidth(), 0).start();
            }
        });
        animatorSet.start();
    }

    private AnimatorSet animateAll(float alpha, float... translationX) {
        AnimatorSet animatorSet = new AnimatorSet();
        Interpolator interpolator = new DecelerateInterpolator();
        int size = arrLinMaterias.size();
        for (int i = 0; i < size; i++) {
            Animator anmtr = ObjectAnimator.ofFloat(arrLinMaterias.get(i), "translationX", translationX);
            anmtr.setInterpolator(interpolator);
            anmtr.setDuration(200);
            animatorSet.play(anmtr).after(80 * i);
        }
        Animator anmtr = ObjectAnimator.ofFloat(btnAdicionar, "alpha", alpha);
        anmtr.setDuration(100 + 40 * size);
        animatorSet.play(anmtr);
        anmtr = ObjectAnimator.ofFloat(tvFaltasTotais, "alpha", alpha);
        anmtr.setDuration(100 + 40 * size);
        animatorSet.play(anmtr);

        return animatorSet;
    }

    private void animateSubjectOut(final LinMateria materia) {
        final List<LinMateria> toBeAnimated = new ArrayList<LinMateria>();
        boolean after = false;
        for (LinMateria mtr : arrLinMaterias) {
            if (after)
                toBeAnimated.add(mtr);
            if (mtr == materia)
                after = true;
        }

        float finalTranslate = getWindowManager().getDefaultDisplay().getWidth();
        ObjectAnimator removedAnimator = ObjectAnimator.ofFloat(materia,
            "translationX", finalTranslate);
        removedAnimator.setDuration(500);
        removedAnimator.setInterpolator(new DecelerateInterpolator(1.2f));

        int maxScroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight(),
            materiaHeight = materia.getHeight();
        final int initScroll = scrollView.getScrollY(),
                  scrollBy = ((maxScroll < initScroll + materiaHeight) ?
                      Math.max(maxScroll - materiaHeight, 0) : initScroll) - initScroll;
        ValueAnimator listAnimator = ValueAnimator.ofFloat(0, -materiaHeight);
        listAnimator.setInterpolator(new DecelerateInterpolator(3.2f));
        listAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                int size = toBeAnimated.size();
                for (int i = 0; i < size; i++)
                    ViewHelper.setTranslationY(toBeAnimated.get(i), value);
                ViewHelper.setTranslationY(btnAdicionar, value);
                ViewHelper.setScrollY(scrollView,
                    (int) (initScroll + scrollBy * animation.getAnimatedFraction()));
            }
        });
        listAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                arrLinMaterias.remove(materia);
                llMaterias.removeView(materia);
                sqlHelper.remove(materia.getData().getSqlID());
                updateTotal();
                scrollView.setVerticalScrollBarEnabled(true);
                scrollView.setOnTouchListener(null);
                for (LinMateria mtr : toBeAnimated)
                    ViewHelper.setTranslationY(mtr, 0);
                ViewHelper.setTranslationY(btnAdicionar, 0);
            }
        });
        listAnimator.setDuration(700);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        }); //disable user scrolling during the animation

        AnimatorSet set = new AnimatorSet();
        set.play(listAnimator).after(removedAnimator);
        set.start();
    }

    private AlertDialog createEditSubjectDialog(LinMateria materia, Runnable success) {
        return createEditSubjectDialog(materia, success, null);
    }

    private AlertDialog createEditSubjectDialog(final LinMateria materia,
                                                final Runnable success,
                                                final Runnable failure) {
        View dialogContent = View.inflate(this, R.layout.edit_dialog, null);
        final TextView etNomeMateria = (TextView) dialogContent
                .findViewById(R.id.nome_materia);
        final TextView etAulasSemanais = (TextView) dialogContent
                .findViewById(R.id.maximo_atrasos);

        etNomeMateria.setText(materia.getStrNome());
        etAulasSemanais.setText(Integer.toString(materia.getAulasSemanais()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Edição de Matéria")
            .setView(dialogContent)
            .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    materia.setStrNome(etNomeMateria.getText().toString());
                    materia.setAulasSemanais(
                        Integer.parseInt(etAulasSemanais.getText().toString()));
                    materia.update();
                    if (success != null)
                        success.run();
                }
            })
            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (failure != null)
                        failure.run();
                }
            });

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (failure != null)
                    failure.run();
            }
        });
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



