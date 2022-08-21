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

package com.falsepattern.lib.internal.tooling;

import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.FinishableWrapperOutputStream;
import org.tukaani.xz.LZMA2Options;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compact {
    public static void main(String[] args) throws IOException {
        byte[] file1 = Files.readAllBytes(Paths.get("mappings", "classes.csv"));
        byte[] file2 = Files.readAllBytes(Paths.get("mappings", "fields.csv"));
        byte[] file3 = Files.readAllBytes(Paths.get("mappings", "methods.csv"));
        FinishableOutputStream lzOut = new LZMA2Options(6).getOutputStream(new FinishableWrapperOutputStream(
                Files.newOutputStream(Paths.get("src", "main", "resources", "mappings.lzma2"))));
        DataOutputStream dOut = new DataOutputStream(lzOut);
        dOut.writeInt(file1.length);
        dOut.write(file1);
        dOut.writeInt(file2.length);
        dOut.write(file2);
        dOut.writeInt(file3.length);
        dOut.write(file3);
        lzOut.finish();
        lzOut.close();
    }
}
