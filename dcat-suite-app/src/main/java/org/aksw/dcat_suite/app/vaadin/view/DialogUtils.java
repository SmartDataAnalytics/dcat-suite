package org.aksw.dcat_suite.app.vaadin.view;

import java.util.function.Consumer;

import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ConfirmDialog;

public class DialogUtils {
    // Signature compatible with ConfirmDialog of vaadin pro
    public static ConfirmDialog confirmDialog(String header, String text, String confirmText,
            Consumer<?> confirmListener,
            String cancelText,
            Consumer<?> cancelListener) {

        return ConfirmDialog
            .createQuestion()
            .withCaption(header)
            .withMessage(text)
            .withOkButton(() -> confirmListener.accept(null), ButtonOption.focus(), ButtonOption.caption(confirmText))
            .withCancelButton(() -> cancelListener.accept(null), ButtonOption.caption(cancelText));
    }
}
