package com.mercadopago.android.px.internal.features.onetap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.CheckoutActivity;
import com.mercadopago.android.px.internal.features.MercadoPagoComponents;
import com.mercadopago.android.px.internal.features.onetap.components.ExplodingViewContainer;
import com.mercadopago.android.px.internal.features.onetap.components.ExplodingViewInfo;
import com.mercadopago.android.px.internal.features.onetap.components.OneTapContainer;
import com.mercadopago.android.px.internal.features.plugins.PaymentProcessorPluginActivity;
import com.mercadopago.android.px.internal.tracker.Tracker;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.exploding.ExplodingButtonListener;
import com.mercadopago.android.px.internal.view.exploding.ExplodingButtonView;
import com.mercadopago.android.px.internal.view.exploding.StatusBarDecorator;
import com.mercadopago.android.px.internal.view.exploding.ViewStylingParams;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.internal.view.exploding.ExplodingButtonFragment;
import java.math.BigDecimal;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class OneTapFragment extends Fragment implements OneTap.View {

    private static final String ARG_ONE_TAP_MODEL = "ARG_ONETAP_MODEL";
    private static final int REQ_CODE_CARD_VAULT = 0x999;
    private static final int REQ_CODE_PAYMENT_PROCESSOR = 0x123;

    private CallBack callback;
    /* default */ OneTapPresenter presenter;
    private OneTapContainer oneTapContainer;
//    private ExplodingViewContainer explodingViewContainer;
    private ViewGroup explodingContainer;
    private ExplodingButtonView explodingButtonView;

    private ScrollView scrollView;
//    private ExplodingViewInfo explodingViewInfo;
//    private ExplodingButtonView explodingButtonView;

    //TODO remove - just for tracking
    private BigDecimal amountToPay;
    private boolean hasDiscount;

    private BusinessPayment businessPayment;
    private PaymentResult paymentResult;

    public static OneTapFragment getInstance(@NonNull final OneTapModel oneTapModel) {
        final OneTapFragment oneTapFragment = new OneTapFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ONE_TAP_MODEL, oneTapModel);
        oneTapFragment.setArguments(bundle);
        return oneTapFragment;
    }

    public interface CallBack {

        void onOneTapCanceled();

        void onChangePaymentMethod();
    }

    @Override
    public void onResume() {
        super.onResume();
        final OneTapModel model = (OneTapModel) getArguments().getSerializable(ARG_ONE_TAP_MODEL);
        configureView(getView(), presenter, model);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof CallBack) {
            callback = (CallBack) context;
        }
    }

    @Override
    public void onDetach() {
        callback = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
        @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_onetap_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final Session session = Session.getSession(view.getContext());
            amountToPay = session.getAmountRepository().getAmountToPay();
            hasDiscount = session.getDiscountRepository().getDiscount() != null;
            final OneTapModel model = (OneTapModel) arguments.getSerializable(ARG_ONE_TAP_MODEL);
            presenter = new OneTapPresenter(model,
                session.getPaymentRepository());
            configureView(view, presenter, model);
            presenter.attachView(this);
            trackScreen(model);
        }
    }

    private void trackScreen(final OneTapModel model) {
        if (getActivity() != null) {
            Tracker.trackOneTapScreen(getActivity().getApplicationContext(), model.getPublicKey(),
                model.getPaymentMethods().getOneTapMetadata(), amountToPay);
        }
    }

    @Override
    public void cancel() {
        if (callback != null) {
            callback.onOneTapCanceled();
        }
    }

    private void configureView(final View view, final OneTap.Actions actions, final OneTapModel model) {
        final ViewGroup container = view.findViewById(R.id.main_container);
        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        container.removeAllViews();
        configureToolbar(toolbar);
        scrollView = view.findViewById(R.id.scrollView);
        oneTapContainer = new OneTapContainer(model, actions);
        View oneTapView = oneTapContainer.render(container);

        configureExplodingView(getView());
    }

    private void configureExplodingView(final View view) {
        explodingContainer = view.findViewById(R.id.explodingView);

        explodingButtonView = new ExplodingButtonView(getContext(), null, 0, getExplodingCallback());
        explodingContainer.addView(explodingButtonView);

//        explodingViewInfo = new ExplodingViewInfo(getScrollViewTopCoordinate(), false);
//        explodingViewContainer = new ExplodingViewContainer(explodingViewInfo, getExplodingCallback());
//        View explodingView = explodingViewContainer.render(explodingContainer);
//        explodingContainer.addView(explodingView);
        //TODO agregar params (startY, text)
//        explodingButtonView = new ExplodingButtonView(getContext(), null, 0, getExplodingCallback());
//        explodingContainer.addView(explodingButtonView);
    }

//    private ExplodingViewContainer.Actions getExplodingCallback() {
//        return new ExplodingViewContainer.Actions() {
//            @Override
//            public void onExplodingAnimationFinished() {
//                Log.d("button", "explosion anim finished on fragment");
//                if (businessPayment != null) {
//                    ((CheckoutActivity) getActivity()).presenter.onBusinessResult(businessPayment);
//                } else if (paymentResult != null) {
//                    ((CheckoutActivity) getActivity()).presenter.checkStartPaymentResultActivity(paymentResult);
//                }
//            }
//
//            @Override
//            public void onStatusBarColorChange(final int primaryColor) {
//                new StatusBarDecorator(getActivity().getWindow()).setupStatusBarColor(primaryColor);
//            }
//        };
//    }

    private ExplodingButtonListener getExplodingCallback() {
        return new ExplodingButtonListener() {
            @Override
            public void onExplodingAnimationFinished() {
                Log.d("button", "explosion anim finished on fragment");
                if (businessPayment != null) {
                    ((CheckoutActivity) getActivity()).presenter.onBusinessResult(businessPayment);
                } else if (paymentResult != null) {
                    ((CheckoutActivity) getActivity()).presenter.checkStartPaymentResultActivity(paymentResult);
                }
            }

            @Override
            public void onStatusBarColorChange(final int primaryColor) {
                new StatusBarDecorator(getActivity().getWindow()).setupStatusBarColor(primaryColor);
            }

            @Override
            public void onButtonClicked(View button) {
                Log.d("button", "do something on click");

                int[] locationInWindow = new int[2];
                button.getLocationInWindow(locationInWindow);
                int xW = locationInWindow[0];
                int yW = locationInWindow[1];

                int[] locationInScreen = new int[2];
                button.getLocationOnScreen(locationInScreen);
                int xS = locationInScreen[0];
                int yS = locationInScreen[1];

                Rect r = new Rect();
                button.getLocalVisibleRect(r);
                int top = r.top;
                int botton = r.bottom;
                final int left = r.left;
                final int right = r.right;

                Log.d("button", String.valueOf(top));

                presenter.confirmPayment();
            }
        };
    }

    private void configureToolbar(final Toolbar toolbar) {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && toolbar != null) {
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    presenter.cancel();
                }
            });
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQ_CODE_CARD_VAULT && resultCode == RESULT_OK) {
            presenter.onTokenResolved();
        } else if (requestCode == REQ_CODE_CARD_VAULT && resultCode == RESULT_CANCELED && callback != null) {
            presenter.cardVaultCanceled();
        } else if (requestCode == REQ_CODE_PAYMENT_PROCESSOR && getActivity() != null) {
            ((CheckoutActivity) getActivity()).resolvePaymentProcessor(resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void changePaymentMethod() {
        if (callback != null) {
            callback.onChangePaymentMethod();
        }
    }

    @Override
    public void showDetailModal(@NonNull final OneTapModel model) {
        PaymentDetailInfoDialog.showDialog(getChildFragmentManager());
    }

    @Override
    public void startExplodingLoading() {

        //TODO it comes here when the payment has started processing

//        explodingButtonView.startProgressBarAnimation();

//        final Fragment fragment = new ExplodingButtonFragment();
//        final Bundle args = new Bundle();
//        args.putInt(ExplodingButtonFragment.START_Y_KEY, getAnimationY());
//        Log.d("button", String.valueOf(getAnimationY()));
//        args.putString(ExplodingButtonFragment.LOADING_TEXT, "Pagando..");
//        fragment.setArguments(args);
//
//        getChildFragmentManager().beginTransaction().add(fragment, ExplodingButtonFragment.TAG).commitNow();

//        ViewStylingParams stylingParams = new ViewStylingParams(R.color.px_order_success_color, R.color.px_order_success_color_dark,
//            R.drawable.px_ic_buy_success);
//
//        ExplodingButtonFragment explodingFragment = (ExplodingButtonFragment) getChildFragmentManager()
//            .findFragmentByTag(ExplodingButtonFragment.TAG);
//        explodingFragment.finishLoading(stylingParams);

    }

    private int getScrollViewTopCoordinate() {
        final Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);
        return scrollBounds.top;
    }

    @Override
    public void trackConfirm(final OneTapModel model) {
        if (getActivity() != null) {
            Tracker.trackOneTapConfirm(getActivity().getApplicationContext(), model.getPublicKey(),
                model.getPaymentMethods().getOneTapMetadata(), amountToPay);
        }
    }

    @Override
    public void trackCancel(final String publicKey) {
        if (getActivity() != null) {
            Tracker.trackOneTapCancel(getActivity().getApplicationContext(), publicKey);
        }
    }

    @Override
    public void trackModal(final OneTapModel model) {
        if (getActivity() != null) {
            Tracker
                .trackOneTapSummaryDetail(getActivity().getApplicationContext(), model.getPublicKey(), hasDiscount,
                    model.getPaymentMethods().getOneTapMetadata().getCard());
        }
    }

    @Override
    public void showPaymentProcessor() {
        PaymentProcessorPluginActivity.start(this, REQ_CODE_PAYMENT_PROCESSOR);
    }

    @Override
    public void showErrorView(@NonNull final MercadoPagoError error) {
        if (getActivity() != null) {
            ErrorUtil.startErrorActivity(getActivity(), error);
        }
    }


    private void finishAnimation() {
        Log.d("button", "finish animation from fragment");
        //TODO prueba: representa cuando terminó de hacerse el pago

        ViewStylingParams stylingParams = new ViewStylingParams(R.color.px_order_success_color, R.color.px_order_success_color_dark,
            R.drawable.px_ic_buy_success);

                explodingButtonView.finishLoading(stylingParams);
//        explodingButtonView.finishLoading(stylingParams);
//        explodingViewInfo.finishAnim();
//        explodingViewContainer.setProps(explodingViewInfo, explodingContainer);

    }

    @Override
    public void showBusinessResult(final BusinessPayment businessPayment) {
        //TODO refactor
        if (getActivity() != null) {
            //TODO fix
            this.businessPayment = businessPayment;
            finishAnimation();
            //((CheckoutActivity) getActivity()).presenter.onBusinessResult(businessPayment);
        }
    }

    @Override
    public void showPaymentResult(final PaymentResult paymentResult) {
        //TODO refactor
        if (getActivity() != null) {
            //TODO fix
            this.paymentResult = paymentResult;
            finishAnimation();
            //((CheckoutActivity) getActivity()).presenter.checkStartPaymentResultActivity(paymentResult);
        }
    }

    @Override
    public void showCardFlow(@NonNull final OneTapModel model, @NonNull final Card card) {
        new MercadoPagoComponents.Activities.CardVaultActivityBuilder()
            .setCard(card)
            .startActivity(this, REQ_CODE_CARD_VAULT);
    }
}
