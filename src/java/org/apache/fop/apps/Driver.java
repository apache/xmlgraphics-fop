/*
 * $Id$
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.apps;

import java.util.logging.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.FOUserAgent;
import org.apache.fop.configuration.SystemOptions;
import org.apache.fop.fo.FOTree;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.version.Version;
import org.apache.fop.xml.FoXmlSerialHandler;
import org.apache.fop.xml.Namespaces;
import org.apache.fop.xml.SyncedXmlEventsBuffer;
import org.apache.fop.xml.XmlEventReader;

/**
 * Sets up and runs serialized component threads.
 * XMLEventSource <=> FOTree <=> AreaTree ...
 * 
 * @author pbw
 */

public class Driver {

    /** private constant to indicate renderer was not defined.  */
    private static final int NOT_SET = 0;
    /** Render to PDF. OutputStream must be set */
    public static final int RENDER_PDF = 1;
    /** Render to a GUI window. No OutputStream neccessary */
    public static final int RENDER_AWT = 2;
    /** Render to MIF. OutputStream must be set */
    public static final int RENDER_MIF = 3;
    /** Render to XML. OutputStream must be set */
    public static final int RENDER_XML = 4;
    /** Render to PRINT. No OutputStream neccessary */
    public static final int RENDER_PRINT = 5;
    /** Render to PCL. OutputStream must be set */
    public static final int RENDER_PCL = 6;
    /** Render to Postscript. OutputStream must be set */
    public static final int RENDER_PS = 7;
    /** Render to Text. OutputStream must be set */
    public static final int RENDER_TXT = 8;
    /** Render to SVG. OutputStream must be set */
    public static final int RENDER_SVG = 9;
    /** Render to RTF. OutputStream must be set */
    public static final int RENDER_RTF = 10;

    protected Logger logger = Logger.getLogger(Fop.fopPackage);
    /** If true, full error stacks are reported */
    private boolean _errorDump = false;
    private Configuration configuration = null;
    private SystemOptions options = null;
    private FOUserAgent userAgent = null;
    
    private InputHandler inputHandler;
    private XMLReader parser;
    private InputSource saxSource;

    private FoXmlSerialHandler xmlhandler;
    private SyncedXmlEventsBuffer eventsBuffer;
    private Namespaces namespaces;
    private XmlEventReader eventReader;
    private FOTree foTree;
    private AreaTree areaTree = new AreaTree();

    private Thread driverThread;
    private Thread parserThread;
    private Thread foThread;
    private Thread areaThread;
    private Thread renderThread;

    /** the renderer type code given by setRenderer  */
    private int rendererType = NOT_SET;

    /** the renderer to use to output the area tree */
    private Renderer renderer;


    /**
     * Error handling, version and logging initialization.
     */
    public Driver() {
        String version = Version.getVersion();
        configuration = new Configuration();
        options = new SystemOptions(configuration);
        userAgent = new FOUserAgent();
        _errorDump = configuration.isTrue("debugMode");
        logger.config(version);
    }
    
    public Driver(Configuration config, SystemOptions options) {
        String version = Version.getVersion();
        configuration = config;
        this.options = options;
        userAgent = new FOUserAgent();
        _errorDump = configuration.isTrue("debugMode");
        logger.config(version);
    }

    /**
     * Sets up the environment and start processing threads.
     * The primary elements of the environment include:<br>
     * the input saxSource, the parser, the
     * {@link org.apache.fop.xml.SyncedXmlEventsBuffer SyncedXmlEventsBuffer}
     * (<code>eventsBuffer</code>), the
     * {@link org.apache.fop.xml.FoXmlSerialHandler FoXmlSerialHandler}
     * (<code>xmlhandler</code>) and the
     * {@link org.apache.fop.fo.FOTree FOTree} (<code>foTree</code>).
     * 
     * <p>The <code>xmlhandler</code> uses the saxSource and the parser to
     * generate XML events which it stores in <code>eventsBuffer</code>.
     * <code>FoXmlSerialHandler</code> implements <code>Runnable</code>.
     * 
     * <p>The <code>foTree</code> reads events from the <code>eventsBuffer</code>
     * buffer, which it interprets to build the FO tree.  <code>FOTree</code>
     * implements <code>Runnable</code>.
     * 
     * <p>The parser thread is passed the runnable <code>xmlhandler</code>.
     * When started, it scans the input, constructs and buffers events.  It
     * blocks when the buffer is full, and continues when notified that the
     * buffer has emptied.
     * <p>
     * The FO Tree builder thread is passed the runnable <code>foTree</code>,
     * which blocks on an empty <code>eventsBuffer</code> buffer, and continues
     * when notified that events are available in the buffer.
     * 
     * @throws FOPException
     */
    public void run () throws FOPException {
        setInputHandler(options.getInputHandler());
        parser = inputHandler.getParser();
        saxSource = inputHandler.getInputSource();
        // Setting of namespace-prefixes feature no longer required
        //setParserFeatures(parser);

        namespaces = new Namespaces();
        eventsBuffer = new SyncedXmlEventsBuffer(namespaces);
        eventReader = new XmlEventReader(eventsBuffer, namespaces);
        xmlhandler = new FoXmlSerialHandler(eventsBuffer, parser, saxSource);
        foTree = new FOTree(eventReader);

        driverThread = Thread.currentThread();
        foThread = new Thread(foTree, "FOTreeBuilder");
        foThread.setDaemon(true);
        parserThread = new Thread(xmlhandler, "XMLSerialHandler");
        parserThread.setDaemon(true);

        xmlhandler.setFoThread(foThread);
        foTree.setParserThread(parserThread);

        System.out.println("Starting parserThread");
        parserThread.start();
        System.out.println("parserThread started");
        foThread.start();
        System.out.println("foThread started");
        try {
            parserThread.join();
        } catch (InterruptedException e) {}
        //System.out.println("Joined to parser.");
        try {
            foThread.join();
        } catch (InterruptedException e) {}

    }

    /**
     * Sets the InputHandler for XML imput as specified in FOPOptions.
     * @param inputHandler the InputHandler
     */
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    protected FOUserAgent getUserAgent() {
        return userAgent;
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
//        case RENDER_PDF:
//            setRenderer("org.apache.fop.render.pdf.PDFRenderer");
//            break;
        case RENDER_AWT:
            throw new IllegalArgumentException("Use renderer form of setRenderer() for AWT");
//        case RENDER_PRINT:
//            setRenderer("org.apache.fop.render.awt.AWTPrintRenderer");
//            break;
//        case RENDER_PCL:
//            setRenderer("org.apache.fop.render.pcl.PCLRenderer");
//            break;
//        case RENDER_PS:
//            setRenderer("org.apache.fop.render.ps.PSRenderer");
//            break;
//        case RENDER_TXT:
//            setRenderer("org.apache.fop.render.txt.TXTRenderer()");
//            break;
//        case RENDER_MIF:
//            //foInputHandler will be set later
//            break;
//        case RENDER_XML:
//            setRenderer("org.apache.fop.render.xml.XMLRenderer");
//            break;
//        case RENDER_SVG:
//            setRenderer("org.apache.fop.render.svg.SVGRenderer");
//            break;
//        case RENDER_RTF:
//            //foInputHandler will be set later
//            break;
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
     * DOES NOTHING
     * TODO Eliminate or actualise
     */
    public synchronized void reset() {
    }

    /**
     * Render the FO document read by a SAX Parser from an InputHandler
     * @param inputHandler the input handler containing the source and
     * parser information.
     * @throws FOPException if anything goes wrong.
     */
    public synchronized void render(InputHandler inputHandler)
                throws FOPException {
        throw new FOPException("Attempting to run null 'render' method");
    }

    /**
     * Prints stack trace of an exception
     * @param e the exception to trace
     */
    public void dumpError(Exception e) {
        if (_errorDump) {
            if (e instanceof SAXException) {
                e.printStackTrace();
                if (((SAXException)e).getException() != null) {
                    ((SAXException)e).getException().printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }
    }

}

