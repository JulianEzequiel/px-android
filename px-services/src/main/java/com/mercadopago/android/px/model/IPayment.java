package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface IPayment {

    @Nullable
    Long getId();

    @Nullable
    String getStatementDescription();

    @NonNull
    String getPaymentStatus();

    @NonNull
    String getPaymentStatusDetail();
}
