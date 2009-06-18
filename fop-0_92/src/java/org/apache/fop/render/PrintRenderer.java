/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

package org.apache.fop.render;

// FOP
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.FontTriplet;

// Java
import java.awt.Color;
import java.util.List;

/** Abstract base class of "Print" type renderers.  */
public abstract class PrintRenderer extends AbstractRenderer {

    /** Font configuration */
    protected FontInfo fontInfo;

    /** list of fonts */
    protected List fontList = null;

    /**
     * Set up the font info
     *
     * @param inFontInfo  font info to set up
     */
    public void setupFontInfo(FontInfo inFontInfo) {
        this.fontInfo = inFontInfo;
        FontResolver resolver = new DefaultFontResolver(userAgent);
        FontSetup.setup(fontInfo, fontList, resolver, 
                userAgent.getFactory().isBase14KerningEnabled());
    }

    /**
     * Returns the internal font key fot a font triplet coming from the area tree
     * @param area the area from which to retrieve the font triplet information
     * @return the internal font key (F1, F2 etc.) or null if not found
     */
    protected String getInternalFontNameForArea(Area area) {
        FontTriplet triplet = (FontTriplet)area.getTrait(Trait.FONT);
        return fontInfo.getInternalFontKey(triplet);
    }
    
    /**
     * Returns a Font object constructed based on the font traits in an area
     * @param area the area from which to retrieve the font triplet information
     * @return the requested Font instance or null if not found
     */
    protected Font getFontFromArea(Area area) {
        FontTriplet triplet = (FontTriplet)area.getTrait(Trait.FONT);
        int size = ((Integer)area.getTrait(Trait.FONT_SIZE)).intValue();
        return fontInfo.getFontInstance(triplet, size);
    }
    
    /**
     * Lightens up a color for groove, ridge, inset and outset border effects.
     * @param col the color to lighten up
     * @param factor factor by which to lighten up (negative values darken the color)
     * @return the modified color
     */
    protected Color lightenColor(Color col, float factor) {
        float[] cols = new float[3];
        cols = col.getColorComponents(cols);
        if (factor > 0) {
            cols[0] += (1.0 - cols[0]) * factor;
            cols[1] += (1.0 - cols[1]) * factor;
            cols[2] += (1.0 - cols[2]) * factor;
        } else {
            cols[0] -= cols[0] * -factor;
            cols[1] -= cols[1] * -factor;
            cols[2] -= cols[2] * -factor;
        }
        return new Color(cols[0], cols[1], cols[2]);
    }

}
