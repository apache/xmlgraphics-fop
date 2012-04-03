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
 * All standard formatting object classes extend this class.
 */
public abstract class FObj extends FONode implements Constants {

    /** the list of property makers */
    private static final PropertyMaker[] PROPERTY_LIST_TABLE
                            = FOPropertyMapping.getGenericMappings();

    /** pointer to the descendant subtree */
    protected FONode firstChild;

    /** pointer to the end of the descendant subtree */
    protected FONode lastChild;

    /** The list of extension attachments, null if none */
    private List<ExtensionAttachment> extensionAttachments = null;

    /** The map of foreign attributes, null if none */
    private Map<QName, String> foreignAttributes = null;

    /** Used to indicate if this FO is either an Out Of Line FO (see rec)
     *  or a descendant of one. Used during FO validation.
     */
    private boolean isOutOfLineFODescendant = false;

    /** Markers added to this element. */
    private Map markers = null;

    private int bidiLevel = -1;

    // The value of properties relevant for all fo objects
    private String id = null;
    // End of property values

    /**
     * Create a new formatting object.
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

    /** {@inheritDoc} */
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
        return PROPERTY_LIST_TABLE[propId];
    }

    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList pList)
                    throws FOPException {
        setLocator(locator);
        pList.addAttributesToList(attlist);
        if (!inMarker() || "marker".equals(elementName)) {
            bind(pList);
        }
    }

    /**
     * Create a default property list for this element.
     * {@inheritDoc}
     */
    protected PropertyList createPropertyList(PropertyList parent,
                    FOEventHandler foEventHandler) throws FOPException {
        return getBuilderContext().getPropertyListMaker().make(this, parent);
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
            Set idrefs = getBuilderContext().getIDReferences();
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
    boolean getIsOutOfLineFODescendant() {
        return isOutOfLineFODescendant;
    }

    /** {@inheritDoc}*/
    protected void addChildNode(FONode child) throws FOPException {
        if (child.getNameId() == FO_MARKER) {
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
                    lastChild = child;
                } else {
                    if (lastChild == null) {
                        FONode prevChild = firstChild;
                        while (prevChild.siblings != null
                                && prevChild.siblings[1] != null) {
                            prevChild = prevChild.siblings[1];
                        }
                        FONode.attachSiblings(prevChild, child);
                    } else {
                        FONode.attachSiblings(lastChild, child);
                        lastChild = child;
                    }
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
    protected static void addChildTo(FONode child, FONode parent)
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
        if (child == lastChild) {
            if (child.siblings != null) {
                lastChild = siblings[0];
            } else {
                lastChild = null;
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
     * TODO see if needed
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
    void notifyChildRemoval(FONode node) {
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
    protected String getContextInfoAlt() {
        StringBuffer sb = new StringBuffer();
        if (getLocalName() != null) {
            sb.append(getName());
            sb.append(", ");
        }
        if (hasId()) {
            sb.append("id=").append(getId());
            return sb.toString();
        }
        String s = gatherContextInfo();
        if (s != null) {
            sb.append("\"");
            if (s.length() < 32) {
                sb.append(s);
            } else {
                sb.append(s.substring(0, 32));
                sb.append("...");
            }
            sb.append("\"");
            return sb.toString();
        } else {
            return null;
        }
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
     *
     * @param nsURI namespace URI of incoming node
     * @param lName local name (i.e., no prefix) of incoming node
     * @return true if a member, false if not
     */
    protected boolean isBlockItem(String nsURI, String lName) {
        return (FO_URI.equals(nsURI)
                && ("block".equals(lName)
                        || "table".equals(lName)
                        || "table-and-caption".equals(lName)
                        || "block-container".equals(lName)
                        || "list-block".equals(lName)
                        || "float".equals(lName)
                        || isNeutralItem(nsURI, lName)));
    }

    /**
     * Convenience method for validity checking.  Checks if the
     * incoming node is a member of the "%inline;" parameter entity
     * as defined in Sect. 6.2 of the XSL 1.0 & 1.1 Recommendations
     *
     * @param nsURI namespace URI of incoming node
     * @param lName local name (i.e., no prefix) of incoming node
     * @return true if a member, false if not
     */
    protected boolean isInlineItem(String nsURI, String lName) {
        return (FO_URI.equals(nsURI)
                && ("bidi-override".equals(lName)
                        || "character".equals(lName)
                        || "external-graphic".equals(lName)
                        || "instream-foreign-object".equals(lName)
                        || "inline".equals(lName)
                        || "inline-container".equals(lName)
                        || "leader".equals(lName)
                        || "page-number".equals(lName)
                        || "page-number-citation".equals(lName)
                        || "page-number-citation-last".equals(lName)
                        || "basic-link".equals(lName)
                        || ("multi-toggle".equals(lName)
                                && (getNameId() == FO_MULTI_CASE
                                        || findAncestor(FO_MULTI_CASE) > 0))
                        || ("footnote".equals(lName)
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
                && ("multi-switch".equals(lName)
                        || "multi-properties".equals(lName)
                        || "wrapper".equals(lName)
                        || (!isOutOfLineFODescendant && "float".equals(lName))
                        || "retrieve-marker".equals(lName)
                        || "retrieve-table-marker".equals(lName)));
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

    /**
     * Clears the list of child nodes.
     */
    public void clearChildNodes() {
        this.firstChild = null;
    }

    /** @return the "id" property. */
    public String getId() {
        return id;
    }

    /** @return whether this object has an id set */
    public boolean hasId() {
        return (id != null && id.length() > 0);
    }

    /** {@inheritDoc} */
    public String getNamespaceURI() {
        return FOElementMapping.URI;
    }

    /** {@inheritDoc} */
    public String getNormalNamespacePrefix() {
        return "fo";
    }

    /** {@inheritDoc} */
    public boolean isBidiRangeBlockItem() {
        String ns = getNamespaceURI();
        String ln = getLocalName();
        return !isNeutralItem(ns, ln) && isBlockItem(ns, ln);
    }

    /**
     * Recursively set resolved bidirectional level of FO (and its ancestors) if
     * and only if it is non-negative and if either the current value is reset (-1)
     * or the new value is less than the current value.
     * @param bidiLevel a non-negative bidi embedding level
     */
    public void setBidiLevel(int bidiLevel) {
        assert bidiLevel >= 0;
        if ( bidiLevel >= 0 ) {
            if ( ( this.bidiLevel < 0 ) || ( bidiLevel < this.bidiLevel ) ) {
                this.bidiLevel = bidiLevel;
                if ( parent != null ) {
                    FObj foParent = (FObj) parent;
                    int parentBidiLevel = foParent.getBidiLevel();
                    if ( ( parentBidiLevel < 0 ) || ( bidiLevel < parentBidiLevel ) ) {
                        foParent.setBidiLevel ( bidiLevel );
                    }
                }
            }
        }
    }

    /**
     * Obtain resolved bidirectional level of FO.
     * @return either a non-negative bidi embedding level or -1
     * in case no bidi levels have been assigned
     */
    public int getBidiLevel() {
        return bidiLevel;
    }

    /**
     * Obtain resolved bidirectional level of FO or nearest FO
     * ancestor that has a resolved level.
     * @return either a non-negative bidi embedding level or -1
     * in case no bidi levels have been assigned to this FO or
     * any ancestor
     */
    public int getBidiLevelRecursive() {
        for ( FONode fn = this; fn != null; fn = fn.getParent() ) {
            if ( fn instanceof FObj ) {
                int level = ( (FObj) fn).getBidiLevel();
                if ( level >= 0 ) {
                    return level;
                }
            }
        }
        return -1;
    }

    /**
     * Add a new extension attachment to this FObj.
     * (see org.apache.fop.fo.FONode for details)
     *
     * @param attachment the attachment to add.
     */
    void addExtensionAttachment(ExtensionAttachment attachment) {
        if (attachment == null) {
            throw new NullPointerException(
                    "Parameter attachment must not be null");
        }
        if (extensionAttachments == null) {
            extensionAttachments = new java.util.ArrayList<ExtensionAttachment>();
        }
        if (log.isDebugEnabled()) {
            log.debug("ExtensionAttachment of category "
                    + attachment.getCategory() + " added to "
                    + getName() + ": " + attachment);
        }
        extensionAttachments.add(attachment);
    }

    /** @return the extension attachments of this FObj. */
    public List/*<ExtensionAttachment>*/ getExtensionAttachments() {
        if (extensionAttachments == null) {
            return Collections.EMPTY_LIST;
        } else {
            return extensionAttachments;
        }
    }

    /** @return true if this FObj has extension attachments */
    public boolean hasExtensionAttachments() {
        return extensionAttachments != null;
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
            foreignAttributes = new java.util.HashMap<QName, String>();
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

    /** Basic {@link FONode.FONodeIterator} implementation */
    public static class FObjIterator implements FONodeIterator {

        private static final int F_NONE_ALLOWED = 0;
        private static final int F_SET_ALLOWED = 1;
        private static final int F_REMOVE_ALLOWED = 2;

        private FONode currentNode;
        private final FObj parentNode;
        private int currentIndex;
        private int flags = F_NONE_ALLOWED;

        FObjIterator(FObj parent) {
            this.parentNode = parent;
            this.currentNode = parent.firstChild;
            this.currentIndex = 0;
            this.flags = F_NONE_ALLOWED;
        }

        /** {@inheritDoc} */
        public FObj parentNode() {
            return parentNode;
        }

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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
                if (currentNode == parentNode.lastChild) {
                    parentNode.lastChild = newNode;
                }
            } else {
                throw new IllegalStateException();
            }
        }

        /** {@inheritDoc} */
        public void add(Object o) {
            FONode newNode = (FONode) o;
            if (currentIndex == -1) {
                if (currentNode != null) {
                    FONode.attachSiblings(newNode, currentNode);
                }
                parentNode.firstChild = newNode;
                currentIndex = 0;
                currentNode = newNode;
                if (parentNode.lastChild == null) {
                    parentNode.lastChild = newNode;
                }
            } else {
                if (currentNode.siblings != null
                        && currentNode.siblings[1] != null) {
                    FONode.attachSiblings((FONode) o, currentNode.siblings[1]);
                }
                FONode.attachSiblings(currentNode, (FONode) o);
                if (currentNode == parentNode.lastChild) {
                    parentNode.lastChild = newNode;
                }
            }
            flags &= F_NONE_ALLOWED;
        }

        /** {@inheritDoc} */
        public boolean hasNext() {
            return (currentNode != null)
                && ((currentIndex == 0)
                        || (currentNode.siblings != null
                            && currentNode.siblings[1] != null));
        }

        /** {@inheritDoc} */
        public boolean hasPrevious() {
            return (currentIndex != 0)
                || (currentNode.siblings != null
                    && currentNode.siblings[0] != null);
        }

        /** {@inheritDoc} */
        public int nextIndex() {
            return currentIndex + 1;
        }

        /** {@inheritDoc} */
        public int previousIndex() {
            return currentIndex - 1;
        }

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
        public FONode lastNode() {
            while (currentNode != null
                    && currentNode.siblings != null
                    && currentNode.siblings[1] != null) {
                currentNode = currentNode.siblings[1];
                currentIndex++;
            }
            return currentNode;
        }

        /** {@inheritDoc} */
        public FONode firstNode() {
            currentNode = parentNode.firstChild;
            currentIndex = 0;
            return currentNode;
        }

        /** {@inheritDoc} */
        public FONode nextNode() {
            return (FONode) next();
        }

        /** {@inheritDoc} */
        public FONode previousNode() {
            return (FONode) previous();
        }
    }
}
