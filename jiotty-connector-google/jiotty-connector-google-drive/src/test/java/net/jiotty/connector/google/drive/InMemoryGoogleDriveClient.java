package net.jiotty.connector.google.drive;

public final class InMemoryGoogleDriveClient implements GoogleDriveClient {
    private final InMemoryGoogleDrivePath rootPath = new InMemoryGoogleDrivePath(null, "/");

    @Override
    public GoogleDrivePath getRootFolder() {
        return rootPath;
    }
}