/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.apache.fop.render.awt;

//FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.layout.FontState;

// Java
import java.util.Enumeration;
import java.util.Hashtable;
import java.awt.Component;
import java.awt.Font;


/**
* This class implements org.apache.fop.layout.FontMetric and
* is added to the hash table in FontInfo. It  deferes the
* actual calculation of the metrics to
* AWTFontMetrics.  It only keeps the java name and
* style as member varibles
*/

public class FontMetricsMapper implements org.apache.fop.layout.FontMetric {
    /**
     *  The first and last non space-character
     */
    private final static int FIRST_CHAR = 32;
    private final static int LAST_CHAR = 255;

    /**
    *  This is a AWTFontMetrics that does the real calculation.
    *  It is only one class that dynamically determines the font-size.
    */
    private static AWTFontMetrics metric = null;

    /**
    *  The java name of the font.
    #  Make the family name immutable.
    */
    private final String family;

    /**
    *   The java style of the font.
     #  Make the style immutable.
    */
    private final int style;

    /**
    *  Constructs a new Font-metrics.
    * @param family the family name of the font (java value)
    * @param style the java type style value of the font
    * @param parent  an AWT component - this is needed  so
    *                that we can get an instance of
    *                java.awt.FontMetrics
    */
    public FontMetricsMapper(String family, int style, Component parent) {
        this.family = family;
        this.style = style;
        if (metric == null)
            metric = new AWTFontMetrics(parent);
    }

    /**
     * Determines the font ascent of the Font described by this
     * FontMetrics object
     * @return ascent in milliponts
     */
    public int getAscender(int size) {
        return metric.getAscender(family, style, size);
    }


    /**
     * The size of a capital letter measured from the font's baseline
     */
    public int getCapHeight(int size) {
        return metric.getCapHeight(family, style, size);
    }

    /**
     * Determines the font descent of the Font described by this
     * FontMetrics object
     * @return descent in milliponts
     */
    public int getDescender(int size) {
        return metric.getDescender(family, style, size);
    }

    /**
     * Determines the typical font height of this
     * FontMetrics object
     * @return font height in milliponts
     */
    public int getXHeight(int size) {
        return metric.getXHeight(family, style, size);
    }


    public int getFirstChar() {
        return FIRST_CHAR;
    }

    public int getLastChar() {
        return LAST_CHAR;
    }

    /**
      * return width (in 1/1000ths of point size) of character at
      * code point i.
      */
    public int width(int i, int size) {
        return metric.width(i, family, style, size);
    }


    /**
      * return width (in 1/1000ths of point size) of all character
      */
    public int[] getWidths(int size) {
        return metric.getWidths(family, style, size);
    }

    /**
      * Gets a Font instance  of the Font that this
      * FontMetrics describes in the desired size.
      * @return font with the desired characeristics.
      */
    public Font getFont(int size) {
        return metric.getFont(family, style, size);
    }
}





