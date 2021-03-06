package com.mercadopago.android.px.tracker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.plugins.PaymentMethodPlugin;
import java.util.List;
import java.util.Set;

/**
 * Created by marlanti on 3/12/18.
 */

public final class TrackingFormatter {

    private static final int MAX_LENGTH = 3000;
    private static final String internPrefix = ":";
    private static String externPrefix = "";
    private static final String ESC_PREFIX = "ESC";

    private TrackingFormatter() {
    }

    public static String getFormattedPaymentMethodsForTracking(@Nullable final PaymentMethodSearch paymentMethodSearch,
        @NonNull final Iterable<PaymentMethodPlugin> plugins, final Set<String> escCardIds) {

        StringBuilder formatted = new StringBuilder(MAX_LENGTH);

        externPrefix = "";

        if (paymentMethodSearch != null) {
            final List<PaymentMethod> paymentMethods = paymentMethodSearch.getPaymentMethods();
            formatted = formatPaymentMethods(formatted, paymentMethods);
            final List<CustomSearchItem> customSearchItems = paymentMethodSearch.getCustomSearchItems();
            formatted = formatSavedCards(formatted, customSearchItems, escCardIds);
        }

        formatted = formatPaymentMethodPlugins(formatted, plugins);

        return formatted.toString();
    }

    private static StringBuilder formatSavedCards(@NonNull final StringBuilder formatted,
        @NonNull final List<CustomSearchItem> customSearchItems,
        final Set<String> escCardIds) {

        for (CustomSearchItem customSearchItem : customSearchItems) {
            formatted.append(externPrefix);
            formatted.append(customSearchItem.getPaymentMethodId());
            formatted.append(internPrefix);
            formatted.append(customSearchItem.getType());
            formatted.append(internPrefix);
            formatted.append(customSearchItem.getId());
            if (escCardIds != null && escCardIds.contains(customSearchItem.getId())) {
                formatted.append(internPrefix);
                formatted.append(ESC_PREFIX);
            }
            externPrefix = "|";
        }

        return formatted;
    }

    private static StringBuilder formatPaymentMethods(@NonNull final StringBuilder formatted,
        @Nullable final Iterable<PaymentMethod> paymentMethods) {
        if (paymentMethods != null) {
            for (final PaymentMethod p : paymentMethods) {
                formatted.append(externPrefix);
                formatted.append(p.getId());
                formatted.append(internPrefix);
                formatted.append(p.getPaymentTypeId());
                externPrefix = "|";
            }
        }
        return formatted;
    }

    private static StringBuilder formatPaymentMethodPlugins(@NonNull final StringBuilder formatted,
        @NonNull final Iterable<PaymentMethodPlugin> plugins) {

        for (final PaymentMethodPlugin info : plugins) {
            formatted.append(externPrefix);
            formatted.append(info.getId());
            formatted.append(internPrefix);
            formatted.append(PaymentTypes.PLUGIN);
            externPrefix = "|";
        }

        return formatted;
    }
}
