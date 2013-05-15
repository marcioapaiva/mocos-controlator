package com.marcioapf.mocos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

    private ScrollView mScrollView;
    private LinearLayout mLlMaterias;
    private Button mBtnAdicionar;
    private LinMateria mSelected = null;
    private TextView mTvFaltasTotais;

    private ArrayList<LinMateria> mLinMaterias;

    private int mTotalAulasSemanais = 0;
    private int mTotalAtrasos = 0;

    private SQLHelper mSqlHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	mSqlHelper = new SQLHelper(this);

        setContentView(R.layout.main);

        mScrollView = (ScrollView) findViewById(R.id.scrllvwNo1);
        mLlMaterias = (LinearLayout) findViewById(R.id.llmaterias);
        mTvFaltasTotais = (TextView) findViewById(R.id.tvFaltasTotais);
        mBtnAdicionar = (Button) findViewById(R.id.btnNovaMateria);

		mBtnAdicionar.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final LinMateria materia = new LinMateria(AbsenceActivity.this, "Nova", 4, false);

                final ObjectAnimator btAnimator = ObjectAnimator.ofFloat(mBtnAdicionar, "alpha", 0);
                btAnimator.setDuration(500);
                btAnimator.start();
                createEditSubjectDialog(materia, new Runnable() {
                        @Override
                        public void run() {
                            mLinMaterias.add(materia);
                            mLlMaterias.addView(materia);
                            mSqlHelper.insertAndID(materia.getData());

                            ViewHelper.setTranslationX(materia, -mLlMaterias.getWidth());
                            materia.animateToDelayed(0, 0, 700);

                            btAnimator.setFloatValues(1);
                            btAnimator.setStartDelay(1100);
                            btAnimator.start();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            btAnimator.setFloatValues(1);
                            btAnimator.start();
                        }
                    }
                ).show();
                updateTotal();
            }
        });


    	mLinMaterias = new ArrayList<LinMateria>();

    	ArrayList<MateriaData> materiasData = mSqlHelper.retrieveAllMateriaData();
    	for (MateriaData mData : materiasData){
    		mLinMaterias.add(new LinMateria(this, mData));
    	}

        for (LinMateria lm : mLinMaterias)
	        mLlMaterias.addView(lm);

        //Atualiza a contagem do total de faltas
        updateTotal();
    }

    public void updateTotal() {
    	mTotalAulasSemanais = 0;
    	mTotalAtrasos = 0;

    	for (LinMateria materia : mLinMaterias){
    		mTotalAulasSemanais += materia.getAulasSemanais();
    		mTotalAtrasos += materia.getAtrasos();
    	}

    	if((int)(2*Math.ceil((float)0.10f*16* mTotalAulasSemanais)) - mTotalAtrasos <=
                0.2f*(int)Math.ceil((float)0.10f*16* mTotalAulasSemanais)) {
            if (mTvFaltasTotais.getCurrentTextColor() != Color.RED){
    		    AnimatorCreationUtil.ofTextColor(mTvFaltasTotais, 300, Color.RED).start();
            }
    	}
    	else if (mTvFaltasTotais.getCurrentTextColor() != Color.DKGRAY) {
            AnimatorCreationUtil.ofTextColor(mTvFaltasTotais, 300, Color.DKGRAY).start();
    	}

    	mTvFaltasTotais.setText("Total: " + (float) mTotalAtrasos / 2 + "/" +
            ((int) Math.ceil((float) 0.10f * 16 * mTotalAulasSemanais)));
    }

    protected void onPause() {
    	super.onPause();
    	//Retirar somente a data de cada matéria
    	ArrayList<MateriaData> materiasData = new ArrayList<MateriaData>();
    	for (LinMateria lm : mLinMaterias){
    		materiasData.add(lm.getData());
    		mSqlHelper.update(lm.getData());
    	}
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        mSelected = (LinMateria) v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final LinMateria currentSelected = mSelected;
        switch (item.getItemId()) {
            case R.id.edit:
                createEditSubjectDialog(currentSelected, new Runnable() {
                    @Override
                    public void run() {
                        mSqlHelper.update(currentSelected.getData());
                    }
                }).show();
                return true;
            case R.id.remove:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Remover")
                    .setMessage("Tem certeza que deseja remover \"" + mSelected.getStrNome() +"\"?")
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
                mSelected.setCheckNeeded(true);
                mSelected.update();
                return true;
            case R.id.notas:
                Intent intent = new Intent(this, NotasActivity.class);
                intent.putExtra("MateriaID", mSelected.getData().getSqlID());
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
        }, animateAllTo(mLlMaterias.getWidth(), 0));
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
        int size = mLinMaterias.size();

        for (int i = 0; i < size; i++)
            mLinMaterias.get(i).animateToDelayed(cardsTranslateX, 0, betweenCardsDelay * i);

        long halfDuration = (betweenCardsDelay * size) / 2 + 120;
        Animator anmtr = AnimatorCreationUtil.ofFloat(mBtnAdicionar, "alpha", halfDuration, null,
            tvAndBtntAlpha);
        anmtr.setStartDelay(halfDuration);
        anmtr.start();

        anmtr = AnimatorCreationUtil.ofFloat(mTvFaltasTotais, "alpha", halfDuration, null,
            tvAndBtntAlpha);
        anmtr.setStartDelay(halfDuration);
        anmtr.start();

        return 2 * halfDuration;
    }

    public void animateSubjectOut(final LinMateria materia) {
        final List<View> toBeAnimated = new ArrayList<View>();
        boolean after = false;
        for (LinMateria mtr : mLinMaterias) {
            if (after)
                toBeAnimated.add(mtr);
            if (mtr == materia)
                after = true;
        }
        toBeAnimated.add(mBtnAdicionar);
        final int numberToBeAnimated = toBeAnimated.size();

        int maxScroll = mScrollView.getChildAt(0).getHeight() - mScrollView.getHeight(),
            materiaHeight = materia.getHeight();
        final int initScroll = mScrollView.getScrollY(),
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
                ViewHelper.setScrollY(mScrollView,
                    (int) (initScroll + scrollBy * animation.getAnimatedFraction()));
            }
        });
        listAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLinMaterias.remove(materia);
                mLlMaterias.removeView(materia);
                mSqlHelper.remove(materia.getData().getSqlID());
                updateTotal();
                mScrollView.setVerticalScrollBarEnabled(true);
                mScrollView.setOnTouchListener(null);
                for (View mtr : toBeAnimated)
                    ViewHelper.setTranslationY(mtr, 0);
            }
        });
        listAnimator.setDuration(700);
        mScrollView.setVerticalScrollBarEnabled(false);
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        }); //disable user scrolling during the animation

        if (ViewHelper.getTranslationX(materia) == 0)
            materia.animateTo(mLlMaterias.getWidth(), 0);
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
