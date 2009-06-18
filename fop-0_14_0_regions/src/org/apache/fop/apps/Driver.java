/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
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
import java.io.PrintWriter;
import java.io.IOException;

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
 * calling getDocumentHandler() and firing the SAX events yourself.
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
 *   driver.setWriter(new PrintWriter(new FileWriter(args[1])));
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

    /** the PrintWriter to use to output the results of the renderer */
    protected PrintWriter writer;

    /** create a new Driver */
    public Driver() {
	this.treeBuilder = new FOTreeBuilder();
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
	    return (Renderer)
		Class.forName(rendererClassName).newInstance();
	} catch (ClassNotFoundException e) {
	    MessageHandler.errorln("Could not find " + rendererClassName);
	} catch (InstantiationException e) {
	    MessageHandler.errorln("Could not instantiate "
			       + rendererClassName);
	} catch (IllegalAccessException e) {
	    MessageHandler.errorln("Could not access " + rendererClassName);
	} catch (ClassCastException e) {
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
	createElementMapping(mappingClassName).addToBuilder(this.treeBuilder);
    }

    /**
     * protected method used by addElementMapping(String) to
     * instantiate element mapping class
     */
    protected ElementMapping createElementMapping(String mappingClassName) {
    MessageHandler.logln("using element mapping " + mappingClassName);

	try {
	    return (ElementMapping)
		Class.forName(mappingClassName).newInstance();
	} catch (ClassNotFoundException e) {
	    MessageHandler.errorln("Could not find " + mappingClassName);
	} catch (InstantiationException e) {
	    MessageHandler.errorln("Could not instantiate "
			       + mappingClassName);
	} catch (IllegalAccessException e) {
	    MessageHandler.errorln("Could not access " + mappingClassName);
	} catch (ClassCastException e) {
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
    protected PropertyListMapping createPropertyList(String listClassName) {
    MessageHandler.logln("using property list mapping " + listClassName);

	try {
	    return (PropertyListMapping)
		Class.forName(listClassName).newInstance();
	} catch (ClassNotFoundException e) {
	    MessageHandler.errorln("Could not find " + listClassName);
	} catch (InstantiationException e) {
	    MessageHandler.errorln("Could not instantiate "
			       + listClassName);
	} catch (IllegalAccessException e) {
	    MessageHandler.errorln("Could not access " + listClassName);
	} catch (ClassCastException e) {
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
    public void buildFOTree(XMLReader parser, InputSource source)
		throws FOPException {

	parser.setContentHandler(this.treeBuilder);
	try {
	    parser.parse(source);
	} catch (SAXException e) {
	    if (e.getException() instanceof FOPException)
		throw (FOPException) e.getException();
	    else
		throw new FOPException(e.getMessage());
	} catch (IOException e) {
	    throw new FOPException(e.getMessage());
	}
    }

    /**
     * build the formatting object tree using the given DOM Document
     */
    public void buildFOTree(Document document) 
	throws FOPException {

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
			Attr att = (Attr)map.item(i);
			currentAtts.addAttribute("",
                         att.getName(),
                         "",
                         "CDATA",
                         att.getValue());
		    }
		    this.treeBuilder.startElement(
			"", currentNode.getNodeName(), "", currentAtts);
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
			    "", currentNode.getNodeName(), "" );
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

    /**
     * set the PrintWriter to use to output the result of the Renderer
     * (if applicable)
     */
    public void setWriter(PrintWriter writer) {
	this.writer = writer;
    }

    /**
     * format the formatting object tree into an area tree
     */
    public void format()
	throws FOPException {
	FontInfo fontInfo = new FontInfo();
	this.renderer.setupFontInfo(fontInfo);

	this.areaTree = new AreaTree();
	this.areaTree.setFontInfo(fontInfo);

	this.treeBuilder.format(areaTree);
    }

    /**
     * render the area tree to the output form
     */
    public void render()
	throws IOException, FOPException {
	this.renderer.render(areaTree, this.writer);
    }
}
