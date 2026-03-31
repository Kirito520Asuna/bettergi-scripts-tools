package com.cloud_guest.domain.http;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedBodyServletInputStream extends ServletInputStream {

    private final InputStream cachedBodyInputStream;

    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public int read() throws IOException {
        return cachedBodyInputStream.read();
    }

    @SneakyThrows
    //@Override
    public boolean isFinished() {
        return cachedBodyInputStream.available() == 0;
    }

    //@Override
    public boolean isReady() {
        return true;
    }

    //@Override
    public void setReadListener(ReadListener listener) {

    }
}
