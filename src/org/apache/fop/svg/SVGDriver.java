/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.svg;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.apps.*;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.PropertyListMapping;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.messaging.MessageHandler;

// DOM
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

// SAX
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.svg.*;

// Java
import java.io.PrintWriter;
import java.io.IOException;

/**
 */
public class SVGDriver {

    /** the FO tree builder */
    protected SVGTreeBuilder treeBuilder;

    /** the PrintWriter to use to output the results of the renderer */
    protected PrintWriter writer;

    /** create a new Driver */
    public SVGDriver() {
        this.treeBuilder = new SVGTreeBuilder();
    }

    /**
     * add the given element mapping.
     *
     * an element mapping maps element names to Java classes
     */
    public void addElementMapping(ElementMapping mapping) {
        mapping.addToBuilder(this.treeBuilder);
    }

    /**
     * add the element mapping with the given class name
     */
    public void addElementMapping(String mappingClassName) {
        createElementMapping(mappingClassName).addToBuilder(
          this.treeBuilder);
    }

    /**
     * protected method used by addElementMapping(String) to
     * instantiate element mapping class
     */
    protected ElementMapping createElementMapping(
      String mappingClassName) {
        MessageHandler.logln("using element mapping " + mappingClassName);

        try {
            return (ElementMapping) Class.forName(
                     mappingClassName).newInstance();
        } catch (ClassNotFoundException e) {
            MessageHandler.errorln("Could not find " + mappingClassName);
        }
        catch (InstantiationException e) {
            MessageHandler.errorln("Could not instantiate " +
                                   mappingClassName);
        }
        catch (IllegalAccessException e) {
            MessageHandler.errorln("Could not access " + mappingClassName);
        }
        catch (ClassCastException e) {
            MessageHandler.errorln(mappingClassName + " is not an element mapping");
        }
        return null;
    }

    /**
     * add the element mapping with the given class name
     */
    public void addPropertyList(String listClassName) {
        createPropertyList(listClassName).addToBuilder(this.treeBuilder);
    }

    /**
     * protected method used by addPropertyList(String) to
     * instantiate list mapping class
     */
    protected PropertyListMapping createPropertyList(
      String listClassName) {
        MessageHandler.logln("using property list mapping " +
                             listClassName);

        try {
            return (PropertyListMapping) Class.forName(
                     listClassName).newInstance();
        } catch (ClassNotFoundException e) {
            MessageHandler.errorln("Could not find " + listClassName);
        }
        catch (InstantiationException e) {
            MessageHandler.errorln("Could not instantiate " +
                                   listClassName);
        }
        catch (IllegalAccessException e) {
            MessageHandler.errorln("Could not access " + listClassName);
        }
        catch (ClassCastException e) {
            MessageHandler.errorln(listClassName + " is not an property list");
        }
        return null;
    }

    /**
     * return the tree builder (a SAX DocumentHandler).
     *
     * used in situations where SAX is used but not via a FOP-invoked
     * SAX parser. A good example is an XSLT engine that fires SAX
     * events but isn't a SAX Parser itself.
     */
    public ContentHandler getContentHandler() {
        return this.treeBuilder;
    }

    /**
     * build the formatting object tree using the given SAX Parser and
     * SAX InputSource
     */
    public void buildSVGTree(XMLReader parser,
                             InputSource source) throws FOPException {

        parser.setContentHandler(this.treeBuilder);
        try {
            parser.parse(source);
        } catch (SAXException e) {
            e.printStackTrace();
            if (e.getException() instanceof FOPException)
                throw (FOPException) e.getException();
            else
                throw new FOPException(e.getMessage());
        }
        catch (IOException e) {
            throw new FOPException(e.getMessage());
        }
    }

    /**
     * build the formatting object tree using the given DOM Document
     */
    public void buildSVGTree(Document document) throws FOPException {

        /* most of this code is modified from John Cowan's */

        Node currentNode;
        AttributesImpl currentAtts;

        /* temporary array for making Strings into character arrays */
        char[] array = null;

        currentAtts = new AttributesImpl();

        /* start at the document element */
        currentNode = document;

        try {
            while (currentNode != null) {
                switch (currentNode.getNodeType()) {
                    case Node.DOCUMENT_NODE:
                        this.treeBuilder.startDocument();
                        break;
                    case Node.CDATA_SECTION_NODE:
                    case Node.TEXT_NODE:
                        String data = currentNode.getNodeValue();
                        int datalen = data.length();
                        if (array == null || array.length < datalen) {
                            /* if the array isn't big enough, make a new
                                one */
                            array = new char[datalen];
                        }
                        data.getChars(0, datalen, array, 0);
                        this.treeBuilder.characters(array, 0, datalen);
                        break;
                    case Node.PROCESSING_INSTRUCTION_NODE:
                        this.treeBuilder.processingInstruction(
                          currentNode.getNodeName(),
                          currentNode.getNodeValue());
                        break;
                    case Node.ELEMENT_NODE:
                        NamedNodeMap map = currentNode.getAttributes();
                        currentAtts.clear();
                        for (int i = map.getLength() - 1; i >= 0; i--) {
                            Attr att = (Attr) map.item(i);
                            currentAtts.addAttribute("", att.getName(),
                                                     "", "CDATA", att.getValue());
                        }
                        this.treeBuilder.startElement("",
                                                      currentNode.getNodeName(), "", currentAtts);
                        break;
                }

                Node nextNode = currentNode.getFirstChild();
                if (nextNode != null) {
                    currentNode = nextNode;
                    continue;
                }

                while (currentNode != null) {
                    switch (currentNode.getNodeType()) {
                        case Node.DOCUMENT_NODE:
                            this.treeBuilder.endDocument();
                            break;
                        case Node.ELEMENT_NODE:
                            this.treeBuilder.endElement("",
                                                        currentNode.getNodeName(), "");
                            break;
                    }

                    nextNode = currentNode.getNextSibling();
                    if (nextNode != null) {
                        currentNode = nextNode;
                        break;
                    }

                    currentNode = currentNode.getParentNode();
                }
            }
        } catch (SAXException e) {
            throw new FOPException(e.getMessage());
        }
    }

    public SVGDocument getSVGDocument() {
        return this.treeBuilder.getSVGDocument();
    }

    /**
     * format the formatting object tree into an area tree
     */
    public void format() throws FOPException {
        FontInfo fontInfo = new FontInfo();
    }
}
