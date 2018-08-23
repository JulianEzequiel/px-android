package com.mercadopago.android.px.internal.features.onetap.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.ButtonLink;
import com.mercadopago.android.px.internal.view.ButtonPrimary;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.internal.view.exploding.ExplodingButtonFragment;
import com.mercadopago.android.px.internal.view.exploding.StatusBarDecorator;
import com.mercadopago.android.px.internal.view.exploding.ViewStylingParams;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Discount;
import javax.annotation.Nonnull;

public class ExplodingViewContainer extends CompactComponent<ExplodingViewInfo, ExplodingViewContainer.Actions> {

    //TODO check timeout
    private static final int MAX_LOADING_TIME = 30000; // the max loading time in milliseconds
    public static final float ICON_SCALE = 3.0f;
    private static final int DEFAULT_ANIMATION_TIME = 300;
    private static final int LONG_ANIMATION_TIME = 500;

    private ProgressBar progressBar;
    private ObjectAnimator animator;
    private ImageView icon;
    private ImageView circle;
    private View reveal;
    private TextView text;
    private View rootView;

    private int startY;
    private String loadingText;

    private Boolean closeState;
    private ViewStylingParams viewStylingParams;

    private AnimatorListenerAdapter resultAnimFinishListener;

    public ExplodingViewContainer(final ExplodingViewInfo explodingViewInfo, final ExplodingViewContainer.Actions callback) {
        super(explodingViewInfo, callback);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        if (props.readyToFinishAnim && rootView != null) {
            ViewStylingParams stylingParams = new ViewStylingParams(R.color.px_order_success_color, R.color.px_order_success_color_dark,
                R.drawable.px_ic_buy_success);
            finishLoading(stylingParams, parent);
        } else {
            rootView = inflate(parent, R.layout.px_loading_buy_fragment);

            circle = (ImageView) rootView.findViewById(R.id.px_loading_buy_circular);
            icon = (ImageView) rootView.findViewById(R.id.px_loading_buy_icon);
            reveal = rootView.findViewById(R.id.px_loading_buy_reveal);
            text = (TextView) rootView.findViewById(R.id.px_loading_buy_progress_text);
            text.setText(loadingText);

            // set the initial Y to match the button clicked
            View loadingContainer = rootView.findViewById(R.id.px_loading_buy_container);
            //TODO fix y coordinate
            startY = 500;
            loadingContainer.setY(startY);

            progressBar = (ProgressBar) rootView.findViewById(R.id.px_loading_buy_progress);
            progressBar.setMax(MAX_LOADING_TIME);

            // inital button animation
            // start loading assuming the worst time possible
            animator = ObjectAnimator.ofInt(progressBar, "progress", 0, MAX_LOADING_TIME);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(MAX_LOADING_TIME).start();
            Log.d("button", "progress bar animator start");

            resultAnimFinishListener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                    ((ValueAnimator) animation).removeAllUpdateListeners();
                    if (isAttached()) {
                        //TODO show
                        Log.d("button", "transform animation end, create icon");
                    createResultIconAnim(parent);
                    }
                }
            };
        }
        return rootView;
    }

    private boolean isAttached() {
//        return listener != null;
        return getActions() != null;
    }


    /**
     * Notify this view that the loading has finish so as to start the finish anim.
     *
     */
    public void finishLoading(@Nullable ViewStylingParams orderViewStylingParams, final ViewGroup parent) {
        this.viewStylingParams = orderViewStylingParams;
        if (this.viewStylingParams == null) {
//             in case of an abort, simply close the loading
            closeLoading(false);
        } else {
            Log.d("button", "in finish loading");
//             now finish the remaining loading progress
            int progress = progressBar.getProgress();
            animator.cancel();
            animator = ObjectAnimator.ofInt(progressBar, "progress", progress, MAX_LOADING_TIME);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(LONG_ANIMATION_TIME);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d("button", "progress bar finish anim on animation end");
                    animator.removeListener(this);
                    if (isAttached()) {
                        Log.d("button", "on progress bar animation end");
                        createResultAnim(parent);
                    }
                }
            });
            animator.start();
            Log.d("button", "progress bar finish anim started");
        }
    }

    /**
     * Transform the progress bar into the result icon background.
     * The color and the shape are animated.
     */
    private void createResultAnim(ViewGroup parent) {
        Log.d("button", "in transform to icon animation");
        @ColorInt
//        int color = ContextCompat.getColor(getContext(), viewStylingParams.getDarkPrimaryColor());
            //TODO fix color
            int color = ContextCompat.getColor(parent.getContext(), R.color.ui_meli_green);
        circle.setColorFilter(color);
        icon.setImageResource(viewStylingParams.getStatusIcon());

        //TODO check
        int duration = DEFAULT_ANIMATION_TIME;
        final int initialWidth = progressBar.getWidth();
        final int finalSize = progressBar.getHeight();
        final int initialRadius = parent.getResources().getDimensionPixelOffset(R.dimen.px_button_corners_radius);
        final int finalRadius = finalSize / 2;

        final GradientDrawable initialBg = getProgressBarShape(ContextCompat.getColor(parent.getContext(), R.color.ui_action_button_pressed), initialRadius);
        final GradientDrawable finalBg = getProgressBarShape(color, initialRadius);
        TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{initialBg, finalBg});
        progressBar.setProgressDrawable(transitionDrawable);
        transitionDrawable.startTransition(duration);

        ValueAnimator a = ValueAnimator.ofFloat(0, 1);
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float interpolatedTime = animation.getAnimatedFraction();
                int radius = getNewRadius(interpolatedTime);
                setRadius(initialBg, radius);
                setRadius(finalBg, radius);
                progressBar.getLayoutParams().width = getNewWidth(interpolatedTime);
                progressBar.requestLayout();
            }

            private int getNewRadius(float t) {
                return initialRadius + (int) ((finalRadius - initialRadius) * t);
            }

            private int getNewWidth(float t) {
                return initialWidth + (int) ((finalSize - initialWidth) * t);
            }

            private void setRadius(Drawable bg, int value) {
                GradientDrawable layerBg = (GradientDrawable) bg;
                layerBg.setCornerRadius(value);
            }
        });
        a.addListener(resultAnimFinishListener);
        a.setInterpolator(new DecelerateInterpolator(2f));
        a.setDuration(duration);
        a.start();
        Log.d("button", "transform to icon animation started");
        text.setVisibility(View.GONE);
    }

    /**
     * @return the shape of the progress bar to transform
     */
    private GradientDrawable getProgressBarShape(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    /**
     * Now that the icon background is visible, animate the icon.
     * The icon will start big and transparent and become small and opaque
     */
    private void createResultIconAnim(final ViewGroup parent) {
        progressBar.setVisibility(View.INVISIBLE);
        icon.setVisibility(View.VISIBLE);
        circle.setVisibility(View.VISIBLE);

        icon.setScaleY(ICON_SCALE);
        icon.setScaleX(ICON_SCALE);
        icon.setAlpha(0f);
        icon.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f)
            .setInterpolator(new DecelerateInterpolator(2f))
            .setDuration(DEFAULT_ANIMATION_TIME)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                    if (isAttached()) {
                        createCircularReveal(parent);
                    }
                }
            }).start();
    }

    /**
     * Wait so that the icon is visible for a while.. then fill the whole screen with the appropriate color.
     */
    private void createCircularReveal(final ViewGroup parent) {
        // when the icon anim has finished, paint the whole screen with the result color
        float finalRadius = (float) Math.hypot(rootView.getWidth(), rootView.getHeight());
        int startRadius = parent.getContext().getResources().getDimensionPixelOffset(R.dimen.px_m_height) / 2;
        int cx = (progressBar.getLeft() + progressBar.getRight()) / 2;
        int cy = (progressBar.getTop() + progressBar.getBottom()) / 2 + startY;

        Animator anim;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(reveal, cx, cy, startRadius, finalRadius);
        } else {
            anim = ObjectAnimator.ofFloat(reveal, "alpha", 0, 1);
        }

        anim.setDuration(LONG_ANIMATION_TIME);
        anim.setStartDelay(LONG_ANIMATION_TIME);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (isAttached()) {
                    circle.setVisibility(View.GONE);
                    icon.setVisibility(View.GONE);
                    reveal.setVisibility(View.VISIBLE);

                    int startColor = ContextCompat.getColor(parent.getContext(), viewStylingParams.getDarkPrimaryColor());
                    int endColor = ContextCompat.getColor(parent.getContext(), viewStylingParams.getPrimaryColor());
                    Drawable[] switchColors = new Drawable[]{new ColorDrawable(startColor), new ColorDrawable(endColor)};
                    TransitionDrawable colorSwitch = new TransitionDrawable(switchColors);
                    reveal.setBackgroundDrawable(colorSwitch);
                    colorSwitch.startTransition((int) animation.getDuration());

                    //TODO fix
//                    new StatusBarDecorator(getActivity().getWindow()).setupStatusBarColor(endColor);
                    if (getActions() != null) {
                        getActions().onStatusBarColorChange(viewStylingParams.getPrimaryColor());
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                closeLoading(true);
            }
        });
        anim.start();
    }


//    private void setFinishExplodingListener(Context context) {
//        if (context == null || context instanceof ExplodingButtonFragment.ExplodingButtonListener) {
//            listener = (ExplodingButtonFragment.ExplodingButtonListener) context;
//            if (listener != null && closeState != null) {
//                // if the loading finished while the view wasn't listening, then notify
//                listener.onLoadingAnimFinished(closeState, viewStylingParams);
//            }
//        } else {
//            throw new RuntimeException(context.toString() + " must implement ExplodingButtonListener");
//        }
//    }

    /**
     * when all animations have finished, close this loading
     */
    private void closeLoading(boolean success) {
        if (getActions() == null) {
            closeState = true; // the listener was not attached, notify later
        } else {
            //TODO fix
            getActions().onExplodingAnimationFinished();
//            listener.onLoadingAnimFinished(success, viewStylingParams);
        }
    }

    public interface Actions {
        void onExplodingAnimationFinished();
        void onStatusBarColorChange(int primaryColor);
    }

}
