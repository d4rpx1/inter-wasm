package com.group4.interwasm.util;

import java.net.URL;

public class FilesUtil {
    /**
     * Returns the full path to a file from resources folder
     *
     * @param path e.x. "wasm-examples/add_with_local/add_with_local.wasm"
     * @return Full path
     */
    public static String getPathToResourceFile(String path) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL resourceUrl = classloader.getResource(path);

        if (resourceUrl == null) {
            throw new IllegalArgumentException("File not found!");
        }

        return resourceUrl.getPath();
    }
}
