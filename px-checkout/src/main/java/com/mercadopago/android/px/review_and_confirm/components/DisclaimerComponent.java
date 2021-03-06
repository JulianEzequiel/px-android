package com.mercadopago.android.px.review_and_confirm.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.components.Component;
import com.mercadopago.android.px.components.RendererFactory;

/**
 * Created by mromar on 2/28/18.
 */

public class DisclaimerComponent extends Component<DisclaimerComponent.Props, Void> {

    static {
        RendererFactory.register(DisclaimerComponent.class, DisclaimerRenderer.class);
    }

    public DisclaimerComponent(@NonNull final DisclaimerComponent.Props props) {
        super(props);
    }

    public static class Props {
        final String disclaimer;

        public Props(final String disclaimer) {
            this.disclaimer = disclaimer;
        }
    }
}
