package jakarta.ecofarm_backend.common.util;

import jakarta.ecofarm_backend.common.dto.response.BaseResponse;
import jakarta.servlet.http.Part;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;


@Builder
@RequiredArgsConstructor
public class ByteArrayPart implements Part {

    private final Part original;
    private final byte[] data;


    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String getContentType() {
        return original.getContentType();
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public String getSubmittedFileName() {
        return original.getSubmittedFileName();
    }

    @Override
    public long getSize() {
        return data.length;
    }

    @Override
    public void write(String fileName) {
    }

    @Override
    public void delete() {
    }

    @Override
    public String getHeader(String name) {
        return original.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return original.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return original.getHeaderNames();
    }
}
