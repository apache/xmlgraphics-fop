/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.apps;

// FOP
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.PropertyListMapping;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.configuration.ConfigurationReader;
import org.apache.fop.configuration.Configuration;


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

// Java
import java.io.*;


/**
 * <P>Primary class that drives overall FOP process.
 *
 * <P>Once this class is instantiated, methods are called to set the
 * Renderer to use, the (possibly multiple) ElementMapping(s) to
 * use and the PrintWriter to use to output the results of the
 * rendering (where applicable). In the case of the Renderer and
 * ElementMapping(s), the Driver may be supplied either with the
 * object itself, or the name of the class, in which case Driver will
 * instantiate the class itself. The advantage of the latter is it
 * enables runtime determination of Renderer and ElementMapping(s).
 *
 * <P>Once the Driver is set up, the buildFOTree method
 * is called. Depending on whether DOM or SAX is being used, the
 * invocation of the method is either buildFOTree(Document) or
 * buildFOTree(Parser, InputSource) respectively.
 *
 * <P>A third possibility may be used to build the FO Tree, namely
 * calling getContentHandler() and firing the SAX events yourself.
 *
 * <P>Once the FO Tree is built, the format() and render() methods may be
 * called in that order.
 *
 * <P>Here is an example use of Driver from CommandLine.java:
 *
 * <PRE>
 *   Driver driver = new Driver();
 *   driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", version);
 *   driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
 *   driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
 *   driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
 *   driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
 *   driver.setOutputStream(new FileOutputStream(args[1]));
 *   driver.buildFOTree(parser, fileInputSource(args[0]));
 *   driver.format();
 *   driver.render();
 * </PRE>
 */
public class Driver {

    /** the FO tree builder */
    protected FOTreeBuilder treeBuilder;

    /** the area tree that is the result of formatting the FO tree */
    protected AreaTree areaTree;

    /** the renderer to use to output the area tree */
    protected Renderer renderer;

    /** the stream to use to output the results of the renderer */
    protected OutputStream stream;

    /** If true, full error stacks are reported */
    protected boolean errorDump = false;

    /** create a new Driver */
    public Driver() {
        this.treeBuilder = new FOTreeBuilder();
    }

    /** Set the error dump option
         * @param dump if true, full stacks will be reported to the error log
         */
    public void setErrorDump(boolean dump) {
        errorDump = dump;
    }

    /** set the Renderer to use */
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    /**
         * set the class name of the Renderer to use as well as the
         * producer string for those renderers that can make use of it
         */
    public void setRenderer(String rendererClassName, String producer) {
        this.renderer = createRenderer(rendererClassName);
        this.renderer.setProducer(producer);
    }

    /**
         * protected method used by setRenderer(String, String) to
         * instantiate the Renderer class
         */
    protected Renderer createRenderer(String rendererClassName) {
        MessageHandler.logln("using renderer " + rendererClassName);

        try {
            return (Renderer) Class.forName(
                     rendererClassName).newInstance();
        } catch (ClassNotFoundException e) {
            MessageHandler.errorln("Could not find " + rendererClassName);
        }
        catch (InstantiationException e) {
            MessageHandler.errorln("Could not instantiate " +
                                   rendererClassName);
        }
        catch (IllegalAccessException e) {
            MessageHandler.errorln("Could not access " + rendererClassName);
        }
        catch (ClassCastException e) {
            MessageHandler.errorln(rendererClassName + " is not a renderer");
        }
        return null;
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
            dumpError(e);
        }
        catch (InstantiationException e) {
            MessageHandler.errorln("Could not instantiate " +
                                   mappingClassName);
            dumpError(e);
        }
        catch (IllegalAccessException e) {
            MessageHandler.errorln("Could not access " + mappingClassName);
            dumpError(e);
        }
        catch (ClassCastException e) {
            MessageHandler.errorln(mappingClassName + " is not an element mapping");
            dumpError(e);
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
            dumpError(e);
        }
        catch (InstantiationException e) {
            MessageHandler.errorln("Could not instantiate " +
                                   listClassName);
            dumpError(e);
        }
        catch (IllegalAccessException e) {
            MessageHandler.errorln("Could not access " + listClassName);
            dumpError(e);
        }
        catch (ClassCastException e) {
            MessageHandler.errorln(listClassName + " is not an property list");
            dumpError(e);
        }
        return null;
    }

    /**
         * return the tree builder (a SAX ContentHandler).
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
    public void buildFOTree(XMLReader parser,
                            InputSource source) throws FOPException {

        parser.setContentHandler(this.treeBuilder);
        try {
            parser.parse(source);
        } catch (SAXException e) {
            if (e.getException() instanceof FOPException) {
                dumpError(e.getException());
                throw (FOPException) e.getException();
            } else {
                dumpError(e);
                throw new FOPException(e.getMessage());
            }
        }
        catch (IOException e) {
            dumpError(e);
            throw new FOPException(e.getMessage());
        }
    }

    /**
         * build the formatting object tree using the given DOM Document
         */
    public void buildFOTree(Document document) throws FOPException {

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
                            currentAtts.addAttribute( att.getNamespaceURI(),
                                                      att.getLocalName(), att.getName(),
                                                      "CDATA", att.getValue());
                        }
                        this.treeBuilder.startElement(
                          currentNode.getNamespaceURI(),
                          currentNode.getLocalName(),
                          currentNode.getNodeName(), currentAtts);
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
                            this.treeBuilder.endElement(
                              currentNode.getNamespaceURI(),
                              currentNode.getLocalName(),
                              currentNode.getNodeName());
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
            dumpError(e);
            throw new FOPException(e.getMessage());
        }
    }

    /**
         * Dumps an error
         */
    public void dumpError(Exception e) {
        if (errorDump) {
            if (e instanceof SAXException) {
                e.printStackTrace();
                if (((SAXException) e).getException() != null) {
                    ((SAXException) e).getException().printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }

    }

    /**
        * set the OutputStream to use to output the result of the Renderer
        * (if applicable)
        */
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    /**
         * format the formatting object tree into an area tree
         */
    public void format() throws FOPException {
        FontInfo fontInfo = new FontInfo();
        this.renderer.setupFontInfo(fontInfo);

        this.areaTree = new AreaTree();
        this.areaTree.setFontInfo(fontInfo);

        this.treeBuilder.format(areaTree);
    }

    /**
         * render the area tree to the output form
         */
    public void render() throws IOException, FOPException {
        this.renderer.render(areaTree, this.stream);
    }


}
