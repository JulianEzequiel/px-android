package com.mercadopago.android.px.internal.features.onetap;

import android.support.annotation.NonNull;
import android.util.Log;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.base.ResourcesProvider;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentServiceHandler;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

/* default */ class OneTapPresenter extends MvpPresenter<OneTap.View, ResourcesProvider>
    implements OneTap.Actions, PaymentServiceHandler {

    @NonNull private final OneTapModel model;
    @NonNull private final PaymentRepository paymentRepository;

    /* default */ OneTapPresenter(@NonNull final OneTapModel model,
        @NonNull final PaymentRepository paymentRepository) {
        this.model = model;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void confirmPayment() {
        getView().trackConfirm(model);
        paymentRepository.startOneTapPayment(model, this);
    }

    @Override
    public void changePaymentMethod() {
        getView().changePaymentMethod();
    }

    @Override
    public void onAmountShowMore() {
        getView().trackModal(model);
        getView().showDetailModal(model);
    }

    public void cancel() {
        if (isViewAttached()) {
            getView().cancel();
            getView().trackCancel();
        }
    }

    @Override
    public void onTokenResolved() {
        if (isViewAttached()) {
            confirmPayment();
        }
    }

    @Override
    public void onPaymentFinished(@NonNull final Payment payment) {
        if (isViewAttached()) {
            getView().showPaymentResult(payment);
        }
    }

    /**
     * When there is no visual interaction needed this callback is called.
     *
     * @param genericPayment plugin payment.
     */
    @Override
    public void onPaymentFinished(@NonNull final GenericPayment genericPayment) {
        if (isViewAttached()) {
            getView().showPaymentResult(genericPayment);
        }
    }

    /**
     * When there is no visual interaction needed this callback is called.
     *
     * @param businessPayment plugin payment.
     */
    @Override
    public void onPaymentFinished(@NonNull final BusinessPayment businessPayment) {
        if (isViewAttached()) {
            getView().showBusinessResult(businessPayment);
        }
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        //TODO add recovery handling to payment service?
//        resolvePaymentError(error, paymentRepository.getPaymentData());
        if (isViewAttached()) {
            getView().showErrorView(error);
        }
    }

    @Override
    public void cancelPayment() {
        if (isViewAttached()) {
            //TODO do something.
        }
    }

    @Override
    public void onVisualPayment() {
        if (isViewAttached()) {
            getView().showPaymentProcessor();
        }
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        if (isViewAttached()) {
            getView().showCardFlow(model, card);
        }
    }

    @Override
    public void onPaymentMethodRequired() {
        //TODO definition
        Log.d(OneTapPresenter.class.getName(), "Should not happen. - onPaymentMethodRequired");
        cancel();
    }

    @Override
    public void onCardError() {
        //TODO definition
        Log.d(OneTapPresenter.class.getName(), "Should not happen. - onCardError");
        cancel();
    }

    @Override
    public void onIssuerRequired() {
        //TODO definition
        Log.d(OneTapPresenter.class.getName(), "Should not happen. - onIssuerRequired");
        cancel();
    }

    @Override
    public void onPayerCostRequired() {
        //TODO definition
        Log.d(OneTapPresenter.class.getName(), "Should not happen. - onPayerCostRequired");
        cancel();
    }

    @Override
    public void onTokenRequired() {
        //TODO definition
        Log.d(OneTapPresenter.class.getName(), "Should not happen. - onPayerCostRequired");
        cancel();
    }

    @Override
    public void cardVaultCanceled() {
        //TODO?
    }

}
