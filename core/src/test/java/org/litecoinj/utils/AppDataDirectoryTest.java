/*
 * Copyright 2019 Michael Sean Gilligan
 * Copyright 2019 Tim Strasser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litecoinj.utils;

import org.litecoinj.base.internal.PlatformUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Basic test of AppDataDirectory
 */
public class AppDataDirectoryTest {
    private static final String HOMEDIR = System.getProperty("user.home");
    private static final String WINAPPDATA = System.getenv("APPDATA");

    @Test
    public void worksOnCurrentPlatform() {
        final String appName = "bitcoinj";
        String path = AppDataDirectory.get(appName).toString();
        if (PlatformUtils.isWindows()) {
            assertEquals("Path wrong on Windows", winPath(appName), path);
        } else if (PlatformUtils.isMac()) {
            assertEquals("Path wrong on Mac",  macPath(appName), path);
        } else if (PlatformUtils.isLinux()) {
            assertEquals("Path wrong on Linux",  unixPath(appName), path);
        } else {
            assertEquals("Path wrong on unknown/default",  unixPath(appName), path);
        }
    }

    @Test
    public void worksOnCurrentPlatformForBitcoinCore() {
        final String appName = "Bitcoin";
        String path = AppDataDirectory.get(appName).toString();
        if (PlatformUtils.isWindows()) {
            assertEquals("Path wrong on Windows", winPath(appName), path);
        } else if (PlatformUtils.isMac()) {
            assertEquals("Path wrong on Mac",  macPath(appName), path);
        } else if (PlatformUtils.isLinux()) {
            assertEquals("Path wrong on Linux",  unixPath(appName), path);
        } else {
            assertEquals("Path wrong on unknown/default",  unixPath(appName), path);
        }
    }

    @Test(expected = RuntimeException.class)
    public void throwsIOExceptionIfPathNotFound() {
        // Force exceptions with illegal characters
        if (PlatformUtils.isWindows()) {
            AppDataDirectory.get(":");  // Illegal character for Windows
        }
        if (PlatformUtils.isMac()) {
            // NUL character
            AppDataDirectory.get("\0"); // Illegal character for Mac
        }
        if (PlatformUtils.isLinux()) {
            // NUL character
            AppDataDirectory.get("\0"); // Illegal character for Linux
        }
    }


    private static String winPath(String appName) {
        return WINAPPDATA + "\\" + appName.toLowerCase();
    }

    private static String macPath(String appName) {
        return HOMEDIR + "/Library/Application Support/" + appName;
    }

    private static String unixPath(String appName) {
        return HOMEDIR + "/." + appName.toLowerCase();
    }
}
