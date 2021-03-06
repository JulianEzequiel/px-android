package com.mercadopago.android.px.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;

/**
 * Created by nfortuna on 11/1/17.
 */

public class LoadingRenderer extends Renderer<LoadingComponent> {

    @Override
    public View render(final LoadingComponent component, final Context context, final ViewGroup parent) {
        return inflate(R.layout.px_view_progress_bar, parent);
    }
}
