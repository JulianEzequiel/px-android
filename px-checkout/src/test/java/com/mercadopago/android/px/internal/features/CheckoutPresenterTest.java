package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.features.hooks.Hook;
import com.mercadopago.android.px.internal.features.providers.CheckoutProvider;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.CheckoutStateModel;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.mocks.Customers;
import com.mercadopago.android.px.mocks.Installments;
import com.mercadopago.android.px.mocks.Issuers;
import com.mercadopago.android.px.mocks.PaymentMethodSearchs;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.mocks.Payments;
import com.mercadopago.android.px.mocks.Tokens;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Cause;
import com.mercadopago.android.px.model.Customer;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.Setting;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.utils.PluginInitializationSuccess;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubExpiredPreference;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItem;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItemAndPayer;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutPresenterTest {

    public static final String DEFAULT_CARD_ID = "260077840";
    public static final String DEBIT_CARD_DEBCABAL = "debcabal";
    @Mock private CheckoutView checkoutView;
    @Mock private CheckoutProvider checkoutProvider;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private AmountRepository amountRepository;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private GroupsRepository groupsRepository;
    @Mock private PluginRepository pluginRepository;
    @Mock private PaymentRepository paymentRepository;

    private MockedView stubView;
    private MockedProvider stubProvider;

    @Before
    public void setUp() {
        stubView = new MockedView();
        stubProvider = new MockedProvider();
        when(discountRepository.configureDiscountAutomatically(amountRepository.getAmountToPay()))
            .thenReturn(new StubSuccessMpCall<>(true));
    }

    @NonNull
    private CheckoutPresenter getPresenter() {
        return getBasePresenter(checkoutView, checkoutProvider);
    }

    @NonNull
    private CheckoutPresenter getPaymentPresenterWithDefaultAdvancedConfigurationMla() {
        final CheckoutPreference preference = stubPreferenceOneItem();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);

        stubProvider.setCheckoutPreferenceResponse(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        return getBasePresenter(stubView, stubProvider);
    }

    @NonNull
    private CheckoutPresenter getPaymentPresenter() {
        final CheckoutPreference preference = stubPreferenceOneItem();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        return getBasePresenter(stubView, stubProvider);
    }

    @NonNull
    private CheckoutPresenter getBasePresenter(
        final CheckoutView view,
        final CheckoutProvider provider) {

        when(pluginRepository.getInitTask()).thenReturn(new PluginInitializationSuccess());

        final CheckoutStateModel model = new CheckoutStateModel();
        final CheckoutPresenter presenter = new CheckoutPresenter(model, paymentSettingRepository, amountRepository,
            userSelectionRepository, discountRepository,
            groupsRepository,
            pluginRepository,
            paymentRepository);
        presenter.attachResourcesProvider(provider);
        presenter.attachView(view);
        return presenter;
    }

    private void whenFlowHasRecoverableTokenProcess(final Payment payment){
        final Token token = mock(Token.class);
        final PaymentMethod paymentMethod = mock(PaymentMethod.class);
        final PayerCost payerCost = mock(PayerCost.class);
        final Issuer issuer = mock(Issuer.class);

        when(paymentSettingRepository.getToken()).thenReturn(token);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        when(userSelectionRepository.getPayerCost()).thenReturn(payerCost);
        when(userSelectionRepository.getIssuer()).thenReturn(issuer);
        when(payment.getPaymentStatus()).thenReturn(Payment.StatusCodes.STATUS_REJECTED);
        when(payment.getPaymentStatusDetail()).thenReturn(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE);
    }

    @Test
    public void whenResolvePaymentErrorEscWasInvalidatedVerifyEscManagerCalledAndRecoveryFlowStarts() {
        final PaymentData paymentData = mock(PaymentData.class);
        final MercadoPagoError error = mock(MercadoPagoError.class);
        final Token token = mock(Token.class);
        final Issuer issuer = mock(Issuer.class);
        final PayerCost payerCost = mock(PayerCost.class);
        final PaymentMethod paymentMethod = mock(PaymentMethod.class);

        when(paymentRepository.getPaymentData()).thenReturn(paymentData);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        when(userSelectionRepository.getPayerCost()).thenReturn(payerCost);
        when(userSelectionRepository.getIssuer()).thenReturn(issuer);
        when(checkoutProvider.manageEscForError(error, paymentData)).thenReturn(true);
        when(paymentSettingRepository.getToken()).thenReturn(token);

        final CheckoutPresenter presenter = getPresenter();

        presenter.onPaymentError(error);

        verify(checkoutView).hideProgress();
        verify(checkoutProvider).manageEscForError(error, paymentData);
        verify(checkoutView).startPaymentRecoveryFlow(any(PaymentRecovery.class));

        verifyNoMoreInteractions(checkoutProvider);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentReceivedAndEscManagerResolvesInvalidateEscThenRecoveryFlowStarts() {
        final CheckoutPresenter presenter = getPresenter();
        final IPayment payment = mock(IPayment.class);
        final PaymentData paymentData = mock(PaymentData.class);
        final Token token = mock(Token.class);
        final Issuer issuer = mock(Issuer.class);
        final PayerCost payerCost = mock(PayerCost.class);
        final PaymentMethod paymentMethod = mock(PaymentMethod.class);

        when(paymentRepository.getPaymentData()).thenReturn(paymentData);
        when(checkoutProvider.manageEscForPayment(paymentData, payment.getPaymentStatus(), payment.getPaymentStatusDetail())).thenReturn(true);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        when(userSelectionRepository.getPayerCost()).thenReturn(payerCost);
        when(userSelectionRepository.getIssuer()).thenReturn(issuer);
        when(paymentSettingRepository.getToken()).thenReturn(token);

        presenter.checkStartPaymentResultActivity(payment);

        verify(checkoutProvider)
            .manageEscForPayment(paymentData, payment.getPaymentStatus(), payment.getPaymentStatusDetail());
        verify(checkoutView).startPaymentRecoveryFlow(any(PaymentRecovery.class));

        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenPaymentReceivedAndEscManagerResolvesValidEscThenShowPaymentResult() {
        final CheckoutPresenter presenter = getPresenter();
        final IPayment payment = mock(IPayment.class);
        final PaymentData paymentData = mock(PaymentData.class);
        final PaymentResult paymentResult = mock(PaymentResult.class);
        final BigDecimal amountToPay = mock(BigDecimal.class);
        final Discount discount = mock(Discount.class);

        when(paymentRepository.getPaymentData()).thenReturn(paymentData);
        when(paymentRepository.createPaymentResult(payment)).thenReturn(paymentResult);
        when(checkoutProvider
            .manageEscForPayment(paymentData, payment.getPaymentStatus(), payment.getPaymentStatusDetail()))
            .thenReturn(false);
        when(amountRepository.getAmountToPay()).thenReturn(amountToPay);
        when(discountRepository.getDiscount()).thenReturn(discount);

        presenter.checkStartPaymentResultActivity(payment);

        verify(checkoutProvider)
            .manageEscForPayment(paymentData, payment.getPaymentStatus(), payment.getPaymentStatusDetail());
        verify(checkoutView).showPaymentResult(paymentResult, amountToPay, discount);

        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenShouldShowBusinessPaymentVerifyEscManagerCalled() {
        final CheckoutPresenter presenter = getPresenter();
        final BusinessPayment paymentResult = mock(BusinessPayment.class);
        final PaymentData paymentData = mock(PaymentData.class);

        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        when(paymentRepository.getPaymentData()).thenReturn(paymentData);
        when(paymentResult.getPaymentStatus()).thenReturn(Payment.StatusCodes.STATUS_APPROVED);
        when(paymentResult.getPaymentStatusDetail()).thenReturn(Payment.StatusDetail.STATUS_DETAIL_ACCREDITED);
        when(checkoutProvider.manageEscForPayment(paymentData,
            paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail())).thenReturn(false);

        presenter.onBusinessResult(paymentResult);

        verify(checkoutProvider).manageEscForPayment(paymentData,
            paymentResult.getPaymentStatus(),
            paymentResult.getPaymentStatusDetail());

        verify(checkoutView).showBusinessResult(any(BusinessPaymentModel.class));

        verifyNoMoreInteractions(checkoutProvider);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenChoHasPrefIdSetRetrievePreferenceFromMercadoPago() {
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(null);
        when(paymentSettingRepository.getCheckoutPreferenceId()).thenReturn("some_pref_id");
        final CheckoutPresenter presenter = getPresenter();
        presenter.initialize();
        verify(checkoutProvider).getCheckoutPreference(any(String.class), any(TaggedCallback.class));
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenChoHasCompletePrefSetDoNotCallProviderToGetPreference() {
        final CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        final CheckoutPresenter presenter = getPresenter();
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        presenter.initialize();
        verify(checkoutProvider).fetchFonts();
        verify(checkoutProvider, times(0)).getCheckoutPreference(any(String.class), any(TaggedCallback.class));
    }

    @Test
    public void whenPreferenceIsExpiredThenShowErrorInView() {
        final CheckoutPreference preference = stubExpiredPreference();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        final CheckoutPresenter presenter = getPresenter();
        presenter.initialize();
        verify(checkoutProvider).getCheckoutExceptionMessage(any(CheckoutPreferenceException.class));
        verify(checkoutView).showError(any(MercadoPagoError.class));
    }

    @Test
    public void whenChoHasPreferenceAndPaymentMethodRetrivedShowPaymentMethodSelection() {
        final CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        final CheckoutPresenter presenter = getPresenter();
        presenter.initialize();

        verify(groupsRepository).getGroups();
        verify(checkoutView).showProgress();
        verify(checkoutView).initializeMPTracker();
        verify(checkoutView).trackScreen();
        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenAPaymentMethodIsSelectedThenShowReviewAndConfirm() {
        final CheckoutPresenter presenter = getPresenter();
        presenter.onPaymentMethodSelectionResponse(null, null);
        verify(checkoutView).showReviewAndConfirm(false);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenDefaultCardIdValidSelectedThenShowSecurityCode() {
        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);
        PaymentMethodSearch search = mockPaymentMethodSearchForDriver(true);
        presenter.startFlow(search);
        assertTrue(stubView.showingSavedCardFlow);
    }

    @Test
    public void whenDefaultCardIdInvalidSelectedThenShowPaymentVault() {
        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);
        PaymentMethodSearch search = mockPaymentMethodSearchForDriver(false);
        presenter.startFlow(search);
        assertTrue(stubView.showingPaymentMethodSelection);
    }

    @Test
    public void whenDefaultCardIdIsNullAndDefaultPaymentTypeIsValidThenShowNewCardFlow() {
        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);
        final PaymentMethodSearch search = mockPaymentMethodSearchForNewCardDriver();
        presenter.startFlow(search);
        assertTrue(stubView.showingNewCardFlow);
    }

    @NonNull
    private PaymentMethodSearch mockPaymentMethodSearchForNewCardDriver() {
        final PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        final CheckoutPreference checkoutPreference = mock(CheckoutPreference.class);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getPaymentPreference()).thenReturn(mock(PaymentPreference.class));
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultCardId()).thenReturn(null);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultPaymentTypeId())
            .thenReturn("debit_card");
        return search;
    }

    @NonNull
    private PaymentMethodSearch mockPaymentMethodSearchForDriver(boolean isValidCard) {
        PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        PaymentMethod paymentMethod = mock(PaymentMethod.class);
        when(paymentMethod.getPaymentTypeId()).thenReturn("debit_card");
        final ArrayList settingsList = mock(ArrayList.class);
        final Setting setting = mock(Setting.class);
        when(setting.getSecurityCode()).thenReturn(null);
        when(settingsList.get(any(int.class))).thenReturn(setting);
        when(paymentMethod.getSettings()).thenReturn(settingsList);
        when(search.getPaymentMethodById(any(String.class))).thenReturn(paymentMethod);
        if (isValidCard) {
            when(search.getCardById(any(String.class))).thenReturn(new Card());
        } else {
            when(search.getCardById(any(String.class))).thenReturn(null);
        }
        final CheckoutPreference checkoutPreference = mock(CheckoutPreference.class);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        final PaymentPreference paymentPreference = mock(PaymentPreference.class);
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultCardId()).thenReturn(
            DEFAULT_CARD_ID);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultPaymentMethodId())
            .thenReturn(DEBIT_CARD_DEBCABAL);
        return search;
    }

    @Test
    public void whenPaymentMethodCanceledThenCancelCheckout() {
        final CheckoutPresenter presenter = getPresenter();
        presenter.onPaymentMethodSelectionCancel();
        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenPaymentIsConfirmedAndNoHookAvailableThenCreatePayment() {
        final CheckoutPresenter presenter = getPresenter();
        final PaymentData paymentData = mock(PaymentData.class);

        when(paymentRepository.getPaymentData()).thenReturn(paymentData);

        //When Ok Response from Review And confirm
        presenter.onPaymentConfirmation();

        verify(paymentRepository).getPaymentData();
        verify(checkoutView).showProgress();
        verify(paymentRepository).startPayment(presenter);

        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(checkoutProvider);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(userSelectionRepository);
    }

    @Test
    public void whenPaymentResultWithCreatedPaymentThenFinishCheckoutWithPaymentResult() {
        final CheckoutPresenter presenter = getPresenter();
        final Payment payment = mock(Payment.class);

        presenter.onPaymentFinished(payment);
        presenter.onPaymentResultResponse();

        verify(checkoutView).finishWithPaymentResult(payment);
    }

    @Test
    public void whenPaymentResultWithoutCreatedPaymentThenFinishCheckoutWithoutPaymentResult() {
        final CheckoutPresenter presenter = getPresenter();

        presenter.onPaymentResultResponse();

        verify(checkoutView).finishWithPaymentResult();
    }

    @Test
    public void whenPaymentIsCanceledBecauseUserWantsToSelectOtherPaymentMethodThenShowPaymentMethodSelection() {
        final CheckoutPresenter presenter = getPresenter();

        presenter.onPaymentResultCancel(PaymentResult.SELECT_OTHER_PAYMENT_METHOD);

        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenPaymentIsCanceledBecausePaymentRecoveryIsRequiredAndPaymentRecoveryCreationIsValidThenStartPaymentRecoveryFlow() {
        final CheckoutPresenter presenter = getPresenter();
        final Payment payment = mock(Payment.class);

        whenFlowHasRecoverableTokenProcess(payment);

        presenter.onPaymentFinished(payment);

        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);

        verify(checkoutView).startPaymentRecoveryFlow(any(PaymentRecovery.class));
    }

    @Test
    public void whenPaymentIsCanceledBecausePaymentRecoveryIsRequiredButPaymentRecoveryCreationIsNotValidThenShowMercadoPagoError() {
        final CheckoutPresenter presenter = getPresenter();

        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);

        verify(checkoutView).showError(any(MercadoPagoError.class));
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCardFlowResponseHasRecoverableTokenProcessThenCreatePayment() {
        final CheckoutPresenter presenter = getPresenter();
        final Payment payment = mock(Payment.class);

        whenFlowHasRecoverableTokenProcess(payment);

        presenter.onPaymentFinished(payment);
        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);
        presenter.onCardFlowResponse();

        verify(checkoutView).showProgress();
        verify(paymentRepository).startPayment(presenter);
    }

    @Test
    public void whenCardFlowResponseHasNotRecoverableTokenProcessAndThereIsNoAvailableHooksThenShowReviewAndConfirm() {
        final CheckoutPresenter presenter = getPresenter();
        final PaymentData paymentData = mock(PaymentData.class);
        when(paymentRepository.getPaymentData()).thenReturn(paymentData);

        presenter.onCardFlowResponse();

        verify(paymentRepository).getPaymentData();
        verify(checkoutView).showReviewAndConfirm(false);
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(paymentRepository);
    }

    //Backs
    @Test
    public void ifCheckoutInitiatedAndUserPressesBackCancelCheckout() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        presenter.initialize();
        assertTrue(stubView.showingPaymentMethodSelection);
        presenter.onPaymentMethodSelectionCancel();
        assertTrue(stubView.checkoutCanceled);
    }

    //
    @Ignore
    @Test
    public void ifReviewAndConfirmShownAndUserPressesBackThenRestartPaymentMethodSelection() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        presenter.initialize();
        assertTrue(stubView.showingPaymentMethodSelection);
        presenter.onPaymentMethodSelectionResponse(null, null);
        assertTrue(stubView.showingReviewAndConfirm);
        presenter.onReviewAndConfirmCancel();
        assertTrue(stubView.showingPaymentMethodSelection);
    }

    //TODO FIX
    @Ignore
    @Test
    public void ifPaymentRecoveryShownAndUserPressesBackThenRestartPaymentMethodSelection() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        stubProvider.setPaymentResponse(Payments.getCallForAuthPayment());
        presenter.initialize();
        assertTrue(stubView.showingPaymentMethodSelection);
        final PaymentMethod paymentMethodOnVisa = PaymentMethods.getPaymentMethodOnVisa();
        final PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethodOnVisa);
        when(userSelectionRepository.getPayerCost()).thenReturn(payerCost);
        userSelectionRepository.select(Issuers.getIssuers().get(0));
        presenter.onPaymentMethodSelectionResponse(Tokens.getVisaToken(), null);
        assertTrue(stubView.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();
        assertTrue(stubView.showingPaymentResult);
        presenter.onPaymentResultCancel(PaymentResult.RECOVER_PAYMENT);
        assertTrue(stubView.showingPaymentRecoveryFlow);
        presenter.onCardFlowCancel();
        assertTrue(stubView.showingPaymentMethodSelection);
    }

    @Test
    public void ifPaymentMethodEditionRequestedAndUserPressesBackTwiceCancelCheckout() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        presenter.initialize();
        assertTrue(stubView.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionResponse(null, null);
        assertTrue(stubView.showingReviewAndConfirm);

        presenter.onChangePaymentMethodFromReviewAndConfirm();
        assertTrue(stubView.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionCancel();
        assertTrue(stubView.showingReviewAndConfirm);

        presenter.onReviewAndConfirmCancel();
        assertTrue(stubView.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionCancel();
        assertTrue(stubView.checkoutCanceled);
    }

    //TODO FIX
    @Ignore
    @Test
    public void whenPaymentCreationRequestedThenGenerateTransactionId() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        stubProvider.setPaymentResponse(Payments.getApprovedPayment());

        presenter.initialize();
        final PaymentMethod paymentMethodOff = PaymentMethods.getPaymentMethodOff();
        //Payment method off, no issuer, installments or token
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethodOff);

        presenter.onPaymentMethodSelectionResponse(null, null);
        assertTrue(stubView.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();

        assertTrue(stubProvider.paymentRequested);
        assertFalse(TextUtil.isEmpty(stubProvider.transactionId));
    }

    // TODO CHECK IF WE WILL SUPPORT THIS KIND OF PM requests.
    @Ignore
    @Test
    public void whenCustomerAvailableAndPaymentCreationRequestedThenCreatePaymentWithCustomerId() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        stubProvider.setPaymentResponse(Payments.getApprovedPayment());
        stubProvider.setCustomerResponse(Customers.getCustomerWithCards());
        presenter.initialize();

        //Payment method off, no issuer, installments or token
        final PaymentMethod paymentMethodOff = PaymentMethods.getPaymentMethodOff();
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethodOff);
        presenter.onPaymentMethodSelectionResponse(null, null);
        assertTrue(stubView.showingReviewAndConfirm);
        presenter.onPaymentConfirmation();

        assertTrue(stubProvider.paymentRequested);
        assertFalse(TextUtil.isEmpty(stubProvider.paymentCustomerId));
    }

    @Test
    public void ifPaymentResultApprovedSetAndTokenWithESCAndESCEnabledThenSaveESC() {
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final Issuer issuer = Issuers.getIssuers().get(0);
        final PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token with card Id and ESC
        final Token token = Tokens.getTokenWithESC();

        final PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);

        presenter.checkStartPaymentResultActivity(Payments.create(Payment.StatusCodes.STATUS_APPROVED,
            Payment.StatusDetail.STATUS_DETAIL_ACCREDITED));

        assertTrue(stubProvider.manageEscRequested);
    }

    @Test
    public void ifPaymentResultApprovedSetAndESCEnabledButTokenHasNoESCThenDontSaveESC() {
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final Issuer issuer = Issuers.getIssuers().get(0);
        final PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token without card id or ESC
        final Token token = Tokens.getVisaToken();

        final PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);

        presenter.checkStartPaymentResultActivity(Payments.create(Payment.StatusCodes.STATUS_APPROVED,
            Payment.StatusDetail.STATUS_DETAIL_ACCREDITED));

        assertTrue(stubProvider.manageEscRequested);
    }

    @Test
    public void ifPaymentResultApprovedSetAndESCEnabledThenShowPaymentResultScreen() {

        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final Issuer issuer = Issuers.getIssuers().get(0);
        final PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token with card Id and ESC
        final Token token = Tokens.getTokenWithESC();

        final PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);

        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);

        presenter.checkStartPaymentResultActivity(Payments.create(Payment.StatusCodes.STATUS_APPROVED,
            Payment.StatusDetail.STATUS_DETAIL_ACCREDITED));
        assertTrue(stubView.showingPaymentResult);
    }

    @Test
    public void whenPaymentResultCalledThenCheckEscManagerIsCalled() {
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final Issuer issuer = Issuers.getIssuers().get(0);
        final PayerCost payerCost = Installments.getInstallments().getPayerCosts().get(0);
        //Token with Card ID (because it was created with ESC enabled)
        final Token token = Tokens.getTokenWithESC();
        final PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setIssuer(issuer);
        paymentData.setPayerCost(payerCost);
        paymentData.setToken(token);
        when(paymentRepository.getPaymentData()).thenReturn(paymentData);

        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);

        presenter.checkStartPaymentResultActivity(Payments.create(Payment.StatusCodes.STATUS_REJECTED,
            Payment.StatusDetail.STATUS_DETAIL_INVALID_ESC));

        assertTrue(stubProvider.manageEscRequested);
    }

    @Test
    public void whenErrorShownAndInvalidIdentificationThenGoBackToPaymentMethodSelection() {
        final CheckoutPresenter presenter = getPresenter();
        final ApiException apiException = Payments.getInvalidIdentificationPayment();
        final MercadoPagoError mpException = new MercadoPagoError(apiException, "");

        presenter.onErrorCancel(mpException);

        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenErrorShownAndValidIdentificationThenCancelCheckout() {
        final CheckoutPresenter presenter = getPresenter();
        final ApiException apiException = mock(ApiException.class);
        final MercadoPagoError mpException = new MercadoPagoError(apiException, "");

        presenter.onErrorCancel(mpException);

        verify(checkoutView).cancelCheckout();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void ifNewFlowThenDoTrackInit() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        presenter.initialize();
        assertTrue(stubView.initTracked);
    }

    private static class MockedView implements CheckoutView {

        MercadoPagoError errorShown;
        boolean showingError = false;
        boolean showingPaymentMethodSelection = false;
        boolean showingReviewAndConfirm = false;
        boolean initTracked = false;
        PaymentData paymentDataFinalResponse;
        boolean showingPaymentResult = false;
        boolean checkoutCanceled = false;
        boolean showingNewCardFlow = false;
        boolean showingSavedCardFlow = false;
        Payment paymentFinalResponse;
        boolean finishedCheckoutWithoutPayment = false;
        boolean showingPaymentRecoveryFlow = false;
        PaymentRecovery paymentRecoveryRequested;

        @Override
        public void showBusinessResult(final BusinessPaymentModel model) {
            //Do nothing
        }

        @Override
        public void showOneTap(@NonNull final OneTapModel oneTapModel) {
            //Do nothing
        }

        @Override
        public void hideProgress() {
            //Do nothing
        }

        @Override
        public void exitCheckout(final int resCode) {
            //Do nothing
        }

        @Override
        public void transitionOut() {
            //Do nothing
        }

        @Override
        public void showSavedCardFlow(final Card card) {
            this.showingSavedCardFlow = true;
        }

        @Override
        public void showNewCardFlow() {
            this.showingNewCardFlow = true;
        }

        @Override
        public void showError(MercadoPagoError error) {
            this.showingError = true;
            this.errorShown = error;
        }

        @Override
        public void showProgress() {
            //Do nothing
        }

        @Override
        public void showReviewAndConfirm(final boolean isUniquePaymentMethod) {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = true;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void showPaymentMethodSelection() {
            showingPaymentMethodSelection = true;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void showPaymentResult(PaymentResult paymentResult, @NonNull final BigDecimal amountToPay,
            final Discount discount) {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = false;
            showingPaymentResult = true;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void backToReviewAndConfirm() {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = true;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void finishWithPaymentResult() {
            finishedCheckoutWithoutPayment = true;
        }

        @Override
        public void finishWithPaymentResult(Integer customResultCode) {

        }

        @Override
        public void finishWithPaymentResult(Payment payment) {
            paymentFinalResponse = payment;
        }

        @Override
        public void finishWithPaymentResult(Integer customResultCode, Payment payment) {

        }

        @Override
        public void cancelCheckout() {
            checkoutCanceled = true;
        }

        @Override
        public void cancelCheckout(MercadoPagoError mercadoPagoError) {

        }

        @Override
        public void cancelCheckout(final Integer customResultCode, final Boolean paymentMethodEdited) {

        }

        @Override
        public void startPaymentRecoveryFlow(PaymentRecovery paymentRecovery) {
            paymentRecoveryRequested = paymentRecovery;
            showingPaymentRecoveryFlow = true;
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
        }

        @Override
        public void initializeMPTracker() {

        }

        @Override
        public void trackScreen() {
            initTracked = true;
        }

        @Override
        public void showHook(Hook hook, int requestCode) {

        }

        @Override
        public void showPaymentProcessor() {

        }

        @Override
        public boolean isActive() {
            return true;
        }
    }

    public class MockedProvider implements CheckoutProvider {

        private boolean campaignsRequested = false;

        private List<Campaign> campaigns;
        private boolean checkoutPreferenceRequested = false;
        private CheckoutPreference preference;
        private boolean paymentMethodSearchRequested = false;
        private PaymentMethodSearch paymentMethodSearchResponse;
        private Payment paymentResponse;
        private boolean paymentRequested;
        private Customer customerResponse;

        private String transactionId;
        private String paymentCustomerId;
        private PaymentMethod paymentMethodPaid;
        private Payer payerPosted;

        private boolean shouldFail = false;
        private MercadoPagoError failedResponse;
        public boolean manageEscRequested = false;

        @Override
        public void fetchFonts() {

        }

        @Override
        public boolean manageEscForPayment(final PaymentData paymentData, final String paymentStatus,
            final String paymentStatusDetail) {
            manageEscRequested = true;
            return false;
        }

        @Override
        public boolean manageEscForError(final MercadoPagoError error, final PaymentData paymentData) {
            return false;
        }

        @Override
        public void getCheckoutPreference(String checkoutPreferenceId,
            TaggedCallback<CheckoutPreference> taggedCallback) {
            checkoutPreferenceRequested = true;
            taggedCallback.onSuccess(preference);
        }

        @Override
        public String getCheckoutExceptionMessage(CheckoutPreferenceException exception) {
            return null;
        }

        @Override
        public String getCheckoutExceptionMessage(final Exception exception) {
            return null;
        }

        public void setCampaignsResponse(List<Campaign> campaigns) {
            this.campaigns = campaigns;
        }

        public void setCheckoutPreferenceResponse(CheckoutPreference preference) {
            this.preference = preference;
        }

        public void setPaymentResponse(Payment paymentResponse) {
            this.paymentResponse = paymentResponse;
        }

        public void setCustomerResponse(Customer customerResponse) {
            this.customerResponse = customerResponse;
        }

        public void setPaymentResponse(MercadoPagoError error) {
            shouldFail = true;
            failedResponse = error;
        }
    }
}