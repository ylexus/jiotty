package net.jiotty.common.lang;

import com.google.common.reflect.TypeToken;
import org.mockito.ArgumentCaptor;

public final class ArgumentCaptors {
    private ArgumentCaptors() {
    }

    @SuppressWarnings("unchecked")
    public static <T> ArgumentCaptor<T> captured(TypeToken<T> type) {
        return (ArgumentCaptor<T>) ArgumentCaptor.forClass(type.getRawType());
    }
}
