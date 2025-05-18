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

package com.falsepattern.lib.internal.impl.dependencies;

import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.config.EarlyConfig;
import lombok.val;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

/**
 * Add Let's Encrypt root certificates to the default SSLContext.
 * CurseForge launcher uses the vanilla JDK for 1.7.10, which is version 8u51.
 * This version does not include these certificates, support for ISRG Root X1 was added in 8u141.
 * Based on <a href="https://github.com/Cloudhunter/LetsEncryptCraft/blob/2471391f7d081a8b7faed9e22051cab6352966fe/src/main/java/uk/co/cloudhunter/letsencryptcraft/LetsEncryptAdder.java">LetsEncryptCraft</a> by Cloudhunter (MIT)
 */
public class LetsEncryptHelper {
    private static volatile boolean patched = false;
    private LetsEncryptHelper() {}
    @SuppressWarnings("java:S6437")
    public static void replaceSSLContext() {
        if (!EarlyConfig.getInstance().enableLetsEncryptRoot()) {
            FPLog.LOG.info("[LetsEncryptHelper] Disabled by config");
            return;
        }
        if (patched) {
            FPLog.LOG.info("[LetsEncryptHelper] Already patched");
            return;
        }
        FPLog.LOG.info("[LetsEncryptHelper] Starting patcher");
        patched = true;

        try (val x1 = LetsEncryptHelper.class.getResourceAsStream("/letsencrypt/isrgrootx1.pem");
             val x2 = LetsEncryptHelper.class.getResourceAsStream("/letsencrypt/isrg-root-x2.pem")) {
            val merged = KeyStore.getInstance(KeyStore.getDefaultType());
            val cacerts = Paths.get(System.getProperty("java.home"),"lib", "security", "cacerts");
            merged.load(Files.newInputStream(cacerts), "changeit".toCharArray());

            val cf = CertificateFactory.getInstance("X.509");

            val cx1 = cf.generateCertificate(x1);
            merged.setCertificateEntry("archaicfix-isrgx1", cx1);

            val cx2 = cf.generateCertificate(x2);
            merged.setCertificateEntry("archaicfix-isrgx2", cx2);

            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(merged);
            val sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
            FPLog.LOG.info("[LetsEncryptHelper] Added certificates to trust store.");
        } catch (IOException e) {
            FPLog.LOG.error("[LetsEncryptHelper] Failed to load certificates from classpath.", e);
        } catch (GeneralSecurityException e) {
            FPLog.LOG.error("[LetsEncryptHelper] Failed to load default keystore.", e);
        } catch (Throwable t) {
            FPLog.LOG.error("[LetsEncryptHelper] Unknown error", t);
            throw t;
        }
    }
}