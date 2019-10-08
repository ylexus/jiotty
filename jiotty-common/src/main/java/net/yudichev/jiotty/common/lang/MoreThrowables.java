package net.yudichev.jiotty.common.lang;

public final class MoreThrowables {
    private MoreThrowables() {
    }

    public static void asUnchecked(CheckedExceptionThrowingRunnable action) {
        try {
            action.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getAsUnchecked(CheckedExceptionThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface CheckedExceptionThrowingRunnable {
        @SuppressWarnings("ProhibitedExceptionDeclared")
            // by design
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedExceptionThrowingSupplier<T> {
        @SuppressWarnings("ProhibitedExceptionDeclared")
            // by design
        T get() throws Exception;
    }
}
