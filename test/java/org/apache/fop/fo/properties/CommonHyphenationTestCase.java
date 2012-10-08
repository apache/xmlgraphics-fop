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

package org.apache.fop.fo.properties;

import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CommonHyphenationTestCase {

    private final String lang = "en";

    @Test
    public void testToLocaleNull() {
        Locale locale = CommonHyphenation.toLocale(null, null);
        assertNull(locale);
        locale = CommonHyphenation.toLocale("none", null);
        assertNull(locale);
        locale = CommonHyphenation.toLocale("NoNe", "US");
        assertNull(locale);
    }

    @Test
    public void testToLocaleWithJustLanguage() {
        Locale locale = new Locale(lang);
        assertEquals(locale, CommonHyphenation.toLocale(lang, null));
        assertEquals(locale, CommonHyphenation.toLocale(lang, "none"));
        assertEquals(locale, CommonHyphenation.toLocale(lang, "NONE"));
    }

    @Test
    public void testToLocaleWithLanguageAndCountry() {
        Locale locale = new Locale(lang, "US");
        assertEquals(locale, CommonHyphenation.toLocale(lang, "US"));
        assertEquals(locale, CommonHyphenation.toLocale(lang, "us"));
    }

}
