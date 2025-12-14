package net.yudichev.jiotty.user.ui;

import net.yudichev.jiotty.common.lang.Closeable;

public interface UIServer {
    Closeable registerDisplayable(Displayable displayable);

    Closeable registerOption(Option<?> option);
}
