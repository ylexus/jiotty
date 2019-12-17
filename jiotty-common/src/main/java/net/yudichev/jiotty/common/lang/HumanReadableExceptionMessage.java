package net.yudichev.jiotty.common.lang;

import com.google.common.base.Throwables;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class HumanReadableExceptionMessage {
    public static String humanReadableMessage(Throwable exception) {
        List<Throwable> causalChain = Throwables.getCausalChain(exception);
        StringBuilder stringBuilder = new StringBuilder(64);
        Throwable parent = null;
        for (Throwable throwable : causalChain) {
            if (parent != null && !parent.getMessage().equals(throwable.toString())) {
                append(stringBuilder, parent.getMessage());
            }
            parent = throwable;
        }
        append(stringBuilder, checkNotNull(parent).getMessage());

        return stringBuilder.toString();
    }

    private static void append(StringBuilder stringBuilder, String message) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(": ");
        }
        stringBuilder.append(message);
    }
}
