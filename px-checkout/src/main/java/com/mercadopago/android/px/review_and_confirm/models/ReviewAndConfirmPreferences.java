package com.mercadopago.android.px.review_and_confirm.models;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.components.CustomComponent;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.ExternalFragment;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.math.BigDecimal;

public class ReviewAndConfirmPreferences {

    @Nullable
    private final ExternalFragment topFragment;

    @Nullable
    private final ExternalFragment bottomFragment;

    private final boolean itemsEnabled;

    private final String disclaimerText;

    // region deprecated
    @Deprecated
    private final CustomComponent topComponent;
    @Deprecated
    private final CustomComponent bottomComponent;
    @Deprecated
    @DrawableRes
    @Nullable private final Integer collectorIcon;
    @Deprecated
    private final String quantityLabel;
    @Deprecated
    private final String unitPriceLabel;
    @Deprecated
    private final BigDecimal productAmount;
    @Deprecated
    private final BigDecimal shippingAmount;
    @Deprecated
    private final BigDecimal arrearsAmount;
    @Deprecated
    private final BigDecimal taxesAmount;
    @Deprecated
    private final BigDecimal discountAmount;
    @Deprecated
    private final BigDecimal totalAmount;
    @Deprecated
    private final String productTitle;
    @Deprecated
    private final String disclaimerTextColor;
    // endregion deprecated

    ReviewAndConfirmPreferences(final Builder builder) {
        itemsEnabled = builder.itemsEnabled;
        topFragment = builder.topFragment;
        bottomFragment = builder.bottomFragment;
        disclaimerText = builder.disclaimerText;

        topComponent = builder.topView;
        collectorIcon = builder.collectorIcon;
        quantityLabel = builder.quantityLabel;
        unitPriceLabel = builder.unitPriceLabel;
        productAmount = builder.productAmount;
        shippingAmount = builder.shippingAmount;
        arrearsAmount = builder.arrearsAmount;
        taxesAmount = builder.taxesAmount;
        discountAmount = builder.discountAmount;
        productTitle = builder.productTitle;
        bottomComponent = builder.bottomView;
        disclaimerTextColor = builder.disclaimerTextColor;
        totalAmount = calculateTotalAmount();
    }

    public boolean hasItemsEnabled() {
        return itemsEnabled;
    }

    public boolean hasCustomTopView() {
        return topFragment != null;
    }

    public boolean hasCustomBottomView() {
        return bottomFragment != null;
    }

    @Nullable
    public ExternalFragment getTopFragment() {
        return topFragment;
    }

    @Nullable
    public ExternalFragment getBottomFragment() {
        return bottomFragment;
    }

    @Deprecated
    public boolean hasProductAmount() {
        return productAmount != null && productAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Deprecated
    public boolean hasExtrasAmount() {
        return shippingAmount != null && shippingAmount.compareTo(BigDecimal.ZERO) > 0
            || arrearsAmount != null && arrearsAmount.compareTo(BigDecimal.ZERO) > 0
            || taxesAmount != null && taxesAmount.compareTo(BigDecimal.ZERO) > 0
            || discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0
            || productAmount != null && productAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Deprecated
    public CustomComponent getTopComponent() {
        return topComponent;
    }

    @Deprecated
    public CustomComponent getBottomComponent() {
        return bottomComponent;
    }

    @DrawableRes
    @Nullable
    @Deprecated
    public Integer getCollectorIcon() {
        return collectorIcon;
    }

    @Deprecated
    public String getQuantityLabel() {
        return quantityLabel;
    }

    @Deprecated
    public String getUnitPriceLabel() {
        return unitPriceLabel;
    }

    @Deprecated
    public BigDecimal getProductAmount() {
        return productAmount;
    }

    @Deprecated
    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    @Deprecated
    public BigDecimal getArrearsAmount() {
        return arrearsAmount;
    }

    @Deprecated
    public BigDecimal getTaxesAmount() {
        return taxesAmount;
    }

    @Deprecated
    public String getDisclaimerText() {
        return disclaimerText;
    }

    @Deprecated
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    @Deprecated
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    @Deprecated
    public String getProductTitle() {
        return productTitle;
    }

    @Deprecated
    public String getDisclaimerTextColor() {
        return disclaimerTextColor;
    }

    @Deprecated
    private BigDecimal calculateTotalAmount() {
        BigDecimal totalAmount = new BigDecimal(0);
        totalAmount = totalAmount.add(productAmount == null ? new BigDecimal(0) : productAmount);
        totalAmount = totalAmount.add(taxesAmount == null ? new BigDecimal(0) : taxesAmount);
        totalAmount = totalAmount.add(shippingAmount == null ? new BigDecimal(0) : shippingAmount);
        totalAmount = totalAmount.add(arrearsAmount == null ? new BigDecimal(0) : arrearsAmount);
        totalAmount =
            totalAmount.subtract(discountAmount == null ? new BigDecimal(0) : discountAmount);
        return totalAmount;
    }

    @SuppressWarnings("unused")
    public static class Builder {

        @Nullable
        ExternalFragment topFragment;

        @Nullable
        ExternalFragment bottomFragment;

        boolean itemsEnabled = true;

        String disclaimerText;

        // region deprecated
        @Deprecated
        BigDecimal discountAmount;
        @Deprecated
        BigDecimal productAmount;
        @Deprecated
        BigDecimal shippingAmount;
        @Deprecated
        BigDecimal arrearsAmount;
        @Deprecated
        BigDecimal taxesAmount;
        @Deprecated
        String productTitle;
        @Deprecated
        Integer collectorIcon;
        @Deprecated
        String quantityLabel;
        @Deprecated
        CustomComponent topView;
        @Deprecated
        CustomComponent bottomView;
        @Deprecated
        String unitPriceLabel;
        @Deprecated
        String disclaimerTextColor;
        // endregion deprecated

        /**
         * Custom fragment that will appear before payment method information in review and confirm screen.
         *
         * @param zClass Fragment class
         * @param args Bundle for fragment
         */
        public void setTopFragment(@NonNull final Class<? extends Fragment> zClass, @Nullable final Bundle args) {
            topFragment = new ExternalFragment(zClass, args);
        }

        /**
         * Custom fragment that will appear after payment method information in review and confirm screen
         *
         * @param zClass Fragment class
         * @param args Bundle for fragment
         */
        public void setBottomFragment(@NonNull final Class<? extends Fragment> zClass, @Nullable final Bundle args) {
            bottomFragment = new ExternalFragment(zClass, args);
        }

        /**
         * Set disclaimer text that will appear in the bottom of review and confirm summary.
         *
         * @param disclaimerText the disclaimer text
         * @return builder
         */
        public Builder setDisclaimerText(final String disclaimerText) {
            this.disclaimerText = disclaimerText;
            return this;
        }

        /**
         * Hide items view in review and confirm screen.
         *
         * @return builder
         */
        public Builder disableItems() {
            itemsEnabled = false;
            return this;
        }

        public ReviewAndConfirmPreferences build() {
            return new ReviewAndConfirmPreferences(this);
        }

        // region deprecated

        /**
         * Set custom icon that will appear in items view.
         * It appears only if the item doesn't have a picture url in it
         *
         * @param collectorIcon drawable that will be shown in items view
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link Item#getPictureUrl()}
         */
        @Deprecated
        public Builder setCollectorIcon(@DrawableRes final int collectorIcon) {
            this.collectorIcon = collectorIcon;
            return this;
        }

        /**
         * Set a custom text that displays in the unit price label of the items view.
         * It appears only if there are multiple items to show, or
         * if the item's quantity is greater than 1
         *
         * @param unitPriceLabel the text that will be shown in the unit price label
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link Item#getUnitPrice()}
         */
        @Deprecated
        public Builder setUnitPriceLabel(final String unitPriceLabel) {
            this.unitPriceLabel = unitPriceLabel;
            return this;
        }

        /**
         * Set product title that will appear in the top of review and confirm summary.
         *
         * @param productTitle the product title
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link Item#getTitle()}
         */
        @Deprecated
        public Builder setProductTitle(final String productTitle) {
            this.productTitle = productTitle;
            return this;
        }

        /**
         * Set product amount that will appear in the top of review and confirm summary.
         *
         * @param productAmount the product amount
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link CheckoutPreference#getTotalAmount()}
         */
        @Deprecated
        public Builder setProductAmount(final BigDecimal productAmount) {
            this.productAmount = productAmount;
            return this;
        }

        /**
         * Set shipping amount that will appear in the top of review and confirm summary.
         *
         * @param shippingAmount the shipping amount
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link } TODO see where we will place this.
         */
        @Deprecated
        public Builder setShippingAmount(final BigDecimal shippingAmount) {
            this.shippingAmount = shippingAmount;
            return this;
        }

        /**
         * Set arrears amount that will appear in the top of review and confirm summary.
         *
         * @param arrearsAmount the arrears amount
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link } TODO see where we will place this.
         */
        @Deprecated
        public Builder setArrearsAmount(final BigDecimal arrearsAmount) {
            this.arrearsAmount = arrearsAmount;
            return this;
        }

        /**
         * Set taxes amount that will appear in the top of review and confirm summary.
         *
         * @param taxesAmount the taxes amount
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link } TODO see where we will place this.
         */
        @Deprecated
        public Builder setTaxesAmount(final BigDecimal taxesAmount) {
            this.taxesAmount = taxesAmount;
            return this;
        }

        /**
         * Set discount amount that will appear in the top of review and confirm summary.
         *
         * @param discountAmount the discount amount
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link com.mercadopago.android.px.core.MercadoPagoCheckout.Builder#setDiscount(Discount, Campaign)}
         */
        @Deprecated
        public Builder setDiscountAmount(final BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        /**
         * Set a custom text that displays in the quantity label of the items view.
         * It appears only if the item's quantity is greater than 1
         *
         * @param quantityLabel the text that will be shown in the quantity label
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link Item#getQuantity()}
         */
        @Deprecated
        public Builder setQuantityLabel(final String quantityLabel) {
            this.quantityLabel = quantityLabel;
            return this;
        }

        /**
         * Set disclaimer text color.
         *
         * @param disclaimerTextColor the disclaimer text color in hex with hashtag
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0. It will not be configurable.
         */
        @Deprecated
        public Builder setDisclaimerTextColor(final String disclaimerTextColor) {
            this.disclaimerTextColor = disclaimerTextColor;
            return this;
        }

        /**
         * Custom view that will appear before payment method description
         * inside Review and confirm screen
         *
         * @param topComponent the top component that renders the top view
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link #setTopFragment(Class, Bundle)}
         */
        @Deprecated
        public Builder setTopComponent(final CustomComponent topComponent) {
            topView = topComponent;
            return this;
        }

        /**
         * Custom view that will appear after payment method description
         * inside Review and confirm screen
         *
         * @param bottomComponent the top bottom component that renders bottom view
         * @return builder
         * @deprecated As of release 4.0.0-beta-35.0.0, replaced by {@link #setBottomFragment(Class, Bundle)}
         */
        @Deprecated
        public Builder setBottomComponent(final CustomComponent bottomComponent) {
            bottomView = bottomComponent;
            return this;
        }

        // endregion deprecated
    }
}
