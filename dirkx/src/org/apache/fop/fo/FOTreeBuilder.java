package org.apache.xml.fop.fo;

// FOP
import org.apache.xml.fop.layout.AreaTree;
import org.apache.xml.fop.apps.FOPException;
import org.apache.xml.fop.fo.pagination.Root;

// SAX
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.AttributeList;

// Java
import java.util.Hashtable;
import java.util.Stack;
import java.io.IOException;

/**
 * SAX Handler that builds the formatting object tree.
 */
public class FOTreeBuilder extends HandlerBase {

    /**
     * table mapping element names to the makers of objects
     * representing formatting objects
     */
    protected Hashtable fobjTable = new Hashtable();

    /**
     * class that builds a property list for each formatting object
     */
    protected PropertyListBuilder propertyListBuilder = new
	PropertyListBuilder(); 
	
    /**
     * current formatting object being handled
     */
    protected FObj currentFObj = null;

    /**
     * the root of the formatting object tree
     */
    protected FObj rootFObj = null;

    /**
     * set of names of formatting objects encountered but unknown
     */
    protected Hashtable unknownFOs = new Hashtable();

    // namespace implementation ideas pinched from John Cowan
    protected static class NSMap {
	String prefix;
	String uri;
	int level;

	NSMap(String prefix, String uri, int level) {
	    this.prefix = prefix;
	    this.uri = uri;
	    this.level = level;
	}
    }

    protected int level = 0;
    protected Stack namespaceStack = new Stack();

    {
	namespaceStack.push(new NSMap("xml",
				      "http://www.w3.org/XML/1998/namespace",
				      -1));
	namespaceStack.push(new NSMap("", "", -1));
    }

    protected String findURI(String prefix) {
	for (int i = namespaceStack.size() - 1; i >= 0; i--) {
	    NSMap nsMap = (NSMap) (namespaceStack.elementAt(i));
	    if (prefix.equals(nsMap.prefix)) return nsMap.uri;
	}
	return null;
    }

    protected String mapName(String name)
	throws SAXException {
	int colon = name.indexOf(':');
	String prefix = "";
	String localPart = name;
	if (colon != -1) {
	    prefix = name.substring(0, colon);
	    localPart = name.substring(colon + 1);
	}
	String uri = findURI(prefix);
	if (uri == null) {
	    if (prefix.equals("")) {
		return name;
	    } else {
		throw new SAXException(new FOPException("Unknown namespace prefix " + prefix));
	    }
	}
	return uri + "^" + localPart;
    }

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

    /** SAX Handler for characters */
    public void characters(char data[], int start, int length) {
	currentFObj.addCharacters(data, start, start + length);
    }

    /** SAX Handler for the end of an element */
    public void endElement(String name) {
	currentFObj.end();
	currentFObj = (FObj) currentFObj.getParent();
	level--;
	while (((NSMap) namespaceStack.peek()).level > level) {
	    namespaceStack.pop();
	}
    }

    /** SAX Handler for the start of the document */
    public void startDocument() {
	System.err.println("building formatting object tree");
    }

    /** SAX Handler for the start of an element */
    public void startElement(String name, AttributeList attlist)
	throws SAXException { 

	/* the formatting object started */
	FObj fobj;

	/* the maker for the formatting object started */
	FObj.Maker fobjMaker;

	level++;
	int length = attlist.getLength();
	for (int i = 0; i < length; i++) {
	    String att = attlist.getName(i);
	    if (att.equals("xmlns")) {
		namespaceStack.push( new NSMap("",
					       attlist.getValue(i),
					       level));
	    } else if (att.startsWith("xmlns:")) {
		String value = attlist.getValue(i);
		namespaceStack.push(new NSMap(att.substring(6), value,
					      level));
	    }
	}
	String fullName = mapName(name);

	fobjMaker = (FObj.Maker) fobjTable.get(fullName);

	if (fobjMaker == null) {
	    if (!this.unknownFOs.containsKey(fullName)) {
		this.unknownFOs.put(fullName, "");
		System.err.println("WARNING: Unknown formatting object "
				   + fullName);  
	    }
	    fobjMaker = new FObjMixed.Maker(); // fall back
	}
	
	try {
	    fobj =
		fobjMaker.make(currentFObj, 
 	       this.propertyListBuilder.makeList(attlist,  
		     (currentFObj == null) ? null : currentFObj.properties));
	} catch (FOPException e) {
	    throw new SAXException(e);
	}

	if (rootFObj == null) {
	    rootFObj = fobj;
	    if (!fobj.getName().equals("fo:root")) {
		throw new SAXException(new FOPException("Root element must"
							+ " be root, not "
							+ fobj.getName())); 
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
    public void format(AreaTree areaTree)
	throws FOPException {
	System.err.println("formatting FOs into areas");
	((Root) this.rootFObj).format(areaTree);
    }
}
