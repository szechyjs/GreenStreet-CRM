package com.goliathonline.android.greenstreetcrm.ui.widget;

import com.goliathonline.android.greenstreetcrm.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * An {@link ImageView} that draws its contents inside a mask and draws a border drawable on top.
 * This is useful for applying a beveled look to image contents, but is also flexible enough for use
 * with other desired aesthetics.
 */
public class BezelImageView extends ImageView {

    private static final String TAG = "BezelImageView";

    private Paint mMaskedPaint;
    private Paint mCopyPaint;

    private Rect mBounds;
    private RectF mBoundsF;

    private Drawable mBorderDrawable;
    private Drawable mMaskDrawable;

    public BezelImageView(Context context) {
        this(context, null);
    }

    public BezelImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezelImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Attribute initialization
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BezelImageView,
                defStyle, 0);

        mMaskDrawable = a.getDrawable(R.styleable.BezelImageView_maskDrawable);
        if (mMaskDrawable == null) {
            mMaskDrawable = getResources().getDrawable(R.drawable.bezel_mask);
        }
        mMaskDrawable.setCallback(this);

        mBorderDrawable = a.getDrawable(R.styleable.BezelImageView_borderDrawable);
        if (mBorderDrawable == null) {
            mBorderDrawable = getResources().getDrawable(R.drawable.bezel_border);
        }
        mBorderDrawable.setCallback(this);

        a.recycle();

        // Other initialization
        mMaskedPaint = new Paint();
        mMaskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        mCopyPaint = new Paint();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        final boolean changed = super.setFrame(l, t, r, b);
        mBounds = new Rect(0, 0, r - l, b - t);
        mBoundsF = new RectF(mBounds);
        mBorderDrawable.setBounds(mBounds);
        mMaskDrawable.setBounds(mBounds);
        return changed;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sc = canvas.saveLayer(mBoundsF, mCopyPaint,
                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
        mMaskDrawable.draw(canvas);
        canvas.saveLayer(mBoundsF, mMaskedPaint, 0);
        super.onDraw(canvas);
        canvas.restoreToCount(sc);
        mBorderDrawable.draw(canvas);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mBorderDrawable.isStateful()) {
            mBorderDrawable.setState(getDrawableState());
        }
        if (mMaskDrawable.isStateful()) {
            mMaskDrawable.setState(getDrawableState());
        }

        // TODO: is this the right place to invalidate?
        invalidate();
    }
}
