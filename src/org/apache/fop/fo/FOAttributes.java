package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.xml.XMLEvent;

import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/*
 * FOAttributes.java
 * $Id$
 *
 * Created: Wed Nov 14 15:19:51 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * The FO Attributes data structures and methods needed to manage the
 * Attributes associated with FO nodes.
 */

public class FOAttributes {

    /**
     * <i>nSpaceAttrLists</i> is an <tt>ArrayList</tt> to hold the array of 
     * <tt>HashMap</tt>s which contain the attribute lists for each
     * namespace which may be active for a particular FO element.  The
     * <tt>ArrayList</tt> is indexed by the URIIndex for this namespace
     * which is statically maintained by <tt>XMLEvent</tt>.  The
     * values in the <tt>HashMap</tt>s are indexed by the local name of the
     * attribute.
     * The <tt>ArrayList</tt> will not be created for a particular instance
     * of <tt>FOAttributes</tt> unless a namespace other than the standard
     * XSL namespace is activated for this instance.
     * See <i>foAttrList</i>.
     */
    private ArrayList nSpaceAttrLists;

    /**
     * <i>foAttrList</i> is a <tt>HashMap</tt> to hold the FO namespace
     * attribute list specified in the FO element with which this list is
     * associated.  The <tt>String</tt> attribute value is stored
     * indexed by the integer constant property identifier from
     * <tt>PropertyConsts</tt>.
     */
    private HashMap foAttrList = new HashMap(0);

    private int DefAttrNSIndex = XMLEvent.DefAttrNSIndex;

    /**
     * Construct an <i>FOAttributes</i> object from the <i>startElement</i>
     * <tt>XMLEvent</tt> which triggered the construction of its parent
     * element.
     * <p>The <tt>Attributes</tt> object on the event is scanned, and each
     * attribute is examined.  If the attribute is in the default namespace
     * for fo: attributes, it is an fo: property, and its value is entered
     * into the <i>foAttrList</i> <tt>Hashmap>/tt> indexed by the property
     * index.
     * <p>If the attribute does not belong to the default namespace, its
     * value is entered into the appropriate <tt>HashMap</tt> in the
     * <tt>ArrayList</tt> <i>nSpaceAttrLists</i>, indexed by the attribute's
     * local name.
     * <p>
     */
    public FOAttributes (XMLEvent event) throws FOPException {
        // If the event is null, there is no event associated with this
        // node, probably because this is a manufactured node; e.g.,
        // an "invented" FopageSequenceMaster.  The default initialisation
        // includes an empty foAttrList HashMap.
        if (event == null) return;
            
        // Create the foAttrList.
        Attributes attributes = event.attributes;
        if (attributes == null) throw new FOPException
                                       ("No Attributes in XMLEvent");
        int propIndex;
        HashMap tmpHash;
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrUri = attributes.getURI(i);
            String attrLocalname = attributes.getLocalName(i);
            String attrValue = attributes.getValue(i);
            int attrUriIndex = XMLEvent.getURIIndex(attrUri);
            //System.out.println("FONode:" + event);
            if (attrUriIndex == DefAttrNSIndex) {
                // Standard FO namespace
                // Catch default namespace declaration here.  This seems to
                // be a kludge.  Should 'xmlns' come through here?
                if (attrLocalname.equals("xmlns")) break;
                // Is this a known (valid) property?
                try {
                    // throws PropertyException if invalid
                    propIndex =
                            PropertyConsts.getPropertyIndex(attrLocalname);
                    // Known attribute name
                    foAttrList.put(Ints.consts.get(propIndex), attrValue);
                } catch (PropertyException e) {
                    // Not known - ignore
                    MessageHandler.errorln(event.qName + " "
                                           + attributes.getQName(i)
                                           + " not recognized.  Ignoring.");
                }
            } else { // Not the XSL FO namespace
                int j;
                if (nSpaceAttrLists == null) {
                    //Create the list
                    System.out.println("Creating nSpaceAttrLists");
                    nSpaceAttrLists = new ArrayList(attrUriIndex + 1);
                    // Add the fo list
                    for (j = 0; j < DefAttrNSIndex; j++)
                        nSpaceAttrLists.add(new HashMap(0));

                    System.out.println("Adding foAttrList");
                    nSpaceAttrLists.add(foAttrList);
                }
                // Make sure there are elements between the last current
                // and the one to be added
                for (j = nSpaceAttrLists.size(); j <= attrUriIndex; j++)
                    nSpaceAttrLists.add(new HashMap(0));
                
                // Does a HashMap exist for this namespace?
                if ((tmpHash =
                     (HashMap)nSpaceAttrLists.get(attrUriIndex)) == null) {
                    tmpHash = new HashMap(1);
                    nSpaceAttrLists.set(attrUriIndex, tmpHash);
                }
                // Now put this value in the HashMap
                tmpHash.put(attrLocalname, attrValue);
            }
        }
    }

    /**
     * @return a unmodifiable <tt>Map</tt> containing the the attribute
     * values for all of the default attribute namespace attributes in this
     * attribute list, indexed by the property name index from
     * <tt>PropNames</tt>.
     */
    public Map getFoAttrList() {
        return Collections.unmodifiableMap((Map)foAttrList);
    }

    /**
     * A convenience method for accessing attribute values from the default
     * attribute namespace.
     * @param property an <tt>int</tt> containing the property name index
     * from <tt>PropNames</tt>.
     * @return a <tt>String</tt> containing the associated property value.
     */
    public String getFoAttrValue(int property) {
        return (String)(foAttrList.get(Ints.consts.get(property)));
    }

    /**
     * A convenience method for accessing attribute values from the default
     * attribute namespace.
     * @param propertyName a <tt>String</tt> containing the property name.
     * @return a <tt>String</tt> containing the associated property value.
     */
    public String getFoAttrValue(String propertyName)
        throws PropertyException
    {
        return getFoAttrValue
                        (PropertyConsts.getPropertyIndex(propertyName));
    }

    /**
     * @param uriIndex an <tt>int</tt> containing the index of the attribute
     * values namespace, maintained in an <tt>XMLEvent</tt> <tt>static</tt>
     * array.
     * @return an unmodifiable <tt>Map</tt> of the attribute values
     * within the indexed namespace, for this attribute list, indexed by the
     * local name of the attribute.  The <tt>Map</tt> returned is
     * derived from the one maintained in <i>nSpaceAttrLists</i>.
     */
    public Map getAttrList(int uriIndex) {
        if (uriIndex == DefAttrNSIndex)
            return Collections.unmodifiableMap((Map)foAttrList);
        if (nSpaceAttrLists != null) {
            if (uriIndex >= nSpaceAttrLists.size()) return null;
            return Collections.unmodifiableMap
                    ((Map)(nSpaceAttrLists.get(uriIndex)));
        } else {
            return null;
        }
    }

    /**
     * @param uriIndex an <tt>int</tt> index of the URIs maintained
     * by <tt>XMLEvent</tt>.
     * @param localName a <tt>String</tt> with the local name of the
     * attribute.  In the case of the default attribute namespace, this
     * will be the fo property name.
     * @return a <tt>String</tt> containing the value of the attribute.
     */
    public String getUriAttrValue(int uriIndex, String localName)
        throws PropertyException
    {
        if (uriIndex == DefAttrNSIndex)
            return getFoAttrValue(PropertyConsts.getPropertyIndex(localName));
        return (String)
                (((HashMap)nSpaceAttrLists.get(uriIndex)).get(localName));
    }

    /**
     * Get the size of the <i>nSpaceAttrLists</i> <tt>ArrayList</tt>
     * containing attribute namespaces active in this set of attributes.
     * <i>N.B.</i> this may be zero if only the default attribute
     * namespace has been seen in the attribute set.
     * @return an <tt>int</tt> containing the size.
     */
    public int getNSpaceAttrListsSize() {
        if (nSpaceAttrLists == null)
            return 0;
        return nSpaceAttrLists.size();
    }

    /**
     */
    public void merge(FOAttributes foAttrs) {
        foAttrList.putAll(foAttrs.getFoAttrList());
        int attrLen = foAttrs.getNSpaceAttrListsSize();
        if (attrLen != 0) {
            // something to copy
            if (nSpaceAttrLists == null) {
                // no "foreign" attribute lists in this
                // copy the others in
                nSpaceAttrLists = new ArrayList(attrLen);
            }
            for (int i = nSpaceAttrLists.size(); i < attrLen; i++)
                nSpaceAttrLists.add(new HashMap(0));
            // Except for foAttrs, which has already been merged, merge
            // the entries from the merging foAttrs
            for (int i = 0; i < attrLen; i++) {
                // skip foAttrList
                if (i == DefAttrNSIndex) continue;
               ((HashMap) nSpaceAttrLists.get(i))
                       .putAll(foAttrs.getAttrList(i));
            }
        }
    }

}// FOAttributes
