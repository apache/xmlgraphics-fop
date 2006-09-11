/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.properties.PropertyMaker;
import org.apache.fop.util.QName;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Base class for representation of formatting objects and their processing.
 */
public abstract class FObj extends FONode implements Constants {
    
    /** the list of property makers */
    private static PropertyMaker[] propertyListTable
                            = FOPropertyMapping.getGenericMappings();
    
    /** The immediate child nodes of this node. */
    protected List childNodes = null;

    /** The list of extension attachments, null if none */
    private List extensionAttachments = null;
    
    /** The map of foreign attributes, null if none */
    private Map foreignAttributes = null;
    
    /** Used to indicate if this FO is either an Out Of Line FO (see rec)
     *  or a descendant of one. Used during FO validation.
     */
    private boolean isOutOfLineFODescendant = false;

    /** Markers added to this element. */
    private Map markers = null;
    
    /**
     * Create a new formatting object.
     * All formatting object classes extend this class.
     *
     * @param parent the parent node
     */
    public FObj(FONode parent) {
        super(parent);
        
        // determine if isOutOfLineFODescendant should be set
        if (parent != null && parent instanceof FObj) {
            if (((FObj) parent).getIsOutOfLineFODescendant()) {
                isOutOfLineFODescendant = true;
            } else {
                int foID = getNameId();
                if (foID == FO_FLOAT || foID == FO_FOOTNOTE
                    || foID == FO_FOOTNOTE_BODY) {
                        isOutOfLineFODescendant = true;
                }
            }
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#clone(FONode, boolean)
     */
    public FONode clone(FONode parent, boolean removeChildren)
        throws FOPException {
        FObj fobj = (FObj) super.clone(parent, removeChildren);
        if (removeChildren) {
            fobj.childNodes = null;
        }
        return fobj;
    }
    
    /**
     * Returns the PropertyMaker for a given property ID.
     * @param propId the property ID
     * @return the requested Property Maker
     */
    public static PropertyMaker getPropertyMakerFor(int propId) {
        return propertyListTable[propId];
    }

    /**
     * @see org.apache.fop.fo.FONode#processNode
     */
    public void processNode(String elementName, Locator locator, 
                            Attributes attlist, PropertyList pList) 
                    throws FOPException {
        setLocator(locator);
        pList.addAttributesToList(attlist);
        if (!inMarker()
                || "marker".equals(elementName)) {
            pList.setWritingMode();
            bind(pList);
        }
    }

    /**
     * Create a default property list for this element. 
     * @see org.apache.fop.fo.FONode
     */
    protected PropertyList createPropertyList(PropertyList parent, 
                    FOEventHandler foEventHandler) throws FOPException {
        return foEventHandler.getPropertyListMaker().make(this, parent);
    }

    /**
     * Bind property values from the property list to the FO node.
     * Must be overridden in all FObj subclasses that have properties
     * applying to it.
     * @param pList the PropertyList where the properties can be found.
     * @throws FOPException if there is a problem binding the values
     */
    public void bind(PropertyList pList) throws FOPException {
    }

    /**
     * Setup the id for this formatting object.
     * Most formatting objects can have an id that can be referenced.
     * This methods checks that the id isn't already used by another FO
     * 
     * @param id    the id to check
     * @throws ValidationException if the ID is already defined elsewhere
     *                              (strict validation only)
     */
    protected void checkId(String id) throws ValidationException {
        if (!inMarker() && !id.equals("")) {
            Set idrefs = getFOEventHandler().getIDReferences();
            if (!idrefs.contains(id)) {
                idrefs.add(id);
            } else {
                if (getUserAgent().validateStrictly()) {
                    throw new ValidationException("Property id \"" + id 
                            + "\" previously used; id values must be unique"
                            + " in document.", locator);
                } else {
                    if (log.isWarnEnabled()) {
                        StringBuffer msg = new StringBuffer();
                        msg.append("Found non-unique id on ").append(getName());
                        if (locator.getLineNumber() != -1) {
                            msg.append(" (at ").append(locator.getLineNumber())
                                .append("/").append(locator.getColumnNumber())
                                .append(")");
                        }
                        msg.append("\nAny reference to it will be considered "
                                + "a reference to the first occurrence "
                                + "in the document.");
                        log.warn(msg);
                    }
                }
            }
        }
    }

    /**
     * Returns Out Of Line FO Descendant indicator.
     * @return true if Out of Line FO or Out Of Line descendant, false otherwise
     */
    public boolean getIsOutOfLineFODescendant() {
        return isOutOfLineFODescendant;
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) throws FOPException {
        if (canHaveMarkers() && child.getNameId() == FO_MARKER) {
            addMarker((Marker) child);
        } else {
            ExtensionAttachment attachment = child.getExtensionAttachment();
            if (attachment != null) {
                /* This removes the element from the normal children, 
                 * so no layout manager is being created for them 
                 * as they are only additional information.
                 */
                addExtensionAttachment(attachment);
            } else {
                if (childNodes == null) {
                    childNodes = new java.util.ArrayList();
                }
                childNodes.add(child);
            }
        }
    }

    /**
     * Used by RetrieveMarker during Marker-subtree cloning
     * @param child     the (cloned) child node
     * @param parent    the (cloned) parent node
     * @throws FOPException when the child could not be added to the parent
     */
    protected static void addChildTo(FONode child, FObj parent) 
                            throws FOPException {
        parent.addChildNode(child);
    }
    
    /** @see org.apache.fop.fo.FONode#removeChild(org.apache.fop.fo.FONode) */
    public void removeChild(FONode child) {
        if (childNodes != null) {
            childNodes.remove(child);
        }
    }
    
    /**
     * Find the nearest parent, grandparent, etc. FONode that is also an FObj
     * @return FObj the nearest ancestor FONode that is an FObj
     */
    public FObj findNearestAncestorFObj() {
        FONode par = parent;
        while (par != null && !(par instanceof FObj)) {
            par = par.parent;
        }
        return (FObj) par;
    }

    /**
     * Check if this formatting object generates reference areas.
     * @return true if generates reference areas
     * @todo see if needed
     */
    public boolean generatesReferenceAreas() {
        return false;
    }

    /**
     * @see org.apache.fop.fo.FONode#getChildNodes()
     */
    public ListIterator getChildNodes() {
        if (childNodes != null) {
            return childNodes.listIterator();
        }
        return null;
    }

    /**
     * Return an iterator over the object's childNodes starting
     * at the passed-in node.
     * @param childNode First node in the iterator
     * @return A ListIterator or null if childNode isn't a child of
     * this FObj.
     */
    public ListIterator getChildNodes(FONode childNode) {
        if (childNodes != null) {
            int i = childNodes.indexOf(childNode);
            if (i >= 0) {
                return childNodes.listIterator(i);
            }
        }
        return null;
    }

    /**
     * Return a FONode based on the index in the list of childNodes.
     * @param nodeIndex index of the node to return
     * @return the node or null if the index is invalid
     */
    public FONode getChildNodeAt(int nodeIndex) {
        if (childNodes != null) {
            if (nodeIndex >= 0 && nodeIndex < childNodes.size()) {
                return (FONode) childNodes.get(nodeIndex);
            }
        }
        return null;
    }

    /**
     * Notifies a FObj that one of it's children is removed.
     * This method is subclassed by Block to clear the 
     * firstInlineChild variable in case it doesn't generate
     * any areas (see addMarker()).
     * @param node the node that was removed
     */
    protected void notifyChildRemoval(FONode node) {
        //nop
    }
    
    /**
     * Add the marker to this formatting object.
     * If this object can contain markers it checks that the marker
     * has a unique class-name for this object and that it is
     * the first child.
     * @param marker Marker to add.
     */
    protected void addMarker(Marker marker) {
        String mcname = marker.getMarkerClassName();
        if (childNodes != null) {
            // check for empty childNodes
            for (Iterator iter = childNodes.iterator(); iter.hasNext();) {
                FONode node = (FONode) iter.next();
                if (node instanceof FObj
                        || (node instanceof FOText
                                && ((FOText) node).willCreateArea())) {
                    getLogger().error(
                            "fo:marker must be an initial child: " + mcname);
                    return;
                } else if (node instanceof FOText) {
                    iter.remove();
                    notifyChildRemoval(node);
                }
            }
        }
        if (markers == null) {
            markers = new java.util.HashMap();
        }
        if (!markers.containsKey(mcname)) {
            markers.put(mcname, marker);
        } else {
            getLogger().error("fo:marker 'marker-class-name' "
                    + "must be unique for same parent: " + mcname);
        }
    }

    /**
     * @return true if there are any Markers attached to this object
     */
    public boolean hasMarkers() {
        return markers != null && !markers.isEmpty();
    }

    /**
     * @return the collection of Markers attached to this object
     */
    public Map getMarkers() {
        return markers;
    }

    /** @see org.apache.fop.fo.FONode#gatherContextInfo() */
    protected String gatherContextInfo() {
        if (getLocator() != null) {
            return super.gatherContextInfo();
        } else {
            ListIterator iter = getChildNodes();
            if (iter == null) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            while (iter.hasNext()) {
                FONode node = (FONode) iter.next();
                String s = node.gatherContextInfo();
                if (s != null) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(s);
                }
            }
            return (sb.length() > 0 ? sb.toString() : null);
        }
    }

    /**
     * Convenience method for validity checking.  Checks if the
     * incoming node is a member of the "%block;" parameter entity
     * as defined in Sect. 6.2 of the XSL 1.0 & 1.1 Recommendations
     * @param nsURI namespace URI of incoming node
     * @param lName local name (i.e., no prefix) of incoming node 
     * @return true if a member, false if not
     */
    protected boolean isBlockItem(String nsURI, String lName) {
        return (FO_URI.equals(nsURI) 
                && (lName.equals("block") 
                        || lName.equals("table") 
                        || lName.equals("table-and-caption") 
                        || lName.equals("block-container")
                        || lName.equals("list-block") 
                        || lName.equals("float")
                        || isNeutralItem(nsURI, lName)));
    }

    /**
     * Convenience method for validity checking.  Checks if the
     * incoming node is a member of the "%inline;" parameter entity
     * as defined in Sect. 6.2 of the XSL 1.0 & 1.1 Recommendations
     * @param nsURI namespace URI of incoming node
     * @param lName local name (i.e., no prefix) of incoming node 
     * @return true if a member, false if not
     */
    protected boolean isInlineItem(String nsURI, String lName) {
        return (FO_URI.equals(nsURI) 
                && (lName.equals("bidi-override") 
                        || lName.equals("character") 
                        || lName.equals("external-graphic") 
                        || lName.equals("instream-foreign-object")
                        || lName.equals("inline") 
                        || lName.equals("inline-container")
                        || lName.equals("leader") 
                        || lName.equals("page-number") 
                        || lName.equals("page-number-citation")
                        || lName.equals("page-number-citation-last")
                        || lName.equals("basic-link")
                        || (lName.equals("multi-toggle")
                                && (getNameId() == FO_MULTI_CASE 
                                        || findAncestor(FO_MULTI_CASE) > 0))
                        || (lName.equals("footnote") 
                                && !isOutOfLineFODescendant)
                        || isNeutralItem(nsURI, lName)));
    }

    /**
     * Convenience method for validity checking.  Checks if the
     * incoming node is a member of the "%block;" parameter entity
     * or "%inline;" parameter entity
     * @param nsURI namespace URI of incoming node
     * @param lName local name (i.e., no prefix) of incoming node 
     * @return true if a member, false if not
     */
    protected boolean isBlockOrInlineItem(String nsURI, String lName) {
        return (isBlockItem(nsURI, lName) || isInlineItem(nsURI, lName));
    }

    /**
     * Convenience method for validity checking.  Checks if the
     * incoming node is a member of the neutral item list
     * as defined in Sect. 6.2 of the XSL 1.0 & 1.1 Recommendations
     * @param nsURI namespace URI of incoming node
     * @param lName local name (i.e., no prefix) of incoming node 
     * @return true if a member, false if not
     */
    protected boolean isNeutralItem(String nsURI, String lName) {
        return (FO_URI.equals(nsURI) 
                && (lName.equals("multi-switch") 
                        || lName.equals("multi-properties")
                        || lName.equals("wrapper") 
                        || (!isOutOfLineFODescendant && lName.equals("float"))
                        || lName.equals("retrieve-marker")));
    }
    
    /**
     * Convenience method for validity checking.  Checks if the
     * current node has an ancestor of a given name.
     * @param ancestorID    ID of node name to check for (e.g., FO_ROOT)
     * @return number of levels above FO where ancestor exists, 
     *         -1 if not found
     */
    protected int findAncestor(int ancestorID) {
        int found = 1;
        FONode temp = getParent();
        while (temp != null) {
            if (temp.getNameId() == ancestorID) {
                return found;
            }
            found += 1;
            temp = temp.getParent();
        }
        return -1;
    }
    
    
    /** @see org.apache.fop.fo.FONode#getNamespaceURI() */
    public String getNamespaceURI() {
        return FOElementMapping.URI;
    }

    /** @see org.apache.fop.fo.FONode#getNormalNamespacePrefix() */
    public String getNormalNamespacePrefix() {
        return "fo";
    }

    /**
     * Add a new extension attachment to this FObj. 
     * (see org.apache.fop.fo.FONode for details)
     * 
     * @param attachment the attachment to add.
     */
    public void addExtensionAttachment(ExtensionAttachment attachment) {
        if (attachment == null) {
            throw new NullPointerException(
                    "Parameter attachment must not be null");
        }
        if (extensionAttachments == null) {
            extensionAttachments = new java.util.ArrayList();
        }
        if (log.isDebugEnabled()) {
            getLogger().debug("ExtensionAttachment of category " 
                    + attachment.getCategory() + " added to " 
                    + getName() + ": " + attachment);
        }
        extensionAttachments.add(attachment);
    }
    
    /** @return the extension attachments of this FObj. */
    public List getExtensionAttachments() {
        if (extensionAttachments == null) {
            return Collections.EMPTY_LIST;
        } else {
            return extensionAttachments;
        }
    }

    /**
     * Adds a foreign attribute to this FObj.
     * @param attributeName the attribute name as a QName instance
     * @param value the attribute value
     */
    public void addForeignAttribute(QName attributeName, String value) {
        /* TODO: Handle this over FOP's property mechanism so we can use 
         *       inheritance.
         */
        if (attributeName == null) {
            throw new NullPointerException("Parameter attributeName must not be null");
        }
        if (foreignAttributes == null) {
            foreignAttributes = new java.util.HashMap();
        }
        foreignAttributes.put(attributeName, value);
    }
    
    /** @return the map of foreign attributes */
    public Map getForeignAttributes() {
        if (foreignAttributes == null) {
            return Collections.EMPTY_MAP;
        } else {
            return foreignAttributes;
        }
    }
}
