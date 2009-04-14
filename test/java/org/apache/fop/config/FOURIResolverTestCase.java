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

package org.apache.fop.config;

import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.apache.fop.apps.FOURIResolver;

/**
 * This tests some aspects of the {@link FOURIResolver} class.
 */
public class FOURIResolverTestCase extends TestCase {

    /**
     * Checks the {@link FOURIResolver#checkBaseURL(String)} method.
     * @throws Exception if an error occurs
     */
    public void testCheckBaseURI() throws Exception {
        FOURIResolver resolver = new FOURIResolver(true);
        System.out.println(resolver.checkBaseURL("./test/config"));
        System.out.println(resolver.checkBaseURL("file:test/config"));
        System.out.println(resolver.checkBaseURL("fantasy:myconfig"));
        try {
            resolver.checkBaseURL("./doesnotexist");
            fail("Expected an exception for a inexistent base directory");
        } catch (MalformedURLException mfue) {
            //expected
        }
        try {
            resolver.checkBaseURL("file:doesnotexist");
            fail("Expected an exception for a inexistent base URI");
        } catch (MalformedURLException mfue) {
            //expected
        }
    }

}
