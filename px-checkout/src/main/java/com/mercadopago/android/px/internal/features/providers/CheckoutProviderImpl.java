package com.mercadopago.android.px.internal.features.providers;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.datasource.MercadoPagoServicesAdapter;
import com.mercadopago.android.px.internal.features.uicontrollers.FontCache;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.EscUtil;
import com.mercadopago.android.px.internal.util.QueryBuilder;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import com.mercadopago.android.px.model.exceptions.ExceptionHandler;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;

public class CheckoutProviderImpl implements CheckoutProvider {

    private final Context context;
    private final MercadoPagoServicesAdapter mercadoPagoServicesAdapter;
    private final MercadoPagoESC mercadoPagoESC;
    private Handler mHandler;

    public CheckoutProviderImpl(@NonNull final Context context,
        @NonNull String publicKey,
        @NonNull String privateKey,
        @NonNull final MercadoPagoESC mercadoPagoESC) {
        this.context = context;
        mercadoPagoServicesAdapter = new MercadoPagoServicesAdapter(context, publicKey, privateKey);
        this.mercadoPagoESC = mercadoPagoESC;
    }

    @Override
    public void fetchFonts() {
        if (!FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            fetchRegularFont();
        }
        if (!FontCache.hasTypeface(FontCache.CUSTOM_MONO_FONT)) {
            fetchMonoFont();
        }
        if (!FontCache.hasTypeface(FontCache.CUSTOM_LIGHT_FONT)) {
            fetchLightFont();
        }
    }

    @Override
    public boolean manageEscForPayment(final PaymentData paymentData, final String paymentStatus,
        final String paymentStatusDetail) {
        if (EscUtil.shouldDeleteEsc(paymentData, paymentStatus,
            paymentStatusDetail)) {
            mercadoPagoESC.deleteESC(paymentData.getToken().getCardId());
        } else if (EscUtil.shouldStoreESC(paymentData, paymentStatus, paymentStatusDetail)) {
            mercadoPagoESC.saveESC(paymentData.getToken().getCardId(), paymentData.getToken().getEsc());
        }
        return EscUtil.isInvalidEscPayment(paymentData, paymentStatus, paymentStatusDetail);
    }

    @Override
    public boolean manageEscForError(final MercadoPagoError error, final PaymentData paymentData) {
        final boolean isInvalidEsc = EscUtil.isErrorInvalidPaymentWithEsc(error, paymentData);
        if (isInvalidEsc) {
            mercadoPagoESC.deleteESC(paymentData.getToken().getCardId());
        }
        return isInvalidEsc;
    }

    private void fetchRegularFont() {
        FontsContractCompat.FontRequestCallback regularFontCallback = new FontsContractCompat
            .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                FontCache.setTypeface(FontCache.CUSTOM_REGULAR_FONT, typeface);
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                //Do nothing
            }
        };
        FontsContractCompat.requestFont(context,
            getFontRequest(FontCache.FONT_ROBOTO, QueryBuilder.WIDTH_DEFAULT,
                QueryBuilder.WEIGHT_DEFAULT, QueryBuilder.ITALIC_DEFAULT),
            regularFontCallback,
            getHandlerThreadHandler());
    }

    private void fetchLightFont() {
        FontsContractCompat.FontRequestCallback lightFontCallback = new FontsContractCompat
            .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                FontCache.setTypeface(FontCache.CUSTOM_LIGHT_FONT, typeface);
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                //Do nothing
            }
        };
        FontsContractCompat.requestFont(context,
            getFontRequest(FontCache.FONT_ROBOTO, QueryBuilder.WIDTH_DEFAULT,
                QueryBuilder.WEIGHT_LIGHT, QueryBuilder.ITALIC_DEFAULT),
            lightFontCallback,
            getHandlerThreadHandler());
    }

    private void fetchMonoFont() {
        FontsContractCompat.FontRequestCallback monoFontCallback = new FontsContractCompat
            .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                FontCache.setTypeface(FontCache.CUSTOM_MONO_FONT, typeface);
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                //Do nothing
            }
        };
        FontsContractCompat.requestFont(context,
            getFontRequest(FontCache.FONT_ROBOTO_MONO, QueryBuilder.WIDTH_DEFAULT,
                QueryBuilder.WEIGHT_DEFAULT, QueryBuilder.ITALIC_DEFAULT),
            monoFontCallback,
            getHandlerThreadHandler());
    }

    private FontRequest getFontRequest(String fontName, int width, int weight, float italic) {
        QueryBuilder queryBuilder = new QueryBuilder(fontName)
            .withWidth(width)
            .withWeight(weight)
            .withItalic(italic)
            .withBestEffort(true);
        String query = queryBuilder.build();

        return new FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            query,
            R.array.com_google_android_gms_fonts_certs);
    }

    private Handler getHandlerThreadHandler() {
        if (mHandler == null) {
            HandlerThread handlerThread = new HandlerThread("fonts");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());
        }
        return mHandler;
    }

    @Override
    public void getCheckoutPreference(String checkoutPreferenceId,
        final TaggedCallback<CheckoutPreference> taggedCallback) {
        mercadoPagoServicesAdapter.getCheckoutPreference(checkoutPreferenceId, new Callback<CheckoutPreference>() {
            @Override
            public void success(CheckoutPreference checkoutPreference) {
                taggedCallback.onSuccess(checkoutPreference);
            }

            @Override
            public void failure(ApiException apiException) {
                taggedCallback.onFailure(new MercadoPagoError(apiException, ApiUtil.RequestOrigin.GET_PREFERENCE));
            }
        });
    }

    @Override
    public String getCheckoutExceptionMessage(CheckoutPreferenceException exception) {
        return ExceptionHandler.getErrorMessage(context, exception);
    }

    @Override
    public String getCheckoutExceptionMessage(final Exception exception) {
        return context.getString(R.string.px_standard_error_message);
    }


    //TODO REMOVE
    /*
    private void createPaymentInMercadoPago(final String transactionId,
                                            final CheckoutPreference checkoutPreference,
                                            final PaymentData paymentData,
                                            final Boolean binaryMode,
                                            final String customerId,
                                            final TaggedCallback<Payment> taggedCallback) {
        final PaymentBody paymentBody = createPaymentBody(transactionId, checkoutPreference, paymentData, binaryMode, customerId);
        mercadoPagoServicesAdapter.createPayment(paymentBody, taggedCallback);
    }

    private PaymentBody createPaymentBody(final String transactionId, final CheckoutPreference checkoutPreference,
                                          final PaymentData paymentData, final Boolean binaryMode, final String customerId) {
        final PaymentBody paymentBody = new PaymentBody();
        paymentBody.setPrefId(checkoutPreference.getId());
        paymentBody.setPublicKey(publicKey);
        paymentBody.setPaymentMethodId(paymentData.getPaymentMethod().getId());
        paymentBody.setBinaryMode(binaryMode);

        final Payer payer = paymentData.getPayer();
        if (!TextUtil.isEmpty(customerId) &&
                MercadoPagoUtil.isCard(paymentData.getPaymentMethod().getPaymentTypeId())) {
            payer.setId(customerId);
        }

        paymentBody.setPayer(payer);

        if (paymentData.getToken() != null) {
            paymentBody.setTokenId(paymentData.getToken().getId());
        }
        if (paymentData.getPayerCost() != null) {
            paymentBody.setInstallments(paymentData.getPayerCost().getInstallments());
        }
        if (paymentData.getIssuer() != null) {
            paymentBody.setIssuerId(paymentData.getIssuer().getId());
        }

        final Discount discount = paymentData.getDiscount();
        if (discount != null) {
            paymentBody.setCampaignId(discount.getId());
            paymentBody.setCouponAmount(discount.getCouponAmount().floatValue());
            if (!TextUtil.isEmpty(paymentData.getCouponCode())) {
                paymentBody.setCouponCode(paymentData.getCouponCode());
            }
        }

        paymentBody.setTransactionId(transactionId);
        return paymentBody;
    }
    */
}