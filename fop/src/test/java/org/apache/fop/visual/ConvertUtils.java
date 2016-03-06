/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.visual;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;

/**
 * Utilities for converting files with external converters.
 */
public final class ConvertUtils {

    private ConvertUtils() {
    }

    /**
     * Calls an external converter application (GhostScript, for example).
     * @param cmd the full command
     * @param envp array of strings, each element of which has environment variable settings
     * in format name=value.
     * @param workDir the working directory of the subprocess, or null if the subprocess should
     * inherit the working directory of the current process.
     * @param log the logger to log output by the external application to
     * @throws IOException in case the external call fails
     */
    public static void convert(String cmd, String[] envp, File workDir, final Log log)
                throws IOException {
        log.debug(cmd);

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd, envp, null);

            //Redirect stderr output
            RedirectorLineHandler errorHandler = new AbstractRedirectorLineHandler() {
                public void handleLine(String line) {
                    log.error("ERR> " + line);
                }
            };
            StreamRedirector errorRedirector
                = new StreamRedirector(process.getErrorStream(), errorHandler);

            //Redirect stdout output
            RedirectorLineHandler outputHandler = new AbstractRedirectorLineHandler() {
                public void handleLine(String line) {
                    log.debug("OUT> " + line);
                }
            };
            StreamRedirector outputRedirector
                = new StreamRedirector(process.getInputStream(), outputHandler);
            new Thread(errorRedirector).start();
            new Thread(outputRedirector).start();

            process.waitFor();
        } catch (java.lang.InterruptedException ie) {
            throw new IOException("The call to the external converter failed: " + ie.getMessage());
        } catch (java.io.IOException ioe) {
            throw new IOException("The call to the external converter failed: " + ioe.getMessage());
        }

        int exitValue = process.exitValue();
        if (exitValue != 0) {
            throw new IOException("The call to the external converter failed. Result: "
                    + exitValue);
        }

    }


}
