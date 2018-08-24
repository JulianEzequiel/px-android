package com.mercadopago.android.px.internal.features.explode;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.Payment;

public class ExplodeParamsMapper extends Mapper<IPayment, ExplodeParams> {

    @Override
    public ExplodeParams map(@NonNull final IPayment val) {
        if (Payment.StatusCodes.STATUS_APPROVED.equals(val.getPaymentStatus())) {
            return new ExplodeParams(R.color.ui_components_success_color, R.color.px_green_status_bar,
                R.drawable.px_badge_check);
        } else if (Payment.isPendingStatus(val.getPaymentStatus(), val.getPaymentStatusDetail())) {
            return new ExplodeParams(R.color.ui_components_warning_color, R.color.px_orange_status_bar,
                R.drawable.px_badge_pending_orange);
        } else if (Payment.StatusCodes.STATUS_REJECTED.equals(val.getPaymentStatus())) {
            return new ExplodeParams(R.color.ui_components_error_color, R.color.px_red_status_bar,
                R.drawable.px_badge_error);
        } else {
            return new ExplodeParams(R.color.ui_components_error_color, R.color.px_red_status_bar,
                R.drawable.px_badge_error);
        }
    }
}
