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

// FOP
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.RenderPagesModel;

import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FOTreeBuilder;

import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.FOTreeHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.render.mif.MIFHandler;
import org.apache.fop.render.rtf.RTFHandler;
import org.apache.fop.tools.DocumentInputSource;
import org.apache.fop.tools.DocumentReader;
import org.apache.fop.tools.ProxyContentHandler;
import org.apache.fop.layoutmgr.LayoutManagerLS;

import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.logging.Log;

// DOM
/* org.w3c.dom.Document is not imported to reduce confusion with
   org.apache.fop.control.Document */

// SAX
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

// Java
import java.io.IOException;
import java.io.OutputStream;

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
 * driver.setLogger(myLogger); //optional
 * driver.setRenderer(RENDER_PDF);
 * driver.run();
 * </PRE>
 * If neccessary, calling classes can call into the lower level
 * methods to setup and
 * render. Methods can be called to set the
 * Renderer to use, the (possibly multiple) ElementMapping(s) to
 * use and the OutputStream to use to output the results of the
 * rendering (where applicable). In the case of the Renderer and
 * ElementMapping(s), the Driver may be supplied either with the
 * object itself, or the name of the class, in which case Driver will
 * instantiate the class itself. The advantage of the latter is it
 * enables runtime determination of Renderer and ElementMapping(s).
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
 * driver.setLogger(myLogger); //optional
 * driver.setRenderer(new org.apache.fop.render.awt.AWTRenderer(translator));
 * driver.render(parser, fileInputSource(args[0]));
 * </PRE>
 */
public class Driver {

    /**
     * private constant to indicate renderer was not defined.
     */
    private static final int NOT_SET = 0;

    /**
     * Render to PDF. OutputStream must be set
     */
    public static final int RENDER_PDF = 1;

    /**
     * Render to a GUI window. No OutputStream neccessary
     */
    public static final int RENDER_AWT = 2;

    /**
     * Render to MIF. OutputStream must be set
     */
    public static final int RENDER_MIF = 3;

    /**
     * Render to XML. OutputStream must be set
     */
    public static final int RENDER_XML = 4;

    /**
     * Render to PRINT. No OutputStream neccessary
     */
    public static final int RENDER_PRINT = 5;

    /**
     * Render to PCL. OutputStream must be set
     */
    public static final int RENDER_PCL = 6;

    /**
     * Render to Postscript. OutputStream must be set
     */
    public static final int RENDER_PS = 7;

    /**
     * Render to Text. OutputStream must be set
     */
    public static final int RENDER_TXT = 8;

    /**
     * Render to SVG. OutputStream must be set
     */
    public static final int RENDER_SVG = 9;

    /**
     * Render to RTF. OutputStream must be set
     */
    public static final int RENDER_RTF = 10;

    /**
     * the FO tree builder
     */
    private FOTreeBuilder treeBuilder;

    /**
     * the renderer type code given by setRenderer
     */
    private int rendererType = NOT_SET;

    /**
     * the renderer to use to output the area tree
     */
    private Renderer renderer;

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
     * the system resources that FOP will use
     */
    private Log log = null;
    private FOUserAgent userAgent = null;

    private Document currentDocument = null;

    /**
     * Main constructor for the Driver class.
     */
    public Driver() {
        stream = null;
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
        treeBuilder.setUserAgent(getUserAgent());
    }

    /**
     * Optionally sets the FOUserAgent instance for FOP to use. The Driver
     * class sets up its own FOUserAgent if none is set through this method.
     * @param agent FOUserAgent to use
     */
    public void setUserAgent(FOUserAgent agent) {
        userAgent = agent;
    }

    public FOUserAgent getUserAgent() {
        if (userAgent == null) {
            userAgent = new FOUserAgent();
            userAgent.setLogger(getLogger());
        }
        return userAgent;
    }

    /**
     * Provide the Driver instance with a logger.
     * @param log the logger. Must not be <code>null</code>.
     */
    public void setLogger(Log log) {
        if (this.log == null) {
            this.log = log;
        } else {
            getLogger().warn("Logger is already set! Won't use the new logger.");
        }
    }


    /**
     * Returns the logger for use by FOP.
     * @return the logger
     */
    public Log getLogger() {
        if (this.log == null) {
            // use SimpleLog as default when logger not explicitly set
            log = new SimpleLog("FOP");
            ((SimpleLog) log).setLevel(SimpleLog.LOG_LEVEL_INFO);
        }

        return this.log;
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
     * Indicates whether FOP has already received input data.
     * @return true, if input data was received
     */
    public boolean hasData() {
        return (treeBuilder.hasData());
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
     * Shortcut to set the rendering type to use. Must be one of
     * <ul>
     * <li>RENDER_PDF</li>
     * <li>RENDER_AWT</li>
     * <li>RENDER_PRINT</li>
     * <li>RENDER_MIF</li>
     * <li>RENDER_XML</li>
     * <li>RENDER_PCL</li>
     * <li>RENDER_PS</li>
     * <li>RENDER_TXT</li>
     * <li>RENDER_SVG</li>
     * <li>RENDER_RTF</li>
     * </ul>
     * @param renderer the type of renderer to use
     * @throws IllegalArgumentException if an unsupported renderer type was required.
     */
    public void setRenderer(int renderer) throws IllegalArgumentException {
        rendererType = renderer;
        switch (renderer) {
        case RENDER_PDF:
            setRenderer("org.apache.fop.render.pdf.PDFRenderer");
            break;
        case RENDER_AWT:
            throw new IllegalArgumentException("Use renderer form of setRenderer() for AWT");
        case RENDER_PRINT:
            setRenderer("org.apache.fop.render.awt.AWTPrintRenderer");
            break;
        case RENDER_PCL:
            setRenderer("org.apache.fop.render.pcl.PCLRenderer");
            break;
        case RENDER_PS:
            setRenderer("org.apache.fop.render.ps.PSRenderer");
            break;
        case RENDER_TXT:
            setRenderer("org.apache.fop.render.txt.TXTRenderer()");
            break;
        case RENDER_MIF:
            //foInputHandler will be set later
            break;
        case RENDER_XML:
            setRenderer("org.apache.fop.render.xml.XMLRenderer");
            break;
        case RENDER_SVG:
            setRenderer("org.apache.fop.render.svg.SVGRenderer");
            break;
        case RENDER_RTF:
            //foInputHandler will be set later
            break;
        default:
            rendererType = NOT_SET;
            throw new IllegalArgumentException("Unknown renderer type " + renderer);
        }
    }

    /**
     * Set the Renderer to use.
     * @param renderer the renderer instance to use (Note: Logger must be set at this point)
     */
    public void setRenderer(Renderer renderer) {
        // AWTStarter calls this function directly
        if (renderer instanceof AWTRenderer) {
            rendererType = RENDER_AWT;
        }
        renderer.setProducer(Version.getVersion());
        renderer.setUserAgent(getUserAgent());
        this.renderer = renderer;
    }

    /**
     * Returns the currently active renderer.
     * @return the renderer
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Set the class name of the Renderer to use as well as the
     * producer string for those renderers that can make use of it.
     * @param rendererClassName classname of the renderer to use such as
     * "org.apache.fop.render.pdf.PDFRenderer"
     * @exception IllegalArgumentException if the classname was invalid.
     * @see #setRenderer(int)
     */
    public void setRenderer(String rendererClassName)
                throws IllegalArgumentException {
        try {
            renderer = (Renderer)Class.forName(rendererClassName).newInstance();
            renderer.setLogger(getLogger());
            renderer.setProducer(Version.getVersion());
            renderer.setUserAgent(getUserAgent());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + rendererClassName);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + rendererClassName);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + rendererClassName);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(rendererClassName
                                               + " is not a renderer");
        }
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

        /** Document creation is hard-wired for now, but needs to be made
         accessible through the API and/or configuration */
        if (currentDocument == null) {
            currentDocument = new Document(this);
        }

        // TODO: - do this stuff in a better way
        // PIJ: I guess the structure handler should be created by the renderer.
        if (rendererType == RENDER_MIF) {
            foInputHandler = new MIFHandler(currentDocument, stream);
        } else if (rendererType == RENDER_RTF) {
            foInputHandler = new RTFHandler(currentDocument, stream);
        } else {
            if (renderer == null) {
                throw new IllegalStateException(
                        "Renderer not set when using standard foInputHandler");
            }
            foInputHandler = new FOTreeHandler(currentDocument, true);
            currentDocument.areaTree = new AreaTree(currentDocument);
            currentDocument.atModel = new RenderPagesModel(renderer);
            //this.atModel = new CachedRenderPagesModel(renderer);
            currentDocument.areaTree.setTreeModel(currentDocument.atModel);
            try {
                renderer.setupFontInfo(currentDocument.getFontInfo());
                // check that the "any,normal,400" font exists
                if (!currentDocument.getFontInfo().isSetupValid()) {
                    throw new FOPException(
                            "No default font defined by OutputConverter");
                }
                renderer.startRenderer(stream);
            } catch (IOException e) {
                throw new FOPException(e);
            }
        }
        currentDocument.foInputHandler = foInputHandler;
        /** LayoutStrategy is hard-wired for now, but needs to be made
        accessible through the API and/or configuration */
        if (foInputHandler instanceof FOTreeHandler) {
            if (currentDocument.getLayoutStrategy() == null) {
                currentDocument.setLayoutStrategy(new LayoutManagerLS(currentDocument));
            }
        }

        foInputHandler.setLogger(getLogger());

        treeBuilder.setUserAgent(getUserAgent());
        treeBuilder.setFOInputHandler(foInputHandler);
        treeBuilder.setFOTreeControl(currentDocument);

        return new ProxyContentHandler(treeBuilder) {
            
            public void startDocument() throws SAXException {
                if (foInputHandler instanceof FOTreeHandler) {
                    FOTreeHandler foTreeHandler = (FOTreeHandler)foInputHandler;
                    foTreeHandler.addFOTreeListener(currentDocument);
                }
                super.startDocument();
            }
                
            public void endDocument() throws SAXException {
                super.endDocument();
                if (foInputHandler instanceof FOTreeHandler) {
                    FOTreeHandler foTreeHandler = (FOTreeHandler)foInputHandler;
                    foTreeHandler.removeFOTreeListener(currentDocument);
                }
            }
                
        };
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
        userAgent.setBaseURL(inputHandler.getBaseURL());
        render(parser, inputHandler.getInputSource());
    }

    /**
     * This is the main render() method. The other render() methods are for
     * convenience, and normalize to this form, then run this.
     * Renders the FO document read by a SAX Parser from an InputSource.
     * @param parser the SAX parser.
     * @param source the input source the parser reads from.
     * @throws FOPException if anything goes wrong.
     */
    public synchronized void render(XMLReader parser, InputSource source)
                throws FOPException {
        parser.setContentHandler(getContentHandler());

        /**
         * The following statement handles the case of a LayoutStrategy that
         * does not wish to build an FO Tree, but wishes to parse the incoming
         * document some other way. This applies primarily to the alt-design
         * system.
         */
        if (currentDocument.getLayoutStrategy() != null) {
            if (!currentDocument.getLayoutStrategy().foTreeNeeded()) {
                currentDocument.getLayoutStrategy().format(null, null);
                return;
            }
        }

        /**
         * For all other cases, we wish to parse normally.
         */
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
    public synchronized void render(org.w3c.dom.Document document)
                throws FOPException {
        DocumentInputSource source = new DocumentInputSource(document);
        DocumentReader reader = new DocumentReader();
        render(reader, source);
    }

    /**
     * Runs the formatting and renderering process using the previously set
     * parser, input source, renderer and output stream.
     * If the renderer was not set, default to PDF.
     * If no parser was set, and the input source is not a dom document,
     * get a default SAX parser.
     * @throws IOException in case of IO errors.
     * @throws FOPException if anything else goes wrong.
     */
    public synchronized void run() throws IOException, FOPException {
        if (!isInitialized()) {
            initialize();
        }

        if (renderer == null && rendererType != RENDER_RTF
            && rendererType != RENDER_MIF) {
                setRenderer(RENDER_PDF);
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

    /**
     * Public accessor for setting the currentDocument to process.
     * @param document the Document object that should be processed.
     */
    public void setCurrentDocument(Document document) {
        currentDocument = document;
    }

    /**
     * Public accessor for getting the currentDocument
     * @return the currentDocument
     */
    public Document getCurrentDocument() {
       return currentDocument;
    }

}
