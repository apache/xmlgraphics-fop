/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.AreaTree;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.system.BufferManager;


// SAX
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;

// Java
import java.util.Hashtable;
import java.util.Stack;
import java.io.IOException;

/**
 * SAX Handler that builds the formatting object tree.
 */
public class FOTreeBuilder extends DefaultHandler implements TreeBuilder {

    /**
     * table mapping element names to the makers of objects
     * representing formatting objects
     */
    protected Hashtable fobjTable = new Hashtable();

    /**
     * class that builds a property list for each formatting object
     */
    protected Hashtable propertylistTable = new Hashtable();

    /**
     * current formatting object being handled
     */
    protected FObj currentFObj = null;

    /**
     * the root of the formatting object tree
     */
    protected FObj rootFObj = null;

    public BufferManager bufferManager;
	
	/**
     * set of names of formatting objects encountered but unknown
     */
    protected Hashtable unknownFOs = new Hashtable();

    // namespace implementation ideas pinched from John Cowan
    //      protected static class NSMap {
    //  	String prefix;
    //  	String uri;
    //  	int level;

    //  	NSMap(String prefix, String uri, int level) {
    //  	    this.prefix = prefix;
    //  	    this.uri = uri;
    //  	    this.level = level;
    //  	}
    //      }

    //      protected int level = 0;
    //      protected Stack namespaceStack = new Stack();

    //      {
    //  	namespaceStack.push(new NSMap("xml",
    //  				      "http://www.w3.org/XML/1998/namespace",
    //  				      -1));
    //  	namespaceStack.push(new NSMap("", "", -1));
    //      }

    //      protected String findURI(String prefix) {
    //  	for (int i = namespaceStack.size() - 1; i >= 0; i--) {
    //  	    NSMap nsMap = (NSMap) (namespaceStack.elementAt(i));
    //  	    if (prefix.equals(nsMap.prefix)) return nsMap.uri;
    //  	}
    //  	return null;
    //      }

    //      protected String mapName(String name)
    //  	throws SAXException {
    //  	int colon = name.indexOf(':');
    //  	String prefix = "";
    //  	String localPart = name;
    //  	if (colon != -1) {
    //  	    prefix = name.substring(0, colon);
    //  	    localPart = name.substring(colon + 1);
    //  	}
    //  	String uri = findURI(prefix);
    //  	if (uri == null) {
    //  	    if (prefix.equals("")) {
    //  		return name;
    //  	    } else {
    //  		throw new SAXException(new FOPException("Unknown namespace prefix " + prefix));
    //  	    }
    //  	}
    //  	return uri + "^" + localPart;
    //      }

    /**
     * add a mapping from element name to maker.
     *
     * @param namespaceURI namespace URI of formatting object element
     * @param localName local name of formatting object element
     * @param maker Maker for class representing formatting object
    */
    public void addMapping(String namespaceURI, String localName,
                           FObj.Maker maker) {
        this.fobjTable.put(namespaceURI + "^" + localName, maker);
    }

    /**
      * add a mapping from element name to maker.
      *
      * @param namespaceURI namespace URI of formatting object element
      * @param localName local name of formatting object element
      * @param maker Maker for class representing formatting object
     */
    public void addPropertyList(String namespaceURI, Hashtable list) {
        PropertyListBuilder plb;
        plb = (PropertyListBuilder) this.propertylistTable.get(
                namespaceURI);
        if (plb == null) {
            plb = new PropertyListBuilder();
            plb.addList(list);
            this.propertylistTable.put(namespaceURI, plb);
        } else {
            plb.addList(list);
        }
    }

    /**
      * add a mapping from element name to maker.
      *
      * @param namespaceURI namespace URI of formatting object element
      * @param localName local name of formatting object element
      * @param maker Maker for class representing formatting object
     */
    public void addElementPropertyList(String namespaceURI,
                                       String localName, Hashtable list) {
        PropertyListBuilder plb;
        plb = (PropertyListBuilder) this.propertylistTable.get(
                namespaceURI);
        if (plb == null) {
            plb = new PropertyListBuilder();
            plb.addElementList(localName, list);
            this.propertylistTable.put(namespaceURI, plb);
        } else {
            plb.addElementList(localName, list);
        }
    }

    /** SAX Handler for characters */
    public void characters(char data[], int start, int length) {
        currentFObj.addCharacters(data, start, start + length);
    }

    /** SAX Handler for the end of an element */
    public void endElement(String uri, String localName, String rawName) {
        currentFObj.end();
        currentFObj = (FObj) currentFObj.getParent();
        //      level--;
        //  	while (((NSMap) namespaceStack.peek()).level > level) {
        //  	    namespaceStack.pop();
        //  	}
    }

    /** SAX Handler for the start of the document */
    public void startDocument() {
		rootFObj = null; // allows FOTreeBuilder to be reused
        MessageHandler.logln("building formatting object tree");
    }

    /** SAX Handler for the start of an element */
    public void startElement(String uri, String localName,
                             String rawName, Attributes attlist) throws SAXException {
        /* the formatting object started */
        FObj fobj;

        /* the maker for the formatting object started */
        FObj.Maker fobjMaker;

        //  	level++;
        //  	int length = attlist.getLength();
        //  	for (int i = 0; i < length; i++) {
        //  	    String att = attlist.getQName(i);
        //  	    if (att.equals("xmlns")) {
        //  		namespaceStack.push( new NSMap("",
        //  					       attlist.getValue(i),
        //  					       level));
        //  	    } else if (att.startsWith("xmlns:")) {
        //  		String value = attlist.getValue(i);
        //  		namespaceStack.push(new NSMap(att.substring(6), value,
        //  					      level));
        //  	    }
        //  	}

        //String fullName = mapName(rawName);
        String fullName = uri + "^" + localName;
        fobjMaker = (FObj.Maker) fobjTable.get(fullName);
        PropertyListBuilder currentListBuilder =
          (PropertyListBuilder) this.propertylistTable.get(uri);

        if (fobjMaker == null) {
            if (!this.unknownFOs.containsKey(fullName)) {
                this.unknownFOs.put(fullName, "");
                MessageHandler.errorln(
                  "WARNING: Unknown formatting object " + fullName);
            }
            fobjMaker = new FObjMixed.Maker(); // fall back
        }

        try {
            PropertyList list = null;
            if (currentListBuilder != null) {
                list = currentListBuilder.makeList(fullName, attlist,
                                                   (currentFObj == null) ? null :
                                                   currentFObj.properties, currentFObj);
            } else {
                list = currentFObj.properties;
            }
            fobj = fobjMaker.make(currentFObj, list);
        } catch (FOPException e) {
            throw new SAXException(e);
        }

        if (rootFObj == null) {
            rootFObj = fobj;
			rootFObj.setBufferManager(this.bufferManager);	
            if (!fobj.getName().equals("fo:root")) {
                throw new SAXException(
                  new FOPException("Root element must" +
                                   " be root, not " + fobj.getName()));
            }
        } else {
            currentFObj.addChild(fobj);
        }

        currentFObj = fobj;
    }

    /**
      * format this formatting object tree
      *
      * @param areaTree the area tree to format into
      */
    public void format(AreaTree areaTree) throws FOPException {
        MessageHandler.logln("formatting FOs into areas");
		this.bufferManager.readComplete();
        ((Root) this.rootFObj).format(areaTree);
    }

    public void reset() 
    {
	currentFObj = null;
	rootFObj = null;
    }
    
    public boolean hasData() 
    {
	return (rootFObj != null);
    }

    public void setBufferManager(BufferManager bufferManager) {
     	this.bufferManager = bufferManager;
    }	    
}
