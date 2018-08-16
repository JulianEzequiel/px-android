package com.mercadopago.android.px.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.internal.callbacks.CallbackHolder;
import com.mercadopago.android.px.internal.datasource.CheckoutStore;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.CheckoutActivity;
import com.mercadopago.android.px.internal.features.uicontrollers.FontCache;
import com.mercadopago.android.px.internal.tracker.FlowHandler;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.PaymentResult;

/**
 * Main class of this project.
 * It provides access to most of the checkout experience.
 */
@SuppressWarnings("unused")
public class MercadoPagoCheckout {

    public static final int PAYMENT_RESULT_CODE = 7;
    public static final String EXTRA_PAYMENT_RESULT = "EXTRA_PAYMENT_RESULT";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    @NonNull
    private final String publicKey;

    @NonNull
    private final AdvancedConfiguration advancedConfiguration;

    @Nullable
    private final String preferenceId;

    @Nullable
    private final String privateKey;

    @Nullable
    private final PaymentConfiguration paymentConfiguration;

    /* default */ boolean prefetch = false;

    /* default */ MercadoPagoCheckout(final Builder builder) {
        publicKey = builder.publicKey;
        advancedConfiguration = builder.advancedConfiguration;
        preferenceId = builder.preferenceId;
        privateKey = builder.privateKey;
        paymentConfiguration = builder.paymentConfiguration;
        configureCheckoutStore(builder);
        FlowHandler.getInstance().generateFlowId();
        CallbackHolder.getInstance().clean();
    }

    /**
     * Starts checkout experience.
     * When the flows ends it returns a {@link PaymentResult} object that
     * will be returned on {@link Activity#onActivityResult(int, int, Intent)} if success or
     * {@link com.mercadopago.android.px.model.exceptions.MercadoPagoError}
     * <p>
     * will return on {@link Activity#onActivityResult(int, int, Intent)}
     *
     * @param context context needed to start checkout.
     * @param requestCode it's the number that identifies the checkout flow request for
     * {@link Activity#onActivityResult(int, int, Intent)}
     */
    public void startPayment(@NonNull final Context context, final int requestCode) {
        startIntent(context, CheckoutActivity.getIntent(context), requestCode);
    }

    private void configureCheckoutStore(final Builder builder) {
        final CheckoutStore store = CheckoutStore.getInstance();
        store.reset();
    }

    private void startIntent(@NonNull final Context context, @NonNull final Intent checkoutIntent,
        final int requestCode) {
        if (!prefetch) {
            Session.getSession(context).init(this);
        }

        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(checkoutIntent, requestCode);
        } else {
            context.startActivity(checkoutIntent);
        }
    }

    @NonNull
    public AdvancedConfiguration getAdvancedConfiguration() {
        return advancedConfiguration;
    }

    @NonNull
    public String getPublicKey() {
        return publicKey;
    }

    @Nullable
    public String getPreferenceId() {
        return preferenceId;
    }

    @NonNull
    public String getPrivateKey() {
        return TextUtil.isEmpty(privateKey) ? "" : privateKey;
    }

    @Nullable
    public PaymentConfiguration getPaymentConfiguration() {
        return paymentConfiguration;
    }

    @SuppressWarnings("unused")
    public static final class Builder {

        /* default */ @NonNull final String publicKey;

        /* default */ @Nullable final String preferenceId;

        /* default */ @NonNull AdvancedConfiguration advancedConfiguration =
            new AdvancedConfiguration.Builder().build();

        /* default */ @Nullable PaymentConfiguration paymentConfiguration;

        /* default */ @Nullable String privateKey;

        /* default */ @Deprecated String regularFontPath;

        /* default */ @Deprecated String lightFontPath;

        /* default */ @Deprecated String monoFontPath;

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout}
         * {@see  <a href="http://developers.mercadopago.com/">our developers site</a>}
         *
         * @param publicKey merchant public key / collector public key {@see <a href="https://www.mercadopago.com/mla/account/credentials">credentials</a>}
         * @param paymentConfiguration the payment configuration for this checkout.
         */
        public Builder(@NonNull final String publicKey, @NonNull final PaymentConfiguration paymentConfiguration) {
            preferenceId = null;
            this.publicKey = publicKey;
            this.paymentConfiguration = paymentConfiguration;
        }

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout}
         * For more information check the following links
         * {@see <a href="https://www.mercadopago.com/mla/account/credentials">credentials</a>}
         * {@see <a href="https://www.mercadopago.com.ar/developers/es/reference/preferences/_preferences/post/">create preference</a>}
         * @param publicKey merchant public key / collector public key
         * @param preferenceId the preference id that represents the payment information.
         */
        public Builder(@NonNull final String publicKey, @NonNull final String preferenceId) {
            this.publicKey = publicKey;
            this.preferenceId = preferenceId;
        }

        /**
         * Private key provides save card capabilities and account money balance.
         *
         * @param privateKey the user private key
         * @return builder to keep operating
         */
        public Builder setPrivateKey(@NonNull final String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        /**
         * It provides support for custom checkout functionality/ configure special behaviour
         * You can enable/disable several functionality.
         *
         * @param advancedConfiguration your configuration.
         * @return builder to keep operating
         */
        public Builder setAdvancedConfiguration(@NonNull final AdvancedConfiguration advancedConfiguration) {
            this.advancedConfiguration = advancedConfiguration;
            return this;
        }

        /**
         * @return {@link MercadoPagoCheckout} instance
         */
        public MercadoPagoCheckout build() {
            return new MercadoPagoCheckout(this);
        }

        /**
         * //TODO we will add a new mechanism
         *
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        public Builder setCustomLightFont(@NonNull final String lightFontPath, @NonNull final Context context) {
            this.lightFontPath = lightFontPath;
            setCustomFont(context, FontCache.CUSTOM_LIGHT_FONT, this.lightFontPath);
            return this;
        }

        /**
         * //TODO we will add a new mechanism
         *
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        public Builder setCustomRegularFont(@NonNull final String regularFontPath, @NonNull final Context context) {
            this.regularFontPath = regularFontPath;
            setCustomFont(context, FontCache.CUSTOM_REGULAR_FONT, this.regularFontPath);
            return this;
        }

        /**
         * //TODO we will add a new mechanism
         *
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        public Builder setCustomMonoFont(@NonNull final String monoFontPath, final Context context) {
            this.monoFontPath = monoFontPath;
            setCustomFont(context, FontCache.CUSTOM_MONO_FONT, this.monoFontPath);
            return this;
        }

        /**
         * //TODO we will add a new mechanism
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        private void setCustomFont(@NonNull final Context context, final String fontType, final String fontPath) {
            final Typeface typeFace;
            if (!FontCache.hasTypeface(fontType)) {
                typeFace = Typeface.createFromAsset(context.getAssets(), fontPath);
                FontCache.setTypeface(fontType, typeFace);
            }
        }
    }
}