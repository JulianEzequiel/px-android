package com.mercadopago.android.px.internal.view.exploding;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ExplodeLayout extends FrameLayout {

    LinearLayout linearLayout;

    public ExplodeLayout(final Context context) {
        super(context);
        linearLayout = new LinearLayout(context);
        // linear vertical
    }

    public ExplodeLayout(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public ExplodeLayout(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(final View child) {
        linearLayout.removeAllViews();
        linearLayout.addView(child);
    }

}
