package com.mercadopago.android.px.internal.features.onetap.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.ReviewAndConfirmConfiguration;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.onetap.OneTap;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.LineSeparatorType;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.ButtonPrimary;
import com.mercadopago.android.px.internal.view.TermsAndConditionsComponent;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;

public class OneTapView extends LinearLayout {

    public OneTapModel oneTapModel;
    public OneTap.Actions actions;

    private View amountContainer;
    private View confirmButton;

    public OneTapView(final Context context) {
        this(context, null);
    }

    public OneTapView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OneTapView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OneTapView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setOneTapModel(@NonNull final OneTapModel model, @NonNull final OneTap.Actions callBack) {
        oneTapModel = model;
        actions = callBack;
        addItems();
        amountContainer = createAmountView();
        addView(amountContainer);
        addPaymentMethod();
        addTermsAndConditions();
        confirmButton = addConfirmButton();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
    }

    private void addItems() {
        final Session session = Session.getSession(getContext());
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final PaymentSettingRepository configuration = configurationModule.getPaymentSettings();

        final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration =
            configuration.getAdvancedConfiguration().getReviewAndConfirmConfiguration();

        final Integer collectorIcon = reviewAndConfirmConfiguration.getCollectorIcon();
        final String defaultMultipleTitle = getContext().getString(R.string.px_review_summary_products);
        final int icon = collectorIcon == null ? R.drawable.px_review_item_default : collectorIcon;
        final String itemsTitle = com.mercadopago.android.px.model.Item
            .getItemsTitle(configuration.getCheckoutPreference().getItems(), defaultMultipleTitle);

        final View view = new CollapsedItem(new CollapsedItem.Props(icon, itemsTitle)).render(this);
        addView(view);
    }

    public void update(@NonNull final OneTapModel model) {
        oneTapModel = model;
        //Update Amount
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).equals(amountContainer)) {
                removeViewAt(i);
                amountContainer = createAmountView();
                addView(amountContainer, i);
            }
        }
    }

    private View createAmountView() {
        final Session session = Session.getSession(getContext());
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final PaymentSettingRepository configuration = configurationModule.getPaymentSettings();
        final DiscountRepository discountRepository = session.getDiscountRepository();
        final Amount.Props props = Amount.Props.from(oneTapModel, configuration, discountRepository);
        return new Amount(props, actions).render(this);
    }

    private void addPaymentMethod() {
        final Session session = Session.getSession(getContext());
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final PaymentSettingRepository configuration = configurationModule.getPaymentSettings();
        final DiscountRepository discountRepository = session.getDiscountRepository();

        final View view =
            new PaymentMethod(PaymentMethod.Props.createFrom(oneTapModel, configuration, discountRepository),
                actions).render(this);
        addView(view);
    }

    private void addTermsAndConditions() {
        final Session session = Session.getSession(getContext());
        final DiscountRepository discountRepository = session.getDiscountRepository();
        final Campaign campaign = discountRepository.getCampaign();
        if (campaign != null) {
            TermsAndConditionsModel model = new TermsAndConditionsModel(campaign.getCampaignTermsUrl(),
                getContext().getString(R.string.px_discount_terms_and_conditions_message),
                getContext().getString(R.string.px_discount_terms_and_conditions_linked_message),
                oneTapModel.getPublicKey(),
                LineSeparatorType.NONE);
            final View view = new TermsAndConditionsComponent(model)
                .render(this);
            addView(view);
        }
    }

    private View addConfirmButton() {
        final Session session = Session.getSession(getContext());
        final DiscountRepository discountRepository = session.getDiscountRepository();
        final Discount discount = discountRepository.getDiscount();

        final String confirm = getContext().getString(R.string.px_confirm);
        final Button.Actions buttonActions = new Button.Actions() {
            @Override
            public void onClick(final Action action) {
                //Do nothing
            }

            @Override
            public void onClick(final int yButtonPosition, final int buttonHeight) {
                if (actions != null) {
                    actions.confirmPayment(yButtonPosition, buttonHeight);
                }
            }
        };

        final Button button = new ButtonPrimary(new Button.Props(confirm), buttonActions);
        final View view = button.render(this);
        final int resMargin = discount != null ? R.dimen.px_zero_height : R.dimen.px_m_margin;
        ViewUtils.setMarginTopInView(view, getContext().getResources().getDimensionPixelSize(resMargin));
        addView(view);
        return view;
    }

    public void hideConfirmButton() {
        confirmButton.setVisibility(INVISIBLE);
    }

    public void showButton() {
        if (confirmButton != null) {
            confirmButton.setVisibility(VISIBLE);
        }
    }
}
