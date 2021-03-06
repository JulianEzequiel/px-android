package com.mercadopago.android.px.review_and_confirm.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.components.RendererFactory;
import com.mercadopago.android.px.review_and_confirm.props.AmountDescriptionProps;

/**
 * Created by mromar on 2/28/18.
 */

public class AmountDescription extends Component<AmountDescriptionProps, Void> {

    static {
        RendererFactory.register(AmountDescription.class, AmountDescriptionRenderer.class);
    }

    public AmountDescription(@NonNull AmountDescriptionProps props) {
        super(props);
    }
}
