/*
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
 * $Id$
 */

package org.apache.fop.apps;

//import java.util.logging.Handler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.fop.area.AreaTree;
import org.apache.fop.configuration.CLI_Options;
import org.apache.fop.configuration.ConfigurationResource;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.FOUserAgent;
import org.apache.fop.configuration.SystemOptions;
import org.apache.fop.configuration.UserOptions;
import org.apache.fop.fo.FOTree;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.version.Version;
import org.apache.fop.xml.FoXmlSerialHandler;
import org.apache.fop.xml.Namespaces;
import org.apache.fop.xml.SyncedXmlEventsBuffer;
import org.apache.fop.xml.XmlEventReader;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class Fop {

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

    public static Runtime runtime;
    public static long startTotal;
    public static long startFree;
    public static long startTime;
    public static long startPCi;
    public static long endPCi;
    private long endtotal, endfree, gctotal, gcfree;
    
    /** The top-level package for FOP */
    public static final String fopPackage = "org.apache.fop";
    /** The logging object */
    private Logger logger;
    /** Configuration data */
    public Configuration configuration = null;
    /** The version string */
    public final String version = Version.getVersion();
    /** The version revision string */
    public final String revision = Version.getRevision();
    /** The version name string */
    public final String name = Version.getName();

    protected Properties properties = null;
    protected SystemOptions options = null;
    protected UserOptions userOptions = null;
    protected CLI_Options cliOptions = null;
    protected FOUserAgent userAgent = null;

    public static void main(String[] args) {
        Fop fopInstance = new Fop(args);
        fopInstance.run();
    }

    /**
     * Gets the parser Class name.
     * 
     * @return a String with the value of the property
     * <code>org.xml.sax.parser</code> or the default value
     * <code>org.apache.xerces.parsers.SAXParser</code>.
     */
    public static final String getParserClassName() {
        String parserClassName = null;
        try {
            parserClassName = System.getProperty("org.xml.sax.parser");
        } catch (SecurityException se) {}

        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
        return parserClassName;
    }
    
    String[] args = null;

    public Fop() {
        setup();
    }
    
    public Fop(String[] args) {
        this.args = args;
        setup();
    }

    public Fop(Configuration config, SystemOptions options) {
        configuration = config;
        this.options = options;
        if (options instanceof CLI_Options) {
            cliOptions = (CLI_Options)options;
        } else {
            if (options instanceof UserOptions) {
                userOptions = (UserOptions)options;
            }
        }
    }
    private void setup() {
        try {
            // Get the initial system properties
            InputStream propsfile =
                ConfigurationResource.getResourceFile(
                        "conf/fop.system.properties", Fop.class);
            properties = new Properties();
            properties.load(propsfile);
        } catch (FOPException e1) {
            throw new RuntimeException(e1);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        Enumeration props = properties.keys();
        while (props.hasMoreElements()) {
            String key = (String)(props.nextElement());
            System.setProperty(key, properties.getProperty(key));
        }
        // Now that the Fop system properties have been added, set up logger
        logger = Logger.getLogger(fopPackage);
        logger.setLevel(Level.CONFIG);
        logger.config(version);
        // Then restrict to WARNING
        logger.setLevel(Level.WARNING);
    }

    private void setupRunStats() {
        runtime = Runtime.getRuntime();
        startTotal = runtime.totalMemory();
        startFree = runtime.freeMemory();
        startTime = System.currentTimeMillis();
    }

    private void printRunStats() {
        System.out.println("Back from driver.run()");
        System.out.println("Elapsed time: " +
                (System.currentTimeMillis() - startTime));
        endtotal = runtime.totalMemory();
        endfree = runtime.freeMemory();
        System.gc();
        gctotal = runtime.totalMemory();
        gcfree = runtime.freeMemory();
        System.out.println("Total memory before run : " + startTotal);
        System.out.println("Total memory after run  : " + endtotal);
        System.out.println("Total memory after GC   : " + gctotal);
        System.out.println("Diff before/after total : "
                + (endtotal - startTotal));
        System.out.println("Diff before/GC total    : "
                + (gctotal - startTotal));
        System.out.println("Diff after/GC total     : "
                + (gctotal - endtotal));
        System.out.println("Free memory before run  : " + startFree);
        System.out.println("Free memory after run   : " + endfree);
        System.out.println("Free memory after GC    : " + gcfree);
        System.out.println("Diff before/after free  : "
                + (endfree - startFree));
        System.out.println("Diff before/GC free     : "
                + (gcfree - startFree));
        System.out.println("Diff after/GC free      : "
                + (gcfree - endfree));
        System.out.println("cg() freed              : "
                + (gcfree - endfree));
        //System.out.println("PC time     : " + (endPCi - startPCi));
    }

    public void run() {
        setupRunStats();
        
        Boolean bool = null;
        try {
            configuration = new Configuration();
            if (args == null) {
                userOptions = new UserOptions(configuration);
                options = userOptions;
                try {
                    userOptions.configure();
                } catch (FileNotFoundException e2) {
                    throw new FOPException(e2);
                }
            } else {
                cliOptions = new CLI_Options(configuration, args);
                options = cliOptions;
                try {
                    cliOptions.configure(args);
                } catch (FileNotFoundException e2) {
                    throw new FOPException(e2);
                }
            }
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
        } catch (FOPException e) {
            logger.warning(e.getMessage());
            if (options.isDebugMode()) {
                e.printStackTrace();
            }
        }

        printRunStats();
            
    }

    /**
     * Sets the InputHandler for XML imput as specified in FOPOptions.
     * @param inputHandler the InputHandler
     */
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }


    /**
     * Optionally sets the FOUserAgent instance for FOP to use. The Driver
     * class sets up its own FOUserAgent if none is set through this method.
     * @param agent FOUserAgent to use
     */
    public void setUserAgent(FOUserAgent agent) {
        userAgent = agent;
    }

    protected FOUserAgent getUserAgent() {
        if (userAgent == null) {
            userAgent = new FOUserAgent();
            userAgent.enableLogging(logger);
            userAgent.setBaseURL("");
        }
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
     * @param renderer the renderer instance to use
     * (Note: Logger must be set at this point)
     */
    public void setRenderer(Renderer renderer) {
        // AWTStarter calls this function directly
        if (renderer instanceof AWTRenderer) {
            rendererType = RENDER_AWT;
        }
        renderer.setOption("producer", Version.getVersion());
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
            renderer.setOption("producer", Version.getVersion());
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
        //userAgent.setBaseURL(inputHandler.getBaseURL());
        throw new FOPException("Attempting to run null 'render' method");
    }

}

