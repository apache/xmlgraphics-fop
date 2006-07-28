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

package org.apache.fop.cli;

import java.io.File;
import java.io.FileFilter;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;

/**
 * Main command-line class for Apache FOP.
 */
public class Main {

    /**
     * @return the list of URLs to all libraries.
     * @throws MalformedURLException In case there is a problem converting java.io.File
     * instances to URLs.
     */
    public static URL[] getJARList() throws MalformedURLException {
        File baseDir = new File(".").getAbsoluteFile().getParentFile();
        File buildDir;
        if ("build".equals(baseDir.getName())) {
            buildDir = baseDir;
            baseDir = baseDir.getParentFile();
        } else {
            buildDir = new File(baseDir, "build");
        }
        File fopJar = new File(buildDir, "fop.jar");
        if (!fopJar.exists()) {
            fopJar = new File(baseDir, "fop.jar");
        }
        if (!fopJar.exists()) {
            throw new RuntimeException("fop.jar not found in directory: " 
                    + baseDir.getAbsolutePath() + " (or below)");
        }
        List jars = new java.util.ArrayList();
        jars.add(fopJar.toURL());
        File[] files;
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        };
        File libDir = new File(baseDir, "lib");
        if (!libDir.exists()) {
            libDir = baseDir;
        }
        files = libDir.listFiles(filter);
        if (files != null) {
            for (int i = 0, size = files.length; i < size; i++) {
                jars.add(files[i].toURL());
            }
        }
        String optionalLib = System.getProperty("fop.optional.lib");
        if (optionalLib != null) {
            files = new File(optionalLib).listFiles(filter);
            if (files != null) {
                for (int i = 0, size = files.length; i < size; i++) {
                    jars.add(files[i].toURL());
                }
            }
        }
        URL[] urls = (URL[])jars.toArray(new URL[jars.size()]);
        /*
        for (int i = 0, c = urls.length; i < c; i++) {
            System.out.println(urls[i]);
        }*/
        return urls;
    }
    
    /**
     * @return true if FOP's dependecies are available in the current ClassLoader setup.
     */
    public static boolean checkDependencies() {
        try {
            //System.out.println(Thread.currentThread().getContextClassLoader());
            Class clazz = Class.forName("org.apache.commons.io.IOUtils");
            if (clazz != null) {
                clazz = Class.forName("org.apache.avalon.framework.configuration.Configuration");
            }
            return (clazz != null);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Dynamically builds a ClassLoader and executes FOP.
     * @param args command-line arguments
     */
    public static void startFOPWithDynamicClasspath(String[] args) {
        try {
            URL[] urls = getJARList();
            //System.out.println("CCL: " 
            //    + Thread.currentThread().getContextClassLoader().toString());
            ClassLoader loader = new java.net.URLClassLoader(urls, null);
            Thread.currentThread().setContextClassLoader(loader);
            Class clazz = Class.forName("org.apache.fop.cli.Main", true, loader);
            //System.out.println("CL: " + clazz.getClassLoader().toString());
            Method mainMethod = clazz.getMethod("startFOP", new Class[] {String[].class});
            mainMethod.invoke(null, new Object[] {args});
        } catch (Exception e) {
            System.err.println("Unable to start FOP:");
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    /**
     * Executes FOP with the given ClassLoader setup.
     * @param args command-line arguments
     */
    public static void startFOP(String[] args) {
        //System.out.println("static CCL: " 
        //    + Thread.currentThread().getContextClassLoader().toString());
        //System.out.println("static CL: " + Fop.class.getClassLoader().toString());
        CommandLineOptions options = null;
        FOUserAgent foUserAgent = null;
        OutputStream out = null;

        try {
            options = new CommandLineOptions();
            options.parse(args);
            
            foUserAgent = options.getFOUserAgent();
            String outputFormat = options.getOutputFormat();

            try {
                if (options.getOutputFile() != null) {
                    out = new java.io.BufferedOutputStream(
                            new java.io.FileOutputStream(options.getOutputFile()));
                    foUserAgent.setOutputFile(options.getOutputFile());
                }
                if (!MimeConstants.MIME_XSL_FO.equals(outputFormat)) {
                    options.getInputHandler().renderTo(foUserAgent, outputFormat, out);
                } else {
                    options.getInputHandler().transformTo(out);
                }
             } finally {
                 IOUtils.closeQuietly(out);
             }

            // System.exit(0) called to close AWT/SVG-created threads, if any.
            // AWTRenderer closes with window shutdown, so exit() should not
            // be called here
            if (!MimeConstants.MIME_FOP_AWT_PREVIEW.equals(outputFormat)) {
                System.exit(0);
            }
        } catch (Exception e) {
            if (options != null) {
                options.getLogger().error("Exception", e);
            }
            if (options.getOutputFile() != null) {
                options.getOutputFile().delete();
            }
            System.exit(1);
        }
    }
    
    /**
     * The main routine for the command line interface
     * @param args the command line parameters
     */
    public static void main(String[] args) {
        if (checkDependencies()) {
            startFOP(args);
        } else {
            startFOPWithDynamicClasspath(args);
        }
    }

}
