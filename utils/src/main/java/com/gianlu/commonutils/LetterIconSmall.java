package com.gianlu.commonutils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class LetterIconSmall extends View {
    private final Rect lettersBounds = new Rect();
    private Paint shapePaint;
    private Paint letterPaint;
    private String letters;
    private String profileFileName;

    public LetterIconSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setColorScheme(@ColorRes int colorPrimary, @ColorRes int colorPrimary_shadow) {
        letterPaint = new Paint();
        letterPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        letterPaint.setAntiAlias(true);
        letterPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf"));
        letterPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));

        shapePaint = new Paint();
        shapePaint.setAntiAlias(true);
        shapePaint.setColor(ContextCompat.getColor(getContext(), colorPrimary));
        shapePaint.setShadowLayer(4, 0, 4, ContextCompat.getColor(getContext(), colorPrimary_shadow));
        setLayerType(LAYER_TYPE_SOFTWARE, shapePaint);
    }

    public String getProfileFileName() {
        return profileFileName;
    }

    public void setProfileName(String fileName, String name) {
        profileFileName = fileName;

        if (name == null) {
            setVisibility(GONE);
            invalidate();
            return;
        }

        if (name.length() <= 2)
            letters = name;
        else
            letters = name.substring(0, 2);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int viewWidthHalf = this.getMeasuredWidth() / 2;
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        int radius;
        if (viewWidthHalf > viewHeightHalf)
            radius = viewHeightHalf - 4;
        else
            radius = viewWidthHalf - 4;

        canvas.drawCircle(viewWidthHalf, viewHeightHalf, radius, shapePaint);

        letterPaint.getTextBounds(letters, 0, letters.length(), lettersBounds);

        canvas.drawText(letters, viewWidthHalf - lettersBounds.exactCenterX(), viewHeightHalf - lettersBounds.exactCenterY(), letterPaint);
    }
}
