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
import com.marcioapf.mocos.data.SubjectData;
import com.marcioapf.mocos.data.SQLHelper;
import com.marcioapf.mocos.view.SubjectCard;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public class AbsenceActivity extends Activity {

    private ScrollView mScrollView;
    private LinearLayout mSubjectsLayout;
    private Button mAddButton;
    private SubjectCard mSelectedSubject = null;
    private TextView mTotalAbsencesTextView;

    private ArrayList<SubjectCard> mSubjectCards;

    private int mTotalWeeklyClasses = 0;
    private int mTotalDelays = 0;

    private SQLHelper mSqlHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	mSqlHelper = new SQLHelper(this);

        setContentView(R.layout.main);

        mScrollView = (ScrollView) findViewById(R.id.scrllvwNo1);
        mSubjectsLayout = (LinearLayout) findViewById(R.id.llmaterias);
        mTotalAbsencesTextView = (TextView) findViewById(R.id.tvFaltasTotais);
        mAddButton = (Button) findViewById(R.id.btnNovaMateria);

		mAddButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final SubjectCard materia = new SubjectCard(AbsenceActivity.this, "Nova", 4, false);

                final ObjectAnimator btAnimator = ObjectAnimator.ofFloat(mAddButton, "alpha", 0);
                btAnimator.setDuration(500);
                btAnimator.start();
                createEditSubjectDialog(materia, new Runnable() {
                        @Override
                        public void run() {
                            mSubjectCards.add(materia);
                            mSubjectsLayout.addView(materia);
                            mSqlHelper.insertAndID(materia.getData());

                            ViewHelper.setTranslationX(materia, -mSubjectsLayout.getWidth());
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


    	mSubjectCards = new ArrayList<SubjectCard>();

    	ArrayList<SubjectData> materiasData = mSqlHelper.retrieveAllMateriaData();
    	for (SubjectData mData : materiasData){
    		mSubjectCards.add(new SubjectCard(this, mData));
    	}

        for (SubjectCard lm : mSubjectCards)
	        mSubjectsLayout.addView(lm);

        //Atualiza a contagem do total de faltas
        updateTotal();
    }

    public void updateTotal() {
    	mTotalWeeklyClasses = 0;
    	mTotalDelays = 0;

    	for (SubjectCard materia : mSubjectCards){
    		mTotalWeeklyClasses += materia.getWeeklyClasses();
    		mTotalDelays += materia.getDelays();
    	}

    	if((int)(2*Math.ceil((float)0.10f*16* mTotalWeeklyClasses)) - mTotalDelays <=
                0.2f*(int)Math.ceil((float)0.10f*16* mTotalWeeklyClasses)) {
            if (mTotalAbsencesTextView.getCurrentTextColor() != Color.RED){
    		    AnimatorCreationUtil.ofTextColor(mTotalAbsencesTextView, 300, Color.RED).start();
            }
    	}
    	else if (mTotalAbsencesTextView.getCurrentTextColor() != Color.DKGRAY) {
            AnimatorCreationUtil.ofTextColor(mTotalAbsencesTextView, 300, Color.DKGRAY).start();
    	}

    	mTotalAbsencesTextView.setText("Total: " + (float) mTotalDelays / 2 + "/" +
            ((int) Math.ceil((float) 0.10f * 16 * mTotalWeeklyClasses)));
    }

    protected void onPause() {
    	super.onPause();
    	//Retirar somente a data de cada matéria
    	ArrayList<SubjectData> materiasData = new ArrayList<SubjectData>();
    	for (SubjectCard lm : mSubjectCards){
    		materiasData.add(lm.getData());
    		mSqlHelper.update(lm.getData());
    	}
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        mSelectedSubject = (SubjectCard) v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final SubjectCard currentSelected = mSelectedSubject;
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
                    .setMessage("Tem certeza que deseja remover \"" +
                        mSelectedSubject.getSubjectName() +"\"?")
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
                mSelectedSubject.setCheckNeeded(true);
                mSelectedSubject.update();
                return true;
            case R.id.notas:
                Intent intent = new Intent(this, GradesActivity.class);
                intent.putExtra("MateriaID", mSelectedSubject.getData().getSqlID());
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
        }, animateAllTo(mSubjectsLayout.getWidth(), 0));
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
        int size = mSubjectCards.size();

        for (int i = 0; i < size; i++)
            mSubjectCards.get(i).animateToDelayed(cardsTranslateX, 0, betweenCardsDelay * i);

        long halfDuration = (betweenCardsDelay * size) / 2 + 120;
        Animator anmtr = AnimatorCreationUtil.ofFloat(mAddButton, "alpha", halfDuration, null,
            tvAndBtntAlpha);
        anmtr.setStartDelay(halfDuration);
        anmtr.start();

        anmtr = AnimatorCreationUtil.ofFloat(mTotalAbsencesTextView, "alpha", halfDuration, null,
            tvAndBtntAlpha);
        anmtr.setStartDelay(halfDuration);
        anmtr.start();

        return 2 * halfDuration;
    }

    public void animateSubjectOut(final SubjectCard materia) {
        final List<View> toBeAnimated = new ArrayList<View>();
        boolean after = false;
        for (SubjectCard mtr : mSubjectCards) {
            if (after)
                toBeAnimated.add(mtr);
            if (mtr == materia)
                after = true;
        }
        toBeAnimated.add(mAddButton);
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
                mSubjectCards.remove(materia);
                mSubjectsLayout.removeView(materia);
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
            materia.animateTo(mSubjectsLayout.getWidth(), 0);
        listAnimator.setStartDelay(500);
        listAnimator.start();
    }

    private AlertDialog createEditSubjectDialog(SubjectCard materia, Runnable success) {
        return createEditSubjectDialog(materia, success, null);
    }

    private AlertDialog createEditSubjectDialog(final SubjectCard materia,
                                                final Runnable success,
                                                final Runnable failure) {
        View dialogContent = View.inflate(this, R.layout.edit_dialog, null);
        final TextView etNomeMateria = (TextView) dialogContent
                .findViewById(R.id.nome_materia);
        final TextView etAulasSemanais = (TextView) dialogContent
                .findViewById(R.id.maximo_atrasos);

        etNomeMateria.setText(materia.getSubjectName());
        etAulasSemanais.setText(Integer.toString(materia.getWeeklyClasses()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Edição de Matéria")
            .setView(dialogContent)
            .setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    materia.setSubjectName(etNomeMateria.getText().toString());
                    materia.setWeeklyClasses(
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
