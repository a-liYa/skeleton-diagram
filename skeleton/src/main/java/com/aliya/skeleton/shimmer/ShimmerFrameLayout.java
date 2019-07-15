package com.aliya.skeleton.shimmer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * ShimmerFrameLayout
 *
 * @author a_liYa
 * @date 2019-07-08 11:13.
 */
public class ShimmerFrameLayout extends FrameLayout{

    private Shimmer mShimmer;
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
        mShimmer = new Shimmer(this, attrs);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mShimmer.drawShimmer(canvas);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mShimmer.onVisibilityChanged(changedView, visibility);
    }

}
