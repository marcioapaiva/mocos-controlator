package com.marcioapf.mocos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.*;
import com.marcioapf.mocos.animation.AnimatorCreationUtil;
import com.marcioapf.mocos.animation.SpringInterpolator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.util.IntProperty;
import com.nineoldandroids.view.ViewHelper;

@SuppressLint("ViewConstructor")
public class LinMateria extends LinearLayout {

    private static boolean block_delete_subject = false;
    private static Runnable reset_block_delete_flag = new Runnable() {
        @Override
        public void run() {
            block_delete_subject = false;
        }
    };

    private final CheckBox cbChecked;
    private final TextView tvMateria;
    private final ProgressBar pBarFaltas;
    private final Button btnAddAtraso, btnRemAtraso, btnAddFalta, btnRemFalta;
    private final TextView tvFaltas;

    private ObjectAnimator textColorAnimator;
    private ObjectAnimator progressBarAnimator;
    private ObjectAnimator checkBoxAlphaAnimator;
    private ObjectAnimator cardTranslationAnimator;
    private SpringInterpolator cardTranslationInterpolator;

    private final IntProperty<ProgressBar> progressProperty = new IntProperty<ProgressBar>("progress") {
        @Override
        public void setValue(ProgressBar object, int value) {
            object.setProgress(value);
        }
        @Override
        public Integer get(ProgressBar object) {
            return object.getProgress();
        }
    };
    private final Interpolator accDeccInterpolator = new AccelerateDecelerateInterpolator();

    private final GestureDetector mGestureDetector;
    private final ViewConfiguration mViewConfiguration;

    private MateriaData data;
    private final Handler handlerTimer = new Handler(Looper.myLooper());
    private final Runnable timerHelper = new Runnable() {
        @Override
        public void run() {
            System.out.println("Timer Finished");
            update();
        }
    };

    public LinMateria(Context context, MateriaData materiaData){
        this(context, materiaData.getStrNome(), materiaData.getAulasSemanais(), materiaData.isCheckNeeded());
        this.setAtrasos(materiaData.getAtrasos());
        this.getData().setSqlID(materiaData.getSqlID());
        update();
    }
    public LinMateria(Context context, String strMateria, int aulasSemanais, boolean checkNeeded) {
        super(context);
        inflate(context, R.layout.linha_materia, this);
        tvMateria = getView(R.id.tv_materia);
        tvFaltas = getView(R.id.tv_faltas);
        pBarFaltas = getView(R.id.pbar_faltas);
        cbChecked = getView(R.id.cb_checked);
        btnRemFalta = getView(R.id.rem_falta);
        btnRemAtraso = getView(R.id.rem_atraso);
        btnAddAtraso = getView(R.id.add_atraso);
        btnAddFalta = getView(R.id.add_falta);

        data = new MateriaData();

        this.setStrNome(strMateria);
        data.setAulasSemanais(aulasSemanais);
        data.setAtrasos(0);
        data.setCheckNeeded(checkNeeded);

        ((Activity)context).registerForContextMenu(this);
        configureViews();
        configureAnimators();
        MateriaGestureListener gestureListener = new MateriaGestureListener();
        mGestureDetector = new GestureDetector(context, gestureListener);
        mViewConfiguration = ViewConfiguration.get(context);
        setOnLongClickListener(gestureListener); // we have to set this to manually show the context
                                                 // menu and update the block delete scheduled flag
        update();
    }

    private void configureViews() {
        tvFaltas.setText((float)data.getAtrasos()/2 + "/" + ((int)Math.ceil((float)0.15f*16*data.getAulasSemanais())));

        cbChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    handlerTimer.postDelayed(timerHelper, 2000);

                    data.setCheckNeeded(false);
                    System.out.println("is checked");
                }
                else {
                    System.out.println("not checked");
                    handlerTimer.removeCallbacks(timerHelper);
                    data.setCheckNeeded(true);
                }
            }
        });

        OnClickListener buttonListener = new OnClickListener() {
            public void onClick(View v){
                switch (v.getId()) {
                    case R.id.rem_falta:
                        if(data.getAtrasos()>=2)
                            setAtrasos(data.getAtrasos()-2);
                        break;
                    case R.id.rem_atraso:
                        if(data.getAtrasos()>=1)
                            setAtrasos(data.getAtrasos()-1);
                        break;
                    case R.id.add_atraso:
                        setAtrasos(data.getAtrasos()+1);
                        break;
                    case R.id.add_falta:
                        setAtrasos(data.getAtrasos()+2);
                        break;
                }
                update();
                ((WelcomeActivity)getContext()).updateTotal();
            }
        };
        btnAddAtraso.setOnClickListener(buttonListener);
        btnRemAtraso.setOnClickListener(buttonListener);
        btnRemFalta.setOnClickListener(buttonListener);
        btnAddFalta.setOnClickListener(buttonListener);
    }

    private void configureAnimators() {
        textColorAnimator = AnimatorCreationUtil.ofTextColor(
            new TextView[]{tvFaltas, tvMateria}, 300, Color.DKGRAY);
        progressBarAnimator = ObjectAnimator.ofInt(pBarFaltas, progressProperty, 0);
        progressBarAnimator.setInterpolator(accDeccInterpolator);
        progressBarAnimator.setDuration(120);
        checkBoxAlphaAnimator = ObjectAnimator.ofFloat(cbChecked, "alpha", 0);
        checkBoxAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!data.isCheckNeeded()) {
                    cbChecked.setVisibility(INVISIBLE);
                    cbChecked.setChecked(true);
                }
            }
        });
        cardTranslationInterpolator = new SpringInterpolator(100.0, 15.0);
        cardTranslationAnimator = ObjectAnimator.ofFloat(this, "translationX", 0);
        cardTranslationAnimator.setInterpolator(cardTranslationInterpolator);
    }

    public void update() {
        tvFaltas.setText((float) data.getAtrasos() / 2 + "/" + ((int) Math.ceil(0.15f * 16 * data.getAulasSemanais())));
        tvMateria.setText(data.getStrNome());
        if (pBarFaltas.getProgress() != data.getAtrasos() * 1000) {
            pBarFaltas.setMax(2000 * (int) Math.ceil(0.15f * 16 * data.getAulasSemanais()));
            progressBarAnimator.setIntValues(data.getAtrasos() * 1000);
            progressBarAnimator.start();
        }

        if(2*(int)Math.ceil(0.15f*16*data.getAulasSemanais()) - data.getAtrasos() <= 4) {
            if (tvFaltas.getCurrentTextColor() != Color.RED) {
                textColorAnimator.setIntValues(Color.RED);
                textColorAnimator.start();
            }
        } else if (tvFaltas.getCurrentTextColor() != Color.DKGRAY) {
            textColorAnimator.setIntValues(Color.DKGRAY);
            textColorAnimator.start();
        }
        System.out.println("updated");

        if(data.isCheckNeeded()){
            if (cbChecked.getVisibility() != VISIBLE) {
                cbChecked.setVisibility(VISIBLE);
                cbChecked.setChecked(false);
                checkBoxAlphaAnimator.setFloatValues(0, 1);
                checkBoxAlphaAnimator.setDuration(500);
                checkBoxAlphaAnimator.start();
            }
        }
        else if (cbChecked.getVisibility() != INVISIBLE) {
            checkBoxAlphaAnimator.setFloatValues(1, 0);
            checkBoxAlphaAnimator.setDuration(300);
            checkBoxAlphaAnimator.start();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return handleTouch(event) || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleTouch(event) || super.onTouchEvent(event);
    }

    private boolean handleTouch(MotionEvent event) {
        MotionEvent ev = MotionEvent.obtain(event);
        ev.setLocation(event.getRawX(), event.getRawY());
        boolean result = mGestureDetector.onTouchEvent(ev);
        if (!result && event.getActionMasked() == MotionEvent.ACTION_UP ||
            event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            animateTo(0, 0);
        }
        return result;
    }

    /**
     * Animates this card to a certain position on the x coordinate.
     * @param finalPosition the position where to move to
     * @param initVelocity the initial velocity of the animation
     * @return the animator that will generate the animation
     */
    public void animateTo(float finalPosition, float initVelocity) {
        cardTranslationAnimator.cancel();
        float initPosition = ViewHelper.getTranslationX(this);
        if (initPosition == finalPosition && initVelocity == 0) {
            return;
        }
        cardTranslationInterpolator
            .setInitialVelocity(initVelocity / (finalPosition - initPosition));

        cardTranslationAnimator.setFloatValues(finalPosition);
        cardTranslationAnimator
            .setDuration((long) (1000 * cardTranslationInterpolator.getDesiredDuration()));
        cardTranslationAnimator.start();
    }

    public boolean isCheckNeeded() {
        return data.isCheckNeeded();
    }

    public void setCheckNeeded(boolean checkNeeded) {
        this.getData().setCheckNeeded(checkNeeded);
    }

    public void setAtrasos(int atrasos) {
        data.setAtrasos(atrasos);
        update();
    }

    public int getAtrasos(){
        return data.getAtrasos();
    }

    public String getStrNome(){
        return data.getStrNome();
    }

    public void setStrNome(String strNome){
        data.setStrNome(strNome);
        tvMateria.setText(strNome);
    }

    public MateriaData getData() {
        return data;
    }

    public int getAulasSemanais() {
        return data.getAulasSemanais();
    }

    public void setAulasSemanais(int aulasSemanais) {
        data.setAulasSemanais(aulasSemanais);
    }

    public <Type extends View> Type getView(int id) {
        return (Type)findViewById(id);
    }

    private class MateriaGestureListener
            extends GestureDetector.SimpleOnGestureListener implements OnLongClickListener {
        private boolean mIsDragging;
        private boolean mIgnoreTouch;

        @Override
        public boolean onDown(MotionEvent e) {
            mIsDragging = false;
            mIgnoreTouch = block_delete_subject;
            return false;
        }

        @Override
        public boolean onLongClick(View v) {
            mIgnoreTouch = block_delete_subject = true;
            showContextMenu();
            getHandler().postDelayed(reset_block_delete_flag, 300);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean oldValue = mIsDragging;
            mIsDragging =  mIsDragging || (!mIgnoreTouch &&
                Math.abs(e2.getX() - e1.getX()) > mViewConfiguration.getScaledTouchSlop());
            if (oldValue ^ mIsDragging) {
                getParent().requestDisallowInterceptTouchEvent(true);
                e2.setAction(MotionEvent.ACTION_CANCEL);
                LinMateria.super.onTouchEvent(e2);
            }
            if (mIsDragging) {
                cardTranslationAnimator.cancel();
                View v = LinMateria.this;
                ViewHelper.setTranslationX(v, ViewHelper.getTranslationX(v) - distanceX);
            }
            return mIsDragging;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!mIsDragging)
                return false;
            final int finalTranslate;
            int flingVel = mViewConfiguration.getScaledMinimumFlingVelocity();
            if (Math.abs(velocityX) > flingVel || Math.abs(e2.getX()-e1.getX()) > getWidth() / 2) {
                if (velocityX > flingVel || e2.getX() - e1.getX() > getWidth() / 2)
                    finalTranslate = getWidth();
                else
                    finalTranslate = -getWidth();

                block_delete_subject = true;
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Remover")
                            .setMessage("Tem certeza que deseja remover \"" + getStrNome() + "\"?")
                            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((WelcomeActivity)getContext())
                                        .animateSubjectOut(LinMateria.this);
                                }
                            })
                            .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    animateTo(0, 0);
                                }
                            });
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                animateTo(0, 0);
                            }
                        });
                        builder.show();
                        getHandler().postDelayed(reset_block_delete_flag, 300);
                    }
                }, 300);
            } else
                finalTranslate = 0;

            animateTo(finalTranslate, velocityX);
            return true;
        }
    }
}
