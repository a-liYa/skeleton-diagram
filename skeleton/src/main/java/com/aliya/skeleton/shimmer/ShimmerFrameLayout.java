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
import android.view.View;
import android.widget.FrameLayout;

import com.aliya.skeleton.R;

/**
 * ShimmerFrameLayout
 *
 * @author a_liYa
 * @date 2019-07-08 11:13.
 */
public class ShimmerFrameLayout extends FrameLayout{

    private int mTiltDegree = 20; // 垂直倾斜度 顺时针为正 : |/  逆时针为负 : \|
    private int mStartColor = getColor(R.color.shimmer_start_color);
    private int mCenterColor = getColor(R.color.shimmer_center_color);
    private int mEndColor = getColor(R.color.shimmer_end_color);

    private int mShimmerWidth;
    private int mAnimationDuration = 1200;
    private int mRepeatDelay;
    private float mAnimatedPercent;
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
            final TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.ShimmerLayout, 0, 0);
            try {
                mTiltDegree = a.getInteger(R.styleable.ShimmerLayout_shimmer_angle, mTiltDegree);
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
            mShimmerPaint.setShader(
                    new LinearGradient(0, 0, mShimmerWidth, 0,
                            new int[]{edgeColor, mStartColor, mCenterColor, mEndColor, edgeColor},
                            new float[]{0, 0, 0.5f, 1, 1},
                            Shader.TileMode.CLAMP));
        }
    }

    public void startShimmer() {
        mAutoStart = true;
        if (mValueAnimator != null && !isShimmerStarted()) {
            mValueAnimator.start();
        }
    }

    public void stopShimmer() {
        if (mValueAnimator != null && isShimmerStarted()) {
            mValueAnimator.cancel();
        }
    }

    public boolean isShimmerStarted() {
        return mValueAnimator != null && mValueAnimator.isStarted();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        maybeStartShimmer();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShimmer();
    }

    void maybeStartShimmer() {
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofFloat(0f,
                    1f + (float) mRepeatDelay / mAnimationDuration);
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.setDuration(mAnimationDuration + mRepeatDelay);

            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
        }
        if (mAutoStart && mShimmerWidth > 0 && getVisibility() == VISIBLE) {
            startShimmer();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mValueAnimator != null) {
            if (visibility == VISIBLE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (mValueAnimator.isPaused()) {
                        mValueAnimator.resume();
                    }
                } else {
                    maybeStartShimmer();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (!mValueAnimator.isPaused()) {
                        mValueAnimator.pause();
                    }
                } else {
                    stopShimmer();
                }
            }
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawShimmer(canvas);
    }

    private void drawShimmer(Canvas canvas) {
        if (mShimmerWidth <= 0 || !isShimmerStarted()) return;

        final float animatedValue = (float) mValueAnimator.getAnimatedValue();
        if (mAnimatedPercent > 1f && animatedValue > 1f) { // 动画间隔期间
            return;
        }
        mAnimatedPercent = animatedValue;
        final int width = getWidth();
        final int height = getHeight();
        final double tiltRadian = Math.toRadians(mTiltDegree);
        final float shimmerRangeWidth = (float) (height * Math.tan(tiltRadian) +
                mShimmerWidth / Math.cos(tiltRadian)); // |// = |/ + //
        final float dx =
                offset(-shimmerRangeWidth / 2f, width + shimmerRangeWidth / 2f, mAnimatedPercent);

        mShaderMatrix.reset();
        mShaderMatrix.setRotate(mTiltDegree, width / 2f, height / 2f);
        mShaderMatrix.postTranslate(dx, 0);
        mShimmerPaint.getShader().setLocalMatrix(mShaderMatrix);

        canvas.drawRect(0, 0, width, height, mShimmerPaint);
    }

    private float offset(float start, float end, float percent) {
        return start + (end - start) * percent;
    }

    public int dip2px(float dip) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                getResources().getDisplayMetrics()));
    }
}
