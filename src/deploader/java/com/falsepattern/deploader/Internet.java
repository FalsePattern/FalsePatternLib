/*
 * This file is part of FalsePatternLib.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalsePatternLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalsePatternLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalsePatternLib. If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.deploader;

import lombok.val;
import lombok.var;

import net.minecraft.launchwrapper.Launch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
            val urlConnection = URL.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                connectHTTP((HttpURLConnection) urlConnection, headers, onError, onSuccess, contentLengthCallback);
            } else {
                executeTransfer(urlConnection, onSuccess, contentLengthCallback);
            }
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

    private static void connectHTTP(HttpURLConnection connection, Map<String, String> headers, Consumer<Exception> onError, Consumer<InputStream> onSuccess, Consumer<Long> contentLengthCallback) throws IOException {
        connection.setConnectTimeout(3500);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("User-Agent",
                                      "FalsePatternLib DepLoader Internet Connector"
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
            executeTransfer(connection, onSuccess, contentLengthCallback);
        }
        connection.disconnect();
    }

    private static void executeTransfer(URLConnection connection, Consumer<InputStream> onSuccess, Consumer<Long> contentLengthCallback) throws IOException {
        contentLengthCallback.accept(connection.getContentLengthLong());
        onSuccess.accept(connection.getInputStream());
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
