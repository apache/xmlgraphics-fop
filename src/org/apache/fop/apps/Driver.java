/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// FOP
import org.apache.fop.fo.FOTree;
import org.apache.fop.datastructs.SyncedCircularBuffer;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.xml.XMLSerialHandler;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.expr.PropertyException;

// DOM

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Java
import java.io.*;
import java.util.*;

public class Driver {
    /**
     * If true, full error stacks are reported
     */
    private static boolean _errorDump = false;

    private InputHandler inputHandler;
    private XMLReader parser;
    private InputSource source;

    private XMLSerialHandler xmlhandler;
    private SyncedCircularBuffer xmlevents;
    private FOTree foTree;
    private AreaTree areaTree = new AreaTree();

    private Thread driverThread;
    private Thread parserThread;
    private Thread foThread;
    private Thread areaThread;
    private Thread renderThread;


    /**
     * What does the Driver do?
     * When it has all of the ancillary requirements, it sets up the
     * serialized components:
     * XMLEventSource <=> FOTree <=> AreaTree ...
     */
    public Driver() throws FOPException {
        _errorDump =
                Configuration.getBooleanValue("debugMode").booleanValue();
        String version = Version.getVersion();
        MessageHandler.logln(version);
    }

    public void run () throws FOPException {
        setInputHandler(Options.getInputHandler());
        parser = inputHandler.getParser();
        source = inputHandler.getInputSource();
        setParserFeatures(parser);

        xmlevents = new SyncedCircularBuffer();
        xmlhandler = new XMLSerialHandler(xmlevents, parser, source);
        foTree = new FOTree(xmlevents);

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
        try {
            foThread.join();
        } catch (InterruptedException e) {}

    }

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

    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

     // setting the parser features
    public void setParserFeatures(XMLReader parser) throws FOPException {
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              true);
        } catch (SAXException e) {
            throw new FOPException(
                "Error in setting up parser feature namespace-prefixes\n"
                       + "You need a parser which supports SAX version 2", e);
        }
    }

    /**
     * Dumps an error
     */
    public static void dumpError(Exception e) {
        if (_errorDump) {
            if (e instanceof SAXException) {
                e.printStackTrace();
                if (((SAXException)e).getException() != null) {
                    ((SAXException)e).getException().printStackTrace();
                }
            } else if (e instanceof FOPException) {
                e.printStackTrace();
                if (((FOPException)e).getException() != null) {
                    ((FOPException)e).getException().printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }
    }

}

