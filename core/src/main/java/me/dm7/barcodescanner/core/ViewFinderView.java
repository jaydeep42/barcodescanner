package me.dm7.barcodescanner.core;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ViewFinderView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;

    private static final float PORTRAIT_WIDTH_RATIO = 6f/8;
    private static final float PORTRAIT_WIDTH_HEIGHT_RATIO = 0.75f;

    private static final float LANDSCAPE_HEIGHT_RATIO = 5f/8;
    private static final float LANDSCAPE_WIDTH_HEIGHT_RATIO = 1.4f;
    private static final int MIN_DIMENSION_DIFF = 50;

    private static final float SQUARE_DIMENSION_RATIO = 5f/8;

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80l;

    private final int mDefaultLaserColor = getResources().getColor(R.color.viewfinder_laser);
    private final int mDefaultMaskColor = getResources().getColor(R.color.viewfinder_mask);
    private final int mDefaultBorderColor = getResources().getColor(R.color.viewfinder_border);
    private final int mDefaultBorderStrokeWidth = getResources().getInteger(R.integer.viewfinder_border_width);
    private final int mDefaultBorderLineLength = getResources().getInteger(R.integer.viewfinder_border_length);

    protected Paint mLaserPaint;
    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected int mBorderLineLength;
    protected boolean mSquareViewFinder;
    private boolean mIsLaserEnabled;

    public ViewFinderView(Context context) {
        super(context);
        init();
    }

    public ViewFinderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        //set up laser paint
        mLaserPaint = new Paint();
        mLaserPaint.setColor(mDefaultLaserColor);
        mLaserPaint.setStyle(Paint.Style.FILL);

        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);

        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);

        mBorderLineLength = mDefaultBorderLineLength;
        mBorderLineLength = 120;
    }

    @Override
    public void setLaserColor(int laserColor) {
        mLaserPaint.setColor(laserColor);
    }

    @Override
    public void setMaskColor(int maskColor) {
        mFinderMaskPaint.setColor(maskColor);
    }

    @Override
    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);
    }

    @Override
    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }

    @Override
    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }

    @Override
    public void setLaserEnabled(boolean isLaserEnabled) { mIsLaserEnabled = isLaserEnabled; }

    // TODO: Need a better way to configure this. Revisit when working on 2.0
    public void setSquareViewFinder(boolean set) {
        mSquareViewFinder = set;
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }

    public Rect getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(getFramingRect() == null) {
            return;
        }

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);

        if (mIsLaserEnabled) {
            drawLaser(canvas);
        }
    }

    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Rect framingRect = getFramingRect();
        
        canvas.drawRect(0, 0, width, framingRect.top, mFinderMaskPaint);
        canvas.drawRect(0, framingRect.top, framingRect.left, framingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(framingRect.right + 1, framingRect.top, width, framingRect.bottom + 1, mFinderMaskPaint);
        canvas.drawRect(0, framingRect.bottom + 1, width, height, mFinderMaskPaint);
    }

    public void drawViewFinderBorder(Canvas canvas) {
        Rect framingRect = getFramingRect();

        float offset = mBorderPaint.getStrokeWidth() / 2;

        // Top-left corner
        canvas.drawLine(framingRect.left - offset, framingRect.top, framingRect.left + mBorderLineLength - offset, framingRect.top, mBorderPaint);
        canvas.drawLine(framingRect.left, framingRect.top, framingRect.left, framingRect.top + mBorderLineLength, mBorderPaint);

        // Top-right corner
        canvas.drawLine(framingRect.right + offset, framingRect.top, framingRect.right - mBorderLineLength + offset, framingRect.top, mBorderPaint);
        canvas.drawLine(framingRect.right, framingRect.top, framingRect.right, framingRect.top + mBorderLineLength, mBorderPaint);

        // Bottom-right corner
        canvas.drawLine(framingRect.right + offset, framingRect.bottom, framingRect.right - mBorderLineLength + offset, framingRect.bottom, mBorderPaint);
        canvas.drawLine(framingRect.right, framingRect.bottom, framingRect.right, framingRect.bottom - mBorderLineLength, mBorderPaint);

        // Bottom-left corner
        canvas.drawLine(framingRect.left - offset, framingRect.bottom, framingRect.left + mBorderLineLength - offset, framingRect.bottom, mBorderPaint);
        canvas.drawLine(framingRect.left, framingRect.bottom, framingRect.left, framingRect.bottom - mBorderLineLength, mBorderPaint);
    }

    public void drawLaser(Canvas canvas) {
        Rect framingRect = getFramingRect();
        
        // Draw a red "laser scanner" line through the middle to show decoding is active
        mLaserPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = framingRect.height() / 2 + framingRect.top;
        canvas.drawRect(framingRect.left + 2, middle - 1, framingRect.right - 1, middle + 2, mLaserPaint);

        postInvalidateDelayed(ANIMATION_DELAY,
                framingRect.left - POINT_SIZE,
                framingRect.top - POINT_SIZE,
                framingRect.right + POINT_SIZE,
                framingRect.bottom + POINT_SIZE);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight());
        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        if(mSquareViewFinder) {
            if(orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (int) (getHeight() * SQUARE_DIMENSION_RATIO);
                width = height;
            } else {
                width = (int) (getWidth() * SQUARE_DIMENSION_RATIO);
                height = width;
            }
        } else {
            if(orientation != Configuration.ORIENTATION_PORTRAIT) {
                height = (int) (getHeight() * LANDSCAPE_HEIGHT_RATIO);
                width = (int) (LANDSCAPE_WIDTH_HEIGHT_RATIO * height);
            } else {
                width = (int) (getWidth() * PORTRAIT_WIDTH_RATIO);
                height = (int) (PORTRAIT_WIDTH_HEIGHT_RATIO * width);
            }
        }

        if(width > getWidth()) {
            width = getWidth() - MIN_DIMENSION_DIFF;
        }

        if(height > getHeight()) {
            height = getHeight() - MIN_DIMENSION_DIFF;
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = (viewResolution.y - height) / 2;
        mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
    }
}

