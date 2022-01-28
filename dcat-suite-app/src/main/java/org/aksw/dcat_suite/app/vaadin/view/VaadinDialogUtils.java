package org.aksw.dcat_suite.app.vaadin.view;

import java.util.function.Consumer;

import org.claspina.confirmdialog.ButtonOption;
import org.claspina.confirmdialog.ButtonType;
import org.claspina.confirmdialog.ConfirmDialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.textfield.TextField;

public class VaadinDialogUtils {
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


        
    public static ConfirmDialog confirmInputDialog(String header, String text, String confirmText,
            Consumer<String> confirmListener,
            String cancelText,
            Consumer<?> cancelListener) {

    	TextField textField = new TextField();
    	textField.setPlaceholder(text);

    	ConfirmDialog result = confirmDialog(header, textField, confirmText, ev -> confirmListener.accept(textField.getValue()), cancelText, cancelListener);

		textField.addKeyDownListener(Key.ENTER, ev -> result.getButton(ButtonType.OK).click());

    	result.addOpenedChangeListener(ev -> {
        	textField.focus();
        });

        return result;
//    	
//        Button btn = new Button();
//        btn.addClickListener(ev -> confirmListener.accept(textField.getValue()));
//        btn.getElement().setAttribute("theme", "primary");        
//        btn.addClickShortcut(Key.ENTER);
//
//        ButtonOption NO_ICON = new ButtonOption() {
//			@Override
//			public void apply(ConfirmDialog confirmDialog, Button button) {
//				button.setIcon(null);
//			}
//        };
//        
//        ConfirmDialog dlg = ConfirmDialog
//            .create()
//            .withCaption(header)
//            .withMessage(textField)
//            .withButton(btn, ButtonOption.caption(confirmText))
//            .withAbortButton(ButtonOption.caption(cancelText), NO_ICON);
//            //.withCancelButton(() -> cancelListener.accept(null), ButtonOption.caption(cancelText));
//        dlg.setModal(true);
//        dlg.addOpenedChangeListener(ev -> {
//        	textField.focus();
//        });
//        return dlg;
    }

    
    public static ConfirmDialog confirmDialog(String header, Component content, String confirmText,
            Consumer<?> confirmListener,
            String cancelText,
            Consumer<?> cancelListener) {

//        Button btn = new Button();
//        btn.addClickListener(ev -> confirmListener.accept(null));
//        btn.getElement().setAttribute("theme", "primary");        
        //btn.addClickShortcut(Key.ENTER);

//        ButtonOption NO_ICON = new ButtonOption() {
//			@Override
//			public void apply(ConfirmDialog confirmDialog, Button button) {
//				button.setIcon(null);
//			}
//        };
        
        ConfirmDialog dlg = ConfirmDialog
            .create()
            .withCaption(header)
            .withMessage(content)
            .withOkButton(() -> confirmListener.accept(null), ButtonOption.caption(confirmText))
            .withAbortButton(() -> cancelListener.accept(null), ButtonOption.caption(cancelText));
        
        dlg.getButton(ButtonType.OK).setIcon(null);
        dlg.getButton(ButtonType.ABORT).setIcon(null);
        
            //.withCancelButton(() -> cancelListener.accept(null), ButtonOption.caption(cancelText));
        dlg.setModal(true);
        return dlg;
    }

}
