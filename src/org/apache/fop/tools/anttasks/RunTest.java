/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.tools.anttasks;

// Ant
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;

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
                for (Iterator keys = diff.keySet().iterator(); keys.hasNext(); ) {
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
