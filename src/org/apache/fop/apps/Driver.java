/*
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 */

package org.apache.fop.apps;

// FOP
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.apache.fop.configuration.Configuration;
import org.apache.fop.fo.FOTree;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.xml.FoXMLSerialHandler;
import org.apache.fop.xml.SyncedFoXmlEventsBuffer;

public class Driver {
    /**
     * If true, full error stacks are reported
     */
    private static boolean _errorDump = false;

    private InputHandler inputHandler;
    private XMLReader parser;
    private InputSource source;

    private FoXMLSerialHandler xmlhandler;
    private SyncedFoXmlEventsBuffer xmlevents;
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

        xmlevents = new SyncedFoXmlEventsBuffer();
        xmlhandler = new FoXMLSerialHandler(xmlevents, parser, source);
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
        //System.out.println("Joined to parser.");
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

