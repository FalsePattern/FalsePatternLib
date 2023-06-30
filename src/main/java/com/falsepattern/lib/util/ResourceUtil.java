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
package com.falsepattern.lib.util;

import com.falsepattern.lib.StableAPI;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A utility class for reading resources in many ways.
 */
@UtilityClass
@StableAPI(since = "0.6.0")
public final class ResourceUtil {

    /**
     * Reads a resource as a string, using the {@link StandardCharsets#UTF_8} charset, from a specific jar file. See
     * {@link #getResourceFromJar(String, Class)}.
     *
     * @param resourcePath   The resource to read
     * @param referenceClass The class, from whose jar to get the resource from
     *
     * @return The string from the resource
     *
     * @throws IOException From {@link #readBytes(InputStream)}
     */
    @StableAPI.Expose
    public static String getResourceStringFromJar(String resourcePath, Class<?> referenceClass) throws IOException {
        return getResourceStringFromJar(resourcePath, referenceClass, StandardCharsets.UTF_8);
    }


    /**
     * Reads a resource as a string from a specific jar file. See {@link #getResourceFromJar(String, Class)}.
     *
     * @param resourcePath   The resource to read
     * @param referenceClass The class, from whose jar to get the resource from
     * @param charset        The charset to decode the raw bytes with
     *
     * @return The string from the resource
     *
     * @throws IOException From {@link #readBytes(InputStream)}
     */
    @StableAPI.Expose
    public static String getResourceStringFromJar(String resourcePath, Class<?> referenceClass, Charset charset)
            throws IOException {
        return new String(getResourceBytesFromJar(resourcePath, referenceClass), charset);
    }

    /**
     * Reads the raw bytes of a resource in a specific jar file. See {@link #getResourceFromJar(String, Class)}.
     *
     * @param resourcePath   The resource to read
     * @param referenceClass The class, from whose jar to get the resource from
     *
     * @return The bytes of the resource
     *
     * @throws IOException From {@link #readBytes(InputStream)}
     */
    @StableAPI.Expose
    public static byte[] getResourceBytesFromJar(String resourcePath, Class<?> referenceClass) throws IOException {
        return readBytesSafe(getResourceFromJar(resourcePath, referenceClass), resourcePath);
    }

    private static byte[] readBytesSafe(InputStream stream, String resourcePath) throws IOException {
        if (stream == null) {
            throw new FileNotFoundException("Could not find resource at " + resourcePath);
        }
        return readBytes(stream);
    }

    /**
     * Retrieves a resource from the jar file of a specific class. Falls back to
     * {@link Class#getResourceAsStream(String)} if the resource is not found in the jar, or the class is not from a jar
     * file.
     *
     * @param resourcePath   The resource to retrieve
     * @param referenceClass The class, from whose jar to get the resource from
     *
     * @return The resource, or null if it was not found.
     */
    @StableAPI.Expose
    public static InputStream getResourceFromJar(String resourcePath, Class<?> referenceClass) {
        URL classFile = referenceClass.getResource('/' + referenceClass.getName().replace('.', '/') + ".class");
        lookup:
        {
            if (classFile == null) {
                break lookup;
            }
            String file = classFile.getFile();
            int id = file.indexOf("!");
            if (!classFile.getProtocol().equals("jar") || id < 0) {
                break lookup;
            }
            //Loading from a jar
            try {
                URL resource = new URL("jar:" + file.substring(0, id) + "!" + resourcePath);
                return resource.openStream();
            } catch (IOException e) {
                System.err.println("Failed to load resource " + resourcePath + " from jar " + file.substring(0, id));
                e.printStackTrace();
            }
        }
        //Fallback logic
        System.out.println("Using fallback resource loading logic for " + resourcePath + " with reference to " +
                           referenceClass.getName());
        return referenceClass.getResourceAsStream(resourcePath);
    }

    /**
     * Fully reads a stream into a byte array.
     *
     * @param stream The stream to read
     *
     * @return The data from the stream
     *
     * @throws IOException From {@link InputStream#read(byte[])}
     */
    @StableAPI.Expose
    public static byte[] readBytes(InputStream stream) throws IOException {
        val out = new ByteArrayOutputStream();
        val buf = new byte[4096];
        int read;
        while ((read = stream.read(buf)) >= 0) {
            out.write(buf, 0, read);
        }
        return out.toByteArray();
    }

    /**
     * Reads a resource as a string, using the {@link StandardCharsets#UTF_8} charset.
     *
     * @param resourcePath The resource to read
     *
     * @return The string from the resource
     *
     * @throws IOException From {@link #readBytes(InputStream)}
     */
    @StableAPI.Expose
    public static String getResourceString(String resourcePath) throws IOException {
        return getResourceString(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * Reads a resource as a string.
     *
     * @param resourcePath The resource to read
     * @param charset      The charset to decode the raw bytes with
     *
     * @return The string from the resource
     *
     * @throws IOException From {@link #readBytes(InputStream)}
     */
    @StableAPI.Expose
    public static String getResourceString(String resourcePath, Charset charset) throws IOException {
        return new String(getResourceBytes(resourcePath), charset);
    }

    /**
     * Reads the raw bytes of a resource.
     *
     * @param resourcePath The resource to read
     *
     * @return The bytes of the resource
     *
     * @throws IOException From {@link #readBytes(InputStream)}
     */
    @StableAPI.Expose
    public static byte[] getResourceBytes(String resourcePath) throws IOException {
        return readBytesSafe(ResourceUtil.class.getResourceAsStream(resourcePath), resourcePath);
    }
}
