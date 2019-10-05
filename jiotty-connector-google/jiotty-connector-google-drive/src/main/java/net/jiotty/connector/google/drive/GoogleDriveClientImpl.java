package net.jiotty.connector.google.drive;

import com.google.api.services.drive.Drive;
import com.google.common.collect.ImmutableList;
import com.google.inject.BindingAnnotation;
import net.jiotty.common.inject.BaseLifecycleComponent;

import javax.inject.Inject;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

final class GoogleDriveClientImpl extends BaseLifecycleComponent implements GoogleDriveClient {
    private static final String ROOT_ID = "root";
    private final Drive drive;

    @Inject
    GoogleDriveClientImpl(@Dependency Drive drive) {
        this.drive = checkNotNull(drive);
    }

    @Override
    public GoogleDrivePath getRootFolder() {
        return new InternalGoogleDrivePath(drive, ROOT_ID, ImmutableList.of());
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface Dependency {
    }
}
