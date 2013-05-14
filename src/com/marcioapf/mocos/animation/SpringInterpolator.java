package com.marcioapf.mocos.animation;

import android.view.animation.Interpolator;

// TODO: Implement Runge-Kutta method instead of calling Math functions for performance.
public class SpringInterpolator implements Interpolator {
    private final double gamma, vDiv2;
    private final boolean oscilative;
    private final double eps;

    private double A, B;
    private double duration;

    public SpringInterpolator(double tension, double damping) {
        this(tension, damping, 0.001);
    }

    public SpringInterpolator(double tension, double damping, double eps) {
        this.eps = eps;
        oscilative = (4 * tension - damping * damping > 0);
        if (oscilative) {
            gamma = Math.sqrt(4 * tension - damping * damping) / 2;
            vDiv2 = damping / 2;
        } else {
            gamma = Math.sqrt(damping * damping - 4 * tension) / 2;
            vDiv2 = damping / 2;
        }
    }

    public void setInitialVelocity(double v0) {
        if (oscilative) {
            B = Math.atan(- gamma / (v0 - vDiv2));
            A = -1 / Math.sin(B);
            duration = Math.log(Math.abs(A) / eps) / vDiv2;
        } else {
            A = (v0 - (gamma + vDiv2)) / (2 * gamma);
            B = -1 - A;
            duration = Math.log(Math.abs(A) / eps) / (vDiv2 - gamma);
        }
    }

    public double getDesiredDuration() {
        return duration;
    }

    @Override
    public float getInterpolation(float input) {
        if (input >= 1) {
            return 1;
        }
        double t = input * duration;
        return (float) (oscilative ?
            (A * Math.exp(- vDiv2 * t) * Math.sin(gamma * t + B) + 1) :
            (A * Math.exp((gamma - vDiv2) * t) + B * Math.exp(-(gamma + vDiv2) * t) + 1));
    }
}
