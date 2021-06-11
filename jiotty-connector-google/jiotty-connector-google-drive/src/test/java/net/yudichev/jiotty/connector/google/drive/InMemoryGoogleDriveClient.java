package net.yudichev.jiotty.connector.google.drive;

import com.google.api.services.drive.model.About;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class InMemoryGoogleDriveClient implements GoogleDriveClient {
    private final InMemoryGoogleDrivePath rootPath = new InMemoryGoogleDrivePath(null, "/");
    private final InMemoryGoogleDrivePath appDataPath = new InMemoryGoogleDrivePath(null, "/.appdata");

    @Override
    public GoogleDrivePath getRootFolder(Executor executor) {
        return rootPath;
    }

    @Override
    public GoogleDrivePath getAppDataFolder(Executor executor) {
        return appDataPath;
    }

    @Override
    public CompletableFuture<About> aboutDrive(Set<String> fields, Executor executor) {
        return CompletableFuture.completedFuture(new About()
                .setStorageQuota(new About.StorageQuota()
                        .setLimit(1024L * 1024 * 1024) // 1Gb
                        .setUsage(usageIn(rootPath) + usageIn(appDataPath))
                ));
    }

    private long usageIn(InMemoryGoogleDrivePath path) {
        long usage = path.getFileData().map(fileData -> fileData.getBytes().length).orElse(0);
        usage += path.getChildren().stream().mapToLong(this::usageIn).sum();
        return usage;
    }
}