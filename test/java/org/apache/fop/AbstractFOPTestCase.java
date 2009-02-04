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

package org.apache.fop;

import java.io.File;

import junit.framework.TestCase;

/**
 * Abstract base test class for FOP's tests.
 */
public abstract class AbstractFOPTestCase extends TestCase {

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public AbstractFOPTestCase(String name) {
        super(name);
    }

    /**
     * Returns the base directory to use for the tests.
     * @return the base directory
     */
    protected File getBaseDir() {
        String basedir = System.getProperty("basedir");
        if (basedir != null) {
            basedir.replaceAll("%20", " "); //Under Java 1.4 the path may contain spaces as %20
            return new File(basedir);
        } else {
            return new File(".");
        }
    }

}
