package org.apache.fop.xml;

/**
 * A class for holding and passing a URI index and local name
 * pair, as used in the <tt>XMLEvent</tt> class.
 */
public class UriLocalName {
    public final int uriIndex;
    public final String localName;

    /**
     * @param uriIndex - the index of the namespace URI maintained in
     * the associated <tt>XMLNamespaces</tt> object.
     * @param localName - the local name of the event.
     */
    public UriLocalName(int uriIndex, String localName) {
	this.uriIndex = uriIndex;
	this.localName = localName;
    }
}
