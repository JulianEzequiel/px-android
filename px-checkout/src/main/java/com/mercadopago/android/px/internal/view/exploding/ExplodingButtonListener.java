package com.mercadopago.android.px.internal.view.exploding;

import android.view.View;

public interface ExplodingButtonListener {

    void onExplodingAnimationFinished();

    void onStatusBarColorChange(int primaryColor);

    void onButtonClicked(View view);
}
