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
package com.falsepattern.lib.internal.tooling;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class SRGGen {
    private static final Map<String, String> class_notch_srg = new HashMap<>();
    private static final Map<String, String> class_notch_mcp = new HashMap<>();
    private static final Map<String, String> class_srg_mcp = new HashMap<>();
    private static final Map<String, String> class_mcp_srg = new HashMap<>();
    private static final Map<String, String> class_mcp_notch = new HashMap<>();
    private static final Map<String, String> field_notch_srg = new HashMap<>();
    private static final Map<String, String> field_notch_mcp = new HashMap<>();
    private static final Map<String, String> field_srg_mcp = new HashMap<>();
    private static final Map<String, String> field_mcp_srg = new HashMap<>();
    private static final Map<String, String> field_mcp_notch = new HashMap<>();
    private static final Map<StringPair, StringPair> method_notch_srg = new HashMap<>();
    private static final Map<StringPair, StringPair> method_notch_mcp = new HashMap<>();
    private static final Map<StringPair, StringPair> method_srg_mcp = new HashMap<>();
    private static final Map<StringPair, StringPair> method_mcp_srg = new HashMap<>();
    private static final Map<StringPair, StringPair> method_mcp_notch = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Path dir = Paths.get(args[0]);
        parseLines(Files.readAllLines(dir.resolve("notch-srg.srg")), class_notch_srg, field_notch_srg,
                   method_notch_srg);
        parseLines(Files.readAllLines(dir.resolve("notch-mcp.srg")), class_notch_mcp, field_notch_mcp,
                   method_notch_mcp);
        parseLines(Files.readAllLines(dir.resolve("srg-mcp.srg")), class_srg_mcp, field_srg_mcp, method_srg_mcp);
        parseLines(Files.readAllLines(dir.resolve("mcp-srg.srg")), class_mcp_srg, field_mcp_srg, method_mcp_srg);
        parseLines(Files.readAllLines(dir.resolve("mcp-notch.srg")), class_mcp_notch, field_mcp_notch,
                   method_mcp_notch);
        try (OutputStream output = Files.newOutputStream(dir.resolve("classes.csv"))) {
            output.write("notch,srg,mcp\n".getBytes(StandardCharsets.UTF_8));
            crossValidate(class_notch_srg, class_notch_mcp, class_srg_mcp, class_mcp_srg, class_mcp_notch, output);
        }
        try (OutputStream output = Files.newOutputStream(dir.resolve("fields.csv"))) {
            output.write("notch,srg,mcp\n".getBytes(StandardCharsets.UTF_8));
            crossValidate(field_notch_srg, field_notch_mcp, field_srg_mcp, field_mcp_srg, field_mcp_notch, output);
        }

        try (OutputStream output = Files.newOutputStream(dir.resolve("methods.csv"))) {
            output.write("notch,notchdesc,srg,srgdesc,mcp,mcpdesc\n".getBytes(StandardCharsets.UTF_8));
            crossValidate(method_notch_srg, method_notch_mcp, method_srg_mcp, method_mcp_srg, method_mcp_notch, output);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean crossValidate(Map<?, ?> notch_srg, Map<?, ?> notch_mcp, Map<?, ?> srg_mcp, Map<?, ?> mcp_srg, Map<?, ?> mcp_notch, OutputStream output)
            throws IOException {
        for (Map.Entry<?, ?> notch_srg_entry : notch_srg.entrySet()) {
            Object notch = notch_srg_entry.getKey();
            Object srg = notch_srg_entry.getValue();
            Object mcp = notch_mcp.get(notch);
            if (!srg_mcp.get(srg).equals(mcp) || !mcp_srg.get(mcp).equals(srg) || !mcp_notch.get(mcp).equals(notch)) {
                throw new RuntimeException(notch + " - " + srg + " - " + mcp);
            }
            output.write((notch + "," + srg + "," + mcp + "\n").getBytes(StandardCharsets.UTF_8));
        }
        return true;
    }

    private static void parseLines(List<String> lines, Map<String, String> classes, Map<String, String> fields, Map<StringPair, StringPair> methods) {
        for (String line : lines) {
            String[] parts = line.split(" ");
            switch (parts[0]) {
                case "CL:":
                    classes.put(parts[1], parts[2]);
                    break;
                case "FD:":
                    fields.put(parts[1], parts[2]);
                    break;
                case "MD:":
                    methods.put(new StringPair(parts[1], parts[2]), new StringPair(parts[3], parts[4]));
                    break;
            }
        }
    }

    private static class StringPair {
        public final String name;
        public final String desc;

        private StringPair(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StringPair that = (StringPair) o;
            return name.equals(that.name) && desc.equals(that.desc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, desc);
        }

        @Override
        public String toString() {
            return name + "," + desc;
        }
    }
}