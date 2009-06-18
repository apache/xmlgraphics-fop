/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Base class for representation of formatting objects and their processing.
 */
public abstract class FObj extends FONode implements Constants {
    
    /** the list of property makers */
    private static PropertyMaker[] propertyListTable = null;
    
    /** The immediate child nodes of this node. */
    protected List childNodes = null;

    /** The list of extension attachments, null if none */
    private List extensionAttachments = null;
    
    /** Used to indicate if this FO is either an Out Of Line FO (see rec)
        or a descendant of one.  Used during validateChildNode() FO 
        validation.
    */
    private boolean isOutOfLineFODescendant = false;

    /** Markers added to this element. */
    protected Map markers = null;

    /**
     * Create a new formatting object.
     * All formatting object classes extend this class.
     *
     * @param parent the parent node
     * @todo move propertyListTable initialization someplace else?
     */
    public FObj(FONode parent) {
        super(parent);
        
        // determine if isOutOfLineFODescendant should be set
        if (parent != null && parent instanceof FObj) {
            if (((FObj)parent).getIsOutOfLineFODescendant()) {
                isOutOfLineFODescendant = true;
            } else {
                int foID = getNameId();
                if (foID == FO_FLOAT || foID == FO_FOOTNOTE
                    || foID == FO_FOOTNOTE_BODY) {
                        isOutOfLineFODescendant = true;
                }
            }
        }
        
        if (propertyListTable == null) {
            propertyListTable = new PropertyMaker[Constants.PROPERTY_COUNT + 1];
            PropertyMaker[] list = FOPropertyMapping.getGenericMappings();
            for (int i = 1; i < list.length; i++) {
                if (list[i] != null) {
                    propertyListTable[i] = list[i];
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
                            Attributes attlist, PropertyList pList) throws FOPException {
        setLocator(locator);
        pList.addAttributesToList(attlist);
        pList.setWritingMode();
        bind(pList);
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
     * This methods checks that the id isn't already used by another
     * fo and sets the id attribute of this object.
     * @param id ID to check
     * @throws ValidationException if the ID is already defined elsewhere
     */
    protected void checkId(String id) throws ValidationException {
        if (!id.equals("")) {
            Set idrefs = getFOEventHandler().getIDReferences();
            if (!idrefs.contains(id)) {
                idrefs.add(id);
            } else {
                throw new ValidationException("Property id \"" + id 
                        + "\" previously used; id values must be unique"
                        + " in document.", locator);
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
        if (PropertySets.canHaveMarkers(getNameId()) && child.getNameId() == FO_MARKER) {
            addMarker((Marker)child);
        } else {
            ExtensionAttachment attachment = child.getExtensionAttachment();
            if (attachment != null) {
                //This removes the element from the normal children, so no layout manager
                //is being created for them as they are only additional information.
                addExtensionAttachment(attachment);
            } else {
                if (childNodes == null) {
                    childNodes = new java.util.ArrayList();
                }
                childNodes.add(child);
            }
        }
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
     * Notifies a FObj that one of it's children is removed.
     * This method is subclassed by Block to clear the firstInlineChild variable.
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
                FONode node = (FONode)iter.next();
                if (node instanceof FOText) {
                    FOText text = (FOText)node;
                    if (text.willCreateArea()) {
                        getLogger().error("fo:marker must be an initial child: " + mcname);
                        return;
                    } else {
                        iter.remove();
                        notifyChildRemoval(node);
                    }
                } else {
                    getLogger().error("fo:marker must be an initial child: " + mcname);
                    return;
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
     * @return th collection of Markers attached to this object
     */
    public Map getMarkers() {
        return markers;
    }

    /*
     * Return a string representation of the fo element.
     * Deactivated in order to see precise ID of each fo element created
     *    (helpful for debugging)
     */
/*    public String toString() {
        return getName() + " at line " + line + ":" + column;
    }
*/    

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
                        || lName.equals("basic-link")
                        || (lName.equals("multi-toggle")
                                && (getNameId() == FO_MULTI_CASE 
                                        || findAncestor(FO_MULTI_CASE) > 0))
                        || (lName.equals("footnote") && !isOutOfLineFODescendant)
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
     * @param ancestorID -- Constants ID of node name to check for (e.g., FO_ROOT)
     * @return number of levels above FO where ancestor exists, 
     *    -1 if not found
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
     * Add a new extension attachment to this FObj. See org.apache.fop.fo.FONode for details.
     * @param attachment the attachment to add.
     */
    public void addExtensionAttachment(ExtensionAttachment attachment) {
        if (attachment == null) {
            throw new NullPointerException("Parameter attachment must not be null");
        }
        if (extensionAttachments == null) {
            extensionAttachments = new java.util.ArrayList();
        }
        if (log.isDebugEnabled()) {
            getLogger().debug("ExtensionAttachment of category " + attachment.getCategory() 
                    + " added to " + getName() + ": " + attachment);
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
    
}

