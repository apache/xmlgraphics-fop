/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.apps;

// Java
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

// FOP
import org.apache.fop.render.awt.AWTRenderer;


/**
 * The main application class for the FOP command line interface (CLI).
 */
public class Fop {

    /**
     * The main routine for the command line interface
     * @param args the command line parameters
     */
    public static void main(String[] args) {
        CommandLineOptions options = null;
        FOUserAgent foUserAgent = null;
        BufferedOutputStream bos = null;

        try {
            Driver driver = new Driver();
            options = new CommandLineOptions(args);

            foUserAgent = options.getFOUserAgent();
            driver.setUserAgent(foUserAgent);
            driver.setRenderer(options.getRenderer());

            try {
                if (options.getOutputFile() != null) {
                    bos = new BufferedOutputStream(new FileOutputStream(
                        options.getOutputFile()));
                    driver.setOutputStream(bos);
                }
                driver.render(foUserAgent.getInputHandler());
             } finally {
                 if (bos != null) {
                     bos.close();
                 }
             }

            // System.exit(0) called to close AWT/SVG-created threads, if any.
            // AWTRenderer closes with window shutdown, so exit() should not
            // be called here
            if (options.getOutputMode() != CommandLineOptions.RENDER_AWT) {
                System.exit(0);
            }
        } catch (FOPException e) {
            if (e.getMessage() == null) {
                System.err.println("Exception occured with a null error message");
            } else {
                System.err.println("" + e.getMessage());
            }
            if (options != null && options.getLogger().isDebugEnabled()) {
                e.printStackTrace();
            } else {
                System.err.println("Turn on debugging for more information");
            }
            System.exit(1);
        } catch (java.io.IOException e) {
            System.err.println("" + e.getMessage());
            if (options != null && options.getLogger().isDebugEnabled()) {
                e.printStackTrace();
            } else {
                System.err.println("Turn on debugging for more information");
            }
            System.exit(1);
        }
    }

    /**
     * Get the version of FOP
     * @return the version string
     */
    public static String getVersion() {
        return "1.0dev";
    }
}

