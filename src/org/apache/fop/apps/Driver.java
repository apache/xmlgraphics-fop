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
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
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

import org.apache.fop.layout.AreaTree;

import org.apache.fop.layout.FontInfo;

import org.apache.fop.render.Renderer;



// DOM

import org.w3c.dom.Document;

import org.w3c.dom.Node;

import org.w3c.dom.NamedNodeMap;

import org.w3c.dom.Attr;



// SAX

import org.xml.sax.DocumentHandler;

import org.xml.sax.InputSource;

import org.xml.sax.Parser;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.AttributeListImpl;



// Java

import java.io.PrintWriter;

import java.io.IOException;



public class Driver {



    protected FOTreeBuilder treeBuilder;

    protected AreaTree areaTree;

    protected Renderer renderer;

    protected PrintWriter writer;



    public Driver() {

	this.treeBuilder = new FOTreeBuilder();

    }

   

    public void setRenderer(Renderer renderer) {

	this.renderer = renderer;

    }



    public void setRenderer(String rendererClassName, String producer) {

	this.renderer = createRenderer(rendererClassName);

	this.renderer.setProducer(producer);

    }



    protected Renderer createRenderer(String rendererClassName) {

	System.err.println("using renderer " + rendererClassName);



	try {

	    return (Renderer)

		Class.forName(rendererClassName).newInstance();

	} catch (ClassNotFoundException e) {

	    System.err.println("Could not find " + rendererClassName);

	} catch (InstantiationException e) {

	    System.err.println("Could not instantiate "

			       + rendererClassName);

	} catch (IllegalAccessException e) {

	    System.err.println("Could not access " + rendererClassName);

	} catch (ClassCastException e) {

	    System.err.println(rendererClassName + " is not a renderer"); 

	}

	return null;

    }

    

    public void addElementMapping(ElementMapping mapping) {

	mapping.addToBuilder(this.treeBuilder);

    }

    

    public void addElementMapping(String mappingClassName) {

	createElementMapping(mappingClassName).addToBuilder(this.treeBuilder);

    }



    protected ElementMapping createElementMapping(String mappingClassName) {

	System.err.println("using element mapping " + mappingClassName);



	try {

	    return (ElementMapping)

		Class.forName(mappingClassName).newInstance();

	} catch (ClassNotFoundException e) {

	    System.err.println("Could not find " + mappingClassName);

	} catch (InstantiationException e) {

	    System.err.println("Could not instantiate "

			       + mappingClassName);

	} catch (IllegalAccessException e) {

	    System.err.println("Could not access " + mappingClassName);

	} catch (ClassCastException e) {

	    System.err.println(mappingClassName + " is not an element mapping"); 

	}

	return null;

    }



    public DocumentHandler getDocumentHandler() {

	return this.treeBuilder;

    }



    public void buildFOTree(Parser parser, InputSource source)

	throws FOPException {

	parser.setDocumentHandler(this.treeBuilder);

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



    public void buildFOTree(Document document) 

	throws FOPException {



	/* most of this code is modified from John Cowan's */



	Node currentNode;

	AttributeListImpl currentAtts;

	

	/* temporary array for making Strings into character arrays */

	char[] array = null;



	currentAtts = new AttributeListImpl();

	

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

			Attr att = (Attr)(map.item(i));

			currentAtts.addAttribute(att.getName(),

						 "CDATA",

						 att.getValue()); 

		    }

		    this.treeBuilder.startElement(

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

	    throw new FOPException(e.getMessage());

	}

    }



    public void setWriter(PrintWriter writer) {

	this.writer = writer;

    }



    public void format()

	throws FOPException {

	FontInfo fontInfo = new FontInfo();

	this.renderer.setupFontInfo(fontInfo);



	this.areaTree = new AreaTree();

	this.areaTree.setFontInfo(fontInfo);



	this.treeBuilder.format(areaTree);

    }



    public void render()

	throws IOException {

	this.renderer.render(areaTree, this.writer);

    }

}

