package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;

import org.xml.sax.helpers.AttributesImpl;

/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * This is a data class to encapsulate the data of an individual XML
 * parse event in an XML-FO file. The current version, while defining
 * accessor methods, leaves the component data of the event as protected.
 * The <tt>XMLSerialHandler</tt> methods set the values directly.
 */

public class FoXMLEvent extends XMLEvent {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** The FO type, as defined in FObjectNames, of fo: XML events. */
    protected int foType = FObjectNames.NO_FO;

    /**
     * The one-argument constructor uses the default initialization values:
     * NOEVENT for the event <i>type</i>, and null references for all others
     * except <i>namespaces</i>.
     */
    public FoXMLEvent (XMLNamespaces namespaces) {
        super(namespaces);
    }

    /**
     * The fully defined constructor takes values for each of the data
     * elements.
     */
    public FoXMLEvent(int type, String chars, int uriIndex,
                    String localName, String qName,
                    AttributesImpl attributes, XMLNamespaces namespaces,
                    int foType)
    {
        super
            (type, chars, uriIndex, localName, qName, attributes, namespaces);
        this.foType = foType;
    }

    /**
     * The cloning constructor takes a reference to an existing
     * <tt>FoXMLEvent</tt> object.
     */
    public FoXMLEvent(FoXMLEvent ev) {
        super(ev);
        foType = ev.foType;
    }

    public FoXMLEvent(int type, String chars, XMLNamespaces namespaces) {
        super(type, chars, namespaces);
    }

    public FoXMLEvent(int type, int uriIndex, AttributesImpl attributes,
                    XMLNamespaces namespaces, int foType) {
        super(namespaces);
        this.type = type;
        this.uriIndex = uriIndex;
        this.attributes = attributes;
        this.foType = foType;
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * The <i>namespaces</i> field is not cleared.
     * @return the cleared <tt>XMLEvent</tt> event.
     */
    public XMLEvent clear() {
        foType = FObjectNames.NO_FO;
        return super.clear();
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * The <i>namespaces</i> field is not cleared.
     * @return the cleared <tt>XMLEvent</tt> event.
     */
    public FoXMLEvent clearFo() {
        return (FoXMLEvent)clear();
    }

    /**
     * Get the FO type of this <i>FoXMLEvent</i>.
     * @returns the FO type.
     */
    public int getFoType() { return foType; }

    /**
     * Set the FO type of this <i>FoXMLEvent</i>.
     * @param foType -the FO type.
     */
    public void setFoType(int foType) { this.foType = foType; }

    public String toString() {
        String tstr;
        try {
            tstr = "FO type: " + FObjectNames.getFOName(foType) + "\n";
        } catch (FOPException e) {
            throw new RuntimeException(e.getMessage());
        }
        tstr = tstr + super.toString();
        return tstr;
    }

}
