package com.mercadopago.android.px.internal.features.plugins;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.IPayment;

import static com.mercadopago.android.px.utils.PaymentUtils.getBusinessPaymentApproved;

public class SamplePaymentProcessorWithoutFragment implements PaymentProcessor {

    private static final int CONSTANT_DELAY_MILLIS = 2000;
    private final IPayment iPayment;
    private final Handler handler = new Handler();

    public SamplePaymentProcessorWithoutFragment(final IPayment iPayment) {
        this.iPayment = iPayment;
    }

    public SamplePaymentProcessorWithoutFragment() {
        iPayment = getBusinessPaymentApproved();
    }

    @Override
    public void startPayment(@NonNull final CheckoutData data, @NonNull final Context context,
        @NonNull final OnPaymentListener paymentListener) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (iPayment instanceof BusinessPayment) {
                    paymentListener.onPaymentFinished((BusinessPayment) iPayment);
                } else if (iPayment instanceof GenericPayment) {
                    paymentListener.onPaymentFinished((GenericPayment) iPayment);
                }
            }
        }, CONSTANT_DELAY_MILLIS);
    }

    @Override
    public int getPaymentTimeout() {
        return CONSTANT_DELAY_MILLIS;
    }

    @Override
    public boolean shouldShowFragmentOnPayment() {
        return false;
    }

    @Nullable
    @Override
    public Bundle getFragmentBundle(@NonNull final CheckoutData data, @NonNull final Context context) {
        return null;
    }

    @Nullable
    @Override
    public Fragment getFragment(@NonNull final CheckoutData data, @NonNull final Context context) {
        return null;
    }
}
