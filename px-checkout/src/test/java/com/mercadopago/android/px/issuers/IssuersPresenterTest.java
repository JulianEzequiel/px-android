package com.mercadopago.android.px.issuers;

import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.mocks.Issuers;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.features.IssuersPresenter;
import com.mercadopago.android.px.internal.features.providers.IssuersProvider;
import com.mercadopago.android.px.internal.features.IssuersActivityView;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by mromar on 5/3/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class IssuersPresenterTest {

    private IssuersPresenter presenter;
    private MockedView mockedView = new MockedView();
    private MockedProvider provider = new MockedProvider();

    @Mock
    private UserSelectionRepository userSelectionRepository;

    @Before
    public void setUp() {
        //Simulation no charge - no discount
        presenter = new IssuersPresenter(userSelectionRepository);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);
    }

    @Test
    public void whenIssuersAreNullThenGetIssuersAndShow() {
        final List<Issuer> issuers = Issuers.getIssuersListMLA();
        provider.setResponse(issuers);

        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();

        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);

        presenter.initialize();

        mockedView.simulateIssuerSelection(0);

        assertTrue(mockedView.issuersShown);
        assertTrue(mockedView.headerShown);
        assertEquals(issuers.get(0), mockedView.selectedIssuer);
        assertTrue(mockedView.finishWithResult);
    }

    @Test
    public void whenGetIssuersHaveOneIssuerThenFinishWithResult() {
        final List<Issuer> issuers = Issuers.getOneIssuerListMLA();
        provider.setResponse(issuers);

        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);

        presenter.initialize();

        assertFalse(mockedView.issuersShown);
        assertFalse(mockedView.headerShown);
        //TODO no puedo acceder a los datos que se guardan en el repositorio para testear la selección
//        assertEquals(userSelectionRepository.getIssuer(), issuers.get(0));
//        assertEquals(mockedView.selectedIssuer, issuers.get(0));
        assertTrue(mockedView.finishWithResult);
    }

    @Test
    public void whenIssuersAreNotNullThenShowIssuers() {
        final List<Issuer> issuers = Issuers.getIssuersListMLA();
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();

        presenter.setIssuers(issuers);

        presenter.initialize();

        mockedView.simulateIssuerSelection(0);

        assertTrue(mockedView.issuersShown);
        assertTrue(mockedView.headerShown);
        assertEquals(issuers.get(0), mockedView.selectedIssuer);
        assertTrue(mockedView.finishWithResult);
    }

    @Test
    public void whenGetIssuersIsNullThenGetNewIssuersList() {
        final List<Issuer> issuers = new ArrayList<Issuer>();
        provider.setResponse(issuers);

        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();

        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);

        presenter.initialize();

        assertTrue(mockedView.errorShown);
    }

    @Test
    public void whenGetIssuersFailThenShowMercadoPagoError() {
        final MercadoPagoError mercadoPagoError = new MercadoPagoError("Error", true);
        provider.setResponse(mercadoPagoError);

        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();

        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);

        presenter.initialize();

        assertFalse(mockedView.loadingViewShown);
        assertTrue(mockedView.errorShown);
    }

    @Test
    public void whenGetIssuersReturnNullThenShowMercadoPagoError() {
        final List<Issuer> issuers = null;
        provider.setResponse(issuers);
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();

        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);

        presenter.initialize();

        assertFalse(mockedView.loadingViewShown);
        assertTrue(mockedView.errorShown);
        assertTrue(provider.emptyIssuersErrorGotten);
    }

    @Test
    public void whenRecoverFromFailureThenGetIssuersAgain() {
        final MercadoPagoError mercadoPagoError = new MercadoPagoError("Error", true);
        provider.setResponse(mercadoPagoError);

        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();

        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);

        presenter.initialize();

        presenter.recoverFromFailure();

        assertFalse(mockedView.loadingViewShown);
        assertTrue(mockedView.errorShown);
        assertNotEquals(presenter.getFailureRecovery(), null);
    }

    @Test
    public void whenRecoverFromFailureIsNullThenNotRecoverFromError() {
        presenter.recoverFromFailure();

        assertEquals(presenter.getFailureRecovery(), null);
    }

    @Test
    public void whenSetCardInfoThenSetBin() {
        final CardInfo cardInfo = getCardInfo();

        presenter.setCardInfo(cardInfo);

        assertEquals(presenter.getBin(), getCardInfo().getFirstSixDigits());
    }

    @Test
    public void whenCardInfoIsNullThenPresenterBinIsEmpty() {
        presenter.setCardInfo(null);

        assertEquals(presenter.getBin(), "");
    }

    @Test
    public void whenIsCardInfoAndPaymentMethodAvailableThenIsNotRequiredCardDrawn() {
        final CardInfo cardInfo = getCardInfo();
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();

        presenter.setCardInfo(cardInfo);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);

        assertTrue(presenter.isRequiredCardDrawn());
    }

    @Test
    public void whenIsNotPaymentMethodAvailableThenIsNotRequiredCardDrawn() {
        final CardInfo cardInfo = getCardInfo();

        presenter.setCardInfo(cardInfo);

        assertFalse(presenter.isRequiredCardDrawn());
    }

    @Test
    public void whenIsNotCardInfoAvailableThenIsNotRequiredCardDrawn() {
        assertFalse(presenter.isRequiredCardDrawn());
    }

    private CardInfo getCardInfo() {
        final Card card = new Card();
        card.setLastFourDigits("4321");
        card.setFirstSixDigits("123456");

        return new CardInfo(card);
    }

    private class MockedProvider implements IssuersProvider {

        private boolean shouldFail;
        private List<Issuer> successfulResponse;
        private MercadoPagoError failedResponse;

        private boolean emptyIssuersErrorGotten = false;

        void setResponse(List<Issuer> issuers) {
            shouldFail = false;
            successfulResponse = issuers;
        }

        private void setResponse(MercadoPagoError exception) {
            shouldFail = true;
            failedResponse = exception;
        }

        @Override
        public void getIssuers(String paymentMethodId, String bin, TaggedCallback<List<Issuer>> taggedCallback) {

            if (shouldFail) {
                taggedCallback.onFailure(failedResponse);
            } else {
                taggedCallback.onSuccess(successfulResponse);
            }
        }

        @Override
        public MercadoPagoError getEmptyIssuersError() {
            this.emptyIssuersErrorGotten = true;
            return null;
        }

        @Override
        public String getCardIssuersTitle() {
            return null;
        }
    }

    private class MockedView implements IssuersActivityView {

        private boolean issuersShown = false;
        private boolean headerShown = false;
        private boolean loadingViewShown = false;
        private boolean errorShown = false;
        private boolean finishWithResult = false;
        private List<Issuer> issuersList;
        private Issuer selectedIssuer;
        private OnSelectedCallback<Integer> issuerSelectionCallback;

        @Override
        public void showIssuers(List<Issuer> issuers, OnSelectedCallback<Integer> onSelectedCallback) {
            this.issuerSelectionCallback = onSelectedCallback;
            this.issuersShown = true;
            this.issuersList = issuers;
        }

        @Override
        public void showHeader() {
            this.headerShown = true;
        }

        @Override
        public void showLoadingView() {
            this.loadingViewShown = true;
        }

        @Override
        public void stopLoadingView() {
            this.loadingViewShown = false;
        }

        @Override
        public void showError(MercadoPagoError error, String requestOrigin) {
            this.errorShown = true;
        }

        @Override
        public void finishWithResult() {
            this.finishWithResult = true;
        }

        private void simulateIssuerSelection(int index) {
            issuerSelectionCallback.onSelected(index);
            if (issuersList != null && !issuersList.isEmpty()) {
                selectedIssuer = issuersList.get(index);
            }
        }
    }
}
