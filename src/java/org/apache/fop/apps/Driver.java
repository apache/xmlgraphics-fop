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
import java.io.IOException;
import java.io.OutputStream;

// XML
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
 * If necessary, calling classes can call into the lower level
 * methods to setup and
 * render. Methods within FOUserAgent can be called to set the
 * Renderer to use, the (possibly multiple) ElementMapping(s) to
 * use and the OutputStream to use to output the results of the
 * rendering (where applicable).
 * <P>
 * Here is an example use of Driver which outputs to AWT:
 *
 * <PRE>
 * Driver driver = new Driver();
 * driver.setRenderer(RENDER_AWT);
 * driver.render(parser, fileInputSource(args[0]));
 * </PRE>
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
     * Main constructor for the Driver class.
     */
    public Driver() {
        foUserAgent = new FOUserAgent();
    }

    /**
     * Convenience constructor for directly setting input and output.
     * @param stream Target output stream
     */
    public Driver(OutputStream stream) {
        this();
        this.stream = stream;
    }

    /**
     * Constructor with FOUserAgent
     * Used by CLI, AWTRenderer
     */
    public Driver(FOUserAgent ua) {
        foUserAgent = ua;
    }

    /**
     * Optionally sets the FOUserAgent instance for FOP to use. The Driver
     * class sets up its own FOUserAgent if none is set through this method.
     * @param agent FOUserAgent to use
     */
    public void setUserAgent(FOUserAgent agent) throws FOPException {
        foUserAgent = agent;
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
     * Method to set the rendering type desired. Must be one of
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
     * @param renderType the type of renderer to use
     * @throws IllegalArgumentException if an unsupported renderer type was requested.
     */
    public void setRenderer(int renderType) throws IllegalArgumentException {
        if (renderType < RENDER_MIN_CONST || renderType > RENDER_MAX_CONST) {
            renderType = NOT_SET;
            throw new IllegalArgumentException(
                "Invalid render ID#" + renderType);
        }

        this.renderType = renderType;
    }

    /**
     * Determines which SAX ContentHandler is appropriate for the renderType.
     * Structure renderers (e.g. MIF & RTF) each have a specialized
     * ContentHandler that directly place data into the output stream. Layout
     * renderers (e.g. PDF & PostScript) use a ContentHandler that builds an FO
     * Tree.
     * @return a SAX ContentHandler for handling the SAX events.
     * @throws FOPException if setting up the ContentHandler fails
     */
    public ContentHandler getContentHandler() throws FOPException {
        return new FOTreeBuilder(renderType, foUserAgent, stream);
    }

    /**
     * Render the FO document read by a SAX Parser from an InputHandler
     * @param inputHandler the input handler containing the source and
     * parser information.
     * @throws FOPException if anything goes wrong.
     */
    public synchronized void render(InputHandler inputHandler)
                throws FOPException {
        XMLReader parser = inputHandler.getParser();
        foUserAgent.setBaseURL(inputHandler.getBaseURL());
        render(parser, inputHandler.getInputSource());
    }

    /**
     * This is the main render() method. The other render() methods are for
     * convenience, and normalize to this form, then run this.
     * Renders the FO document read by a SAX Parser from an InputSource.
     * For versions not needing an FO Tree (e.g., Alt-Design), override this.
     *
     * @param parser the SAX parser.
     * @param source the input source the parser reads from.
     * @throws FOPException if anything goes wrong.
     */
    public synchronized void render(XMLReader parser, InputSource source)
                throws FOPException {
        parser.setContentHandler(getContentHandler());

        try {
            /**
             The following statement triggers virtually all of the processing
             for this document. The SAX parser fires events that are handled by
             the appropriate InputHandler object, which means that you will need
             to look in those objects to see where FOP picks up control of
             processing again. For Structure Renderers (e.g. MIF & RTF), the SAX
             events are handled directly. For Layout Renderers (e.g. PDF &
             PostScript), an Area Tree is managed by the AreaTreeHandler.
             */
            parser.parse(source);
        } catch (SAXException e) {
            if (e.getException() instanceof FOPException) {
                // Undo exception tunneling.
                throw (FOPException)e.getException();
            } else {
                throw new FOPException(e);
            }
        } catch (IOException e) {
            throw new FOPException(e);
        }
    }
}
