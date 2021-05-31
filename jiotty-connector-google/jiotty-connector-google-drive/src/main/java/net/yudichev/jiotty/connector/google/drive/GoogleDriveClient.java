package net.yudichev.jiotty.connector.google.drive;

import com.google.api.services.drive.model.About;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public interface GoogleDriveClient {
    GoogleDrivePath getRootFolder(Executor executor);

    default GoogleDrivePath getRootFolder() {
        return getRootFolder(ForkJoinPool.commonPool());
    }

    GoogleDrivePath getAppDataFolder(Executor executor);

    default GoogleDrivePath getAppDataFolder() {
        return getAppDataFolder(ForkJoinPool.commonPool());
    }

    CompletableFuture<About> aboutDrive(Set<String> fields, Executor executor);

    default CompletableFuture<About> aboutDrive(Set<String> fields) {
        return aboutDrive(fields, ForkJoinPool.commonPool());
    }
}
