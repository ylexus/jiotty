package net.yudichev.jiotty.connector.google.drive;

import com.google.api.services.drive.model.About;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class InMemoryGoogleDriveClient implements GoogleDriveClient {
    private final GoogleDrivePath rootPath = new InMemoryGoogleDrivePath(null, "/");
    private final GoogleDrivePath appDataPath = new InMemoryGoogleDrivePath(null, "/.appdata");

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
        return CompletableFuture.completedFuture(new About());
    }
}