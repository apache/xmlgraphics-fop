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

package org.apache.fop.apps;

// Java
import java.io.OutputStream;

// XML
import org.xml.sax.helpers.DefaultHandler;
   
// FOP
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOTreeBuilder;

/**
 * Primary class that drives the overall FOP process.
 * <P>
 * JAXP is the standard method of embedding FOP in Java programs.
 * Please check our embedding page (http://xml.apache.org/fop/embedding.html)
 * for samples (these are also available within the distribution in 
 * FOP_DIR\examples\embedding)
 * <P>
 * Methods within FOUserAgent are available to customize portions of the
 * process.  Specific Renderer object can be specified, also ElementMappings
 * (which determine elements in the FO that can be processed) can be added.
 */
public class Driver implements Constants {

    /**
     * the render type code given by setRender
     */
    private int renderType = NOT_SET;

    /**
     * the stream to use to output the results of the renderer
     */
    private OutputStream stream = null;

    /**
     * The system resources that FOP will use
     */
    private FOUserAgent foUserAgent = null;

    /**
     * Constructor for use with already-created FOUserAgents
     * @param renderType the type of renderer to use.  Must be one of
     * <ul>
     * <li>Driver.RENDER_PDF</li>
     * <li>Driver.RENDER_AWT</li>
     * <li>Driver.RENDER_PRINT</li>
     * <li>Driver.RENDER_MIF</li>
     * <li>Driver.RENDER_XML</li>
     * <li>Driver.RENDER_PCL</li>
     * <li>Driver.RENDER_PS</li>
     * <li>Driver.RENDER_TXT</li>
     * <li>Driver.RENDER_SVG</li>
     * <li>Driver.RENDER_RTF</li>
     * </ul>
     * @param ua FOUserAgent object
     * @throws IllegalArgumentException if an unsupported renderer type was requested.
     */
    public Driver(int renderType, FOUserAgent ua) {
        if (renderType < Constants.RENDER_MIN_CONST 
            || renderType > Constants.RENDER_MAX_CONST) {
            throw new IllegalArgumentException(
                "Invalid render type #" + renderType);
        }

        this.renderType = renderType;

        foUserAgent = ua;
        if (foUserAgent == null) {
            foUserAgent = new FOUserAgent();
        }
    }

    /**
     * Constructor that creates a default FOUserAgent
     * @see org.apache.fop.apps.Driver#(int, FOUserAgent)
     */
    public Driver(int renderType) {
        this(renderType, new FOUserAgent());
    }

    /**
     * Get the FOUserAgent instance for this process
     * @return the user agent
     */
    public FOUserAgent getUserAgent() {
        return foUserAgent;
    }

    /**
     * Set the OutputStream to use to output the result of the Render
     * (if applicable)
     * @param stream the stream to output the result of rendering to
     */
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    /**
     * Returns a DefaultHandler object used to generate the document.
     * Note this object implements the ContentHandler interface.
     * For processing with a Transformer object, this DefaultHandler object
     * can be used in the SAXResult constructor.
     * Alternatively, for processing with a SAXParser, this object can be
     * used as the DefaultHandler argument to its parse() methods.
     *
     * @return a SAX DefaultHandler for handling the SAX events.
     * @throws FOPException if setting up the DefaultHandler fails
     */
    public DefaultHandler getDefaultHandler() throws FOPException {
        return new FOTreeBuilder(renderType, foUserAgent, stream);
    }
}
