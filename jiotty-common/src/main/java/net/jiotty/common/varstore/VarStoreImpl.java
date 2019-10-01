package net.jiotty.common.varstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableSet;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.nio.file.Files.*;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static net.jiotty.common.lang.Locks.inLock;
import static net.jiotty.common.lang.MoreThrowables.asUnchecked;
import static net.jiotty.common.lang.MoreThrowables.getAsUnchecked;

final class VarStoreImpl implements VarStore {
    private final static ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .registerModule(new GuavaModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path storeFile;
    private final Lock lock = new ReentrantLock();

    @Inject
    VarStoreImpl(@AppName String applicationName) {
        storeFile = Paths.get(System.getProperty("user.home"), "." + applicationName, "data.json");
    }

    @Override
    public void saveValue(String key, Object value) {
        inLock(lock, () -> asUnchecked(() -> {
            ObjectNode configNode = readConfig();

            configNode.set(key, mapper.valueToTree(value));
            mapper.writeValue(storeFile.toFile(), configNode);
        }));
    }

    @Override
    public <T> Optional<T> readValue(Class<T> type, String key) {
        return inLock(lock, () -> getAsUnchecked(() -> {
            ObjectNode configNode = readConfig();

            return Optional.ofNullable(configNode.get(key))
                    .map(valueNode -> getAsUnchecked(() -> mapper.readerFor(type).readValue(valueNode)));
        }));
    }

    private ObjectNode readConfig() throws IOException {
        if (!isRegularFile(storeFile)) {
            createDirectories(storeFile.getParent());
            createFile(storeFile, PosixFilePermissions.asFileAttribute(ImmutableSet.of(OWNER_READ, OWNER_WRITE)));
        }

        ObjectNode configNode;
        byte[] contents = readAllBytes(storeFile);
        if (contents.length > 0) {
            configNode = mapper.readValue(contents, ObjectNode.class);
        } else {
            configNode = mapper.createObjectNode();
        }

        return configNode;
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    @interface AppName {
    }
}
