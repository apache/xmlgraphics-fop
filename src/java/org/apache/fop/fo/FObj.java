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
import java.util.NoSuchElementException;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.properties.PropertyMaker;

/**
 * Base class for representation of formatting objects and their processing.
 */
public abstract class FObj extends FONode implements Constants {
    
    /** the list of property makers */
    private static PropertyMaker[] propertyListTable
                            = FOPropertyMapping.getGenericMappings();
    
    /** 
     * pointer to the descendant subtree
     */
    protected FONode firstChild;
    
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
    
    // The value of properties relevant for all fo objects
    private String id = null;
    // End of property values

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
     * {@inheritDoc} 
     */
    public FONode clone(FONode parent, boolean removeChildren)
        throws FOPException {
        FObj fobj = (FObj) super.clone(parent, removeChildren);
        if (removeChildren) {
            fobj.firstChild = null;
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
        id = pList.get(PR_ID).getString();
    }

    /**
     * {@inheritDoc}
     * @throws FOPException FOP Exception
     */
    protected void startOfNode() throws FOPException {
        if (id != null) {
            checkId(id);
        }
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
    private void checkId(String id) throws ValidationException {
        if (!inMarker() && !id.equals("")) {
            Set idrefs = getFOEventHandler().getIDReferences();
            if (!idrefs.contains(id)) {
                idrefs.add(id);
            } else {
                getFOValidationEventProducer().idNotUnique(this, getName(), id, true, locator);
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
     * {@inheritDoc}
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
                if (firstChild == null) {
                    firstChild = child;
                } else {
                    FONode prevChild = firstChild;
                    while (prevChild.siblings != null
                            && prevChild.siblings[1] != null) {
                        prevChild = prevChild.siblings[1];
                    }
                    FONode.attachSiblings(prevChild, child);
                }
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
    
    /** {@inheritDoc} */
    public void removeChild(FONode child) {
        FONode nextChild = null;
        if (child.siblings != null) {
            nextChild = child.siblings[1];
        }
        if (child == firstChild) {
            firstChild = nextChild;
            if (firstChild != null) {
                firstChild.siblings[0] = null;
            }
        } else {
            FONode prevChild = child.siblings[0];
            prevChild.siblings[1] = nextChild;
            if (nextChild != null) {
                nextChild.siblings[0] = prevChild;
            }
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

    /** {@inheritDoc} */
    public FONodeIterator getChildNodes() {
        if (hasChildren()) {
            return new FObjIterator(this);
        }
        return null;
    }

    /**
     * Indicates whether this formatting object has children.
     * @return true if there are children
     */
    public boolean hasChildren() {
        return this.firstChild != null;
    }
    
    /**
     * Return an iterator over the object's childNodes starting
     * at the passed-in node (= first call to iterator.next() will
     * return childNode)
     * @param childNode First node in the iterator
     * @return A ListIterator or null if childNode isn't a child of
     * this FObj.
     */
    public FONodeIterator getChildNodes(FONode childNode) {
        FONodeIterator it = getChildNodes();
        if (it != null) {
            if (firstChild == childNode) {
                return it;
            } else {
                while (it.hasNext()
                        && it.nextNode().siblings[1] != childNode) {
                    //nop
                }
                if (it.hasNext()) {
                    return it;
                } else {
                    return null;
                }
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
        if (firstChild != null) {
            // check for empty childNodes
            for (Iterator iter = getChildNodes(); iter.hasNext();) {
                FONode node = (FONode) iter.next();
                if (node instanceof FObj
                        || (node instanceof FOText
                                && ((FOText) node).willCreateArea())) {
                    getFOValidationEventProducer().markerNotInitialChild(this, getName(),
                            mcname, locator);
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
            getFOValidationEventProducer().markerNotUniqueForSameParent(this, getName(),
                    mcname, locator);
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

    /** {@inheritDoc} */
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
    
    /** @return the "id" property. */
    public String getId() {
        return id;
    }
    
    /** @return whether this object has an id set */
    public boolean hasId() {
        return id != null && id.length() > 0;
    }

    /** {@inheritDoc} */
    public String getNamespaceURI() {
        return FOElementMapping.URI;
    }

    /** {@inheritDoc} */
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
            log.debug("ExtensionAttachment of category " 
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
    
    /** {@inheritDoc} */
    public String toString() {
        return (super.toString() + "[@id=" + this.id + "]");
    }


    public class FObjIterator implements FONodeIterator {
        
        private static final int F_NONE_ALLOWED = 0;
        private static final int F_SET_ALLOWED = 1;
        private static final int F_REMOVE_ALLOWED = 2;
        
        private FONode currentNode;
        private FObj parentNode;
        private int currentIndex;
        private int flags = F_NONE_ALLOWED;
        
        protected FObjIterator(FObj parent) {
            this.parentNode = parent;
            this.currentNode = parent.firstChild;
            this.currentIndex = 0;
            this.flags = F_NONE_ALLOWED;
        }
        
        /**
         * {@inheritDoc}
         */
        public FObj parentNode() {
            return parentNode;
        }
        
        /**
         * {@inheritDoc}
         */
        public Object next() {
            if (currentNode != null) {
                if (currentIndex != 0) {
                    if (currentNode.siblings != null
                        && currentNode.siblings[1] != null) {
                        currentNode = currentNode.siblings[1];
                    } else {
                        throw new NoSuchElementException();
                    }
                }
                currentIndex++;
                flags |= (F_SET_ALLOWED | F_REMOVE_ALLOWED);
                return currentNode;
            } else {
                throw new NoSuchElementException();
            }
        }

        /**
         * {@inheritDoc}
         */
        public Object previous() {
            if (currentNode.siblings != null
                    && currentNode.siblings[0] != null) {
                currentIndex--;
                currentNode = currentNode.siblings[0];
                flags |= (F_SET_ALLOWED | F_REMOVE_ALLOWED);
                return currentNode;
            } else {
                throw new NoSuchElementException();
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public void set(Object o) {
            if ((flags & F_SET_ALLOWED) == F_SET_ALLOWED) {
                FONode newNode = (FONode) o;
                if (currentNode == parentNode.firstChild) {
                    parentNode.firstChild = newNode;
                } else {
                    FONode.attachSiblings(currentNode.siblings[0], newNode);
                }
                if (currentNode.siblings != null
                        && currentNode.siblings[1] != null) {
                    FONode.attachSiblings(newNode, currentNode.siblings[1]);
                }
            } else {
                throw new IllegalStateException();
            }
        }
        
        /**
         * {@inheritDoc}
         */
        public void add(Object o) {
            FONode newNode = (FONode) o;
            if (currentIndex == -1) {
                if (currentNode != null) {
                    FONode.attachSiblings(newNode, currentNode);
                }
                parentNode.firstChild = newNode;
                currentIndex = 0;
                currentNode = newNode;
            } else {
                if (currentNode.siblings != null
                        && currentNode.siblings[1] != null) {
                    FONode.attachSiblings((FONode) o, currentNode.siblings[1]);
                }
                FONode.attachSiblings(currentNode, (FONode) o);
            }
            flags &= F_NONE_ALLOWED;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return (currentNode != null)
                && ((currentIndex == 0)
                        || (currentNode.siblings != null
                            && currentNode.siblings[1] != null));
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasPrevious() {
            return (currentIndex != 0)
                || (currentNode.siblings != null
                    && currentNode.siblings[0] != null);
        }
        
        /**
         * {@inheritDoc}
         */
        public int nextIndex() {
            return currentIndex + 1;
        }

        /**
         * {@inheritDoc}
         */
        public int previousIndex() {
            return currentIndex - 1;
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            if ((flags & F_REMOVE_ALLOWED) == F_REMOVE_ALLOWED) {
                parentNode.removeChild(currentNode);
                if (currentIndex == 0) {
                    //first node removed
                    currentNode = parentNode.firstChild;
                } else if (currentNode.siblings != null
                        && currentNode.siblings[0] != null) {
                    currentNode = currentNode.siblings[0];
                    currentIndex--;
                } else {
                    currentNode = null;
                }
                flags &= F_NONE_ALLOWED;
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * {@inheritDoc}
         */
        public FONode lastNode() {
            while (currentNode != null
                    && currentNode.siblings != null
                    && currentNode.siblings[1] != null) {
                currentNode = currentNode.siblings[1];
                currentIndex++;
            }
            return currentNode;
        }
        
        /**
         * {@inheritDoc}
         */
        public FONode firstNode() {
            currentNode = parentNode.firstChild;
            currentIndex = 0;
            return currentNode;
        }
        
        /**
         * {@inheritDoc}
         */
        public FONode nextNode() {
            return (FONode) next();
        }
        
        /**
         * {@inheritDoc}
         */
        public FONode previousNode() {
            return (FONode) previous();
        }
    }

}
