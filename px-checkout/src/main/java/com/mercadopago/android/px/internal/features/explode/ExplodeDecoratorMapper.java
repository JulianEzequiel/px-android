package com.mercadopago.android.px.internal.features.explode;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.viewmodel.mappers.Mapper;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.Payment;

public class ExplodeDecoratorMapper extends Mapper<IPayment, ExplodeDecorator> {

    @Override
    public ExplodeDecorator map(@NonNull final IPayment val) {
        //TODO add warning status and match status with payment result's logic
        if (Payment.StatusCodes.STATUS_APPROVED.equals(val.getPaymentStatus())) {
            return new ExplodeDecorator(R.color.ui_components_success_color, R.color.px_green_status_bar,
                R.drawable.px_ic_payment_success);
        } else if (Payment.isPendingStatus(val.getPaymentStatus(), val.getPaymentStatusDetail())) {
            return new ExplodeDecorator(R.color.ui_components_warning_color, R.color.px_orange_status_bar,
                R.drawable.px_ic_payment_pending);
        } else if (Payment.StatusCodes.STATUS_REJECTED.equals(val.getPaymentStatus())) {
            return new ExplodeDecorator(R.color.ui_components_error_color, R.color.px_red_status_bar,
                R.drawable.px_ic_payment_error);
        } else {
            return new ExplodeDecorator(R.color.ui_components_error_color, R.color.px_red_status_bar,
                R.drawable.px_ic_payment_error);
        }
    }
}
