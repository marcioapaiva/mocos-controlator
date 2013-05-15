package com.marcioapf.mocos.view;

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
import com.marcioapf.mocos.AbsenceActivity;
import com.marcioapf.mocos.R;
import com.marcioapf.mocos.animation.AnimatorCreationUtil;
import com.marcioapf.mocos.animation.SpringInterpolator;
import com.marcioapf.mocos.data.MateriaData;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
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

    private final CheckBox mCbChecked;
    private final TextView mTvMateria;
    private final ProgressBar mPBarFaltas;
    private final Button mBtnAddAtraso;
    private final Button mBtnRemAtraso;
    private final Button mBtnAddFalta;
    private final Button mBtnRemFalta;
    private final TextView mTvFaltas;

    private ObjectAnimator mTextColorAnimator;
    private ObjectAnimator mProgressBarAnimator;
    private ObjectAnimator mCheckBoxAlphaAnimator;
    private ObjectAnimator mCardTranslationAnimator;
    private SpringInterpolator mCardTranslationInterpolator;

    private final IntProperty<ProgressBar> progressProperty =
      new IntProperty<ProgressBar>("progress") {
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
        this(context, materiaData.getStrNome(),
            materiaData.getAulasSemanais(), materiaData.isCheckNeeded());
        setAtrasos(materiaData.getAtrasos());
        getData().setSqlID(materiaData.getSqlID());

        update();
    }
    public LinMateria(Context context, String strMateria, int aulasSemanais, boolean checkNeeded) {
        super(context);
        inflate(context, R.layout.linha_materia, this);
        mTvMateria = getView(R.id.tv_materia);
        mTvFaltas = getView(R.id.tv_faltas);
        mPBarFaltas = getView(R.id.pbar_faltas);
        mCbChecked = getView(R.id.cb_checked);
        mBtnRemFalta = getView(R.id.rem_falta);
        mBtnRemAtraso = getView(R.id.rem_atraso);
        mBtnAddAtraso = getView(R.id.add_atraso);
        mBtnAddFalta = getView(R.id.add_falta);

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
        mTvFaltas.setText((float) data.getAtrasos() / 2 + "/" +
            ((int) Math.ceil((float) 0.15f * 16 * data.getAulasSemanais())));

        mCbChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    handlerTimer.postDelayed(timerHelper, 2000);

                    data.setCheckNeeded(false);
                    System.out.println("is checked");
                } else {
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
                ((AbsenceActivity)getContext()).updateTotal();
            }
        };
        mBtnAddAtraso.setOnClickListener(buttonListener);
        mBtnRemAtraso.setOnClickListener(buttonListener);
        mBtnRemFalta.setOnClickListener(buttonListener);
        mBtnAddFalta.setOnClickListener(buttonListener);
    }

    private void configureAnimators() {
        mTextColorAnimator = AnimatorCreationUtil.ofTextColor(
            new TextView[]{mTvFaltas, mTvMateria}, 300, Color.DKGRAY);
        mProgressBarAnimator = ObjectAnimator.ofInt(mPBarFaltas, progressProperty, 0);
        mProgressBarAnimator.setInterpolator(accDeccInterpolator);
        mProgressBarAnimator.setDuration(120);
        mCheckBoxAlphaAnimator = ObjectAnimator.ofFloat(mCbChecked, "alpha", 0);
        mCheckBoxAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!data.isCheckNeeded()) {
                    mCbChecked.setVisibility(INVISIBLE);
                    mCbChecked.setChecked(true);
                }
            }
        });
        mCardTranslationInterpolator = new SpringInterpolator(100.0, 15.0);
        mCardTranslationAnimator = ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat("translationX", 0),
            PropertyValuesHolder.ofFloat("alpha", 1));
        mCardTranslationAnimator.setInterpolator(mCardTranslationInterpolator);
    }

    public void update() {
        mTvFaltas.setText((float) data.getAtrasos() / 2 + "/" +
            ((int) Math.ceil(0.15f * 16 * data.getAulasSemanais())));
        mTvMateria.setText(data.getStrNome());
        if (mPBarFaltas.getProgress() != data.getAtrasos() * 1000) {
            mPBarFaltas.setMax(2000 * (int) Math.ceil(0.15f * 16 * data.getAulasSemanais()));
            mProgressBarAnimator.setIntValues(data.getAtrasos() * 1000);
            mProgressBarAnimator.start();
        }

        if(2*(int)Math.ceil(0.15f*16*data.getAulasSemanais()) - data.getAtrasos() <= 4) {
            if (mTvFaltas.getCurrentTextColor() != Color.RED) {
                mTextColorAnimator.setIntValues(Color.RED);
                mTextColorAnimator.start();
            }
        } else if (mTvFaltas.getCurrentTextColor() != Color.DKGRAY) {
            mTextColorAnimator.setIntValues(Color.DKGRAY);
            mTextColorAnimator.start();
        }
        System.out.println("updated");

        if(data.isCheckNeeded()){
            if (mCbChecked.getVisibility() != VISIBLE) {
                mCbChecked.setVisibility(VISIBLE);
                mCbChecked.setChecked(false);
                mCheckBoxAlphaAnimator.setFloatValues(0, 1);
                mCheckBoxAlphaAnimator.setDuration(500);
                mCheckBoxAlphaAnimator.start();
            }
        }
        else if (mCbChecked.getVisibility() != INVISIBLE) {
            mCheckBoxAlphaAnimator.setFloatValues(1, 0);
            mCheckBoxAlphaAnimator.setDuration(300);
            mCheckBoxAlphaAnimator.start();
        }
    }

    public float translationToAlpha(float translation) {
        return 1 - Math.abs(translation / ((View)getParent()).getWidth());
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

    public void animateTo(float finalPosition, float initVelocity) {
        animateToDelayed(finalPosition, initVelocity, 0);
    }

    /**
     * Animates this card to a certain position on the x coordinate.
     * @param finalPosition the position where to move to
     * @param initVelocity the initial velocity of the animation
     * @param delay the delay to wait before animating the view
     * @return the animator that will generate the animation
     */
    public void animateToDelayed(float finalPosition, float initVelocity, long delay) {
        mCardTranslationAnimator.cancel();
        float initPosition = ViewHelper.getTranslationX(this);
        ViewHelper.setAlpha(this, translationToAlpha(initPosition));
        if (initPosition == finalPosition && initVelocity == 0) {
            return;
        }
        mCardTranslationInterpolator
            .setInitialVelocity(initVelocity / (finalPosition - initPosition));

        mCardTranslationAnimator.setValues(
            PropertyValuesHolder.ofFloat("translationX", finalPosition),
            PropertyValuesHolder.ofFloat("alpha", translationToAlpha(finalPosition)));
        mCardTranslationAnimator
            .setDuration((long) (1000 * mCardTranslationInterpolator.getDesiredDuration()));
        mCardTranslationAnimator.setStartDelay(delay);
        mCardTranslationAnimator.start();
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
        mTvMateria.setText(strNome);
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
                mCardTranslationAnimator.cancel();
                View v = LinMateria.this;
                ViewHelper.setTranslationX(v, ViewHelper.getTranslationX(v) - distanceX);
                ViewHelper.setAlpha(v, translationToAlpha(ViewHelper.getTranslationX(v)));
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
                                    ((AbsenceActivity)getContext())
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
