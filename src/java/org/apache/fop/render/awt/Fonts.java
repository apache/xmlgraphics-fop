/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 23/05/2004
 * $Id$
 */
package org.apache.fop.render.awt;

import java.awt.Font;
//import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
//import java.awt.font.FontRenderContext;
//import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.FontStretch;
import org.apache.fop.fo.properties.FontStyle;
import org.apache.fop.fo.properties.FontVariant;
import org.apache.fop.fo.properties.FontWeight;
import org.apache.fop.fonts.FontException;
import org.apache.fop.render.FontData;

/**
 * Java font selection is based on family names.  It seems that Java
 * handles font mapping something like this:<br>
 * Given a set of physical fonts like, e.g., Arial, Java reports them as
 * <pre>
 * font face: Arial
 *     logical:Arial
 *     family:Arial
 *     PSName:ArialMT
 *     Style:
 *         PLAIN
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * font face: Arial Cursiva
 *     logical:Arial Cursiva
 *     family:Arial
 *     PSName:Arial-ItalicMT
 *     Style:
 *         PLAIN
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * font face: Arial Negreta
 *     logical:Arial Negreta
 *     family:Arial
 *     PSName:Arial-BoldMT
 *     Style:
 *         PLAIN
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * font face: Arial Negreta cursiva
 *     logical:Arial Negreta cursiva
 *     family:Arial
 *     PSName:Arial-BoldItalicMT
 *     Style:
 *         PLAIN
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * </pre>
 * There are other Arial forms, e.g. Arial Black and Arial Narrow, but
 * they fall into different families, as indicated by the font name.
 * java.awt.font.TextAttribute defines a number of TextAttribute
 * constants, and querying a Font object via getAvailableAttributes()
 * will provide an array of attributes available on the Font.
 * <p>It seems there is a common set available on both Type1 and TrueType
 * fonts in 1.4.2; viz FAMILY, WEIGHT, POSTURE, SIZE and TRANSFORM.
 * Note that style is reported as PLAIN on all fonts, irrespective of
 * the actual style according to the font name.
 * <p>SIZE works as one might expect.  WEIGHT is supported directly only
 * for the weights provided in the set of family fonts.  In the case of
 * Arial, only REGULAR and BOLD.  The same is true of POSTURE: REGULAR
 * and OBLIQUE.  There seems to be room here to experiment with
 * virtual fonts.  A virtual Arial font might be constructed from the
 * Arial, Arial Narrow, Arial Black and Arial Black MT fonts.
 * Another area where virtual fonts might be handy is for small caps.
 * 
 * <p>In the case of the set of <i>logical</i> fonts defined for all Java
 * implementations, the characteristics are reported like this:
 * <pre>
 * font face: serif.plain
 *     logical:serif
 *     family:serif
 *     PSName:serif
 *     Style:
 *         PLAIN              
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * font face: serif.bold
 *     logical:serif.bold
 *     family:serif
 *     PSName:serif.bold
 *     Style:
 *         PLAIN              
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * font face: serif.bolditalic
 *     logical:serif.bolditalic
 *     family:serif
 *     PSName:serif.bolditalic
 *     Style:
 *         PLAIN              
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * font face: serif.italic
 *     logical:serif.italic
 *     family:serif
 *     PSName:serif.italic
 *     Style:
 *         PLAIN              
 *     FAMILY
 *     WEIGHT
 *     POSTURE
 *     SIZE
 *     TRANSFORM
 * </pre>
 * Note that in this case, the logical name of the serif.plain font is
 * <i>serif</i>.  This correspondence only seems to occur with the logical
 * fonts.
 * 
 * <p>Three names are available for each <code>Font</code> object:
 * <dl>
 * <dt>Font Face Name</dt>
 *   <dd>Aka Font Name. Corresponds to a phsical font in the underlying
 *       system.</dd>
 * <dt>Family Name</dt>
 *   <dd>the name of the font family that determines the typographic design
 *       across several faces.</dd>
 * <dt>Logical Name</dt>
 *   <dd>The name that was used to construct the font.  Each font has
 *       such a name, irrespective of whether it is a <i>logical</i> or
 *       <i>physical</i> font.  The logical name only differs from the
 *       font face name in the case of <i>logical</i> fonts.  <i>E.g.</i>
 *       the logical name of <code>serif.plain</code> is
 *       <code>serif</code>, which is also the name of the pre-defined
 *       logical font <i>serif</i>.</dd>
 * <dt>Postscript Name</dt>
 *   <dd>The Postscript name of the font.  Derivation and significance
 *       unknown.<dd>
 * </dl>
 * 
 * Initial font mapping is based on the names available to the font.
 * 
 * <h4>XSL-FO/CSS2 system fonts</h4>
 * The CSS2 system fonts are:
 * <ul>
 * <li>caption</li>
 * <li>icon</li>
 * <li>menu</li>
 * <li>message-box</li>
 * <li>small-caption</li>
 * <li>status-bar</li>
 * </ul>
 * The situation on linux systems is that there are no system fonts as
 * such.  Individual GUI environments like Gnome, KDE, CDE and the like
 * may define such fonts, but determining them will depend on the
 * individual system's GUI environment.  The closest parallel in Java is
 * the set of <i>logical</i> fonts defined in every Java implementation,
 * <i>viz.</i>
 * <ul>
 * <li>Serif</li>
 * <li>SansSerif</li>
 * <li>Monospaced</li>
 * <li>Dialog</li>
 * <li>DialogInput</li>.
 * </ul>
 * The most obvious mapping from Java logical fonts to XSL-FO/CSS2
 * system fonts is
 * <dl>
 * <dt>caption</dt><dd>SansSerif at size A</dd>
 * <dt>icon</dt><dd>SansSerif at size B</dd>
 * <dt>menu</dt><dd>SansSerif at size C</dd>
 * <dt>message-box</dt><dd>Dialog</dd>
 * <dt>small-caption</dt><dd>caption at size A/1.2</dd>
 * <dt>status-bar</dt><dd>SansSerif at size D</dd>
 * </dl>
 * where sizes A, B, C and D are UserAgent prerogatives determined in
 * consultation with the underlying JVM font system.  I.e., the fonts
 * must support fractional metrics and dynamic sizing, which, in default
 * Java implementations, they do, as far as I know.
 * 
 * <h4>XSL-FO/CSS2 Generic Font Families</h4>
 * The generic families in the Recommendation are:
 * <ul>
 * <li>serif</li>
 * <li>sans-serif</li>
 * <li>cursive</li>
 * <li>fantasy</li>
 * <li>monospace</li>.
 * </ul> 
 * The mapping of the CSS2 generics <code>serif</code>,
 * <code>sans-serif</code> and <code>monospace</code> is a straightforward
 * name translation.  There is no such convenient correspondence between
 * the Java font system and <code>cursive</code> and <code>fantasy</code>
 * fonts.  This mapping must be determined by the UserAgent by
 * interrogating the JVM.
 * 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class Fonts implements FontData {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";


//    public static final int
//                   NO_ATTR = 0
//                   ,BACKGROUND = 1
//                   ,BIDI_EMBEDDING = 2
//                   ,CHAR_REPLACEMENT = 4
//                   ,FAMILY = 8
//                   ,FONT = 16
//                   ,FOREGROUND = 32
//                   ,INPUT_METHOD_HIGHLIGHT = 64
//                   ,INPUT_METHOD_UNDERLINE = 128
//                   ,JUSTIFICATION = 256
//                   ,NUMERIC_SHAPING = 512
//                   ,POSTURE = 1024
//                   ,RUN_DIRECTION = 2048
//                   ,SIZE = 4096
//                   ,STRIKETHROUGH = 8192
//                   ,SUPERSCRIPT = 16384
//                   ,SWAP_COLORS = 32768
//                   ,TRANSFORM = 65536
//                   ,UNDERLINE = 131072
//                   ,WEIGHT = 262144
//                   ,WIDTH = 524288
//                   ;

    //private HashMap fontAttributes = null;

    private HashMap fontFamilies = null;
    private HashSet serif = new HashSet();
    private HashSet sansserif = new HashSet();
    private HashSet monospace = new HashSet();
    private HashSet cursive = new HashSet();
    private HashSet fantasy = new HashSet();
    private HashSet symbols = new HashSet();
    
    public Fonts() {
        setupFonts();
    }

    /**
     * Sets up the font family maps applying to the fonts available to the JVM
     */
    private void setupFonts() {
//        // Set up the graphics environment
//        BufferedImage fontImage =
//            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2D = fontImage.createGraphics();
//        FontRenderContext frcontext = g2D.getFontRenderContext();
        // Set up the fonts environment
        // TODO Check whether this is needed to provide better mapping between
        // requested fonts and those available on the system
        GraphicsEnvironment gEnv =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = gEnv.getAllFonts();
        String[] families = gEnv.getAvailableFontFamilyNames();
        Locale locale = Locale.getDefault();
        fontFamilies =
            new HashMap((int)(families.length + fonts.length * 4.5));
        //fontAttributes = new HashMap((int)(fonts.length / 0.7));
        // Enter all of the family names, keyed on themselves, and keyed on 
        // the lower-case version of the family name, if different.
        // N.B. If there are two font family names which differ only in that
        // one is the locale-specific lower-case version of the first, they
        // will be recorded in availabelFonsts as two separate font families.
        for (int i = 0; i < families.length; i++) {
            if (fontFamilies.get(families[i]) == null) {
                fontFamilies.put(families[i], families[i]);
            }
        }
        for (int i = 0; i < families.length; i++) {
            String lcase = families[i].toLowerCase(locale);
            if (fontFamilies.get(lcase) == null) {
                fontFamilies.put(lcase, families[i]);
            }
        }
        String[] fontNames = new String[fonts.length];
        String[] psNames = new String[fonts.length];
        for (int i = 0; i < fonts.length; i++) {
            Font f = fonts[i];
            String family = f.getFamily();
            String lcfamily = family.toLowerCase(locale);
            String font = f.getFontName();
            String lcfont = font.toLowerCase(locale);
            String psname = f.getPSName();
            String lcpsname = psname.toLowerCase(locale);
            String logical = f.getName();
            String lclogical = logical.toLowerCase(locale);
            // Map each of the font names to the family name
            if (fontFamilies.get(font) == null) {
                fontFamilies.put(font, family);
            }
            if (fontFamilies.get(psname) == null) {
                fontFamilies.put(psname, family);
            }
            if (fontFamilies.get(logical) == null) {
                fontFamilies.put(logical, family);
            }
            // Collect styles for possible intelligent font substitution
            // TODO if this is not used, delete
            checkMonospace(monospace, family, lcfamily);
            checkSerif(serif, family, lcfamily);
            checkSansSerif(sansserif, family, lcfamily);
            checkCursive(cursive, family, lcfamily);
            checkFantasy(fantasy, family, lcfamily);
            checkSymbols(symbols, family, lcfamily);
            // Add mappings for some of the CSS2 generic font families
            setupCSSGenericMapping(fontFamilies);
            // Add mappings for the CSS2 system fonts
            setupCSSSystemFontMapping(fontFamilies);
        }
    }

    /**
     * Adds mappings for the CSS2/XSL-FO generic font families (serif,
     * sans-serif, monospace, cursive and fantasy) to the <code>Map</code> of
     * font families.
     * @param fontFamilies the map of font families
     */
    public void setupCSSGenericMapping(Map fontFamilies) {
        // Add mappings for some of the CSS2 generic font families
        // TODO set up mappings for "cursive" and "fantasy"
        if (fontFamilies.get("serif") == null) {
            fontFamilies.put("serif", "Serif");
        }
        if (fontFamilies.get("sans-serif") == null) {
            fontFamilies.put("sans-serif", "SansSerif");
        }
        if (fontFamilies.get("monospace") == null) {
            fontFamilies.put("monospace", "Monospaced");
        }
    }

    /**
     * Adds a mapping of the CSS2/XSL-FO system fonts (caption, icon, menu,
     * message-box, small-caption, status-bar) to the <code>Map</code> of font
     * families.
     * @param fontFamilies the map of font families
     */
    public void setupCSSSystemFontMapping(Map fontFamilies) {
        // TODO
    }

    /**
     * Adds a font to the <code>Set</code> of monspace fonts, if it qualifies.
     * @param monospace the set of monospace fonts
     * @param family the font family to check
     * @param lcfamily the lowercase version of the font family to check
     */
    private void checkMonospace(Set monospace, String family, String lcfamily) {
        int mono = lcfamily.lastIndexOf("mono");
        if (mono >= 0) {
            if (lcfamily.indexOf("monotype") != mono) {
                // Didn't find "Monotype"
                monospace.add(family);
            }
        }
        if (lcfamily.indexOf("courier") >= 0) {
            monospace.add(family);
        }
        if (lcfamily.indexOf("console") >= 0) {
            monospace.add(family);
        }
        if (lcfamily.indexOf("typewriter") >= 0) {
            monospace.add(family);
        }
    }

    /**
     * Adds a font to the <code>Set</code> of serif fonts, if it qualifies.
     * @param serif the set of serif fonts
     * @param family the font family to check
     * @param lcfamily the lowercase version of the font family to check
     */
    private void checkSerif(Set serif, String family, String lcfamily) {
        int ser = lcfamily.indexOf("serif");
        if (ser >= 0) {
            if (lcfamily.indexOf("sans") < 0) {
                // Didn't find "sans serif"
                serif.add(family);
            }
        }
        if (lcfamily.indexOf("roman") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("times") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("bookman") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("utopia") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("palatino") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("palladio") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("bright") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("georgia") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("schoolbook") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("charter") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("antiqua") >= 0) {
            serif.add(family);
        }
        if (lcfamily.indexOf("footlight") >= 0) {
            serif.add(family);
        }
    }

    /**
     * Adds a font to the <code>Set</code> of sans-serif fonts, if it qualifies.
     * @param sansserif the set of sans-serif fonts
     * @param family the font family to check
     * @param lcfamily the lowercase version of the font family to check
     */
    private void checkSansSerif(Set sansserif, String family, String lcfamily) {
        if (lcfamily.indexOf("sansserif") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("sans-serif") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("helvetica") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("arial") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("avantgarde") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("gothic") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("tahoma") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("thonburi") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("trebuchet") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("verdana") >= 0) {
            sansserif.add(family);
        }
        if (lcfamily.indexOf("sans") >= 0) {
            if (lcfamily.indexOf("comic") < 0) {
                sansserif.add(family);
            }
        }
    }

    /**
     * Adds a font to the <code>Set</code> of cursive fonts, if it qualifies.
     * @param cursive the set of cursive fonts
     * @param family the font family to check
     * @param lcfamily the lowercase version of the font family to check
     */
    private void checkCursive(Set cursive, String family, String lcfamily) {
        if (lcfamily.indexOf("chancery") >= 0) {
            cursive.add(family);
        }
        if (lcfamily.indexOf("brush") >= 0) {
            cursive.add(family);
        }
        if (lcfamily.indexOf("script") >= 0) {
            cursive.add(family);
        }
        if (lcfamily.indexOf("naskh") >= 0) {
            cursive.add(family);
        }
        if (lcfamily.indexOf("shuwiefat") >= 0) {
            cursive.add(family);
        }
    }

    /**
     * Adds a font to the <code>Set</code> of fantasy fonts, if it qualifies.
     * @param fantasy the set of fantasy fonts
     * @param family the font family to check
     * @param lcfamily the lowercase version of the font family to check
     */
    private void checkFantasy(Set fantasy, String family, String lcfamily) {
        if (lcfamily.indexOf("algerian") >= 0) {
            fantasy.add(family);
        }
        if (lcfamily.indexOf("americantext") >= 0) {
            fantasy.add(family);
        }
        if (lcfamily.indexOf("braggadocio") >= 0) {
            fantasy.add(family);
        }
        if (lcfamily.indexOf("colonna") >= 0) {
            fantasy.add(family);
        }
        if (lcfamily.indexOf("comic") >= 0) {
            fantasy.add(family);
        }
        if (lcfamily.indexOf("desdemona") >= 0) {
            fantasy.add(family);
        }
        if (lcfamily.indexOf("kino") >= 0) {
            fantasy.add(family);
        }
        if (lcfamily.indexOf("playbill") >= 0) {
            fantasy.add(family);
        }
    }

    /**
     * Adds a font to the <code>Set</code> of symbol fonts, if it qualifies.
     * @param symbols the set of symbol fonts
     * @param family the font family to check
     * @param lcfamily the lowercase version of the font family to check
     */
    private void checkSymbols(Set symbols, String family, String lcfamily) {
        if (lcfamily.indexOf("symbol") >= 0) {
            symbols.add(family);
        }
        if (lcfamily.indexOf("dingbats") >= 0) {
            symbols.add(family);
        }
        if (lcfamily.indexOf("webdings") >= 0) {
            symbols.add(family);
        }
        if (lcfamily.indexOf("wingdings") >= 0) {
            symbols.add(family);
        }
        if (lcfamily.indexOf("sorts") >= 0) {
            symbols.add(family);
        }
    }

    public Map makeFontAttributes(String family, int style, int variant,
            int weight, int stretch, float size)
    throws FontException {
        HashMap attributes = new HashMap();
        attributes.put(TextAttribute.FAMILY, family);
        switch (style) {
            case FontStyle.NORMAL:
                attributes.put(
                        TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
                break;
            case FontStyle.ITALIC:
            case FontStyle.OBLIQUE:
                attributes.put(
                        TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
                break;
            default:
                throw new FontException(
                "Only NORMAL, OBLIQUE and ITALIC supported for style");
        }
        switch (variant) {
            case FontVariant.NORMAL:
                break;
            default:
                throw new FontException("Only NORMAL supported for variant");
        }
        switch (weight) {
            case FontWeight.NORMAL:
                attributes.put(
                        TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
                break;
            case FontWeight.BOLD:
                attributes.put(
                        TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            break;
            default:
                throw new FontException(
                        "Only NORMAL and BOLD supported for weight");
        }
        switch (stretch) {
            case FontStretch.NORMAL:
                break;
            default:
                throw new FontException("Only NORMAL supported for stretch");
        }
        attributes.put(TextAttribute.SIZE, new Float(size));
        return attributes;
    }

    /**
     * Gets a java.awt.Font matching the given criteria.
     * @param family the font family
     * @param style only NORMAL, ITALIC and OBLIQUE are supported
     * @param variant only NORMAL is supported
     * @param weight only NORMAL and BOLD are supported
     * @param stretch only NORMAL is supported
     * @param size the size of the font in fractional points
     * @param strategy currently ignored
     * @return
     */
    public Font getFont(
            String family, int style, int variant, int weight,
            int stretch, float size, int strategy)
    throws FontException {
        Map attributes = makeFontAttributes(family, style, variant, weight,
                stretch, size);
        return new Font(attributes);
    }

    public Font getFont(Map attributes, int strategy) {
        // strategy currently ignored
        return new Font(attributes);
    }

    /**
     * Gets a font corresponding to one of the CSS2/XSL-FO generic fonts:
     * serif, sans-serif, monospace, cursive, fantasy.
     * @param type the generic font type
     * @param style only NORMAL, ITALIC and OBLIQUE are supported
     * @param variant only NORMAL is supported
     * @param weight only NORMAL and BOLD are supported
     * @param stretch only NORMAL is supported
     * @param size the size of the font in fractional points
     * @return
     */
    public Font getGenericFont(
            String type, int style, int variant, int weight,
            int stretch, float size)
        throws FontException {
            Map attributes = makeFontAttributes(type, style, variant, weight,
                    stretch, size);
            return new Font(attributes);
        
    }

    public Font getGenericFont(Map attributes) {
        return new Font(attributes);
    }

    /**
     * Gets a font corresponding to one of the CSS2/XSL-FO system fonts:
     * caption, icon. menu. message-box, small-caption, status-bar.
     * @param type one of the system fonts
     * @return the font
     * @throws FontException
     */
    public Font getSystemFont(int type) throws FontException {
        PropertyConsts pconsts = PropertyConsts.getPropertyConsts();
        try {
            // Validate the type
            String sysfont = pconsts.getEnumText(PropNames.FONT, type);
        } catch (PropertyException e) {
            throw new FontException(e);
        }
        // TODO implement this
        throw new FontException("getSystemFont not supported");
    }
}
