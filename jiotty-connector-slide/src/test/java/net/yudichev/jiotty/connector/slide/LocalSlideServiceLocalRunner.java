package net.yudichev.jiotty.connector.slide;

import java.util.concurrent.TimeUnit;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToSystemExit"})
final class LocalSlideServiceLocalRunner {
    public static void main(String[] args) throws Exception {
        var service = new LocalSlideService(args[0], args[1]);
        System.out.println(service.getSlideInfo(-1).get(5, TimeUnit.SECONDS));
        System.exit(0);
    }
}