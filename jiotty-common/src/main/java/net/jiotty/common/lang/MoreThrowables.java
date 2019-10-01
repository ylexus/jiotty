package net.jiotty.common.lang;

public final class MoreThrowables {
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
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedExceptionThrowingSupplier<T> {
        T get() throws Exception;
    }
}
