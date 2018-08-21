package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class GenericPayment implements IPayment {

    public final Long id;
    public final String status;
    public final String statusDetail;
    @Nullable public final String statementDescription;

    public GenericPayment(final Long paymentId,
        @NonNull final String status,
        @NonNull final String statusDetail) {
        id = paymentId;
        this.status = status;
        this.statusDetail = processStatusDetail(status, statusDetail);
        statementDescription = null;
    }

    //TODO verify with IOS statement description.
    public GenericPayment(final Long paymentId,
        @NonNull final String status,
        @NonNull final String statusDetail,
        @NonNull final String statementDescription) {
        id = paymentId;
        this.status = status;
        this.statusDetail = processStatusDetail(status, statusDetail);
        this.statementDescription = statementDescription;
    }

    /**
     * Resolve the status type, it transforms a generic status and detail
     * into a known status detail
     * {@link Payment.StatusDetail }
     *
     * @param status the payment status type
     * @param statusDetail the payment detail type
     * @return an status detail type
     */
    private String processStatusDetail(@NonNull final String status, @NonNull final String statusDetail) {

        if (Payment.StatusCodes.STATUS_APPROVED.equals(status)) {
            return Payment.StatusDetail.STATUS_DETAIL_APPROVED_PLUGIN_PM;
        }

        if (Payment.StatusCodes.STATUS_REJECTED.equals(status)) {

            if (Payment.StatusDetail.isKnownErrorDetail(statusDetail)) {
                return statusDetail;
            } else {
                return Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_PLUGIN_PM;
            }
        }

        return statusDetail;
    }

    @Nullable
    @Override
    public Long getId() {
        return id;
    }

    @Nullable
    @Override
    public String getStatementDescription() {
        return statementDescription;
    }

    @NonNull
    @Override
    public String getPaymentStatus() {
        return status;
    }

    @NonNull
    @Override
    public String getPaymentStatusDetail() {
        return statusDetail;
    }

    public static GenericPayment from(final IPayment payment) {
        return new GenericPayment(payment.getId(),
            payment.getPaymentStatus(),
            payment.getPaymentStatusDetail(),
            payment.getStatementDescription());
    }
}