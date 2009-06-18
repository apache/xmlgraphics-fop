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

package org.apache.fop.render;

// FOP
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.w3c.dom.Document;

// Java
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

// aXSL
import org.axsl.fontR.FontConsumer;

/** Abstract base class of "Print" type renderers.  */
public abstract class PrintRenderer extends AbstractRenderer {

    /** Font consumer */
    protected FontConsumer fontConsumer;

    /**
     * Set up the font consumer
     *
     * @param inFontConsumer  font consumer to set up
     */
    public void setupFontConsumer(FontConsumer inFontConsumer) {
        this.fontConsumer = inFontConsumer;
        // TODO vh: adaptation needed
//        FontResolver resolver = new DefaultFontResolver(userAgent);
//        FontSetup.setup(fontInfo, fontList, resolver, 
//                userAgent.getFactory().isBase14KerningEnabled());
    }

    /**
     * Returns the internal font key fot a font triplet coming from the area tree
     * @param area the area from which to retrieve the font triplet information
     * @return the internal font key (F1, F2 etc.) or null if not found
     */
    // TODO vh: re-enable
    protected String getInternalFontNameForArea(Area area) {
    	return null;
//        FontTriplet triplet = (FontTriplet)area.getTrait(Trait.FONT);
//        return fontInfo.getInternalFontKey(triplet);
    }
    
    /**
     * Returns a Font object constructed based on the font traits in an area
     * @param area the area from which to retrieve the font triplet information
     * @return the requested Font instance or null if not found
     */
    // TODO vh: re-enable
//    protected Font getFontFromArea(Area area) {
//        FontTriplet triplet = (FontTriplet)area.getTrait(Trait.FONT);
//        int size = ((Integer)area.getTrait(Trait.FONT_SIZE)).intValue();
//        return fontInfo.getFontInstance(triplet, size);
//    }
    
    /**
     * Lightens up a color for groove, ridge, inset and outset border effects.
     * @param col the color to lighten up
     * @param factor factor by which to lighten up (negative values darken the color)
     * @return the modified color
     */
    public static Color lightenColor(Color col, float factor) {
        // TODO: This function converts the color into the sRGB namespace.
        // This should be avoided if possible.
        float[] cols = new float[4];
        cols = col.getRGBComponents(cols);
        if (factor > 0) {
            cols[0] += (1.0 - cols[0]) * factor;
            cols[1] += (1.0 - cols[1]) * factor;
            cols[2] += (1.0 - cols[2]) * factor;
        } else {
            cols[0] -= cols[0] * -factor;
            cols[1] -= cols[1] * -factor;
            cols[2] -= cols[2] * -factor;
        }
        return new Color(cols[0], cols[1], cols[2], cols[3]);
    }

    /**
     * Creates a RendererContext for an image.
     * @param x the x coordinate (in millipoints)
     * @param y the y coordinate (in millipoints)
     * @param width the width of the image (in millipoints)
     * @param height the height of the image (in millipoints)
     * @param foreignAttributes a Map or foreign attributes, may be null
     * @return the RendererContext
     */
    protected RendererContext createRendererContext(int x, int y, int width, int height, 
            Map foreignAttributes) {
        RendererContext context;
        context = new RendererContext(this, getMimeType());
        context.setUserAgent(userAgent);

        context.setProperty(RendererContextConstants.WIDTH,
                            new Integer(width));
        context.setProperty(RendererContextConstants.HEIGHT,
                            new Integer(height));
        context.setProperty(RendererContextConstants.XPOS,
                            new Integer(x));
        context.setProperty(RendererContextConstants.YPOS,
                            new Integer(y));
        context.setProperty(RendererContextConstants.PAGE_VIEWPORT, 
                            getCurrentPageViewport());
        if (foreignAttributes != null) {
            context.setProperty(RendererContextConstants.FOREIGN_ATTRIBUTES, foreignAttributes);
        }
        return context;
    }

    /**
     * Renders an XML document (SVG for example).
     * @param doc the DOM Document containing the XML document to be rendered
     * @param ns the namespace URI for the XML document
     * @param pos the position for the generated graphic/image
     * @param foreignAttributes the foreign attributes containing rendering hints, or null
     */
    public void renderDocument(Document doc, String ns, Rectangle2D pos, Map foreignAttributes) {
        int x = currentIPPosition + (int) pos.getX(); 
        int y = currentBPPosition + (int) pos.getY();
        int width = (int)pos.getWidth();
        int height = (int)pos.getHeight();
        RendererContext context = createRendererContext(x, y, width, height, foreignAttributes);
        
        renderXML(context, doc, ns);
    }
    
}
