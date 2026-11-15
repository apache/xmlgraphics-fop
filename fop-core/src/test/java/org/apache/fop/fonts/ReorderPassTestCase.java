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

package org.apache.fop.fonts;

import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.traits.MinOptMax;

/**
 * SVG text maps its words keeping the associations, which index the original text, through a reorder pass over the
 * characters substitution produced. Paired with those associations, the pass read a later glyph's character after
 * any ligature, and gave the glyph that character as its ToUnicode text: e as "c" in "reflection". DejaVuLGCSerif
 * has GDEF, without which the pass does not run, and fi, fl and ffi ligatures.
 */
public class ReorderPassTestCase {

    private MultiByteFont mbf;

    private Font font;

    @Before
    public void setUp() throws Exception {
        InternalResourceResolver resolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        File file = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        CustomFont loaded = FontLoader.loadFont(new FontUris(file.toURI(), null), "", true,
                EmbeddingMode.AUTO, EncodingMode.AUTO, false, true, resolver, false, false, true);
        assertTrue("expected a MultiByteFont", loaded instanceof MultiByteFont);
        mbf = (MultiByteFont) loaded;
        assertTrue("the font must carry GDEF, or the reorder pass never runs", mbf.getGDEF() != null);
        font = new Font("DejaVuLGCSerif", null, mbf, 12000);
    }

    /** As SVG text maps a run: associations kept. */
    private GlyphMapping svgMapping(String text) {
        return GlyphMapping.doGlyphMapping(new Fragment(text), 0, text.length(), font, MinOptMax.ZERO,
                new MinOptMax[text.length()], '\0', '\0', false, -1, true, true, false);
    }

    /** As FO text maps a word: no associations. */
    private GlyphMapping foMapping(String text) {
        return GlyphMapping.doGlyphMapping(new Fragment(text), 0, text.length(), font, MinOptMax.ZERO,
                new MinOptMax[text.length()], '\0', '\0', false, -1, false, false, false);
    }

    private String meaningOf(int codePoint) {
        return mbf.getGlyphMeaning(mbf.findGlyphIndex(codePoint));
    }

    private static String associations(List associations) {
        StringBuilder sb = new StringBuilder();
        for (Object o : associations) {
            CharAssociation a = (CharAssociation) o;
            sb.append(sb.length() > 0 ? " " : "").append(a.getStart()).append('+').append(a.getCount());
        }
        return sb.toString();
    }

    /** No plain letter after a ligature takes a meaning; each ligature means its letters. */
    @Test
    public void aGlyphAfterALigatureKeepsItsOwnText() {
        GlyphMapping m = svgMapping("office reflection");
        assertEquals("oﬃce reﬂection", m.mapping);
        for (char c : "ocer tin".toCharArray()) {
            assertNull("'" + c + "' was given a meaning", meaningOf(c));
        }
        assertEquals("ffi", meaningOf(0xFB03));
        assertEquals("fl", meaningOf(0xFB02));
    }

    /**
     * A combining acute after a ligature is reordered before its base. The associations come back as they did
     * before the fix: the original text's indexes, each moved with its glyph. The space and the letters keep no
     * meaning; the pass, paired with these associations, gave the space the text "a".
     */
    @Test
    public void theReorderedAssociationsAreTheOriginalTextsIndexes() {
        GlyphMapping m = svgMapping("fi á");
        assertEquals("ﬁ ́a", m.mapping);
        assertEquals("0+2 2+1 4+1 3+1", associations(m.associations));
        assertNull(meaningOf(' '));
        assertNull(meaningOf('a'));
        assertNull(meaningOf(0x0301));
        assertEquals("fi", meaningOf(0xFB01));
    }

    /** FO text, which keeps no associations, maps the same characters, as it did before. */
    @Test
    public void foTextMapsTheSameCharacters() {
        assertEquals("ﬁ ́a", foMapping("fi á").mapping);
        assertEquals("oﬃce reﬂection", foMapping("office reflection").mapping);
        assertNull(foMapping("office reflection").associations);
    }

    /**
     * Where the caller's associations do not number the pass's characters (a supplementary character takes two),
     * the caller gets the pass's own associations, as before: this pins that behaviour, which is Apache's.
     */
    @Test
    public void aCountMismatchKeepsThePassesAssociations() {
        List associations = new ArrayList();
        associations.add(new CharAssociation(0, 2));
        associations.add(new CharAssociation(2, 1));
        associations.add(new CharAssociation(3, 2));
        mbf.reorderCombiningMarks("ﬁ 𝐀", null, "latn", "dflt", associations);
        assertEquals("0+1 1+1 2+1", associations(associations));
    }

    private static final class Fragment implements TextFragment {

        private final String text;

        private Fragment(String text) {
            this.text = text;
        }

        public CharacterIterator getIterator() {
            return new StringCharacterIterator(text);
        }

        public int getBeginIndex() {
            return 0;
        }

        public int getEndIndex() {
            return text.length();
        }

        public String getScript() {
            return "auto";
        }

        public String getLanguage() {
            return "none";
        }

        public int getBidiLevel() {
            return -1;
        }

        public char charAt(int index) {
            return text.charAt(index);
        }

        public CharSequence subSequence(int startIndex, int endIndex) {
            return text.subSequence(startIndex, endIndex);
        }
    }
}
