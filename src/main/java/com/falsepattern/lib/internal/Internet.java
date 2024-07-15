/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.internal;

import lombok.val;

import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class Internet {
    private static final Map<String, String> NO_HEADERS = Collections.emptyMap();

    public static void connect(URL URL, Consumer<Exception> onError, Consumer<InputStream> onSuccess, Consumer<Long> contentLengthCallback) {
        connect(URL, NO_HEADERS, onError, onSuccess, contentLengthCallback);
    }

    public static void connect(URL URL, Map<String, String> headers, Consumer<Exception> onError, Consumer<InputStream> onSuccess, Consumer<Long> contentLengthCallback) {
        try {
            val connection = (HttpURLConnection) URL.openConnection();
            connection.setConnectTimeout(3500);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent",
                                          Tags.MODNAME
                                          + " "
                                          + Tags.VERSION
                                          + " Internet Connector"
                                          + " (https://github.com/FalsePattern/FalsePatternLib)");
            for (val header : headers.entrySet()) {
                val key = header.getKey();
                val value = header.getValue();
                if (key == null || value == null) {
                    throw new IllegalArgumentException("Null key or value");
                }
                if (key.isEmpty()) {
                    throw new IllegalArgumentException("Empty key");
                }
                connection.setRequestProperty(key, value);
            }
            if (connection.getResponseCode() != 200) {
                onError.accept(new Exception("HTTP response code " + connection.getResponseCode()));
            } else {
                contentLengthCallback.accept(connection.getContentLengthLong());
                onSuccess.accept(connection.getInputStream());
            }
            connection.disconnect();
        } catch (Exception e) {
            //Check if NonUpdate is present
            try {
                if (Launch.classLoader.getClassBytes("moe.mickey.forge.nonupdate.NonUpdate") != null) {
                    e.addSuppressed(new Exception(
                            "NonUpdate is present, it's possible that it's blocking a library download."
                            + " Please disable it for a single run to allow mod dependencies to download."));
                }
            } catch (IOException ex) {
                e.addSuppressed(ex);
            }
            onError.accept(e);
        }
    }

    public static void transferAndClose(InputStream is, OutputStream target, Consumer<Integer> downloadSizeCallback)
            throws IOException {
        var bytesRead = 0;

        byte[] smallBuffer = new byte[256 * 1024];
        while ((bytesRead = is.read(smallBuffer)) >= 0) {
            target.write(smallBuffer, 0, bytesRead);
            downloadSizeCallback.accept(bytesRead);
        }
        target.close();
        is.close();
    }
}
