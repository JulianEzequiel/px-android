package com.mercadopago.android.px.internal.datasource;

import com.mercadopago.android.px.internal.features.hooks.CheckoutHooks;
import com.mercadopago.android.px.internal.features.hooks.Hook;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentResult;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public final class CheckoutStore {

    private static final CheckoutStore INSTANCE = new CheckoutStore();

    private CheckoutHooks checkoutHooks;

    //App state
    private Hook hook;
    private final Map<String, Object> data = new HashMap<>();

    //Payment
    private PaymentResult paymentResult;
    private Payment payment;

    private CheckoutStore() {
    }

    public static CheckoutStore getInstance() {
        return INSTANCE;
    }

    public Hook getHook() {
        return hook;
    }

    public void setHook(final Hook hook) {
        this.hook = hook;
    }

    public CheckoutHooks getCheckoutHooks() {
        return checkoutHooks;
    }

    public Map<String, Object> getData() {
        return data;
    }


    public Payment getPayment() {
        return payment;
    }

    public void setPayment(final Payment payment) {
        this.payment = payment;
    }

    public PaymentResult getPaymentResult() {
        return paymentResult;
    }

    public void setPaymentResult(final PaymentResult paymentResult) {
        this.paymentResult = paymentResult;
    }

    public void reset() {
        paymentResult = null;
        payment = null;
    }
}