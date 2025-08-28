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

    private static void append(StringBuilder sb, Throwable throwable, @Nullable String message) {
        if (!sb.isEmpty()) {
            sb.append(": ");
        }
        boolean typeAppended;
        if (throwable.getClass() == RuntimeException.class) {
            typeAppended = false;
        } else {
            switch (throwable) {
                case Exception e -> appendType(sb, e, "Exception".length());
                case Error e -> appendType(sb, e, "Error".length());
                default -> appendType(sb, throwable, 0);
            }
            typeAppended = true;
        }
        if (message != null) {
            if (typeAppended) {
                sb.append(": ");
            }
            sb.append(message);
        }
    }

    private static void appendType(StringBuilder sb, Throwable throwable, int suffixLength) {
        String simpleName = throwable.getClass().getSimpleName();
        sb.append(simpleName.substring(0, simpleName.length() - suffixLength));
    }
}
