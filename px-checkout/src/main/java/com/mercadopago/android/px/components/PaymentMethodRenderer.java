package com.mercadopago.android.px.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.customviews.MPTextView;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.util.ResourceUtil;
import com.mercadopago.android.px.util.TextUtils;
import java.util.Locale;

public class PaymentMethodRenderer extends Renderer<PaymentMethodComponent> {

    @Override
    public View render(@NonNull final PaymentMethodComponent component, @NonNull final Context context,
        final ViewGroup parent) {

        final View paymentMethodView = inflate(R.layout.px_payment_method_component, parent);
        final ViewGroup paymentMethodViewGroup = paymentMethodView.findViewById(R.id.mpsdkPaymentMethodContainer);
        final ImageView imageView = paymentMethodView.findViewById(R.id.mpsdkPaymentMethodIcon);
        final MPTextView descriptionTextView = paymentMethodView.findViewById(R.id.mpsdkPaymentMethodDescription);
        final MPTextView statementDescriptionTextView = paymentMethodView.findViewById(R.id.mpsdkStatementDescription);

        addTotalAmountContainer(component, context, paymentMethodView);

        final PaymentMethodComponent.PaymentMethodProps props = component.props;
        imageView.setImageDrawable(
            ContextCompat.getDrawable(context, ResourceUtil.getIconResource(context, props.paymentMethod.getId())));
        setText(descriptionTextView,
            getDescription(props.paymentMethod.getName(), props.paymentMethod.getPaymentTypeId(),
                props.lastFourDigits, context));
        setText(statementDescriptionTextView, getDisclaimer(props.paymentMethod.getPaymentTypeId(),
            props.disclaimer, context));

        stretchHeight(paymentMethodViewGroup);
        return paymentMethodView;
    }

    private void addTotalAmountContainer(final @NonNull PaymentMethodComponent component,
        final @NonNull Context context,
        final View paymentMethodView) {

        final FrameLayout totalAmountContainer = paymentMethodView.findViewById(R.id.mpsdkTotalAmountContainer);
        RendererFactory.create(context, getTotalAmountComponent(component.props.totalAmountProps))
            .render(totalAmountContainer);
    }

    private Component getTotalAmountComponent(final TotalAmount.TotalAmountProps totalAmountProps) {
        return new TotalAmount(totalAmountProps);
    }

    @VisibleForTesting
    String getDisclaimer(final String paymentMethodTypeId, final String disclaimer, final Context context) {
        if (PaymentTypes.isCardPaymentType(paymentMethodTypeId) && TextUtils.isNotEmpty(disclaimer)) {
            return String.format(context.getString(R.string.px_text_state_account_activity_congrats), disclaimer);
        }
        return "";
    }

    @VisibleForTesting
    String getDescription(final String paymentMethodName,
        final String paymentMethodType,
        final String lastFourDigits,
        final Context context) {
        if (PaymentTypes.isCardPaymentType(paymentMethodType)) {
            return String.format(Locale.getDefault(), "%s %s %s",
                paymentMethodName,
                context.getString(R.string.px_ending_in),
                lastFourDigits);
        } else {
            return paymentMethodName;
        }
    }
}
