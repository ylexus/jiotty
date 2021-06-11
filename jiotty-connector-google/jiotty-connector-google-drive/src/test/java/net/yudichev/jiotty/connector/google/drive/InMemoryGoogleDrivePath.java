package net.yudichev.jiotty.connector.google.drive;

import net.yudichev.jiotty.common.lang.CompletableFutures;
import net.yudichev.jiotty.common.lang.PublicImmutablesStyle;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.CompletableFuture.completedFuture;

public final class InMemoryGoogleDrivePath implements GoogleDrivePath {
    @Nullable
    private final InMemoryGoogleDrivePath parent;
    private final String name;
    @Nullable
    private final FileData fileData;

    private final Map<String, InMemoryGoogleDrivePath> childrenByName = new ConcurrentHashMap<>();

    InMemoryGoogleDrivePath(@Nullable InMemoryGoogleDrivePath parent, String name) {
        this(parent, name, null);
    }

    private InMemoryGoogleDrivePath(@Nullable InMemoryGoogleDrivePath parent, String name, @Nullable FileData fileData) {
        this.parent = parent;
        this.name = checkNotNull(name);
        this.fileData = fileData;
        if (parent != null) {
            //noinspection ThisEscapedInObjectConstruction OK in this particular case
            parent.childrenByName.put(name, this);
        }
    }

    @Override
    public CompletableFuture<GoogleDrivePath> createSubFolder(String childFolderName) {
        return completedFuture(new InMemoryGoogleDrivePath(this, childFolderName));
    }

    @Override
    public CompletableFuture<GoogleDrivePath> createFile(String filename, String mimeType, byte[] fileData) {
        return completedFuture(new InMemoryGoogleDrivePath(this, filename, FileData.of(mimeType, fileData)));
    }

    @Override
    public CompletableFuture<Void> delete() {
        if (parent == null) {
            return CompletableFutures.failure("cannot delete root");
        }
        parent.childrenByName.remove(name);
        return CompletableFutures.completedFuture();
    }

    @Override
    public CompletableFuture<Optional<GoogleDrivePath>> findFolderByPath(List<String> path) {
        InMemoryGoogleDrivePath node = this;
        for (String pathElement : path) {
            InMemoryGoogleDrivePath childPath = node.childrenByName.get(pathElement);
            if (childPath == null) {
                return completedFuture(Optional.empty());
            }
            node = childPath;
        }
        return completedFuture(Optional.of(node));
    }

    public Optional<FileData> getFileData() {
        return Optional.ofNullable(fileData);
    }

    public String getName() {
        return name;
    }

    Collection<InMemoryGoogleDrivePath> getChildren() {
        return childrenByName.values();
    }

    @Immutable
    @PublicImmutablesStyle
    interface BaseFileData {
        @Value.Parameter
        String getMimeType();

        @Value.Parameter
        byte[] getBytes();
    }
}
