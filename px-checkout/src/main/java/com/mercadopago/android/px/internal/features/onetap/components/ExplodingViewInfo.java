package com.mercadopago.android.px.internal.features.onetap.components;

public class ExplodingViewInfo {

    private int scrollViewTopCoordinate;
    public boolean readyToFinishAnim;

    public ExplodingViewInfo(final int scrollViewTopCoordinate, final boolean readyToFinishAnim) {
        this.scrollViewTopCoordinate = scrollViewTopCoordinate;
        this.readyToFinishAnim = readyToFinishAnim;
    }

    public void finishAnim() {
        this.readyToFinishAnim = true;
    }

}
