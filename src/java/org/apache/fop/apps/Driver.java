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
import org.w3c.dom.Document;

// FOP
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.FOTreeHandler;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.render.mif.MIFHandler;
import org.apache.fop.render.rtf.RTFHandler;
import org.apache.fop.tools.DocumentInputSource;
import org.apache.fop.tools.DocumentReader;

/**
 * Primary class that drives overall FOP process.
 * <P>
 * The simplest way to use this is to instantiate it with the
 * InputSource and OutputStream, then set the renderer desired, and
 * calling run();
 * <P>
 * Here is an example use of Driver which outputs PDF:
 *
 * <PRE>
 * Driver driver = new Driver(new InputSource (args[0]),
 * new FileOutputStream(args[1]));
 * driver.setRenderer(RENDER_PDF);
 * driver.run();
 * </PRE>
 * If necessary, calling classes can call into the lower level
 * methods to setup and
 * render. Methods within FOUserAgent can be called to set the
 * Renderer to use, the (possibly multiple) ElementMapping(s) to
 * use and the OutputStream to use to output the results of the
 * rendering (where applicable). In the case of 
 * ElementMapping(s), the Driver may be supplied either with the
 * object itself, or the name of the class, in which case Driver will
 * instantiate the class itself. The advantage of the latter is it
 * enables runtime determination of ElementMapping(s).
 * <P>
 * Once the Driver is set up, the render method
 * is called. Depending on whether DOM or SAX is being used, the
 * invocation of the method is either render(Document) or
 * buildFOTree(Parser, InputSource) respectively.
 * <P>
 * A third possibility may be used to build the FO Tree, namely
 * calling getContentHandler() and firing the SAX events yourself.
 * <P>
 * Once the FO Tree is built, the format() and render() methods may be
 * called in that order.
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
     * the FO tree builder
     */
    private FOTreeBuilder treeBuilder;

    /**
     * the renderer type code given by setRenderer
     */
    private int rendererType = NOT_SET;

    /**
     * the SAX ContentHandler
     */
    private FOInputHandler foInputHandler;

    /**
     * the source of the FO file
     */
    private InputSource source;

    /**
     * the stream to use to output the results of the renderer
     */
    private OutputStream stream;

    /**
     * The XML parser to use when building the FO tree
     */
    private XMLReader reader;

    /**
     * The system resources that FOP will use
     */
    private FOUserAgent foUserAgent = null;

    /**
     * Main constructor for the Driver class.
     */
    public Driver() {
        stream = null;
    }

    /**
     * Constructor for AWTRenderer, which reuses the
     * same renderer instance for document reloading
     */
    public Driver(AWTRenderer renderer) {
        this();
        rendererType = RENDER_AWT;
        foUserAgent = renderer.getUserAgent();
    }

    /**
     * Convenience constructor for directly setting input and output.
     * @param source InputSource to take the XSL-FO input from
     * @param stream Target output stream
     */
    public Driver(InputSource source, OutputStream stream) {
        this();
        this.source = source;
        this.stream = stream;
    }

    private boolean isInitialized() {
        return (treeBuilder != null);
    }

    /**
     * Initializes the Driver object.
     */
    public void initialize() {
        if (isInitialized()) {
            throw new IllegalStateException("Driver already initialized");
        }
        treeBuilder = new FOTreeBuilder();
    }

    /**
     * Resets the Driver so it can be reused. Property and element
     * mappings are reset to defaults.
     * The output stream is cleared. The renderer is cleared.
     */
    public synchronized void reset() {
        source = null;
        stream = null;
        reader = null;
        if (treeBuilder != null) {
            treeBuilder.reset();
        }
    }

    /**
     * Optionally sets the FOUserAgent instance for FOP to use. The Driver
     * class sets up its own FOUserAgent if none is set through this method.
     * @param agent FOUserAgent to use
     */
    public void setUserAgent(FOUserAgent agent) throws FOPException {
        if (foUserAgent != null) {
            throw new IllegalStateException("FOUserAgent " +
                "instance already set.");
        }
        foUserAgent = agent;
    }

    /**
     * Get the FOUserAgent instance for this process
     * @return the user agent
     */
    public FOUserAgent getUserAgent() {
        if (foUserAgent == null) {
            foUserAgent = new FOUserAgent();
        }
        return foUserAgent;
    }

    /**
     * Set the OutputStream to use to output the result of the Renderer
     * (if applicable)
     * @param stream the stream to output the result of rendering to
     */
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    private void validateOutputStream() {
        if (this.stream == null) {
            throw new IllegalStateException("OutputStream has not been set");
        }
    }

    /**
     * Set the source for the FO document. This can be a normal SAX
     * InputSource, or an DocumentInputSource containing a DOM document.
     * @see DocumentInputSource
     */
    public void setInputSource(InputSource source) {
        this.source = source;
    }

    /**
     * Sets the reader used when reading in the source. If not set,
     * this defaults to a basic SAX parser.
     * @param reader the reader to use.
     */
    public void setXMLReader(XMLReader reader) {
        this.reader = reader;
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
            rendererType = NOT_SET;
            throw new IllegalArgumentException(
                "Invalid renderer ID#" + renderType);
        }

        this.rendererType = renderType;
    }

    /**
     * Add the given element mapping.
     * An element mapping maps element names to Java classes.
     *
     * @param mapping the element mappingto add
     */
    public void addElementMapping(ElementMapping mapping) {
        treeBuilder.addElementMapping(mapping);
    }

    /**
     * Add the element mapping with the given class name.
     * @param mappingClassName the class name representing the element mapping.
     */
    public void addElementMapping(String mappingClassName) {
        treeBuilder.addElementMapping(mappingClassName);
    }

    /**
     * Determines which SAX ContentHandler is appropriate for the rendererType.
     * Structure renderers (e.g. MIF & RTF) each have a specialized
     * ContentHandler that directly place data into the output stream. Layout
     * renderers (e.g. PDF & PostScript) use a ContentHandler that builds an FO
     * Tree.
     * @return a SAX ContentHandler for handling the SAX events.
     * @throws FOPException if setting up the ContentHandler fails
     */
    public ContentHandler getContentHandler() throws FOPException {
        if (!isInitialized()) {
            initialize();
        }

        if (rendererType != RENDER_PRINT && rendererType != RENDER_AWT) {
           validateOutputStream();
        }

        // TODO: - do this stuff in a better way
        // PIJ: I guess the structure handler should be created by the renderer.
        if (rendererType == RENDER_MIF) {
            foInputHandler = new MIFHandler(foUserAgent, stream);
        } else if (rendererType == RENDER_RTF) {
            foInputHandler = new RTFHandler(foUserAgent, stream);
        } else {
            if (rendererType == NOT_SET) {
                throw new IllegalStateException(
                        "Renderer must be set using setRenderer(int renderType)");
            }

            foInputHandler = new FOTreeHandler(foUserAgent, rendererType, 
                stream, true);
        }

        treeBuilder.setFOInputHandler(foInputHandler);
        return treeBuilder;
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
             PostScript), an FO Tree is built by the FOTreeHandler, which in
             turn fires events when a PageSequence object is complete. This
             allows higher-level control objects (such as this class) to work
             directly with PageSequence objects. See foPageSequenceComplete()
             where this level of control is implemented.
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

    /**
     * This method overloads the main render() method, adding the convenience
     * of using a DOM Document as input.
     * @see #render(XMLReader, InputSource)
     * @param document the DOM document to read from
     * @throws FOPException if anything goes wrong.
     */
    public synchronized void render(Document document)
                throws FOPException {
        DocumentInputSource source = new DocumentInputSource(document);
        DocumentReader reader = new DocumentReader();
        render(reader, source);
    }

    /**
     * Runs the formatting and renderering process using the previously set
     * parser, input source, renderer and output stream.
     * If no parser was set, and the input source is not a dom document,
     * get a default SAX parser.
     * @throws IOException in case of IO errors.
     * @throws FOPException if anything else goes wrong.
     */
    public synchronized void run() throws IOException, FOPException {
        if (!isInitialized()) {
            initialize();
        }

        if (rendererType == NOT_SET) {
            rendererType = RENDER_PDF;
        }

        if (source == null) {
            throw new FOPException("InputSource is not set.");
        }

        if (reader == null) {
            if (!(source instanceof DocumentInputSource)) {
                reader = FOFileHandler.createParser();
            }
        }

        if (source instanceof DocumentInputSource) {
            render(((DocumentInputSource)source).getDocument());
        } else {
            render(reader, source);
        }
    }
}
