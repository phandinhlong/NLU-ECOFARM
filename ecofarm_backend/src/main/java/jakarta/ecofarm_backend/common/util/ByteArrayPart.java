package jakarta.ecofarm_backend.common.util;

import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.EntityPart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Adapts an {@link EntityPart} and in-memory byte array into a Servlet {@link Part}
 * for compatibility with StorageService.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ByteArrayPart implements Part {

    private EntityPart original;
    private byte[] data;

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data != null ? data : new byte[0]);
    }

    @Override
    public String getContentType() {
        if (original == null || original.getMediaType() == null) {
            return null;
        }
        return original.getMediaType().getType() + "/" + original.getMediaType().getSubtype();
    }

    @Override
    public String getName() {
        return original != null ? original.getName() : null;
    }

    @Override
    public String getSubmittedFileName() {
        if (original == null) {
            return "unknown";
        }
        return original.getFileName().orElse(original.getName());
    }

    @Override
    public long getSize() {
        return data != null ? data.length : 0;
    }

    @Override
    public void write(String fileName) {
    }

    @Override
    public void delete() {
    }

    @Override
    public String getHeader(String name) {
        if (original == null || original.getHeaders() == null) {
            return null;
        }
        return original.getHeaders().getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        if (original == null || original.getHeaders() == null) {
            return Collections.emptyList();
        }
        return original.getHeaders().get(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        if (original == null || original.getHeaders() == null) {
            return Collections.emptyList();
        }
        return original.getHeaders().keySet();
    }
}
