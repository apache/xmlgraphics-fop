/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.fop.render.awt;

// Java
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.FontMetrics;
import java.awt.font.TextLayout;

/**
 * This is a FontMetrics to be used  for AWT  rendering.
 * It  instanciates a font, depening on family and style
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
    private Graphics2D graphics;

    /**
     * Constructs a new Font-metrics.
     * @param graphics a temp graphics object - this is needed  so
     * that we can get an instance of java.awt.FontMetrics
     */
    public AWTFontMetrics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    /**
     * Determines the font ascent of the Font described by this
     * FontMetrics object
     * @param family font family (java name) to use
     * @param style font style (java def.) to use
     * @param size font size
     * @return ascent in milliponts
     */
    public int getAscender(String family, int style, int size) {
        setFont(family, style, size);
        return FONT_FACTOR * fmt.getAscent();

//        // workaround for sun bug on FontMetrics.getAscent()
//        // http://developer.java.sun.com/developer/bugParade/bugs/4399887.html
//        int realAscent = fmt.getAscent()
//                         - (fmt.getDescent() + fmt.getLeading());
//        return FONT_FACTOR * realAscent;
        /*
         * Bug 4399887 has status Closed, not a bug.  The comments on the bug
         * are:
         * The submitter is incorrectly assuming that the string he has used
         * is displaying characters which represent those with the maximum
         * ascent in the font. If (for example) the unicode character
         * \u00c1 which is the A-acute character used in many European
         * languages is placed in the bodu of the "Wrong" string it can be
         * seen that the JDK is exactly correct in its determination of the
         * ascent of the font.
         * If the bounds of a particular string are interesting then the
         * Rectangle FontMetrics.getStringBounds(..) method can be called.
         * The y value of the rectangle is the offset from the origin
         * (baseline) apparently needed by the sample test program
         * 
         * xxxxx@xxxxx 2001-05-15
         */
    }


    /**
     * The size of a capital letter measured from the font's baseline
     * @param family font family
     * @param style font style
     * @param size font size
     * @return capital height in millipoints
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
     * @param size font size
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
     * @param size font size
     * @return font height in milliponts
     */
    public int getXHeight(String family, int style, int size) {
        setFont(family, style, size);
        return (FONT_FACTOR * xHeight);
    }

    /**
     * Returns width (in 1/1000ths of point size) of character at
     * code point i
     * @param  i the character for which to get the width
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return character width in millipoints
     */
    public int width(int i, String family, int style, int size) {
        int w;
        setFont(family, style, size);
        // the output seems to look a little better if the
        // space is rendered larger than given by
        // the FontMetrics object
        // TODO find out why
        if (i <= 32) {
            w = (int)(1.4 * fmt.charWidth(i) * FONT_FACTOR);
        } else {
            w = (fmt.charWidth(i) * FONT_FACTOR);
        }
        return w;
    }

    /**
     * Return widths (in 1/1000ths of point size) of all
     * characters
     * @param family font family (jave name) to use
     * @param style font style (jave def.) to use
     * @param size font size
     * @return array of character widths in millipoints
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
     * @param size font size
     * @return true if the font was changed, false otherwise
     */
    private boolean setFont(String family, int style, int size) {
        boolean changed = false;
        Rectangle2D rect;
        TextLayout layout;
        int s = (int)(size / 1000f);

        if (f1 == null) {
            f1 = new Font(family, style, s);
            fmt = graphics.getFontMetrics(f1);
            changed = true;
        } else {
            if ((this.style != style) || !this.family.equals(family)
                    || this.size != s) {
                if (family.equals(this.family)) {
                    f1 = f1.deriveFont(style, s);
                } else {
                    f1 = new Font(family, style, s);
                }
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






