package com.xugaoxiang.ott.live.view;

import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.xugaoxiang.ott.live.R;

/**
 * Created by user on 2016/10/19.
 */
public class RotaProgressBar extends View{

    private int circleColor;
    private int roundProgressColor;

    private Paint paint = new Paint();
    private float roundProgressWidth;
    private float circleWidth;
    private ValueAnimator valueAnimator;
    private PropertyValuesHolder pvh;

    private TimeInterpolator interpolator = new DecelerateInterpolator();

    public RotaProgressBar(Context context) {
        this(context , null);
    }

    public RotaProgressBar(Context context, AttributeSet attrs) {
        this(context , attrs , 0);
    }

    public RotaProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RotaProgressBar);
        circleColor = typedArray.getColor(R.styleable.RotaProgressBar_circleColor , Color.WHITE);
        roundProgressColor = typedArray.getColor(R.styleable.RotaProgressBar_roundProgressColor , Color.BLUE);
        roundProgressWidth = typedArray.getDimension(R.styleable.RotaProgressBar_roundProgressWidth, 4);
        circleWidth = typedArray.getDimension(R.styleable.RotaProgressBar_circleWidth, 6);
        typedArray.recycle();
        init();
    }

    private void init() {
        pvh = PropertyValuesHolder.ofFloat("sweepAngle", 0, 360.0f);
        valueAnimator = ValueAnimator.ofPropertyValuesHolder(pvh);
        valueAnimator.setInterpolator(interpolator);
        valueAnimator.setDuration(1500);
        valueAnimator.setRepeatCount(Integer.MAX_VALUE);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == GONE || visibility == INVISIBLE) {
            endAnimation();
        } else {
            startAnimation();
        }
        super.setVisibility(visibility);
    }

    public void endAnimation() {
        valueAnimator.end();
        valueAnimator.cancel();
    }

    public void startAnimation() {
        valueAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        float cx = paddingLeft + (width - paddingLeft - paddingRight) / 2.0f;
        float py = paddingTop + (height - paddingTop - paddingBottom) / 2.0f;

        paint.setColor(roundProgressColor);
        paint.setStrokeWidth(roundProgressWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        int radius = (int) ((width - paddingLeft - paddingRight) / 2.0f - roundProgressWidth);
        canvas.drawCircle(cx , py , radius , paint);

        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.save();
        canvas.rotate(((Float) valueAnimator.getAnimatedValue("sweepAngle")).floatValue() , cx , py);
        canvas.drawCircle(cx , paddingTop+circleWidth/2+1 , circleWidth , paint);
        canvas.restore();
    }
}
