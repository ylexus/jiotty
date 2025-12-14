package net.yudichev.jiotty.common.lang;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.common.lang.MoreThrowables.asUnchecked;

@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
public interface Appender extends Appendable {
    @Override
    Appender append(CharSequence csq);

    @Override
    Appender append(CharSequence csq, int start, int end);

    @Override
    Appender append(char c);

    Appender append(int i);

    Appender append(Object object);

    static Appender wrap(Appendable appendable) {
        return new AppendableWrapper(appendable);
    }

    static Appender wrap(StringBuilder sb) {
        return new StringBuilderAppender(sb);
    }

    abstract class BaseWrapper implements Appender {
        private final Appendable appendable;

        private BaseWrapper(Appendable appendable) {
            this.appendable = checkNotNull(appendable);
        }

        @Override
        public Appender append(CharSequence csq) {
            asUnchecked(() -> appendable.append(csq));
            return this;
        }

        @Override
        public Appender append(CharSequence csq, int start, int end) {
            asUnchecked(() -> appendable.append(csq, start, end));
            return this;
        }

        @Override
        public Appender append(char c) {
            asUnchecked(() -> appendable.append(c));
            return this;
        }
    }

    final class AppendableWrapper extends BaseWrapper {

        private AppendableWrapper(Appendable appendable) {
            super(appendable);
        }

        @Override
        public Appender append(int i) {
            return append(Integer.toString(i));
        }

        @Override
        public Appender append(Object object) {
            return append(String.valueOf(object));
        }

    }

    class StringBuilderAppender extends BaseWrapper {
        @SuppressWarnings("StringBufferField")
        private final StringBuilder sb;

        public StringBuilderAppender(StringBuilder sb) {
            super(sb);
            this.sb = sb;
        }

        @Override
        public Appender append(int i) {
            sb.append(i);
            return this;
        }

        @Override
        public Appender append(Object object) {
            sb.append(object);
            return this;
        }
    }
}
