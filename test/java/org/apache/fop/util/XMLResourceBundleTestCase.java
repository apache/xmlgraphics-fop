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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.junit.Test;

/**
 * Tests for XMLResourceBundle.
 */
public class XMLResourceBundleTestCase {

    @Test
    public void testWithValidFile() throws Exception {
        ResourceBundle bundle = XMLResourceBundle.getXMLBundle(
                getClass().getName(), Locale.ENGLISH, getClass().getClassLoader());
        ResourceBundle bundleDE = XMLResourceBundle.getXMLBundle(
                getClass().getName(), Locale.GERMAN, getClass().getClassLoader());

        assertEquals("", bundle.getLocale().getLanguage());
        assertEquals("de", bundleDE.getLocale().getLanguage());

        assertEquals("Hello World!", bundle.getString("hello-world"));
        assertEquals("Hallo Welt!", bundleDE.getString("hello-world"));

        //Check fallback to English
        assertEquals("Untranslatable", bundle.getString("untranslatable"));
        assertEquals("Untranslatable", bundleDE.getString("untranslatable"));
    }

    @Test
    public void testWithInvalidFile() throws Exception {
        try {
            ResourceBundle bundle = XMLResourceBundle.getXMLBundle(
                    "org.apache.fop.util.invalid-translation-file", getClass().getClassLoader());
            fail("Expected exception");
        } catch (MissingResourceException e) {
            //expected
        }
    }

}
