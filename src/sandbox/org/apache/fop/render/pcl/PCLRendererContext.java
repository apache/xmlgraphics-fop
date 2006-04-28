/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.render.pcl;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.render.RendererContext;
import org.apache.fop.util.QName;

/**
 * Wrapper on the RendererContext to access the information structure for drawing 
 * the XML document.
 */
public class PCLRendererContext {

    private RendererContext context;
    
    /**
     * Wrap the render context to allow easier access to its values.
     *
     * @param context the renderer context
     * @return the PCL-specific renderer context wrapper
     */
    public static PCLRendererContext wrapRendererContext(RendererContext context) {
        PCLRendererContext pcli = new PCLRendererContext(context);
        return pcli;
    }

    /**
     * Main constructor
     * @param context the RendererContent instance
     */
    public PCLRendererContext(RendererContext context) {
        this.context = context;
    }
    
    /** @return the currentXPosition */
    public int getCurrentXPosition() {
        return ((Integer)context.getProperty(PCLSVGHandler.XPOS)).intValue();
    }

    /** @return the currentYPosition */
    public int getCurrentYPosition() {
        return ((Integer)context.getProperty(PCLSVGHandler.YPOS)).intValue();
    }

    /** @return the width of the image */
    public int getWidth() {
        return ((Integer)context.getProperty(PCLSVGHandler.WIDTH)).intValue();
    }

    /** @return the height of the image */
    public int getHeight() {
        return ((Integer)context.getProperty(PCLSVGHandler.HEIGHT)).intValue();
    }

    /** @return the handler configuration */
    public Configuration getHandlerConfiguration() {
        return (Configuration)context.getProperty(PCLSVGHandler.HANDLER_CONFIGURATION);
    }

    /** @return the foreign attributes */
    public Map getForeignAttributes() {
        return (Map)context.getProperty(PCLSVGHandler.FOREIGN_ATTRIBUTES);
    }
    
    /** @return true if the SVG image should be rendered as a bitmap */
    public boolean paintAsBitmap() {
        QName qName = new QName(ExtensionElementMapping.URI, null, "conversion-mode");
        return getForeignAttributes() != null 
             && "bitmap".equals(getForeignAttributes().get(qName));
    }

}