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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.apache.fop.configuration.Configuration;
import org.apache.fop.fo.FOTree;
import org.apache.fop.layout.AreaTree;
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

    /** If true, full error stacks are reported */
    private boolean _errorDump = false;
    private Configuration configuration = null;
    private FOPOptions options = null;
    
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


    /**
     * Error handling, version and logging initialization.
     */
    public Driver() {
        String version = Version.getVersion();
        configuration = new Configuration();
        options = new FOPOptions(configuration);
        _errorDump = configuration.isTrue("debugMode");
        Fop.logger.config(version);
    }
    
    public Driver(String[] args, Configuration config, FOPOptions options) {
        String version = Version.getVersion();
        configuration = config;
        this.options = options;
        _errorDump = configuration.isTrue("debugMode");
        Fop.logger.config(version);
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

    /**
     * Sets the parser features.
     * @param parser the XMLReader used to parse the input
     * @throws FOPException
     */
    /*
    public void setParserFeatures(XMLReader parser) throws FOPException {
    }
    */

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

