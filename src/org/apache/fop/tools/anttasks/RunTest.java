/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.anttasks;

// Ant
import org.apache.tools.ant.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Testing ant task.
 * This task is used to test FOP as a build target.
 * This uses the TestConverter (with weak code dependancy) to run the tests
 * and check the results.
 */
public class RunTest extends Task {
    String basedir;
    String testsuite = "";
    String referenceJar = "";
    String refVersion = "";

    public RunTest() {}

    public void setTestSuite(String str) {
        testsuite = str;
    }

    public void setBasedir(String str) {
        basedir = str;
    }

    public void setReference(String str) {
        referenceJar = str;
    }

    public void setRefVersion(String str) {
        refVersion = str;
    }

    /**
     * Execute this ant task.
     * This creates the reference output, if required, then tests
     * the current build.
     */
    public void execute() throws BuildException {
        runReference();
        testNewBuild();
    }

    /**
     * Test the current build.
     * This uses the current jar file (in build/fop.jar) to run the
     * tests with.
     * The output is then compared with the reference output.
     */
    protected void testNewBuild() {
        try {
            ClassLoader loader = new URLClassLoader(new URL[] {
                new URL("file:build/fop.jar")
            });
            HashMap diff = runConverter(loader, "areatree",
                                          "reference/output/");
            if (diff != null &&!diff.isEmpty()) {
                System.out.println("====================================");
                System.out.println("The following files differ:");
                boolean broke = false;
                for (Iterator keys = diff.keySet().iterator();
                        keys.hasNext(); ) {
                    Object fname = keys.next();
                    Boolean pass = (Boolean)diff.get(fname);
                    System.out.println("file: " + fname
                                       + " - reference success: " + pass);
                    if (pass.booleanValue()) {
                        broke = true;
                    }
                }
                if (broke) {
                    throw new BuildException("Working tests have been changed.");
                }
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
    }

    /**
     * Run the tests for the reference jar file.
     * This checks that the reference output has not already been
     * run and then checks the version of the reference jar against
     * the version required.
     * The reference output is then created.
     */
    protected void runReference() throws BuildException {
        // check not already done
        File f = new File(basedir + "/reference/output/");
        // if(f.exists()) {
        // need to check that files have actually been created.
        // return;
        // } else {
        try {
            ClassLoader loader = new URLClassLoader(new URL[] {
                new URL("file:" + referenceJar)
            });
            boolean failed = false;

            try {
                Class cla = Class.forName("org.apache.fop.apps.Options",
                                          true, loader);
                Object opts = cla.newInstance();
                cla = Class.forName("org.apache.fop.apps.Version", true,
                                    loader);
                Method get = cla.getMethod("getVersion", new Class[]{});
                if (!get.invoke(null, new Object[]{}).equals(refVersion)) {
                    throw new BuildException("Reference jar is not correct version it must be: "
                                             + refVersion);
                }
            } catch (IllegalAccessException iae) {
                failed = true;
            } catch (IllegalArgumentException are) {
                failed = true;
            } catch (InvocationTargetException are) {
                failed = true;
            } catch (ClassNotFoundException are) {
                failed = true;
            } catch (InstantiationException are) {
                failed = true;
            } catch (NoSuchMethodException are) {
                failed = true;
            }
            if (failed) {
                throw new BuildException("Reference jar could not be found in: "
                                         + basedir + "/reference/");
            }
            f.mkdirs();
            runConverter(loader, "reference/output/", null);
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
        // }
    }

    /**
     * Run the Converter.
     * Runs the test converter using the specified class loader.
     * This loads the TestConverter using the class loader and
     * then runs the test suite for the current test suite
     * file in the base directory.
     * @param loader the class loader to use to run the tests with
     */
    protected HashMap runConverter(ClassLoader loader, String dest,
                                     String compDir) {
        String converter = "org.apache.fop.tools.TestConverter";

        HashMap diff = null;
        try {
            Class cla = Class.forName(converter, true, loader);
            Object tc = cla.newInstance();
            Method meth;

            meth = cla.getMethod("setBaseDir", new Class[] {
                String.class
            });
            meth.invoke(tc, new Object[] {
                basedir
            });

            meth = cla.getMethod("runTests", new Class[] {
                String.class, String.class, String.class
            });
            diff = (HashMap)meth.invoke(tc, new Object[] {
                testsuite, dest, compDir
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diff;
    }

}
