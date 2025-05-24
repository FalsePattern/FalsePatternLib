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

package com.falsepattern.lib.internal.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.falsepattern.lib.internal.FPLog;
import com.falsepattern.lib.internal.Tags;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import net.minecraft.launchwrapper.Launch;

// This class is a port of NotEnoughVerbosity, licensed under the Unlicense
// It has been merged into FalsePatternLib so that it's available inside more modpacks
// CHANGES:
// This variant will inspect the blackboard for an entry called "notenoughverbosity". If it's present, it will no-op.
// This will allow other people's lib mods to pull in this class without each one of them trying to reapply.
public class NotEnoughVerbosity {
    
    public static final Logger LOGGER = FPLog.LOG;

    public static void apply() {
        try {
            if (needsToRun()) {
                reconfigureLog4j();
            }
        } catch (Throwable t) {
            LOGGER.error("NotEnoughVerbosity failed to apply!", t);
        }
    }
    
    private static boolean needsToRun() {
        try {
            Class.forName("io.github.legacymoddingmc.notenoughverbosity.NotEnoughVerbosity");
            // OG mod already present, do nothing
            return false;
        } catch (Throwable ignored) {}

        if (Launch.blackboard.containsKey("notenoughverbosity")) {
            // Other mod already present
            return false;
        }
        
        boolean foundBadRoot = false;
        for (LoggerContext context : getLoggerContexts()) {
            LoggerConfig rootConfig = context.getConfiguration().getLoggers().get("");
            if(rootConfig != null) {
                if(rootConfig.getLevel().intLevel() < Level.ALL.intLevel()) {
                    LOGGER.warn("Found root logger with verbosity " + rootConfig.getLevel() + ", will try to switch to Forge config.");
                    foundBadRoot = true;
                    break;
                } else {
                    LOGGER.debug("Found root logger with verbosity " + rootConfig.getLevel() + ", this is fine.");
                }
            } else {
                throw new RuntimeException("Couldn't find root logger.");
            }
        }
        if(!foundBadRoot) {
            LOGGER.debug("Root config seems fine, log4j will not be reconfigured.");
        }
        return foundBadRoot;
    }
    
    public static void reconfigureLog4j() {
        URI log4jUri = findForgeLog4j();
        if (log4jUri != null) {
            LOGGER.info("Reconfiguring logger to use config at " + log4jUri);
            LOGGER.info("New messages will go to fml-x-latest.log");
            String cookie = UUID.randomUUID().toString();
            LOGGER.info("Magic cookie: " + cookie);
            setLog4jConfig(log4jUri);
            int count = getLoggerContexts().size();
            LOGGER.info("Reconfigured logger (" + count + " context" + (count != 1 ? "s" : "") + ") to use config at " + log4jUri);
            LOGGER.info("Earlier messages may be located in latest.log");
            
            List<String> latestLogLines = readLatestLog(cookie);
            if(latestLogLines != null) {
                LOGGER.info("Found earlier messages:\n-----Begin latest.log-----\n" + String.join("\n", latestLogLines) + "\n-----End latest.log-----");
            }
            Launch.blackboard.put("notenoughverbosity", Tags.MODID);
        } else {
            LOGGER.warn("Could not find Forge's log4j2.xml on classpath, doing nothing");
        }
    }
    
    private static List<String> readLatestLog(String cookie) {
        File latestLog = new File(Launch.minecraftHome, "logs/latest.log");
        if(!latestLog.exists()) {
            LOGGER.debug("Couldn't find latest.log at " + latestLog.getAbsolutePath());
            return null;
        }
        
        long lastModified = latestLog.lastModified();
        if(lastModified < System.currentTimeMillis() - 1000 * 60) {
            LOGGER.debug("latest.log at " + latestLog.getAbsolutePath() + " is too old (timestamp: " + lastModified + ")");
            return null;
        }
        
        try(InputStream is = new FileInputStream(latestLog)) {
            List<String> lines = readLines(is);
            if(lines.stream().anyMatch(l -> l.contains(cookie))) {
                return lines;
            } else {
                LOGGER.debug("Failed to find magic cookie in latest.log at " + latestLog.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read latest.log at " + latestLog.getAbsolutePath(), e);
        }
        
        return null;
    }

    private static void setLog4jConfig(URI uri) {
        for (LoggerContext context : getLoggerContexts()) {
            context.setConfigLocation(uri);
            context.reconfigure();
        }
    }

    private static Collection<LoggerContext> getLoggerContexts() {
        LoggerContextFactory loggerContextFactory = LogManager.getFactory();
        if (loggerContextFactory instanceof Log4jContextFactory) {
            return ((Log4jContextFactory) loggerContextFactory).getSelector().getLoggerContexts();
        } else {
            throw new IllegalStateException("Logger context factory is not a Log4jContextFactory");
        }
    }
    
    private static URI findForgeLog4j() {
        try {
            List<URL> candidates = Collections.list(NotEnoughVerbosity.class.getClassLoader().getResources("log4j2.xml"));
            
            LOGGER.info("Logger configs on classpath: " + candidates);
            
            for(URL url : candidates) {
                try {
                    if(readLines(url.openStream()).stream().anyMatch(l -> l.contains("Root level=\"all\""))) {
                        return url.toURI();
                    }
                } catch(Exception e) {
                    LOGGER.error("Failed to read inspect logger config at URL " + url, e);
                }
            }
        } catch(Exception e) {
            LOGGER.error("Exception enumerating logger configs", e);
        }
        return null;
    }

    private static List<String> readLines(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.toList());
    }
}
