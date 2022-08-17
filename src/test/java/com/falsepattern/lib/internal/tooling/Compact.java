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
        FinishableOutputStream lzOut = new LZMA2Options(6).getOutputStream(new FinishableWrapperOutputStream(Files.newOutputStream(Paths.get("src", "main", "resources", "mappings.lzma2"))));
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
