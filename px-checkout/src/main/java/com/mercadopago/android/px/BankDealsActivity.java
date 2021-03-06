package com.mercadopago.android.px;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.adapters.BankDealsAdapter;
import com.mercadopago.android.px.callbacks.FailureRecovery;
import com.mercadopago.android.px.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.core.MercadoPagoServicesAdapter;
import com.mercadopago.android.px.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.mvp.TaggedCallback;
import com.mercadopago.android.px.tracker.FlowHandler;
import com.mercadopago.android.px.tracker.MPTrackingContext;
import com.mercadopago.android.px.tracking.model.ScreenViewEvent;
import com.mercadopago.android.px.tracking.utils.TrackingUtil;
import com.mercadopago.android.px.util.ApiUtil;
import com.mercadopago.android.px.util.ErrorUtil;
import com.mercadopago.android.px.util.JsonUtil;
import com.mercadopago.android.px.util.ViewUtils;
import java.lang.reflect.Type;
import java.util.List;

public class BankDealsActivity extends MercadoPagoActivity implements OnSelectedCallback<BankDeal> {

    //Activity parameters
    protected String mMerchantPublicKey;

    // Local vars
    protected MercadoPagoServicesAdapter mMercadoPago;
    protected RecyclerView mRecyclerView;
    protected Toolbar mToolbar;

    protected List<BankDeal> mBankDeals;
    protected String mPayerAccessToken;

    @Override
    protected void onValidStart() {
        trackInitialScreen();
        mMercadoPago = new MercadoPagoServicesAdapter(getActivity(), mMerchantPublicKey, mPayerAccessToken);
        getBankDeals();
    }

    protected void trackInitialScreen() {
        MPTrackingContext mpTrackingContext = new MPTrackingContext.Builder(this, mMerchantPublicKey)
            .setVersion(BuildConfig.VERSION_NAME)
            .build();
        ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(TrackingUtil.SCREEN_ID_BANK_DEALS)
            .setScreenName(TrackingUtil.SCREEN_NAME_BANK_DEALS)
            .build();

        mpTrackingContext.trackEvent(event);
    }

    @Override
    protected void onInvalidStart(String message) {
        ErrorUtil.startErrorActivity(this, message, false, mMerchantPublicKey);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.px_activity_bank_deals);
    }

    @Override
    protected void initializeControls() {
        initializeToolbar();
        mRecyclerView = findViewById(R.id.mpsdkBankDealsList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    protected void getActivityParameters() {
        mMerchantPublicKey = getIntent().getStringExtra("merchantPublicKey");
        mPayerAccessToken = getIntent().getStringExtra("payerAccessToken");
        try {
            Type listType = new TypeToken<List<BankDeal>>() {
            }.getType();
            mBankDeals = JsonUtil.getInstance().getGson().fromJson(getIntent().getStringExtra("bankDeals"), listType);
        } catch (Exception ex) {
            mBankDeals = null;
        }
    }

    private void initializeToolbar() {
        mToolbar = findViewById(R.id.mpsdkToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void validateActivityParameters() throws IllegalStateException {
        if (mMerchantPublicKey == null) {
            throw new IllegalStateException("public key not set");
        }
    }

    private void getBankDeals() {
        ViewUtils.showProgressLayout(this);
        mMercadoPago.getBankDeals(new TaggedCallback<List<BankDeal>>(ApiUtil.RequestOrigin.GET_BANK_DEALS) {

            @Override
            public void onSuccess(final List<BankDeal> bankDeals) {
                solveBankDeals(bankDeals);
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                if (isActivityActive()) {
                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            getBankDeals();
                        }
                    });

                    ApiUtil.showApiExceptionError(getActivity(),
                        error.getApiException(),
                        mMerchantPublicKey,
                        ApiUtil.RequestOrigin.GET_BANK_DEALS);
                } else {
                    finishWithCancelResult();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                recoverFromFailure();
            } else {
                finishWithCancelResult();
            }
        }
    }

    private void finishWithCancelResult() {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void solveBankDeals(List<BankDeal> bankDeals) {
        mRecyclerView.setAdapter(new BankDealsAdapter(bankDeals, this));
        ViewUtils.showRegularLayout(getActivity());
    }

    @Override
    public void onSelected(final BankDeal selectedBankDeal) {
        BankDealDetailActivity.startWithBankDealLegals(this, selectedBankDeal);
    }
}
