/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.apps;

// FOP
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.FOTreeHandler;
import org.apache.fop.mif.MIFHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.rtf.renderer.RTFHandler;
import org.apache.fop.tools.DocumentInputSource;
import org.apache.fop.tools.DocumentReader;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

// DOM
import org.w3c.dom.Document;

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
 * driver.enableLogging(myLogger); //optional
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
 * driver.enableLogging(myLogger); //optional
 * driver.setRenderer(new org.apache.fop.render.awt.AWTRenderer(translator));
 * driver.render(parser, fileInputSource(args[0]));
 * </PRE>
 */
public class Driver implements LogEnabled {

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
    private int rendererType;

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
    private Logger log = null;
    private FOUserAgent userAgent = null;

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

    private FOUserAgent getUserAgent() {
        if (userAgent == null) {
            userAgent = new FOUserAgent();
            userAgent.enableLogging(getLogger());
            userAgent.setBaseURL("");
        }
        return userAgent;
    }

    /**
     * Provide the Driver instance with a logger. More information on Avalon
     * logging can be found at the
     * <a href="http://avalon.apache.org">Avalon site</a>.
     *
     * @param log the logger. Must not be <code>null</code>.
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(Logger)
     */
    public void enableLogging(Logger log) {
        if (this.log == null) {
            this.log = log;
        } else {
            getLogger().warn("Logger is already set! Won't use the new logger.");
        }
    }

    /**
     * Provide the Driver instance with a logger.
     * @param log the logger. Must not be <code>null</code>.
     * @deprecated Use #enableLogging(Logger) instead.
     */
    public void setLogger(Logger log) {
        enableLogging(log);
    }


    /**
     * Returns the logger for use by FOP.
     * @return the logger
     * @see #enableLogging(Logger)
     */
    protected Logger getLogger() {
        if (this.log == null) {
            this.log = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
            this.log.error("Logger not set. Using ConsoleLogger as default.");
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
            throw new IllegalArgumentException("Unknown renderer type");
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
     * Sets the renderer.
     * @param rendererClassName the fully qualified classname of the renderer
     * class to use.
     * @param version version number
     * @deprecated use renderer.setProducer(version) + setRenderer(renderer) or
     * just setRenderer(rendererType) which will use the default producer string.
     * @see #setRenderer(int)
     * @see #setRenderer(Renderer)
     */
    public void setRenderer(String rendererClassName, String version) {
        setRenderer(rendererClassName);
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
            renderer =
                (Renderer)Class.forName(rendererClassName).newInstance();
            if (renderer instanceof LogEnabled) {
                ((LogEnabled)renderer).enableLogging(getLogger());
            }
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
     */
    public ContentHandler getContentHandler() {
        if (!isInitialized()) {
            initialize();
        }

        if (rendererType != RENDER_PRINT && rendererType != RENDER_AWT) {
           validateOutputStream();
        }

        // TODO: - do this stuff in a better way
        // PIJ: I guess the structure handler should be created by the renderer.
        if (rendererType == RENDER_MIF) {
            foInputHandler = new MIFHandler(stream);
        } else if (rendererType == RENDER_RTF) {
            foInputHandler = new RTFHandler(stream);
        } else {
            if (renderer == null) {
                throw new IllegalStateException(
                        "Renderer not set when using standard foInputHandler");
            }
            foInputHandler = new FOTreeHandler(stream, renderer, true);
        }

        foInputHandler.enableLogging(getLogger());

        treeBuilder.setUserAgent(getUserAgent());
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
        if (!isInitialized()) {
            initialize();
        }
        parser.setContentHandler(getContentHandler());
        try {
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
     * @see render(XMLReader, InputSource)
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
        if (renderer == null) {
            setRenderer(RENDER_PDF);
        }

        if (source == null) {
            throw new FOPException("InputSource is not set.");
        }

        if (reader == null) {
            if (!(source instanceof DocumentInputSource)) {
                //TODO: (gm) rename to FOFileHandler or similar
                reader = org.apache.fop.apps.FOInputHandler.createParser();
            }
        }

        if (source instanceof DocumentInputSource) {
            render(((DocumentInputSource)source).getDocument());
        } else {
            render(reader, source);
        }
    }
}

