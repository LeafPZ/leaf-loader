package dev.aoqia.leaf.loader.impl.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class UnverifiedJarStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new UnverifiedJarURLConnection(url);
    }
}
