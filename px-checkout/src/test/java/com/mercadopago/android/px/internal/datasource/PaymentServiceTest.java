package com.mercadopago.android.px.internal.datasource;

import android.content.Context;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardPaymentMetadata;
import com.mercadopago.android.px.model.OneTapMetadata;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    @Mock private OneTapModel oneTapModel;

    @Mock private PaymentServiceHandler handler;

    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private PluginRepository pluginRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private AmountRepository amountRepository;
    @Mock private PaymentProcessor paymentProcessor;
    @Mock private Context context;
    @Mock private MercadoPagoESC mercadoPagoESC;

    private PaymentService paymentService;

    @Mock OneTapMetadata oneTapMetadata;
    @Mock CardPaymentMetadata cardPaymentMetadata;

    @Before
    public void setUp() {
        paymentService = new PaymentService(userSelectionRepository,
            paymentSettingRepository,
            pluginRepository,
            discountRepository,
            amountRepository,
            paymentProcessor,
            context,
            mercadoPagoESC);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(mock(CheckoutPreference.class));
    }

    @Test
    public void whenOneTapPaymentIsCardSelectCard() {
        final Card card = creditCardPresetMock();
        paymentService.startOneTapPayment(oneTapModel, handler);
        verify(userSelectionRepository).select(card);
    }

    @Test
    public void whenOneTapPaymentIsCardSelectPayerCost() {
        creditCardPresetMock();
        paymentService.startOneTapPayment(oneTapModel, handler);
        verify(userSelectionRepository).select(cardPaymentMetadata.getAutoSelectedInstallment());
    }

    @Test
    public void whenOneTapPaymentIsCardSelectPayerCostAndCard() {
        final Card card = creditCardPresetMock();
        paymentService.startOneTapPayment(oneTapModel, handler);
        verify(userSelectionRepository).select(card);
        verify(userSelectionRepository).select(cardPaymentMetadata.getAutoSelectedInstallment());
    }

    private Card creditCardPresetMock() {
        final Card card = mock(Card.class);
        final PaymentMethodSearch paymentMethodSearch = mock(PaymentMethodSearch.class);
        when(oneTapMetadata.getCard()).thenReturn(cardPaymentMetadata);
        when(oneTapMetadata.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(paymentMethodSearch.getOneTapMetadata()).thenReturn(oneTapMetadata);
        when(paymentMethodSearch.getCardById(cardPaymentMetadata.getId())).thenReturn(card);
        when(oneTapModel.getPaymentMethods()).thenReturn(paymentMethodSearch);
        when(paymentMethodSearch.getPaymentMethodById(oneTapMetadata.getPaymentMethodId()))
            .thenReturn(mock(PaymentMethod.class));
        return card;
    }
}