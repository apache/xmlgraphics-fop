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
package org.apache.fop.render.awt;

// FOP
import org.apache.fop.messaging.MessageHandler;

// Java
import java.io.InputStream;
import java.net.URL;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.FontMetrics;
import java.awt.font.TextLayout;
import java.util.Map;

/**
 * This is a FontMetrics to be used  for AWT  rendering.
 * It  instanciates a font, depening on famil and style
 * values. The java.awt.FontMetrics for this font is then
 * created to be used for the actual measurement.
 * Since layout is word by word and since it is expected that
 * two subsequent words often share the same style, the
 * Font and FontMetrics is buffered and only changed if needed.
 * <p>
 * Since FontState and FontInfo multiply all factors by
 * size, we assume a "standard" font of FONT_SIZE.
 */

public class AWTFontMetrics {

    /**
     * Font size standard used for metric measurements
     */
    public static final int FONT_SIZE = 1;

    /**
     * This factor multiplies the calculated values to scale
     * to FOP internal measurements
     */
    public static final int FONT_FACTOR = (1000 * 1000) / FONT_SIZE;

    /**
     * The width of all 256 character, if requested
     */
    private int width[] = null;

    /**
     * The typical height of a small cap latter
     */
    private int xHeight = 0;

    /**
     * Buffered font.
     * f1 is bufferd for metric measurements during layout.
     * fSized is buffered for display purposes
     */
    private Font f1 = null;    // , fSized = null;

    /**
     * The family type of the font last used
     */
    private String family = "";

    /**
     * The style of the font last used
     */
    private int style = 0;

    /**
     * The size of the font last used
     */
    private float size = 0;

    /**
     * The FontMetrics object used to calculate character width etc.
     */
    private FontMetrics fmt = null;

    /**
     * Temp graphics object needed to get the font metrics
     */
    Graphics2D graphics;

    /**
     * Embed Font List.
     */
    private Map embedFontList = null;

    /**
     * Physical Font Cache.
     */
    private Map fontCache = null;

    /**
     * Constructs a new Font-metrics.
     * @param parent  an temp graphics object - this is needed  so
     * that we can get an instance of
     * java.awt.FontMetrics
     */
    public AWTFontMetrics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    /**
     * Determines the font ascent of the Font described by this
     * FontMetrics object
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @return ascent in milliponts
     */
    public int getAscender(String family, int style, int size) {
        setFont(family, style, size);
        // return (int)(FONT_FACTOR * fmt.getAscent());

        // workaround for sun bug on FontMetric.getAscent()
        // http://developer.java.sun.com/developer/bugParade/bugs/4399887.html
        int realAscent = fmt.getAscent()
                         - (fmt.getDescent() + fmt.getLeading());
        return FONT_FACTOR * realAscent;
    }


    /**
     * The size of a capital letter measured from the font's baseline
     */
    public int getCapHeight(String family, int style, int size) {
        // currently just gets Ascent value but maybe should use
        // getMaxAcent() at some stage
        return getAscender(family, style, size);
    }

    /**
     * Determines the font descent of the Font described by this
     * FontMetrics object
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @return descent in milliponts
     */
    public int getDescender(String family, int style, int size) {
        setFont(family, style, size);
        return (-1 * FONT_FACTOR * fmt.getDescent());
    }

    /**
     * Determines the typical font height of a small cap letter
     * FontMetrics object
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @return font height in milliponts
     */
    public int getXHeight(String family, int style, int size) {
        setFont(family, style, size);
        return (int)(FONT_FACTOR * xHeight);
    }

    /**
     * Returns width (in 1/1000ths of point size) of character at
     * code point i
     * @param  i the character for which to get the width
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size the of the font
     */
    public int width(int i, String family, int style, int size) {
        int w;
        setFont(family, style, size);
        
        // Nov 18, 2002,  aml/rlc  
        // measure character width using getStringBounds for better results
        
        char [] ac = new char [1];
        ac [0] = (char)i;
        
        double dWidth = fmt.getStringBounds (ac, 0, 1, graphics).getWidth() * FONT_FACTOR;
        
        // The following was left in based on this comment from the past (may be vestigial)
        
        // the output seems to look a little better if the
        // space is rendered larger than given by
        // the FontMetrics object
        
        if (i <=32) {
          dWidth = dWidth * 1.4;
        }

        return (int) dWidth;
    }

    /**
     * Return widths (in 1/1000ths of point size) of all
     * characters
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     */
    public int[] getWidths(String family, int style, int size) {
        int i;

        if (width == null) {
            width = new int[256];
        }
        setFont(family, style, size);
        for (i = 0; i < 256; i++) {
            width[i] = FONT_FACTOR * fmt.charWidth(i);
        }
        return width;
    }

    /**
     * Checks whether the font  for which values are
     * requested is the one used immediately before or
     * whether it is a new one
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @return true if the font was changed, false otherwise
     */
    private boolean setFont(String family, int style, int size) {
        boolean changed = false;
        Rectangle2D rect;
        TextLayout layout;
        int s = (int)(size / 1000f);

        if (f1 == null) {
            f1 = createFont(family, style, s);
            fmt = graphics.getFontMetrics(f1);
            changed = true;
        } else {
            if ((this.style != style) ||!this.family.equals(family)
                    || this.size != s) {
                if (family.equals(this.family)) {
                    f1 = f1.deriveFont(style, (float)s);
                } else
                    f1 = createFont(family, style, s);
                fmt = graphics.getFontMetrics(f1);
                changed = true;
            }
            // else the font is unchanged from last time
        }
        if (changed) {
            layout = new TextLayout("m", f1, graphics.getFontRenderContext());
            rect = layout.getBounds();
            xHeight = (int)rect.getHeight();
        }
        // save the family and style for later comparison
        this.family = family;
        this.style = style;
        this.size = s;
        return changed;
    }

    /**
     * set embed font.
     * @param family font-family name
     * @param style font style
     * @param fontURL URL to physical font
     */
    public void setEmbedFont(String family, int style, URL fontURL) {
        if (embedFontList == null)
            embedFontList = new java.util.HashMap();
        embedFontList.put(family+style, fontURL);
    }

    /**
     * create Font to draw.
     * @param family font-family name
     * @param style font style
     * @param size font size
     */
    public java.awt.Font createFont(String family, int style, int size) {
        URL fontURL = null;
        if (embedFontList != null)
            fontURL = (URL)embedFontList.get(family+style);
        if (fontURL == null)
            return new Font(family, style, size);
        // lazy instanciation for fontCache.
        if (fontCache == null)
            fontCache = new java.util.HashMap();
        Font cachedFont = (Font)fontCache.get(fontURL.toExternalForm());
        if (cachedFont == null) {
            // Create specified TrueType Font.
            InputStream fontStream = null;
            try {
                MessageHandler.logln("Create embedded AWT font from stream "+fontURL.toExternalForm());
                fontStream = fontURL.openStream();
                // createFont methods supports higer than JDK1.3
                // Currently supports only TrueType font.
                cachedFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            } catch(Throwable th) {
                MessageHandler.error("Failed to create embedded AWT font "+
                                     fontURL.toExternalForm() + ": " + th.toString());
                // if failed create font, use system "Dialog" logical font
                // name for each Locale.
                cachedFont = new Font("Dialog", style, size);
            } finally {
                if (fontStream != null)
                    try { fontStream.close(); } catch(Exception ex) {}
            }
            fontCache.put(fontURL.toExternalForm(), cachedFont);
        }
        Font font = cachedFont.deriveFont(style, (float)size);
        return font;
    }

    /**
     * Returns a java.awt.Font instance for the desired
     * family, style and size type.
     * This is here, so that the font-mapping
     * of FOP-defined fonts to java-fonts can be done
     * in one place and does not need to occur in
     * AWTFontRenderer.
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return font with the desired characeristics.
     */
    public java.awt.Font getFont(String family, int style, int size) {
        Font f;

        setFont(family, style, size);
        return f1;
        /*
         * if( setFont(family,style, size) ) fSized = null;
         * if( fSized == null ||  this.size != size ) {
         * fSized = f1.deriveFont( size / 1000f );
         * }
         * this.size = size;
         * return fSized;
         */
    }

}






