package org.aksw.dcat_suite.app.vaadin.view;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class InvokeUtils {

    /**
     * Invoke the callable and return an optional with the value.
     * In case of an exception yields an empty optional.
     */
    public static <T> Optional<T> tryCall(Callable<T> callable) {
        Optional<T> result;
        try {
            result = Optional.ofNullable(callable.call());
        } catch (Exception e) {
            result = Optional.empty();
        }
        return result;
    }

    public static <T> void invoke(AutoCloseable action, Consumer<? super Exception> exceptionHandler) {
        try {
            action.close();
        } catch (Exception e) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(e);
            }
        }
    }

}
