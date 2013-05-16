package com.marcioapf.mocos.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.*;
import com.marcioapf.mocos.AbsenceActivity;
import com.marcioapf.mocos.R;
import com.marcioapf.mocos.animation.AnimatorCreationUtil;
import com.marcioapf.mocos.data.SubjectData;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.util.IntProperty;

@SuppressLint("ViewConstructor")
public class SubjectCard extends LinearLayout {

    private static boolean block_delete_subject = false;
    private static Runnable reset_block_delete_flag = new Runnable() {
        @Override
        public void run() {
            block_delete_subject = false;
        }
    };

    private final CheckBox mCheckedCheckBox;
    private final TextView mSubjectTextView;
    private final ProgressBar mAbsencesProgressBar;
    private final Button mRemoveAbsenceButton;
    private final Button mRemoveDelayButton;
    private final Button mAddDelayButton;
    private final Button mAddAbsenceButton;
    private final TextView mAbsencesTextView;

    private ObjectAnimator mTextColorAnimator;
    private ObjectAnimator mProgressBarAnimator;
    private ObjectAnimator mCheckBoxAlphaAnimator;

    private SwipeableViewDelegate mSwipeDelegate;

    private final IntProperty<ProgressBar> mProgressProperty =
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
    private final Interpolator mAccDeccInterpolator = new AccelerateDecelerateInterpolator();

    private SubjectData mData;
    private final Handler mHandlerTimer = new Handler(Looper.myLooper());
    private final Runnable mTimerHelper = new Runnable() {
        @Override
        public void run() {
            System.out.println("Timer Finished");
            update();
        }
    };

    public SubjectCard(Context context, SubjectData subjectData){
        this(context, subjectData.getName(),
            subjectData.getWeeklyClasses(), subjectData.isCheckNeeded());
        setAtrasos(subjectData.getDelays());
        getData().setSqlID(subjectData.getSqlID());

        update();
    }
    public SubjectCard(Context context, String subject, int weeklyClasses, boolean checkNeeded) {
        super(context);
        inflate(context, R.layout.linha_materia, this);
        mSubjectTextView = getView(R.id.tv_materia);
        mAbsencesTextView = getView(R.id.tv_faltas);
        mAbsencesProgressBar = getView(R.id.pbar_faltas);
        mCheckedCheckBox = getView(R.id.cb_checked);
        mRemoveAbsenceButton = getView(R.id.rem_falta);
        mRemoveDelayButton = getView(R.id.rem_atraso);
        mAddDelayButton = getView(R.id.add_atraso);
        mAddAbsenceButton = getView(R.id.add_falta);

        mData = new SubjectData();

        this.setSubjectName(subject);
        mData.setWeeklyClasses(weeklyClasses);
        mData.setDelays(0);
        mData.setCheckNeeded(checkNeeded);

        ((Activity)context).registerForContextMenu(this);
        configureViews();
        configureAnimators();
        update();
    }

    private void configureViews() {
        mAbsencesTextView.setText((float) mData.getDelays() / 2 + "/" +
            ((int) Math.ceil((float) 0.15f * 16 * mData.getWeeklyClasses())));

        mCheckedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mHandlerTimer.postDelayed(mTimerHelper, 2000);

                    mData.setCheckNeeded(false);
                    System.out.println("is checked");
                } else {
                    System.out.println("not checked");
                    mHandlerTimer.removeCallbacks(mTimerHelper);
                    mData.setCheckNeeded(true);
                }
            }
        });

        OnClickListener buttonListener = new OnClickListener() {
            public void onClick(View v){
                switch (v.getId()) {
                    case R.id.rem_falta:
                        if(mData.getDelays()>=2)
                            setAtrasos(mData.getDelays()-2);
                        break;
                    case R.id.rem_atraso:
                        if(mData.getDelays()>=1)
                            setAtrasos(mData.getDelays()-1);
                        break;
                    case R.id.add_atraso:
                        setAtrasos(mData.getDelays()+1);
                        break;
                    case R.id.add_falta:
                        setAtrasos(mData.getDelays()+2);
                        break;
                }
                update();
                ((AbsenceActivity)getContext()).updateTotal();
            }
        };
        mAddDelayButton.setOnClickListener(buttonListener);
        mRemoveDelayButton.setOnClickListener(buttonListener);
        mRemoveAbsenceButton.setOnClickListener(buttonListener);
        mAddAbsenceButton.setOnClickListener(buttonListener);

        mSwipeDelegate = new SwipeableViewDelegate(this);
        mSwipeDelegate.setOnSwipeListener(new CardSwipeOutListener());
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                block_delete_subject = true;
                showContextMenu();
                getHandler().postDelayed(reset_block_delete_flag, 300);
                return true;
            }
        }); // we have to set this to manually show the context menu and block the swipe motion
    }

    private void configureAnimators() {
        mTextColorAnimator = AnimatorCreationUtil.ofTextColor(
            new TextView[]{mAbsencesTextView, mSubjectTextView}, 300, Color.DKGRAY);
        mProgressBarAnimator = ObjectAnimator.ofInt(mAbsencesProgressBar, mProgressProperty, 0);
        mProgressBarAnimator.setInterpolator(mAccDeccInterpolator);
        mProgressBarAnimator.setDuration(120);
        mCheckBoxAlphaAnimator = ObjectAnimator.ofFloat(mCheckedCheckBox, "alpha", 0);
        mCheckBoxAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mData.isCheckNeeded()) {
                    mCheckedCheckBox.setVisibility(INVISIBLE);
                    mCheckedCheckBox.setChecked(true);
                }
            }
        });
    }

    public void update() {
        mAbsencesTextView.setText((float) mData.getDelays() / 2 + "/" +
            ((int) Math.ceil(0.15f * 16 * mData.getWeeklyClasses())));
        mSubjectTextView.setText(mData.getName());
        if (mAbsencesProgressBar.getProgress() != mData.getDelays() * 1000) {
            mAbsencesProgressBar
                .setMax(2000 * (int) Math.ceil(0.15f * 16 * mData.getWeeklyClasses()));
            mProgressBarAnimator.setIntValues(mData.getDelays() * 1000);
            mProgressBarAnimator.start();
        }

        if(2*(int)Math.ceil(0.15f*16* mData.getWeeklyClasses()) - mData.getDelays() <= 4) {
            if (mAbsencesTextView.getCurrentTextColor() != Color.RED) {
                mTextColorAnimator.setIntValues(Color.RED);
                mTextColorAnimator.start();
            }
        } else if (mAbsencesTextView.getCurrentTextColor() != Color.DKGRAY) {
            mTextColorAnimator.setIntValues(Color.DKGRAY);
            mTextColorAnimator.start();
        }
        System.out.println("updated");

        if(mData.isCheckNeeded()){
            if (mCheckedCheckBox.getVisibility() != VISIBLE) {
                mCheckedCheckBox.setVisibility(VISIBLE);
                mCheckedCheckBox.setChecked(false);
                mCheckBoxAlphaAnimator.setFloatValues(0, 1);
                mCheckBoxAlphaAnimator.setDuration(500);
                mCheckBoxAlphaAnimator.start();
            }
        }
        else if (mCheckedCheckBox.getVisibility() != INVISIBLE) {
            mCheckBoxAlphaAnimator.setFloatValues(1, 0);
            mCheckBoxAlphaAnimator.setDuration(300);
            mCheckBoxAlphaAnimator.start();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return (!block_delete_subject && mSwipeDelegate.handleTouch(event))
            || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (!block_delete_subject && mSwipeDelegate.handleTouch(event))
            || super.onTouchEvent(event);
    }

    public void swipeRight(float initVelocity, long delay) {
        mSwipeDelegate.swipeRight(initVelocity, delay);
    }

    public void swipeBack(float initVelocity, long delay) {
        mSwipeDelegate.swipeRight(initVelocity, delay);
    }

    public boolean isCheckNeeded() {
        return mData.isCheckNeeded();
    }

    public void setCheckNeeded(boolean checkNeeded) {
        this.getData().setCheckNeeded(checkNeeded);
    }

    public void setAtrasos(int atrasos) {
        mData.setDelays(atrasos);
        update();
    }

    public int getAtrasos(){
        return mData.getDelays();
    }

    public String getStrNome(){
        return mData.getName();
    }

    public void setSubjectName(String name){
        mData.setName(name);
        mSubjectTextView.setText(name);
    }

    public SubjectData getData() {
        return mData;
    }

    public int getAulasSemanais() {
        return mData.getWeeklyClasses();
    }

    public void setAulasSemanais(int aulasSemanais) {
        mData.setWeeklyClasses(aulasSemanais);
    }

    public <Type extends View> Type getView(int id) {
        return (Type)findViewById(id);
    }

    private class CardSwipeOutListener implements SwipeableViewDelegate.OnSwipeListener {

        @Override
        public void onSwipeOut() {
            final Handler handler = getHandler();
            if (handler == null)
                return;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Remover")
                        .setMessage("Tem certeza que deseja remover \"" + getStrNome() + "\"?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((AbsenceActivity)getContext()).animateSubjectOut(SubjectCard.this);
                            }
                        })
                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSwipeDelegate.swipeBack(0);
                            }
                        });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mSwipeDelegate.swipeBack(0);
                        }
                    });
                    builder.show();
                    handler.postDelayed(reset_block_delete_flag, 300);
                }
            }, 300);
        }

        @Override
        public void onStartTracking(MotionEvent event) {
            MotionEvent motionEvent = MotionEvent.obtain(event);
            motionEvent.setAction(MotionEvent.ACTION_CANCEL);
            SubjectCard.super.onTouchEvent(event);
        }

        @Override
        public void onSwipeBack() {
        }
    }
}
