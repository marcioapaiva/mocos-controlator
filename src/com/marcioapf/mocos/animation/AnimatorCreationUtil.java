package com.marcioapf.mocos.animation;

import android.R;
import android.graphics.Color;
import android.view.animation.Interpolator;
import android.widget.TextView;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.util.Property;

public class AnimatorCreationUtil {

    private static final Property<TextView[], Integer> textColorProperty =
        new Property<TextView[], Integer>(Integer.class, "textColor") {
            @Override
            public Integer get(TextView[] object) {
                return object[0].getCurrentTextColor();
            }

            @Override
            public void set(TextView[] object, Integer value) {
                for (TextView tv : object) {
                    tv.setTextColor(value);
                }
            }
        };
    private static final TypeEvaluator argEvaluator = new ArgbEvaluator();

    public static final ObjectAnimator ofFloat(Object object,
                                                String propertyName,
                                                long duration,
                                                Interpolator interpolator,
                                                float... values) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(object, propertyName, values);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        return animator;
    }

    public static final ObjectAnimator ofTextColor(TextView tv, long duration, Integer... values) {
        return AnimatorCreationUtil.ofTextColor(new TextView[]{tv}, duration, values);
    }

    public static final ObjectAnimator ofTextColor(TextView[] tvs,
                                                   long duration,
                                                   Integer... values) {
        ObjectAnimator animator = ObjectAnimator.ofObject(tvs, textColorProperty,
            argEvaluator, values);
        animator.setDuration(duration);
        return animator;
    }
}
