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

package org.apache.fop.tools.anttasks;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Testing ant task.
 * This task is used to test FOP as a build target.
 * This uses the TestConverter (with weak code dependency)
 * to run the tests and check the results.
 */
public class RunTest extends Task {

    private String basedir;
    private String testsuite = "";
    private String referenceJar = "";
    private String refVersion = "";

    /**
     * Sets the test suite name.
     * @param str name of the test suite
     */
    public void setTestSuite(String str) {
        testsuite = str;
    }

    /**
     * Sets the base directory.
     * @param str base directory
     */
    public void setBasedir(String str) {
        basedir = str;
    }

    /**
     * Sets the reference directory.
     * @param str reference directory
     */
    public void setReference(String str) {
        referenceJar = str;
    }

    /**
     * Sets the reference version.
     * @param str reference version
     */
    public void setRefVersion(String str) {
        refVersion = str;
    }

    /**
     * This creates the reference output, if required, then tests
     * the current build.
     * {@inheritDoc}
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
            ClassLoader loader = new URLClassLoader(
                                    createUrls("build/fop.jar"));
            Map diff = runConverter(loader, "areatree",
                                          "reference/output/");
            if (diff != null && !diff.isEmpty()) {
                System.out.println("====================================");
                System.out.println("The following files differ:");
                boolean broke = false;
                for (Iterator keys = diff.keySet().iterator();
                        keys.hasNext();) {
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
     * @throws BuildException if an error occurs
     */
    protected void runReference() throws BuildException {
        // check not already done
        File f = new File(basedir + "/reference/output/");
        // if(f.exists()) {
        // need to check that files have actually been created.
        // return;
        // } else {
        try {
            ClassLoader loader = new URLClassLoader(createUrls(referenceJar));
            boolean failed = false;

            try {
                Class cla = Class.forName("org.apache.fop.apps.Fop", true,
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
     * (Note class loader option provided to allow for different
     * fop.jar and other libraries to be activated.)
     * @param loader the class loader to use to run the tests with
     * @param dest destination directory
     * @param compDir comparison directory
     * @return A Map with differences
     */
    protected Map runConverter(ClassLoader loader, String dest,
                                     String compDir) {
        String converter = "org.apache.fop.tools.TestConverter";

        Map diff = null;
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
            diff = (Map)meth.invoke(tc, new Object[] {
                testsuite, dest, compDir
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diff;
    }

    /**
     * Return a list of URL's with the specified URL first and followed
     * by all the jar files from lib/.
     * @return a list of urls to the runtime jar files.
     */
    private URL[] createUrls(String mainJar) throws MalformedURLException {
        ArrayList urls = new ArrayList();
        urls.add(new File(mainJar).toURI().toURL());
        File[] libFiles = new File("lib").listFiles();
        for (int i = 0; i < libFiles.length; i++) {
            if (libFiles[i].getPath().endsWith(".jar")) {
                urls.add(libFiles[i].toURI().toURL());
            }
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }
}
