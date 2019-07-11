package com.aliya.skeleton.shimmer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.aliya.skeleton.R;

/**
 * ShimmerFrameLayout
 *
 * @author a_liYa
 * @date 2019-07-08 11:13.
 */
public class ShimmerFrameLayout extends FrameLayout {

    private int mTiltDegree = 20;
    private int mStartColor = getColor(R.color.shimmer_start_color);
    private int mCenterColor = getColor(R.color.shimmer_center_color);
    private int mEndColor = getColor(R.color.shimmer_end_color);

    private int mShimmerWidth;
    private int mAnimationDuration = 1200;
    private int mRepeatDelay;
    private boolean mAutoStart = true;
    private ValueAnimator mValueAnimator;

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mShimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ShimmerFrameLayout(Context context) {
        this(context, null);
    }

    public ShimmerFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShimmerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        int shimmerWidth = dip2px(50);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ShimmerLayout,
                    0, 0);
            try {
                mTiltDegree = a.getInteger(R.styleable.ShimmerLayout_shimmer_angle, 20);
                mStartColor = a.getColor(R.styleable.ShimmerLayout_shimmer_start_color,
                        mStartColor);
                mCenterColor = a.getColor(R.styleable.ShimmerLayout_shimmer_center_color,
                        mCenterColor);
                mEndColor = a.getColor(R.styleable.ShimmerLayout_shimmer_end_color, mEndColor);
                shimmerWidth = a.getDimensionPixelSize(R.styleable.ShimmerLayout_shimmer_width,
                        shimmerWidth);

                mAnimationDuration =
                        a.getInteger(R.styleable.ShimmerLayout_shimmer_animation_duration,
                                mAnimationDuration);
                mRepeatDelay = a.getInteger(R.styleable.ShimmerLayout_shimmer_repeat_delay,
                        mRepeatDelay);
                mAutoStart = a.getBoolean(R.styleable.ShimmerLayout_shimmer_auto_start, mAutoStart);
            } finally {
                a.recycle();
            }
        }

        setShimmerWidth(shimmerWidth);
        setLayerType(LAYER_TYPE_HARDWARE, new Paint());
        mShimmerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
    }

    private int getColor(int id) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? getContext().getColor(id) :
                getResources().getColor(id);
    }

    public void setShimmerWidth(int width) {
        if (width > 0) {
            mShimmerWidth = width;
            final int edgeColor = Color.TRANSPARENT;
            mShimmerPaint.setShader(new LinearGradient(0, 0, mShimmerWidth, 0,
                    new int[]{edgeColor, mStartColor, mCenterColor, mEndColor, edgeColor},
                    new float[]{0, 0, 0.5f, 1, 1},
                    Shader.TileMode.CLAMP));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mValueAnimator = ValueAnimator.ofFloat(0f,
                1f + (float) mRepeatDelay / mAnimationDuration);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setDuration(mAnimationDuration);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                postInvalidate();
            }
        });
        mValueAnimator.start();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawShimmer(canvas);
    }

    private void drawShimmer(Canvas canvas) {
        float dx;
        float animatedValue = mValueAnimator != null ? mValueAnimator.getAnimatedFraction() : 0f;
        dx = offset(-calculateMaskWidth() / 2f, getWidth() + calculateMaskWidth() / 2f,
                animatedValue);

        mShaderMatrix.reset();
        mShaderMatrix.setRotate(mTiltDegree, getWidth() / 2f, getHeight() / 2f);
        mShaderMatrix.postTranslate(dx, 0);
        mShimmerPaint.getShader().setLocalMatrix(mShaderMatrix);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mShimmerPaint);
    }

    private float offset(float start, float end, float percent) {
        return start + (end - start) * percent;
    }

    private int calculateMaskWidth() {
        double radian = Math.toRadians(Math.abs(mTiltDegree));
        return (int) (getHeight() * Math.tan(radian) + mShimmerWidth / Math.cos(radian) + 0.5);
    }

    public int dip2px(float dip) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                getResources().getDisplayMetrics()));
    }
}
