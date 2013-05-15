package com.marcioapf.mocos.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.marcioapf.mocos.animation.SpringInterpolator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.view.ViewHelper;

public class SwipeableViewDelegate {

    private final View mView;
    private final GestureDetector mGestureDetector;

    private ObjectAnimator mTranslationAnimator;
    private SpringInterpolator mTranslationInterpolator;
    private OnSwipeListener mListener;

    private final int mMinFlingVelocity;
    private final int mTouchSlope;

    public SwipeableViewDelegate(View view) {
        mView = view;
        mGestureDetector = new GestureDetector(mView.getContext(), new SwipeGestureListener());

        ViewConfiguration vc = ViewConfiguration.get(mView.getContext());
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mTouchSlope = vc.getScaledTouchSlop();

        mTranslationInterpolator = new SpringInterpolator(100.0, 15.0);
        mTranslationAnimator = ObjectAnimator.ofPropertyValuesHolder(mView,
            PropertyValuesHolder.ofFloat("translationX", 0),
            PropertyValuesHolder.ofFloat("alpha", 1));
        mTranslationAnimator.setInterpolator(mTranslationInterpolator);
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        mListener = listener;
    }

    public float translationToAlpha(float translation) {
        return 1 - Math.abs(translation / ((View)mView.getParent()).getWidth());
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
        mTranslationAnimator.cancel();
        float initPosition = ViewHelper.getTranslationX(mView);
        ViewHelper.setAlpha(mView, translationToAlpha(initPosition));
        if (initPosition == finalPosition && initVelocity == 0) {
            return;
        }
        mTranslationInterpolator
            .setInitialVelocity(initVelocity / (finalPosition - initPosition));

        mTranslationAnimator.setValues(
            PropertyValuesHolder.ofFloat("translationX", finalPosition),
            PropertyValuesHolder.ofFloat("alpha", translationToAlpha(finalPosition)));
        mTranslationAnimator
            .setDuration((long) (1000 * mTranslationInterpolator.getDesiredDuration()));
        mTranslationAnimator.setStartDelay(delay);
        mTranslationAnimator.start();
    }

    public boolean handleTouch(MotionEvent event) {
        MotionEvent ev = MotionEvent.obtain(event);
        ev.setLocation(event.getRawX(), event.getRawY());
        boolean result = mGestureDetector.onTouchEvent(ev);
        if (!result && event.getActionMasked() == MotionEvent.ACTION_UP ||
            event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            animateTo(0, 0);
        }
        return result;
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean mIsDragging;

        @Override
        public boolean onDown(MotionEvent e) {
            mIsDragging = false;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean oldValue = mIsDragging;
            mIsDragging =  mIsDragging || Math.abs(e2.getX() - e1.getX()) > mTouchSlope;
            if (oldValue ^ mIsDragging) {
                mView.getParent().requestDisallowInterceptTouchEvent(true);
                e2.setAction(MotionEvent.ACTION_CANCEL); // so that when the event reaches the super
                                                         // it cancels anything that was being done
            }
            if (mIsDragging) {
                mTranslationAnimator.cancel();
                ViewHelper.setTranslationX(mView, ViewHelper.getTranslationX(mView) - distanceX);
                ViewHelper.setAlpha(mView, translationToAlpha(ViewHelper.getTranslationX(mView)));
            }
            return oldValue && mIsDragging;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!mIsDragging)
                return false;
            final int finalTranslate;
            if (Math.abs(velocityX) > mMinFlingVelocity ||
                    Math.abs(e2.getX()-e1.getX()) > mView.getWidth() / 2) {
                int translateAbs = ((View)mView.getParent()).getWidth();
                if (velocityX > mMinFlingVelocity || e2.getX() - e1.getX() > mView.getWidth() / 2)
                    finalTranslate = translateAbs;
                else
                    finalTranslate = -translateAbs;

                if (mListener != null)
                    mListener.onSwipeOut();

            } else {
                finalTranslate = 0;
                if (mListener != null)
                    mListener.onSwipeBack();
            }
            animateTo(finalTranslate, velocityX);
            return true;
        }
    }

    public interface OnSwipeListener {
        void onSwipeOut();

        void onSwipeBack();
    }
}
