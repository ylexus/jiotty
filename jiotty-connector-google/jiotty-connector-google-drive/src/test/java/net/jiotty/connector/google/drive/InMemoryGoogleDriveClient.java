package net.jiotty.connector.google.drive;

public final class InMemoryGoogleDriveClient implements GoogleDriveClient {
    private final GoogleDrivePath rootPath = new InMemoryGoogleDrivePath(null, "/");

    @Override
    public GoogleDrivePath getRootFolder() {
        return rootPath;
    }
}