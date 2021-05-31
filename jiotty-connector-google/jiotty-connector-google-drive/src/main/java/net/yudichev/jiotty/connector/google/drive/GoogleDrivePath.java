package net.yudichev.jiotty.connector.google.drive;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public interface GoogleDrivePath {
    CompletableFuture<GoogleDrivePath> createSubFolder(String childFolderName);

    CompletableFuture<GoogleDrivePath> createFile(String filename, String mimeType, byte[] fileData);

    CompletableFuture<Void> delete();

    CompletableFuture<Optional<GoogleDrivePath>> findFolderByPath(List<String> path);

    default CompletableFuture<GoogleDrivePath> findOrCreateSubFolder(String folderName) {
        return findFolderByPath(ImmutableList.of(folderName))
                .thenCompose(optionalFolderId -> optionalFolderId
                        .map(CompletableFuture::completedFuture)
                        .orElseGet(() -> createSubFolder(folderName)));
    }

    default CompletableFuture<GoogleDrivePath> findOrCreateSubFolders(List<String> path) {
        CompletableFuture<GoogleDrivePath> folder = completedFuture(this);
        for (String pathElement : path) {
            folder = folder.thenCompose(parent -> parent.findOrCreateSubFolder(pathElement));
        }
        return folder;
    }
}
