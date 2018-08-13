package com.mercadopago;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.plugins.MainPaymentProcessor;
import com.mercadopago.android.px.plugins.SamplePaymentMethodPlugin;
import com.mercadopago.android.px.plugins.model.BusinessPayment;
import com.mercadopago.android.px.plugins.model.ExitAction;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.testcheckout.assertions.AlwaysOnDiscountValidator;
import com.mercadopago.android.px.testcheckout.assertions.OneShotDiscountValidator;
import com.mercadopago.android.px.testcheckout.flows.DiscountTestFlow;
import com.mercadopago.android.px.testcheckout.idleresources.CheckoutResource;
import com.mercadopago.android.px.testcheckout.input.Country;
import com.mercadopago.android.px.testcheckout.input.FakeCard;
import com.mercadopago.android.px.testcheckout.input.Visa;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.testlib.HttpResource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DiscountTest {

    private static final String DIRECT_DISCOUNT_PUBLIC_KEY = "APP_USR-b8925182-e1bf-4c0e-bc38-1d893a19ab45";
    private static final String CODE_DISCOUNT_PUBLIC_KEY = "APP_USR-2e257493-3b80-4b71-8547-c841d035e8f2";

    private static final String DIRECT_DISCOUNT_PREFERENCE_ID = "241261700-459d4126-903c-4bad-bc05-82e5f13fa7d3";
    private static final String CODE_DISCOUNT_PREFERENCE_ID = "241261708-cd353b1b-940f-493b-b960-10106a24203c";

    private static final String MERCHANT_PUBLIC_KEY = "TEST-c6d9b1f9-71ff-4e05-9327-3c62468a23ee";
    private static final String PREFERENCE_ID = "243962506-0bb62e22-5c7b-425e-a0a6-c22d0f4758a9";

    private static final String BUSINESS_PAYMENT_IMAGE_URL =
        "https://www.jqueryscript.net/images/Simplest-Responsive-jQuery-Image-Lightbox-Plugin-simple-lightbox.jpg";
    private static final String BUSINESS_PAYMENT_TITLE = "Title";
    private static final String BUSINESS_PAYMENT_BUTTON_NAME = "ButtonSecondaryName";

    private static final String MERCHANT_DISCOUNT_ID = "77";
    private static final String MERCHANT_DISCOUNT_CURRENCY = "ARS";

    private static final String ONE_TAP_MERCHANT_PUBLIC_KEY = "APP_USR-648a260d-6fd9-4ad7-9284-90f22262c18d";
    private static final String ONE_TAP_DIRECT_DISCOUNT_MERCHANT_PUBLIC_KEY =
        "APP_USR-ef65214d-59a2-4c82-be23-6cf6eb945d4c";
    private static final String ONE_TAP_PAYER_3_ACCESS_TOKEN =
        "TEST-244508097630521-031308-7b8b58d617aec50b3e528ca98606b116__LC_LA__-150216849";
    private static final String PAYER_EMAIL_DUMMY = "prueba@gmail.com";
    private static final String ITEM_DESCRIPTION = "Descripción del producto";
    private static final String ITEM_TITLE = "Título del producto";
    private static final String ITEM_ID = "1234";

    @Rule
    public HttpResource httpResource = new CheckoutResource();

    @Rule
    public ActivityTestRule<CheckoutExampleActivity> activityRule =
        new ActivityTestRule<>(CheckoutExampleActivity.class);

    private DiscountTestFlow discountTestFlow;
    private BusinessPayment businessPayment;
    private MainPaymentProcessor mainPaymentProcessor;
    private Discount discount;
    private Campaign campaign;
    private Visa card;
    private CheckoutPreference checkoutPreferenceWithPayerEmail;

    @Before
    public void setUp() {
        final MercadoPagoCheckout.Builder builder =
            new MercadoPagoCheckout.Builder(DIRECT_DISCOUNT_PUBLIC_KEY, DIRECT_DISCOUNT_PREFERENCE_ID);
        discountTestFlow = new DiscountTestFlow(builder.build(), activityRule.getActivity());

        businessPayment = new BusinessPayment.Builder(BusinessPayment.Decorator.APPROVED,
            Payment.StatusCodes.STATUS_APPROVED,
            Payment.StatusDetail.STATUS_DETAIL_ACCREDITED,
            BUSINESS_PAYMENT_IMAGE_URL,
            BUSINESS_PAYMENT_TITLE)
            .setPaymentMethodVisibility(true)
            .setSecondaryButton(new ExitAction(BUSINESS_PAYMENT_BUTTON_NAME, 34))
            .build();

        mainPaymentProcessor = new MainPaymentProcessor(businessPayment);

        discount = new Discount.Builder(MERCHANT_DISCOUNT_ID, MERCHANT_DISCOUNT_CURRENCY, new BigDecimal(50))
            .setPercentOff(BigDecimal.TEN).build();
        campaign =
            new Campaign.Builder(MERCHANT_DISCOUNT_ID).setMaxCouponAmount(new BigDecimal(200)).setMaxRedeemPerUser(2)
                .build();

        card = new Visa(FakeCard.CardState.APRO, Country.ARGENTINA);

        final List<Item> items = new ArrayList<>();
        final Item item = new Item(ITEM_DESCRIPTION, 1, new BigDecimal(120));
        item.setId(ITEM_ID);
        item.setTitle(ITEM_TITLE);
        item.setCurrencyId(Sites.ARGENTINA.getCurrencyId());
        items.add(item);
        checkoutPreferenceWithPayerEmail = new CheckoutPreference.Builder(Sites.ARGENTINA,
            PAYER_EMAIL_DUMMY, items)
            .build();
    }

    @Test
    public void whenMerchantDiscountIsAlwaysOnAndHasPaymentProcessorThenShowMerchantDiscountAndGetCongrats() {
        final MercadoPagoCheckout.Builder builder =
            new MercadoPagoCheckout.Builder(MERCHANT_PUBLIC_KEY, PREFERENCE_ID)
                .setPaymentProcessor(mainPaymentProcessor)
                .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor)
                .setDiscount(discount, campaign);

        campaign =
            new Campaign.Builder(MERCHANT_DISCOUNT_ID).setMaxCouponAmount(new BigDecimal(200)).setMaxRedeemPerUser(2)
                .build();

        discountTestFlow = new DiscountTestFlow(builder.build(), activityRule.getActivity());

        final CongratsPage congratsPage =
            discountTestFlow
                .runCreditCardPaymentFlowWithMerchantDiscountApplied(card, 1, new AlwaysOnDiscountValidator(campaign));
        assertNotNull(congratsPage);
    }

    @Test
    public void whenMerchantDiscountIsAlwaysOnHasPaymentProcessorAndPayerHasOneTapThenShowMerchantDiscountAndGetCongrats() {
        final MercadoPagoCheckout.Builder builder =
            new MercadoPagoCheckout.Builder(ONE_TAP_MERCHANT_PUBLIC_KEY,
                checkoutPreferenceWithPayerEmail)
                .setPaymentProcessor(mainPaymentProcessor)
                .setDiscount(discount, campaign)
                .setPrivateKey(ONE_TAP_PAYER_3_ACCESS_TOKEN);;

        campaign =
            new Campaign.Builder(MERCHANT_DISCOUNT_ID).setMaxCouponAmount(new BigDecimal(200)).setMaxRedeemPerUser(2)
                .build();

        discountTestFlow = new DiscountTestFlow(builder.build(), activityRule.getActivity());

        final CongratsPage congratsPage =
            discountTestFlow
                .runCreditCardWithOneTapWithoutESCPaymentFlowWithMerchantDiscountApplied(card,
                    new AlwaysOnDiscountValidator(campaign));
        assertNotNull(congratsPage);
    }

    @Test
    public void whenMerchantDiscountIsOneShotAndHasPaymentProcessorThenShowMerchantDiscountAndGetCongrats() {
        campaign =
            new Campaign.Builder(MERCHANT_DISCOUNT_ID).setMaxCouponAmount(new BigDecimal(200)).setMaxRedeemPerUser(1)
                .build();

        final MercadoPagoCheckout.Builder builder =
            new MercadoPagoCheckout.Builder(MERCHANT_PUBLIC_KEY, PREFERENCE_ID)
                .setPaymentProcessor(mainPaymentProcessor)
                .addPaymentMethodPlugin(new SamplePaymentMethodPlugin(), mainPaymentProcessor)
                .setDiscount(discount, campaign);

        discountTestFlow = new DiscountTestFlow(builder.build(), activityRule.getActivity());

        final CongratsPage congratsPage =
            discountTestFlow
                .runCreditCardPaymentFlowWithMerchantDiscountApplied(card, 1, new OneShotDiscountValidator(campaign));
        assertNotNull(congratsPage);
    }

    @Test
    public void whenMerchantDiscountIsAppliedAndHasNotPaymentProcessorThenNotShowDiscountAndGetCongrats() {
        final MercadoPagoCheckout.Builder builder =
            new MercadoPagoCheckout.Builder(MERCHANT_PUBLIC_KEY, PREFERENCE_ID)
                .setDiscount(discount, campaign);

        discountTestFlow = new DiscountTestFlow(builder.build(), activityRule.getActivity());

        final CongratsPage congratsPage =
            discountTestFlow.runCreditCardPaymentFlowWithoutPaymentProcessorWithMerchantDiscountApplied(card, 1);
        assertNotNull(congratsPage);
    }

    @Test
    public void whenDirectDiscountIsAppliedAndPaidWithCreditCardThenShowDiscountAndGetCongrats() {
        final MercadoPagoCheckout.Builder builder =
            new MercadoPagoCheckout.Builder(DIRECT_DISCOUNT_PUBLIC_KEY, DIRECT_DISCOUNT_PREFERENCE_ID);
        discountTestFlow = new DiscountTestFlow(builder.build(), activityRule.getActivity());

        final Visa card = new Visa(FakeCard.CardState.APRO, Country.ARGENTINA);
        final CongratsPage congratsPage = discountTestFlow.runCreditCardPaymentFlowWithDiscountApplied(card, 1);
        assertNotNull(congratsPage);
    }

    @Test
    public void whenDirectDiscountIsAppliedAndPaidWithCashThenShowDiscountAndGetCongrats() {
        final MercadoPagoCheckout.Builder builder =
            new MercadoPagoCheckout.Builder(DIRECT_DISCOUNT_PUBLIC_KEY, DIRECT_DISCOUNT_PREFERENCE_ID);
        discountTestFlow = new DiscountTestFlow(builder.build(), activityRule.getActivity());

        final CongratsPage congratsPage = discountTestFlow.runCashPaymentFlowWithDiscountApplied();
        assertNotNull(congratsPage);
    }
}