/*
 *
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.fop.xml;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.xml.Namespaces;

/**
 * <tt>FoXmlSerialHandler</tt> is the <tt>ContentHandler</tt> for the
 * background <tt>XMLReader</tt> thread.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class FoXmlSerialHandler
extends DefaultHandler
implements Runnable {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private SyncedXmlEventsBuffer events;
    private XMLReader parser;
    private Namespaces namespaces;
    private InputSource source;
    private Thread foThread;
    private Thread renderThread;
    protected Logger log = Logger.getLogger(Fop.fopPackage);
    private boolean errorDump;

    /**
     * @param events the events buffer.
     * @param parser the xml parser.
     * @param source the parser input source.
     */
    public FoXmlSerialHandler
        (SyncedXmlEventsBuffer events, XMLReader parser, InputSource source)
    {
        this.events = events;
        this.parser = parser;
        this.source = source;
        namespaces = events.getNamespaces();
        parser.setContentHandler(this);
        Level level = log.getLevel();
        if (level.intValue() <= Level.FINE.intValue()) {
            errorDump = true;
        }
    }

    /**
     * Allow the thread starter process to notify the serial handler of
     * the FO Tree building thread.
     * @param foThread
     */
    public void setFoThread(Thread foThread) {
        this.foThread = foThread;
    }

    /**
     * Allow the thread starter process to notify the serial handler of
     * the rendering thread.
     * @param renderThread
     */
    public void setRenderThread(Thread renderThread) {
        this.renderThread = renderThread;
    }
    /**
     * This is the run method for the callback parser thread.
     */
    public void run() {
        // I''m in the thread - run the parser
        try {
            parser.parse(source);
        } catch (Exception e) {
            if (errorDump) {
                e.printStackTrace();
            }
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
     * method of a <tt>SyncedXmlEventsBuffer</tt>.
     */
    public void putEvent(XmlEvent event) throws NoSuchElementException {
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
            XmlEvent event = acquireXMLEvent(Namespaces.DefAttrNSIndex);
            event.type = XmlEvent.STARTDOCUMENT;
            putEvent(event);
        }
    }

    /**
     * Callback routine for the parser.
     */
    public void endDocument() throws NoSuchElementException {
        synchronized (events) {
            XmlEvent event = acquireXMLEvent(Namespaces.DefAttrNSIndex);
            event.type = XmlEvent.ENDDOCUMENT;
            putEvent(event);
            events.producerExhausted();
        }
    }
    
    /**
     * An internal method to acquire an event for a given namespace
     * @param nsIndex the namespace index
     * @return the acquired event
     */
    private XmlEvent acquireXMLEvent(int nsIndex) {
        try {
            return
                namespaces.acquireXMLEvent(nsIndex);
        } catch (FOPException ex) {
            throw new RuntimeException(
            "Namespace index " + nsIndex + " not recognized");
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
            try {
                int uriIndex = namespaces.getURIIndex(uri);
                XmlEvent event = acquireXMLEvent(uriIndex);
                if (uriIndex == Namespaces.XSLNSpaceIndex) {
                        event.setFoType(FObjectNames.getFOIndex(localName));
                }
                event.type = XmlEvent.STARTELEMENT;
                // Is this from the fo: namespace?
                event.uriIndex = uriIndex;
                event.localName = localName;
                //event.qName = qName;
                event.attributes = new AttributesImpl(attributes);
                putEvent(event);
            } catch (FOPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
            try {
                int uriIndex = namespaces.getURIIndex(uri);
                XmlEvent event = namespaces.acquireXMLEvent(uriIndex);
                event.type = XmlEvent.ENDELEMENT;
                event.uriIndex = uriIndex;
                if (uriIndex == Namespaces.XSLNSpaceIndex) {
                    event.setFoType(FObjectNames.getFOIndex(localName));
                }
                event.localName = localName;
                //event.qName = qName;
                putEvent(event);
            } catch (FOPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
            try {
                // TODO chars events have no namespace, but a namespace is
                // essential for subsequent processing.  Use the default
                // attribute namespace (the empty string), and rely on
                // downstream processing to determine the environment in
                // which the characters belong.
                XmlEvent event
                    = namespaces.acquireXMLEvent(Namespaces.DefAttrNSIndex);
                event.type = XmlEvent.CHARACTERS;
                event.chars = new String(ch, start, length);
                // Can't setFoType, because this event is now an XmlEvent,
                // not an FoXmlEvent
                //event.setFoType(FObjectNames.PCDATA);
                //System.out.println("SerialHandler: " + event);
                putEvent(event);
            } catch (FOPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
