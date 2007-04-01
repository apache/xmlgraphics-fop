package org.apache.fop.fo.extensions.destination;

import org.apache.fop.fo.ValidationException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.extensions.ExtensionElementMapping;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Class for named destinations in PDF.
 */
public class Destination extends FObj {

    String internalDestination;
    Root root;

    /**
     * Constructs a Destination object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public Destination(FONode parent) {
        super(parent);
        root = parent.getRoot();
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        internalDestination = pList.get(PR_INTERNAL_DESTINATION).getString();
        if (internalDestination.length() == 0) {
            attributeError("Missing attribute:  internal-destination must be specified.");
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        root.addDestination(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    public String getInternalDestination() {
        return internalDestination;
    }

    /** @see org.apache.fop.fo.FONode#getNamespaceURI() */
    public String getNamespaceURI() {
        return ExtensionElementMapping.URI;
    }

    /** @see org.apache.fop.fo.FONode#getNormalNamespacePrefix() */
    public String getNormalNamespacePrefix() {
        return "fox";
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "destination";
    }

}

