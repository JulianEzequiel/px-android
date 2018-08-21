package com.mercadopago.android.px.internal.viewmodel;

import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import java.io.Serializable;

public final class CheckoutStateModel implements Serializable {

    public Token createdToken;
    public Card selectedCard;

    //TODO FIX BEHAVIOUR with payment repository.
    public Payment createdPayment;
    public boolean paymentMethodEdited;
    public boolean editPaymentMethodFromReviewAndConfirm;
    public PaymentRecovery paymentRecovery;
    public boolean isUniquePaymentMethod;
    public boolean isOneTap;

    public CheckoutStateModel() {
    }
}
