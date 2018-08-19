package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.PaymentData;

public interface PaymentRepository {

    void startPayment(@NonNull final PaymentServiceHandler paymentServiceHandler);

    void startOneTapPayment(@NonNull final OneTapModel oneTapModel, @NonNull final PaymentServiceHandler paymentServiceHandler);

    //TODO remove duplication - Presenter Checkout
    PaymentData getPaymentData();
}
