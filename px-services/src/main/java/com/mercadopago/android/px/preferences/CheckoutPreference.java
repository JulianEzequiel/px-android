package com.mercadopago.android.px.preferences;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import com.google.gson.annotations.SerializedName;
import com.mercadopago.android.px.model.DifferentialPricing;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.services.exceptions.CheckoutPreferenceException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.mercadopago.android.px.services.util.TextUtil.isEmpty;

/**
 * Model that represents curl -X OPTIONS "https://api.mercadopago.com/checkout/preferences" | json_pp
 * It can be not exactly the same because exists custom configurations for open Preference.
 */
@SuppressWarnings("unused")
public class CheckoutPreference implements Serializable {

    /**
     * When the preference comes from backend then
     * id is received - Custom created CheckoutPreferences have null id.
     */
    @SuppressWarnings("UnusedDeclaration")
    @Nullable private String id;

    @SuppressWarnings("UnusedDeclaration")
    @NonNull private final String siteId;

    @SerializedName("differential_pricing")
    @Nullable private final DifferentialPricing differentialPricing;

    @SerializedName("payment_methods")
    private PaymentPreference paymentPreference;

    @NonNull private final List<Item> items;

    @NonNull private final Payer payer;

    @Nullable private final Date expirationDateTo;

    @Nullable private final Date expirationDateFrom;

    //region support external integrations - payment processor instores
    @Nullable private final BigDecimal marketplaceFee;

    @Nullable private final BigDecimal shippingCost;

    @Nullable private final String operationType;

    @Nullable private final BigDecimal conceptAmount;

    @Nullable private final String conceptId;
    //endregion support external integrations

    CheckoutPreference(final Builder builder) {
        items = builder.items;
        expirationDateFrom = builder.expirationDateFrom;
        expirationDateTo = builder.expirationDateTo;
        siteId = builder.site.getId();
        marketplaceFee = builder.marketplaceFee;
        shippingCost = builder.shippingCost;
        operationType = builder.operationType;
        differentialPricing = builder.differentialPricing;
        conceptAmount = builder.conceptAmount;
        conceptId = builder.conceptId;
        payer = new Payer();
        payer.setEmail(builder.payerEmail);

        final PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setExcludedPaymentTypeIds(builder.excludedPaymentTypes);
        paymentPreference.setExcludedPaymentMethodIds(builder.excludedPaymentMethods);
        paymentPreference.setMaxAcceptedInstallments(builder.maxInstallments);
        paymentPreference.setDefaultInstallments(builder.defaultInstallments);

        this.paymentPreference = paymentPreference;
    }

    public void validate() throws CheckoutPreferenceException {
        if (!Item.validItems(items)) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.INVALID_ITEM);
        } else if (!isEmpty(payer.getEmail())) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.NO_EMAIL_FOUND);
        } else if (isExpired()) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.EXPIRED_PREFERENCE);
        } else if (!isActive()) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.INACTIVE_PREFERENCE);
        } else if (!validInstallmentsPreference()) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.INVALID_INSTALLMENTS);
        } else if (!validPaymentTypeExclusion()) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.EXCLUDED_ALL_PAYMENT_TYPES);
        }
    }


    public boolean validPaymentTypeExclusion() {
        return paymentPreference == null || paymentPreference.excludedPaymentTypesValid();
    }

    public boolean validInstallmentsPreference() {
        return paymentPreference == null || paymentPreference.installmentPreferencesValid();
    }

    public Boolean isExpired() {
        final Date date = new Date();
        return expirationDateTo != null && date.after(expirationDateTo);
    }

    public Boolean isActive() {
        final Date date = new Date();
        return expirationDateFrom == null || date.after(expirationDateFrom);
    }

    //region support external integrations - payment processor instores

    @Nullable
    public String getOperationType() {
        return operationType;
    }

    @Nullable
    public BigDecimal getMarketplaceFee() {
        return marketplaceFee;
    }

    @Nullable
    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    @Nullable
    public DifferentialPricing getDifferentialPricing() {
        return differentialPricing;
    }

    @Nullable
    public BigDecimal getConceptAmount() {
        return conceptAmount;
    }

    @Nullable
    public String getConceptId() {
        return conceptId;
    }

    //endregion support external integrations

    /**
     * Sum of value * quantity of listed items in a preference.
     *
     * @return items total amount
     */
    @NonNull
    public BigDecimal getTotalAmount() {
        return Item.getTotalAmountWith(items);
    }

    @NonNull
    public List<String> getExcludedPaymentTypes() {
        if (paymentPreference != null) {
            return paymentPreference.getExcludedPaymentTypes();
        } else {
            return new ArrayList<>();
        }
    }

    public Site getSite() {
        return Sites.getById(siteId);
    }

    @Size(min = 1)
    @NonNull
    public List<Item> getItems() {
        return items;
    }

    @NonNull
    public Payer getPayer() {
        return payer;
    }

    @Nullable
    public Integer getMaxInstallments() {
        if (paymentPreference != null) {
            return paymentPreference.getMaxInstallments();
        } else {
            return null;
        }
    }

    @Nullable
    public Integer getDefaultInstallments() {
        if (paymentPreference != null) {
            return paymentPreference.getDefaultInstallments();
        } else {
            return null;
        }
    }

    @Nullable
    public List<String> getExcludedPaymentMethods() {
        if (paymentPreference != null) {
            return paymentPreference.getExcludedPaymentMethodIds();
        } else {
            return null;
        }
    }

    @Nullable
    public Date getExpirationDateFrom() {
        return expirationDateFrom;
    }

    @Nullable
    public Date getExpirationDateTo() {
        return expirationDateTo;
    }

    @Nullable
    public String getDefaultPaymentMethodId() {
        if (paymentPreference != null) {
            return paymentPreference.getDefaultPaymentMethodId();
        } else {
            return null;
        }
    }

    public PaymentPreference getPaymentPreference() {
        // If payment preference does not exists create one.
        if (paymentPreference == null) {
            paymentPreference = new PaymentPreference();
        }
        return paymentPreference;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public static class Builder {

        //region mandatory params
        /* default */ final List<Item> items;
        /* default */ final Site site;
        /* default */ final String payerEmail;
        //endregion mandatory params

        /* default */ final List<String> excludedPaymentMethods;
        /* default */ final List<String> excludedPaymentTypes;
        /* default */ Integer maxInstallments;
        /* default */ Integer defaultInstallments;
        /* default */ Date expirationDateTo;
        /* default */ Date expirationDateFrom;
        /* default */ BigDecimal marketplaceFee;
        /* default */ BigDecimal shippingCost;
        /* default */ String operationType;
        /* default */ @Nullable DifferentialPricing differentialPricing;
        /* default */ BigDecimal conceptAmount;
        /* default */ String conceptId;

        /**
         * Builder for custom CheckoutPreference construction
         *
         * @param site preference site
         * @param payerEmail payer email
         * @param items items to pay
         */
        public Builder(@NonNull final Site site, @NonNull final String payerEmail,
            @Size(min = 1) @NonNull final List<Item> items) {
            this.items = items;
            this.payerEmail = payerEmail;
            this.site = site;
            excludedPaymentMethods = new ArrayList<>();
            excludedPaymentTypes = new ArrayList<>();
        }

        public Builder addExcludedPaymentMethod(@NonNull final String paymentMethodId) {
            excludedPaymentMethods.add(paymentMethodId);
            return this;
        }

        public Builder addExcludedPaymentMethods(@NonNull final Collection<String> paymentMethodIds) {
            excludedPaymentMethods.addAll(paymentMethodIds);
            return this;
        }

        public Builder addExcludedPaymentType(@NonNull final String paymentTypeId) {
            excludedPaymentTypes.add(paymentTypeId);
            return this;
        }

        public Builder addExcludedPaymentTypes(@NonNull final Collection<String> paymentTypeIds) {
            excludedPaymentTypes.addAll(paymentTypeIds);
            return this;
        }

        public Builder setMaxInstallments(@Nullable final Integer maxInstallments) {
            this.maxInstallments = maxInstallments;
            return this;
        }

        public Builder setDefaultInstallments(@Nullable final Integer defaultInstallments) {
            this.defaultInstallments = defaultInstallments;
            return this;
        }

        public Builder setExpirationDate(@Nullable final Date date) {
            expirationDateTo = date;
            return this;
        }

        public Builder setActiveFrom(@Nullable final Date date) {
            expirationDateFrom = date;
            return this;
        }

        /**
         * Differential pricing configuration for this preference.
         *
         * @param differentialPricing differential pricing object
         * @return builder
         */
        public Builder setDifferentialPricing(@Nullable final DifferentialPricing differentialPricing) {
            this.differentialPricing = differentialPricing;
            return this;
        }

        public Builder setMarketplaceFee(final BigDecimal marketplaceFee) {
            this.marketplaceFee = marketplaceFee;
            return this;
        }

        public Builder setShippingCost(final BigDecimal shippingCost) {
            this.shippingCost = shippingCost;
            return this;
        }

        public Builder setOperationType(final String operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder setConceptAmount(final BigDecimal conceptAmount) {
            this.conceptAmount = conceptAmount;
            return this;
        }

        public Builder setConceptId(final String conceptId) {
            this.conceptId = conceptId;
            return this;
        }

        public CheckoutPreference build() {
            return new CheckoutPreference(this);
        }
    }
}
