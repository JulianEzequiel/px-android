package com.mercadopago.android.px.util;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.CheckoutStore;
import com.mercadopago.android.px.plugins.PaymentMethodPlugin;

public class ResourceUtil {

    private static final String SDK_PREFIX = "px_";
    private static final String DEF_TYPE_DRAWABLE = "drawable";
    public static final String BANK_SUFFIX = "bank";
    public static final String TINT_PREFIX = "grey_";

    @DrawableRes
    private static int getPaymentMethodIcon(Context context, String id) {
        int resource;
        id = SDK_PREFIX + id;
        try {
            resource = context.getResources().getIdentifier(id, DEF_TYPE_DRAWABLE, context.getPackageName());
        } catch (Exception e) {
            try {
                resource = context.getResources()
                    .getIdentifier(SDK_PREFIX + BANK_SUFFIX, DEF_TYPE_DRAWABLE, context.getPackageName());
            } catch (Exception ex) {
                resource = 0;
            }
        }
        return resource;
    }

    @DrawableRes
    public static int getIconResource(Context context, String id) {
        PaymentMethodPlugin paymentMethodPlugin = CheckoutStore.getInstance().getPaymentMethodPluginById(id);
        int icon;
        try {
            if (paymentMethodPlugin != null) {
                icon = paymentMethodPlugin.getPaymentMethodInfo(context).icon;
            } else {
                icon = getPaymentMethodIcon(context, id);
            }
        } catch (final Resources.NotFoundException e) {
            // Avoid crashes if the image doesn exist return empty default one.
            icon = R.drawable.px_none;
        }
        return icon;
    }
}
