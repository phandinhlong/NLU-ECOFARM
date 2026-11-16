package jakarta.ecofarm_backend.modules.log;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalImageStoreTest {
    @TempDir Path directory;

    @Test
    void savesPdfAndHashesExactlyTheBytesReceived() throws Exception {
        byte[] bytes = "%PDF-1.7\nexample".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        var store = new LocalImageStore(directory);
        var image = store.save(new ByteArrayInputStream(bytes), "application/pdf");
        assertEquals("4c05a9d358d6ae170333b35b69ddf857bde90fe521d98136c23d8cda233fcedd", image.sha256());
        assertArrayEquals(bytes, store.read(image.fileKey()));
    }

    @Test
    void rejectsInvalidAndOversizeFilesWithoutLeavingPartialFiles() throws Exception {
        var store = new LocalImageStore(directory);
        assertThrows(IllegalArgumentException.class, () -> store.save(new ByteArrayInputStream("not an image".getBytes()), "image/png"));
        byte[] oversized = new byte[LocalImageStore.MAX_BYTES + 1];
        System.arraycopy(new byte[] {(byte) 0x89, 80, 78, 71, 13, 10, 26, 10}, 0, oversized, 0, 8);
        assertThrows(IllegalArgumentException.class, () -> store.save(new ByteArrayInputStream(oversized), "image/png"));
        try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
    }

    @Test
    void enforcesConfiguredLimitOnStream() throws Exception {
        byte[] bytes = "%PDF-1.7".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        var store = new LocalImageStore(directory, bytes.length);
        assertArrayEquals(bytes, store.read(store.save(new ByteArrayInputStream(bytes), "application/pdf").fileKey()));
        var smallerStore = new LocalImageStore(directory, bytes.length - 1);
        assertThrows(IllegalArgumentException.class, () -> smallerStore.save(new ByteArrayInputStream(bytes), "application/pdf"));
    }

    @Test
    void refusesPathTraversalAndSymlinkedFiles() throws Exception {
        var store = new LocalImageStore(directory);
        assertThrows(IllegalArgumentException.class, () -> store.read("../secret"));
        Path target = directory.resolve("target");
        Files.writeString(target, "private");
        String key = java.util.UUID.randomUUID().toString();
        Files.createSymbolicLink(directory.resolve(key), target);
        assertThrows(java.io.IOException.class, () -> store.read(key));
    }
}
