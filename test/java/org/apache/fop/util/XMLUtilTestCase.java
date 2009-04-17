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

package org.apache.fop.util;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests {@link XMLUtil}.
 */
public class XMLUtilTestCase extends TestCase {

    public void testLocaleToRFC3066() throws Exception {
        assertNull(XMLUtil.toRFC3066(null));
        assertEquals("en", XMLUtil.toRFC3066(new Locale("en")));
        assertEquals("en-US", XMLUtil.toRFC3066(new Locale("en", "US")));
        assertEquals("en-US", XMLUtil.toRFC3066(new Locale("EN", "us")));
    }

    public void testRFC3066ToLocale() throws Exception {
        assertNull(XMLUtil.convertRFC3066ToLocale(null));
        assertNull(XMLUtil.convertRFC3066ToLocale(""));
        assertEquals(new Locale("en"), XMLUtil.convertRFC3066ToLocale("en"));
        assertEquals(new Locale("en", "US"), XMLUtil.convertRFC3066ToLocale("en-US"));
        assertEquals(new Locale("en", "US"), XMLUtil.convertRFC3066ToLocale("EN-us"));
    }
}
