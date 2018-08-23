package com.mercadopago.android.px.internal.view.exploding;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;

public class ExplodingButtonView extends FrameLayout {

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
//    private String loadingText;

    private Boolean closeState;
    private ViewStylingParams viewStylingParams;

    private AnimatorListenerAdapter resultAnimFinishListener;
    private ExplodingButtonListener explodingButtonListener;
    private MeliButton confirmButton;

    public ExplodingButtonView(@NonNull final Context context) {
        super(context, null);
    }

    public ExplodingButtonView(@NonNull final Context context,
        @Nullable final AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ExplodingButtonView(@NonNull final Context context, @Nullable final AttributeSet attrs,
        final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExplodingButtonView(@NonNull final Context context, @Nullable final AttributeSet attrs,
        final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public ExplodingButtonView(@NonNull final Context context, @Nullable final AttributeSet attrs,
        final int defStyleAttr, ExplodingButtonListener listener) {
        this(context, attrs, defStyleAttr);
        explodingButtonListener = listener;
    }

    private void initView(final Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.px_exploding_button, this);
        final FrameLayout.LayoutParams layoutParams =
            new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);

        circle =  findViewById(R.id.px_loading_buy_circular);
        icon =  findViewById(R.id.px_loading_buy_icon);
        reveal = findViewById(R.id.px_loading_buy_reveal);
        text =  findViewById(R.id.px_loading_buy_progress_text);
        //TODO fix
//        text.setText("text");

        // set the initial Y to match the button clicked
        View loadingContainer = findViewById(R.id.px_loading_buy_container);
        //TODO fix y coordinate
        startY = 72;
        loadingContainer.setY(startY);

        progressBar = findViewById(R.id.px_loading_buy_progress);
        progressBar.setMax(MAX_LOADING_TIME);
        progressBar.setVisibility(GONE);
        text.setVisibility(GONE);

        confirmButton = findViewById(R.id.px_confirm_button_one_tap);
        confirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isAttached()) {
                    explodingButtonListener.onButtonClicked(v);
                    startProgressBarAnimation();
                }
            }
        });
        confirmButton.setVisibility(VISIBLE);

        resultAnimFinishListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                ((ValueAnimator) animation).removeAllUpdateListeners();
                if (isAttached()) {
                    //TODO show
                    Log.d("button", "transform animation end, create icon");
                    createResultIconAnim();
                }
            }
        };

        //startProgressBarAnimation();
    }

    public void startProgressBarAnimation() {
        confirmButton.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        setClickable(true);

        // inital button animation
        // start loading assuming the worst time possible
        animator = ObjectAnimator.ofInt(progressBar, "progress", 0, MAX_LOADING_TIME);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(MAX_LOADING_TIME).start();
        Log.d("button", "progress bar animator start");
    }

    private boolean isAttached() {
        return explodingButtonListener != null;
    }

    /**
     * Now that the icon background is visible, animate the icon.
     * The icon will start big and transparent and become small and opaque
     */
    private void createResultIconAnim() {
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
                        createCircularReveal();
                    }
                }
            }).start();
    }

    /**
     * Wait so that the icon is visible for a while.. then fill the whole screen with the appropriate color.
     */
    private void createCircularReveal() {
        // when the icon anim has finished, paint the whole screen with the result color
        float finalRadius = (float) Math.hypot(rootView.getWidth(), rootView.getHeight());
        int startRadius = getContext().getResources().getDimensionPixelOffset(R.dimen.px_m_height) / 2;
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

                    int startColor = ContextCompat.getColor(getContext(), viewStylingParams.getDarkPrimaryColor());
                    int endColor = ContextCompat.getColor(getContext(), viewStylingParams.getPrimaryColor());
                    Drawable[] switchColors = new Drawable[]{new ColorDrawable(startColor), new ColorDrawable(endColor)};
                    TransitionDrawable colorSwitch = new TransitionDrawable(switchColors);
                    reveal.setBackgroundDrawable(colorSwitch);
                    colorSwitch.startTransition((int) animation.getDuration());

                    //TODO fix
//                    new StatusBarDecorator(getActivity().getWindow()).setupStatusBarColor(endColor);
                    if (isAttached()) {
                        explodingButtonListener.onStatusBarColorChange(viewStylingParams.getPrimaryColor());
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


    /**
     * when all animations have finished, close this loading
     */
    private void closeLoading(boolean success) {
        if (!isAttached()) {
            closeState = true; // the listener was not attached, notify later
        } else {
            //TODO fix
            explodingButtonListener.onExplodingAnimationFinished();
//            listener.onLoadingAnimFinished(success, viewStylingParams);
        }
    }

    /**
     * Notify this view that the loading has finish so as to start the finish anim.
     *
     */
    public void finishLoading(@Nullable ViewStylingParams orderViewStylingParams) {
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
                        createResultAnim();
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
    private void createResultAnim() {
        Log.d("button", "in transform to icon animation");
        @ColorInt
//        int color = ContextCompat.getColor(getContext(), viewStylingParams.getDarkPrimaryColor());
            //TODO fix color
            int color = ContextCompat.getColor(getContext(), R.color.ui_meli_green);
        circle.setColorFilter(color);
        icon.setImageResource(viewStylingParams.getStatusIcon());

        //TODO check
        int duration = DEFAULT_ANIMATION_TIME;
        final int initialWidth = progressBar.getWidth();
        final int finalSize = progressBar.getHeight();
        final int initialRadius = getResources().getDimensionPixelOffset(R.dimen.px_button_corners_radius);
        final int finalRadius = finalSize / 2;

        final GradientDrawable initialBg = getProgressBarShape(ContextCompat.getColor(getContext(), R.color.ui_action_button_pressed), initialRadius);
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
}
