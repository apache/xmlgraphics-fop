package org.apache.fop.xml;

import org.apache.fop.fo.FObjectNames;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.SyncedFoXmlEventsBuffer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Driver;
import org.apache.fop.configuration.Configuration;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.util.NoSuchElementException;

/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * <tt>FoXMLSerialHandler</tt> is the <tt>ContentHandler</tt> for the
 * background <tt>XMLReader</tt> thread.
 */

public class FoXMLSerialHandler extends DefaultHandler implements Runnable {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private SyncedFoXmlEventsBuffer events;
    private XMLReader parser;
    private XMLNamespaces namespaces;
    private InputSource source;
    private Thread foThread;
    private boolean errorDump;

    /**
     * @param events the events buffer.
     * @param parser the xml parser.
     * @param source the parser input source.
     */
    public FoXMLSerialHandler
        (SyncedFoXmlEventsBuffer events, XMLReader parser, InputSource source)
    {
        this.events = events;
        this.parser = parser;
        this.source = source;
        namespaces = events.getNamespaces();
        parser.setContentHandler(this);
        errorDump = Configuration.getBooleanValue("debugMode").booleanValue();
    }

    public void setFoThread(Thread foThread) {
        this.foThread = foThread;
    }

    /**
     * This is the run method for the callback parser thread.
     */
    public void run() {
        // I''m in the thread - run the parser
        try {
            parser.parse(source);
            //System.out.println("Parser terminating.");
        } catch (Exception e) {
            if (errorDump) Driver.dumpError(e);
            if (foThread != null) {
                try {
                    foThread.interrupt();
                } catch (Exception ex) {} //ignore
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Utility routine for the callback methods.  It captures the
     * <tt>InterruptedException</tt> that is possible from the <i>put</i>
     * method of a <tt>SyncedFoXmlEventsBuffer</tt>.
     */
    public void putEvent(FoXMLEvent event) throws NoSuchElementException {
        synchronized (events) {
            try {
                events.put(event);
            } catch (InterruptedException e) {
                throw new RuntimeException
                        ("Producer interrupted: " +e.getMessage());
            }
        }
    }

    /**
     * Callback routine for the parser.
     */
    public void startDocument() throws NoSuchElementException {
        synchronized (events) {
            FoXMLEvent event = new FoXMLEvent(namespaces);
            //System.out.println("StartDocument thread "
            //                   + Thread.currentThread().getName());
            event.type = XMLEvent.STARTDOCUMENT;
            //System.out.println("SerialHandler: " + event);
            putEvent(event);
        }
    }

    /**
     * Callback routine for the parser.
     */
    public void endDocument() throws NoSuchElementException {
        synchronized (events) {
            FoXMLEvent event = new FoXMLEvent(namespaces);
            //System.out.println("EndDocument thread "
                               //+ Thread.currentThread().getName());
            event.type = XMLEvent.ENDDOCUMENT;
            //System.out.println("SerialHandler: " + event);
            putEvent(event);
            events.producerExhausted();
        }
    }

    /**
     * Callback routine for the parser.
     * @param uri the URI of the element name
     * @param localName the localname of the element
     * @param qName the (prefix) qualified name of the element
     * @param attributes the attribute set of the element, as an object
     * implementing the <tt>Attributes</tt> interface.
     */
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
        throws NoSuchElementException
    {
        synchronized (events) {
            FoXMLEvent event = new FoXMLEvent(namespaces);
            //System.out.println("StartElement thread "
            //                   + Thread.currentThread().getName());
            event.type = XMLEvent.STARTELEMENT;
            // Is this from the fo: namespace?
            event.uriIndex = namespaces.getURIIndex(uri);
            if (event.uriIndex == XMLNamespaces.XSLNSpaceIndex) {
                try {
                    event.foType = FObjectNames.getFOIndex(localName);
                } catch (FOPException e) {}
            }
            event.localName = localName;
            event.qName = qName;
            event.attributes = new AttributesImpl(attributes);
            //System.out.println("SerialHandler: " + event);
            putEvent(event);
        }
    }

    /**
     * Callback routine for parser.
     * @param uri the URI of this element name
     * @param localName the unqualified localname of this element
     * @param qName the (prefix) qualified name of this element
     */
    public void endElement(String uri, String localName,
                           String qName)
        throws NoSuchElementException
    {
        synchronized (events) {
            FoXMLEvent event = new FoXMLEvent(namespaces);
            //System.out.println("EndElement thread "
                               //+ Thread.currentThread().getName());
            event.type = XMLEvent.ENDELEMENT;
            event.uriIndex = namespaces.getURIIndex(uri);
            if (event.uriIndex == XMLNamespaces.XSLNSpaceIndex) {
                try {
                    event.foType = FObjectNames.getFOIndex(localName);
                } catch (FOPException e) {}
            }
            event.localName = localName;
            event.qName = qName;
            putEvent(event);
        }
    }

    /**
     * Callback routine for parser to handle characters.
     * @param ch the array of characters
     * @param start starting position of this set of characters in the
     * <i>ch</i> array.
     * @param length number of characters in this set
     */
    public void characters(char[] ch, int start, int length)
        throws NoSuchElementException
    {
        synchronized (events) {
            FoXMLEvent event = new FoXMLEvent(namespaces);
            //System.out.println("characters thread "
            //                   + Thread.currentThread().getName());
            event.type = XMLEvent.CHARACTERS;
            event.chars = new String(ch, start, length);
            //System.out.println("SerialHandler: " + event);
            putEvent(event);
        }
    }
}
