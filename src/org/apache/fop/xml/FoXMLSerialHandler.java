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
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.xml;

import org.apache.fop.fo.FObjectNames;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.SyncedFoXmlEventsBuffer;
import org.apache.fop.xml.FoXMLEventPool;
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

    /** The pool associated with the buffer. */
    private FoXMLEventPool pool;

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
        pool = events.getPool();
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
            FoXMLEvent event = pool.acquireFoXMLEvent();
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
            FoXMLEvent event = pool.acquireFoXMLEvent();
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
            FoXMLEvent event = pool.acquireFoXMLEvent();
            //System.out.println("startElement: acquired " + event.id);
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
            FoXMLEvent event = pool.acquireFoXMLEvent();
            //System.out.println("endElement: acquired " + event.id);
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
            FoXMLEvent event = pool.acquireFoXMLEvent();
            //System.out.println("characters thread "
            //                   + Thread.currentThread().getName());
            event.type = XMLEvent.CHARACTERS;
            event.chars = new String(ch, start, length);
            event.foType = FObjectNames.PCDATA;
            //System.out.println("SerialHandler: " + event);
            putEvent(event);
        }
    }
}
