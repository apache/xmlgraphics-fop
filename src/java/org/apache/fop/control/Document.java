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
package org.apache.fop.control;

// Java
import java.util.Map;

// FOP
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;

/**
 * The FontInfo for the layout and rendering of a fo document.
 * This stores the list of available fonts that are setup by
 * the renderer. The font name can be retrieved for the
 * family style and weight.
 * <br>
 * Currently font supported font-variant small-caps is not
 * implemented.
 */
public class Document {

    /** Map containing fonts that have been used */
    private Map usedFonts;

    /** look up a font-triplet to find a font-name */
    private Map triplets;

    /** look up a font-name to get a font (that implements FontMetrics at least) */
    private Map fonts;

    /**
     * Main constructor
     */
    public Document() {
        this.triplets = new java.util.HashMap();
        this.fonts = new java.util.HashMap();
        this.usedFonts = new java.util.HashMap();
    }

    /**
     * Checks if the font setup is valid (At least the ultimate fallback font
     * must be registered.)
     * @return True if valid
     */
    public boolean isSetupValid() {
        return triplets.containsKey(Font.DEFAULT_FONT);
    }

    /**
     * Adds a new font triplet.
     * @param name internal key
     * @param family font family name
     * @param style font style (normal, italic, oblique...)
     * @param weight font weight
     */
    public void addFontProperties(String name, String family, String style,
                                  int weight) {
        /*
         * add the given family, style and weight as a lookup for the font
         * with the given name
         */

        String key = createFontKey(family, style, weight);
        this.triplets.put(key, name);
    }

    /**
     * Adds font metrics for a specific font.
     * @param name internal key
     * @param metrics metrics to register
     */
    public void addMetrics(String name, FontMetrics metrics) {
        // add the given metrics as a font with the given name

        this.fonts.put(name, metrics);
    }

    /**
     * Lookup a font.
     * <br>
     * Locate the font name for a given family, style and weight.
     * The font name can then be used as a key as it is unique for
     * the associated document.
     * This also adds the font to the list of used fonts.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public String fontLookup(String family, String style,
                             int weight) {
        String key;
        // first try given parameters
        key = createFontKey(family, style, weight);
        String f = (String)triplets.get(key);
        if (f == null) {
            // then adjust weight, favouring normal or bold
            f = findAdjustWeight(family, style, weight);

            // then try any family with orig weight
            if (f == null) {
                key = createFontKey("any", style, weight);
                f = (String)triplets.get(key);
            }

            // then try any family with adjusted weight
            if (f == null) {
                f = findAdjustWeight(family, style, weight);
            }

            // then use default
            if (f == null) {
                f = (String)triplets.get(Font.DEFAULT_FONT);
            }

        }

        usedFonts.put(f, fonts.get(f));
        return f;
    }

    /**
     * Find a font with a given family and style by trying
     * different font weights according to the spec.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public String findAdjustWeight(String family, String style,
                             int weight) {
        String key;
        String f = null;
        int newWeight = weight;
        if (newWeight < 400) {
            while (f == null && newWeight > 0) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
        } else if (newWeight == 500) {
            key = createFontKey(family, style, 400);
            f = (String)triplets.get(key);
        } else if (newWeight > 500) {
            while (f == null && newWeight < 1000) {
                newWeight += 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
            newWeight = weight;
            while (f == null && newWeight > 400) {
                newWeight -= 100;
                key = createFontKey(family, style, newWeight);
                f = (String)triplets.get(key);
            }
        }
        if (f == null) {
            key = createFontKey(family, style, 400);
            f = (String)triplets.get(key);
        }

        return f;
    }

    /**
     * Determines if a particular font is available.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return True if available
     */
    public boolean hasFont(String family, String style, int weight) {
        String key = createFontKey(family, style, weight);
        return this.triplets.containsKey(key);
    }

    /**
     * Creates a key from the given strings.
     * @param family font family
     * @param style font style
     * @param weight font weight
     * @return internal key
     */
    public static String createFontKey(String family, String style,
                                       int weight) {
        return family + "," + style + "," + weight;
    }

    /**
     * Gets a Map of all registred fonts.
     * @return a read-only Map with font key/FontMetrics pairs
     */
    public Map getFonts() {
        return java.util.Collections.unmodifiableMap(this.fonts);
    }

    /**
     * This is used by the renderers to retrieve all the
     * fonts used in the document.
     * This is for embedded font or creating a list of used fonts.
     * @return a read-only Map with font key/FontMetrics pairs
     */
    public Map getUsedFonts() {
        return this.usedFonts;
    }

    /**
     * Returns the FontMetrics for a particular font
     * @param fontName internal key
     * @return font metrics
     */
    public FontMetrics getMetricsFor(String fontName) {
        usedFonts.put(fontName, fonts.get(fontName));
        return (FontMetrics)fonts.get(fontName);
    }
}

