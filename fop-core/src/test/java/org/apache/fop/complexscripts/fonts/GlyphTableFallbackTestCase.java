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

package org.apache.fop.complexscripts.fonts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.fop.complexscripts.fonts.GlyphTable.LookupSpec;
import org.apache.fop.complexscripts.fonts.GlyphTable.LookupTable;
import org.apache.fop.complexscripts.scripts.ScriptProcessor;

/**
 * {@link GlyphTable#matchLookups} falls back the way OpenType layout engines do: from
 * (script, language) to (script, dflt), and only then to (DFLT, dflt). Before FOP-3341 it went
 * straight from the first to the third, so a language with no language system in the font lost
 * the script's own features, and a font with no DFLT table lost everything.
 */
public class GlyphTableFallbackTestCase {

    /** A table whose lookups sit under the given (script, language) pairs, one liga lookup each. */
    private GlyphTable tableWith(String... scriptLanguagePairs) {
        Map<LookupSpec, List<String>> lookups = new HashMap<LookupSpec, List<String>>();
        for (int i = 0; i + 1 < scriptLanguagePairs.length; i += 2) {
            lookups.put(new LookupSpec(scriptLanguagePairs[i], scriptLanguagePairs[i + 1], "liga"),
                    new ArrayList<String>(Arrays.asList("lu" + i)));
        }
        return new GlyphTable(null, lookups, new HashMap<String, ScriptProcessor>());
    }

    private static List<String> scriptsOf(Map<LookupSpec, List<LookupTable>> matched) {
        List<String> found = new ArrayList<String>();
        for (LookupSpec ls : matched.keySet()) {
            found.add(ls.getScript() + "/" + ls.getLanguage());
        }
        return found;
    }

    /** Carlito's shape: latn, grek and cyrl, no DFLT. A language the font has no system for. */
    @Test
    public void testLanguageWithoutSystemTakesTheScriptsDefault() {
        GlyphTable t = tableWith("latn", "dflt", "grek", "dflt", "cyrl", "dflt");
        assertEquals(Arrays.asList("latn/dflt"), scriptsOf(t.matchLookups("latn", "en", "*")));
    }

    /** A language the font does have a system for is matched directly, not defaulted. */
    @Test
    public void testLanguageWithSystemIsMatchedDirectly() {
        GlyphTable t = tableWith("latn", "dflt", "latn", "TRK");
        assertEquals(Arrays.asList("latn/TRK"), scriptsOf(t.matchLookups("latn", "TRK", "*")));
    }

    /** DejaVu Sans's shape: a DFLT table without the feature, the script's own table with it. */
    @Test
    public void testScriptsDefaultIsPreferredToDefaultScript() {
        GlyphTable t = tableWith("DFLT", "dflt", "latn", "dflt");
        assertEquals(Arrays.asList("latn/dflt"), scriptsOf(t.matchLookups("latn", "en", "*")));
    }

    /** A script the font lacks still falls back to the default script, as before. */
    @Test
    public void testMissingScriptTakesTheDefaultScript() {
        GlyphTable t = tableWith("DFLT", "dflt", "latn", "dflt");
        assertEquals(Arrays.asList("DFLT/dflt"), scriptsOf(t.matchLookups("grek", "el", "*")));
    }

    /** A font with neither the script nor DFLT yields nothing, as before; the caller then does nothing. */
    @Test
    public void testNothingMatchesNothing() {
        GlyphTable t = tableWith("latn", "dflt");
        assertTrue(t.matchLookups("arab", "ar", "*").isEmpty());
    }

    /** The wildcard script with a language: the language falls back, the wildcard still matches every script. */
    @Test
    public void testWildcardScriptWithLanguage() {
        GlyphTable t = tableWith("latn", "dflt", "grek", "dflt");
        List<String> found = scriptsOf(t.matchLookups("*", "en", "*"));
        assertTrue(found.contains("latn/dflt"));
        assertTrue(found.contains("grek/dflt"));
    }

    /** An ISO 639 code, as the FO language property carries it, finds the font's system for that language. */
    @Test
    public void testIsoCodeFindsTheLanguageSystem() {
        GlyphTable t = tableWith("latn", "dflt", "latn", "TRK");
        assertEquals(Arrays.asList("latn/TRK"), scriptsOf(t.matchLookups("latn", "tr", "*")));
        assertEquals(Arrays.asList("latn/TRK"), scriptsOf(t.matchLookups("latn", "tr-TR", "*")));
        assertEquals(Arrays.asList("latn/TRK"), scriptsOf(t.matchLookups("latn", "TRK ", "*")));
        assertEquals(Arrays.asList("latn/dflt"), scriptsOf(t.matchLookups("latn", "en-US", "*")));
    }

    /** The code-to-tag mapping, including the Chinese variants and an unknown code. */
    @Test
    public void testLanguageCodeToTag() {
        assertEquals("TRK", OTFLanguage.fromLanguageCode("tr"));
        assertEquals("ENG", OTFLanguage.fromLanguageCode("EN"));
        assertEquals("DEU", OTFLanguage.fromLanguageCode("de_AT"));
        assertEquals("ZHS", OTFLanguage.fromLanguageCode("zh"));
        assertEquals("ZHS", OTFLanguage.fromLanguageCode("zh-CN"));
        assertEquals("ZHT", OTFLanguage.fromLanguageCode("zh-TW"));
        assertEquals("ZHT", OTFLanguage.fromLanguageCode("zh-Hant"));
        assertEquals("ZHH", OTFLanguage.fromLanguageCode("zh-HK"));
        assertEquals("NSM", OTFLanguage.fromLanguageCode("se"));
        assertEquals(null, OTFLanguage.fromLanguageCode("xx"));
        assertEquals(null, OTFLanguage.fromLanguageCode(""));
        assertEquals(null, OTFLanguage.fromLanguageCode(null));
    }

    /** {@code OTFScript.isWildCard} compared with the default script; it now compares with the wildcard. */
    @Test
    public void testScriptWildcardTest() {
        assertTrue(OTFScript.isWildCard("*"));
        assertFalse(OTFScript.isWildCard("DFLT"));
        assertTrue(OTFScript.isDefault("DFLT"));
    }
}
