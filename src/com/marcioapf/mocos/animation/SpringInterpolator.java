package com.marcioapf.mocos.animation;

import android.view.animation.Interpolator;

// TODO: Implement Runge-Kutta method instead of calling Math functions for performance.
public class SpringInterpolator implements Interpolator {
    private final double mGamma, mVDiv2;
    private final boolean mOscilative;
    private final double mEps;

    private double mA, mB;
    private double mDuration;

    public SpringInterpolator(double tension, double damping) {
        this(tension, damping, 0.001);
    }

    public SpringInterpolator(double tension, double damping, double eps) {
        mEps = eps;
        mOscilative = (4 * tension - damping * damping > 0);
        if (mOscilative) {
            mGamma = Math.sqrt(4 * tension - damping * damping) / 2;
            mVDiv2 = damping / 2;
        } else {
            mGamma = Math.sqrt(damping * damping - 4 * tension) / 2;
            mVDiv2 = damping / 2;
        }
    }

    public void setInitialVelocity(double v0) {
        if (mOscilative) {
            mB = Math.atan(-mGamma / (v0 - mVDiv2));
            mA = -1 / Math.sin(mB);
            mDuration = Math.log(Math.abs(mA) / mEps) / mVDiv2;
        } else {
            mA = (v0 - (mGamma + mVDiv2)) / (2 * mGamma);
            mB = -1 - mA;
            mDuration = Math.log(Math.abs(mA) / mEps) / (mVDiv2 - mGamma);
        }
    }

    public double getDesiredDuration() {
        return mDuration;
    }

    @Override
    public float getInterpolation(float input) {
        if (input >= 1) {
            return 1;
        }
        double t = input * mDuration;
        return (float) (mOscilative ?
            (mA * Math.exp(-mVDiv2 * t) * Math.sin(mGamma * t + mB) + 1) :
            (mA * Math.exp((mGamma - mVDiv2) * t) + mB * Math.exp(-(mGamma + mVDiv2) * t) + 1));
    }
}
