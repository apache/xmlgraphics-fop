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

package org.apache.fop.datatypes;

import junit.framework.TestCase;

/**
 * Tests for URISpecification.
 */
public class URISpecificationTestCase extends TestCase {

    public void testGetURL() throws Exception {
        String actual;
        
        actual = URISpecification.getURL("http://localhost/test");
        assertEquals("http://localhost/test", actual);

        actual = URISpecification.getURL("url(http://localhost/test)");
        assertEquals("http://localhost/test", actual);

        actual = URISpecification.getURL("url('http://localhost/test')");
        assertEquals("http://localhost/test", actual);

        actual = URISpecification.getURL("url(\"http://localhost/test\")");
        assertEquals("http://localhost/test", actual);
    }
    
    public void testEscapeURI() throws Exception {
        String actual;
        
        actual = URISpecification.escapeURI("http://localhost/test");
        assertEquals("http://localhost/test", actual);

        actual = URISpecification.escapeURI("http://localhost/test%20test");
        assertEquals("http://localhost/test%20test", actual);

        actual = URISpecification.escapeURI("http://localhost/test test");
        assertEquals("http://localhost/test%20test", actual);

        actual = URISpecification.escapeURI("http://localhost/test test.pdf#page=6");
        assertEquals("http://localhost/test%20test.pdf#page=6", actual);
    }
    
}
