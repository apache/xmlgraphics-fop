/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import java.io.OutputStream;

//Avalon
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.render.mif.MIFHandler;
import org.apache.fop.render.rtf.RTFHandler;

/**
 * Factory for FOEventHandlers and Renderers.
 */
public class RendererFactory {
    
    /**
     * Creates a Renderer object based on render-type desired
     * @param renderType the type of renderer to use
     * @return the new Renderer instance
     * @throws IllegalArgumentException if an unsupported renderer type was requested
     */
    private static Renderer newInstance(int renderType) throws IllegalArgumentException {

        switch (renderType) {
        case Constants.RENDER_PDF:
            return new org.apache.fop.render.pdf.PDFRenderer();
        case Constants.RENDER_AWT:
            return new org.apache.fop.render.awt.AWTRenderer();
        case Constants.RENDER_PRINT:
            return new org.apache.fop.render.awt.AWTPrintRenderer();
        case Constants.RENDER_PCL:
            return new org.apache.fop.render.pcl.PCLRenderer();
        case Constants.RENDER_PS:
            return new org.apache.fop.render.ps.PSRenderer();
        case Constants.RENDER_TXT:
            return new org.apache.fop.render.txt.TXTRenderer();
        case Constants.RENDER_XML:
            return new org.apache.fop.render.xml.XMLRenderer();
        case Constants.RENDER_SVG:
            return new org.apache.fop.render.svg.SVGRenderer();
        default:
            throw new IllegalArgumentException("Invalid renderer type " 
                + renderType);
        }
    }

    /**
     * Creates a Renderer object based on render-type desired
     * @param userAgent the user agent for access to configuration
     * @param renderType the type of renderer to use
     * @return the new Renderer instance
     * @throws FOPException if the renderer cannot be properly constructed
     */
    public static Renderer createRenderer(FOUserAgent userAgent, int renderType) 
                    throws FOPException {
        if (userAgent.getRendererOverride() != null) {
            return userAgent.getRendererOverride();
        } else {
            Renderer rend = newInstance(renderType);
            rend.setUserAgent(userAgent);
            String mimeType = rend.getMimeType();
            Configuration userRendererConfig = null;
            if (mimeType != null) {
                userRendererConfig
                    = userAgent.getUserRendererConfig(mimeType);
            }
            if (userRendererConfig != null) {
                try {
                    ContainerUtil.configure(rend, userRendererConfig);
                } catch (ConfigurationException e) {
                    throw new FOPException(e);
                }
            }
            return rend;
        }
    }
    
    
    /**
     * Creates FOEventHandler instances based on the desired output.
     * @param userAgent the user agent for access to configuration
     * @param renderType the type of renderer to use
     * @param out the OutputStream where the output is written to (if applicable)
     * @return the newly constructed FOEventHandler
     * @throws FOPException if the FOEventHandler cannot be properly constructed
     */
    public static FOEventHandler createFOEventHandler(FOUserAgent userAgent, 
                int renderType, OutputStream out) throws FOPException {

        if (userAgent.getFOEventHandlerOverride() != null) {
            return userAgent.getFOEventHandlerOverride();
        } else {
            if (renderType != Constants.RENDER_PRINT 
                    && renderType != Constants.RENDER_AWT) {
                if (out == null && userAgent.getRendererOverride() == null) {
                    throw new IllegalStateException(
                        "OutputStream has not been set");
                }
            }
                    
            if (renderType == Constants.RENDER_MIF) {
                return new MIFHandler(userAgent, out);
            } else if (renderType == Constants.RENDER_RTF) {
                return new RTFHandler(userAgent, out);
            } else {
                if (renderType < Constants.RENDER_MIN_CONST 
                    || renderType > Constants.RENDER_MAX_CONST) {
                    throw new IllegalArgumentException(
                        "Invalid render ID#" + renderType);
                }
    
                return new AreaTreeHandler(userAgent, renderType, out);
            }
        }
    }
}
