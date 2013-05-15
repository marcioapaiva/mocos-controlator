package com.marcioapf.mocos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.marcioapf.mocos.animation.AnimatorCreationUtil;
import com.marcioapf.mocos.data.MateriaData;
import com.marcioapf.mocos.data.SQLHelper;
import com.marcioapf.mocos.view.LinMateria;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public class AbsenceActivity extends Activity {

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
				final LinMateria materia = new LinMateria(AbsenceActivity.this, "Nova", 4, false);

                final ObjectAnimator btnAnimator = ObjectAnimator.ofFloat(btnAdicionar, "alpha", 0);
                btnAnimator.setDuration(500);
                btnAnimator.start();
                createEditSubjectDialog(materia, new Runnable() {
				    @Override
				    public void run() {
				        arrLinMaterias.add(materia);
				        llMaterias.addView(materia);
				        sqlHelper.insertAndID(materia.getData());

                        ViewHelper.setTranslationX(materia, -llMaterias.getWidth());
                        materia.animateToDelayed(0, 0, 700);

                        btnAnimator.setFloatValues(1);
                        btnAnimator.setStartDelay(1100);
                        btnAnimator.start();
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

    public void updateTotal() {
    	totalAulasSemanais = 0;
    	totalAtrasos = 0;

    	for (LinMateria materia : arrLinMaterias){
    		totalAulasSemanais += materia.getAulasSemanais();
    		totalAtrasos += materia.getAtrasos();
    	}

    	if((int)(2*Math.ceil((float)0.10f*16*totalAulasSemanais)) - totalAtrasos <= 0.2f*(int)Math.ceil((float)0.10f*16*totalAulasSemanais)) {
            if (tvFaltasTotais.getCurrentTextColor() != Color.RED){
    		    AnimatorCreationUtil.ofTextColor(tvFaltasTotais, 300, Color.RED).start();
            }
    	}
    	else if (tvFaltasTotais.getCurrentTextColor() != Color.DKGRAY) {
            AnimatorCreationUtil.ofTextColor(tvFaltasTotais, 300, Color.DKGRAY).start();
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AbsenceActivity.super.onBackPressed();
            }
        }, animateAllTo(llMaterias.getWidth(), 0));
    }

    /**
     * Animates all the cards displayed to a certain position on X. The total skipped classes text
     * view and the add subject button will be animated to the provided alpha as well.
     * @param cardsTranslateX the translation x position where to move the cards to
     * @param tvAndBtntAlpha the alpha to set the text view and add subject button to
     * @return the estimated duration of the whole animation
     */
    private long animateAllTo(final float cardsTranslateX, float tvAndBtntAlpha) {
        final long betweenCardsDelay = 80;
        int size = arrLinMaterias.size();

        for (int i = 0; i < size; i++)
            arrLinMaterias.get(i).animateToDelayed(cardsTranslateX, 0, betweenCardsDelay * i);

        long halfDuration = (betweenCardsDelay * size) / 2 + 120;
        Animator anmtr = AnimatorCreationUtil.ofFloat(btnAdicionar, "alpha", halfDuration, null,
            tvAndBtntAlpha);
        anmtr.setStartDelay(halfDuration);
        anmtr.start();

        anmtr = AnimatorCreationUtil.ofFloat(tvFaltasTotais, "alpha", halfDuration, null, tvAndBtntAlpha);
        anmtr.setStartDelay(halfDuration);
        anmtr.start();

        return 2 * halfDuration;
    }

    public void animateSubjectOut(final LinMateria materia) {
        final List<View> toBeAnimated = new ArrayList<View>();
        boolean after = false;
        for (LinMateria mtr : arrLinMaterias) {
            if (after)
                toBeAnimated.add(mtr);
            if (mtr == materia)
                after = true;
        }
        toBeAnimated.add(btnAdicionar);
        final int numberToBeAnimated = toBeAnimated.size();

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
                float value = (Float) animation.getAnimatedValue();
                for (int i = 0; i < numberToBeAnimated; i++)
                    ViewHelper.setTranslationY(toBeAnimated.get(i), value);
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
                for (View mtr : toBeAnimated)
                    ViewHelper.setTranslationY(mtr, 0);
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

        if (ViewHelper.getTranslationX(materia) == 0)
            materia.animateTo(llMaterias.getWidth(), 0);
        listAnimator.setStartDelay(500);
        listAnimator.start();
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



