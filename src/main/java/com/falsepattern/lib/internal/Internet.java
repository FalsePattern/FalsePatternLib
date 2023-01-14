/*
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.falsepattern.lib.internal;

import lombok.Cleanup;
import lombok.val;
import lombok.var;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Internet {
    public static void connect(URL URL, Consumer<Exception> onError, Consumer<InputStream> onSuccess) {
        try {
            val connection = (HttpURLConnection) URL.openConnection();
            connection.setConnectTimeout(3500);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", Tags.MODNAME + " " + Tags.VERSION + " Internet Connector" +
                                                        " (https://github.com/FalsePattern/FalsePatternLib)");
            if (connection.getResponseCode() != 200) {
                onError.accept(new Exception("HTTP response code " + connection.getResponseCode()));
            } else {
                onSuccess.accept(connection.getInputStream());
            }
            connection.disconnect();
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    public static CompletableFuture<byte[]> download(URL... urls) {
        return CompletableFuture.supplyAsync(() -> {
            val result = new ByteArrayOutputStream();
            val caught = new AtomicReference<Exception>();
            val success = new AtomicBoolean(false);
            for (val url : urls) {
                connect(url, caught::set, (input) -> {
                    try {
                        transferAndClose(input, result);
                        success.set(true);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                });
                if (success.get()) return result.toByteArray();
            }
            throw new CompletionException(caught.get());
        });
    }

    public static CompletableFuture<Void> downloadFile(File file, URL... urls) {
        return CompletableFuture.runAsync(() -> {
            val caught = new AtomicReference<Exception>();
            val success = new AtomicBoolean(false);
            for (val url : urls) {
                connect(url, caught::set, (input) -> {
                    try {
                        @Cleanup val outputStream = Files.newOutputStream(file.toPath());
                        transferAndClose(input, outputStream);
                        success.set(true);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                });
                if (success.get()) return;
            }
            throw new CompletionException(caught.get());
        });
    }


    public static void transferAndClose(InputStream is, OutputStream target) throws IOException {
        var bytesRead = 0;

        byte[] smallBuffer = new byte[4096];
        while ((bytesRead = is.read(smallBuffer)) >= 0) {
            target.write(smallBuffer, 0, bytesRead);
        }
        target.close();
        is.close();
    }
}
