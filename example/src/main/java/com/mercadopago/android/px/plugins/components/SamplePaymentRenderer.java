package com.mercadopago.android.px.plugins.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.components.Renderer;
import com.mercadopago.example.R;

public class SamplePaymentRenderer extends Renderer<SamplePayment> {

    @Override
    public View render(final SamplePayment component, final Context context, final ViewGroup parent) {
        return inflate(R.layout.px_sample_payment_procesor, parent);
    }
}