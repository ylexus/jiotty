package net.yudichev.jiotty.common.lang;

import com.google.common.base.Throwables;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class HumanReadableExceptionMessage {
    public static String humanReadableMessage(Throwable exception) {
        List<Throwable> causalChain = Throwables.getCausalChain(exception);
        StringBuilder stringBuilder = new StringBuilder(64);
        Throwable parent = null;
        for (Throwable throwable : causalChain) {
            String parentExceptionMessage;
            if (parent != null && !throwable.toString().equals(parentExceptionMessage = exceptionMessage(parent))) {
                append(stringBuilder, parent, parentExceptionMessage);
            }
            parent = throwable;
        }
        append(stringBuilder, checkNotNull(parent), exceptionMessage(parent));

        return stringBuilder.toString();
    }

    @Nullable
    private static String exceptionMessage(Throwable exception) {
        return exception instanceof InterruptedException ? null : exception.getMessage();
    }

    private static void append(StringBuilder stringBuilder, Throwable throwable, @Nullable String message) {
        if (!stringBuilder.isEmpty()) {
            stringBuilder.append(": ");
        }
        if (throwable.getClass() == RuntimeException.class) {
            stringBuilder.append("Failure");
        } else {
            switch (throwable) {
                case Exception e -> appendType(stringBuilder, e, "Exception".length());
                case Error e -> appendType(stringBuilder, e, "Error".length());
                default -> appendType(stringBuilder, throwable, 0);
            }
        }
        if (message != null) {
            stringBuilder.append(": ").append(message);
        }
    }

    private static void appendType(StringBuilder stringBuilder, Throwable throwable, int suffixLength) {
        String simpleName = throwable.getClass().getSimpleName();
        stringBuilder.append(simpleName.substring(0, simpleName.length() - suffixLength));
    }
}
