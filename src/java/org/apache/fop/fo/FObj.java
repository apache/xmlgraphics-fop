/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.PropertyMaker;
import org.apache.fop.layoutmgr.AddLMVisitor;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Base class for representation of formatting objects and their processing.
 */
public class FObj extends FONode implements Constants {
    public static PropertyMaker[] propertyListTable = null;
    
    /** Formatting properties for this fo element. */
    protected PropertyList propertyList;

    /** Property manager for providing refined properties/traits. */
    protected PropertyManager propMgr;

    /** The immediate child nodes of this node. */
    public ArrayList childNodes = null;

    /** Used to indicate if this FO is either an Out Of Line FO (see rec)
        or a descendant of one.  Used during validateChildNode() FO 
        validation.
    */
    private boolean isOutOfLineFODescendant = false;

    /** Id of this fo element or null if no id. */
    protected String id = null;

    /** Markers added to this element. */
    protected Map markers = null;

    /** Dynamic layout dimension. Used to resolve relative lengths. */
    protected Map layoutDimension = null;

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
            if (((FObj)parent).getIsOutOfLineFODescendant() == true) {
                isOutOfLineFODescendant = true;
            } else if ("fo:float".equals(getName())
                || "fo:footnote".equals(getName())
                || "fo:footnote-body".equals(getName())) {
                isOutOfLineFODescendant = true;
            }
        }
        
        if (propertyListTable == null) {
            propertyListTable = new PropertyMaker[Constants.PROPERTY_COUNT+1];
            PropertyMaker[] list = FOPropertyMapping.getGenericMappings();
            for (int i = 1; i < list.length; i++) {
                if (list[i] != null)
                    propertyListTable[i] = list[i]; 
            }
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#processNode
     */
    public void processNode(String elementName, Locator locator, 
                            Attributes attlist) throws SAXParseException {
        setLocation(locator);
        addProperties(attlist);
    }

    /**
     * Set properties for this FO based on node attributes
     * @param attlist Collection of attributes passed to us from the parser.
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        FObj parentFO = findNearestAncestorFObj();
        PropertyList parentPL = null;

        if (parentFO != null) {
            parentPL = parentFO.getPropertiesForNamespace(FOElementMapping.URI);
        }

        propertyList = new PropertyList(this, parentPL, FOElementMapping.URI);
        propertyList.addAttributesToList(attlist);
        propMgr = new PropertyManager(propertyList);
        setWritingMode();
        
        // if this FO can have a PR_ID, make sure it is unique
        if (PropertySets.canHaveId(getNameId())) {
            setupID();
        }
    }

    /**
     * Setup the id for this formatting object.
     * Most formatting objects can have an id that can be referenced.
     * This methods checks that the id isn't already used by another
     * fo and sets the id attribute of this object.
     */
    private void setupID() {
        Property prop = this.propertyList.get(PR_ID);
        if (prop != null) {
            String str = prop.getString();
            if (str != null && !str.equals("")) {
                Set idrefs = getFOInputHandler().getIDReferences();
                if (!idrefs.contains(str)) {
                    id = str;
                    idrefs.add(id);
                } else {
                    getLogger().warn("duplicate id:" + str + " ignored");
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
     * Return the PropertyManager object for this FO.  PropertyManager
     * tends to hold the traits for this FO, and is primarily used in layout.
     * @return the property manager for this FO
     */
    public PropertyManager getPropertyManager() {
        return propMgr;
    }

    /**
     * Return the property list object for this FO.  PropertyList tends
     * to hold the base, pre-trait properties for this FO, either explicitly
     * declared in the input XML or from inherited values.
     */
    public PropertyList getPropertyList() {
        return propertyList;
    }

    /**
     * Helper method to quickly obtain the value of a property
     * for this FO, without querying for the propertyList first.
     * @param name - the name of the desired property to obtain
     * @return the property
     */
    public Property getProperty(int propId) {
        return propertyList.get(propId);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) {
        if (PropertySets.canHaveMarkers(getNameId()) && 
                "fo:marker".equals(child.getName())) {
            addMarker((Marker) child);
        } else {
            if (childNodes == null) {
                childNodes = new ArrayList();
            }
            childNodes.add(child);
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
     * Find nearest ancestor which generates Reference Areas.
     *
     * @param includeSelf Set to true to consider the current FObj as an
     * "ancestor". Set to false to only return a true ancestor.
     * @param returnRoot Supposing a condition where no appropriate ancestor
     * FObj is found, setting returnRoot to true will return the FObj with no
     * parent (presumably the root FO). Otherwise, null will be returned.
     * Note that this will override a false setting for includeSelf, and return
     * the current node if it is the root FO. Setting returnRoot to true should
     * always return a valid FObj.
     * @return FObj of the nearest ancestor that generates Reference Areas
     * and fits the parameters.
     */
    private FObj findNearestAncestorGeneratingRAs(boolean includeSelf,
                                                  boolean returnRoot) {
        FObj p = this;
        if (includeSelf && p.generatesReferenceAreas()) {
            return p;
        }
        FObj parent = p.findNearestAncestorFObj();
        if (parent == null && returnRoot) {
            return p;
        }
        do {
            p = parent;
            parent = p.findNearestAncestorFObj();
        } while (parent != null && !p.generatesReferenceAreas());
        if (p.generatesReferenceAreas()) {
            return p;
        }
        // if we got here, it is because parent is null
        if (returnRoot) {
            return p;
        } else {
            return null;
        }
    }

    /**
     * For a given namespace, determine whether the properties of this object
     * match that namespace.
     * @param nameSpaceURI the namespace URI to be tested against
     * @return this.propertyList, if the namespaces match; otherwise, null
     */
    public PropertyList getPropertiesForNamespace(String nameSpaceURI) {
        if (this.propertyList == null) {
            return null;
        }
        if (!nameSpaceURI.equals(this.propertyList.getNameSpace())) {
            return null;
        }
        return this.propertyList;
    }

    /* This section is the implemenation of the property context. */

    /**
     * Assign the size of a layout dimension to the key. 
     * @param key the Layout dimension, from PercentBase.
     * @param dimension The layout length.
     */
    public void setLayoutDimension(Integer key, int dimension) {
        if (layoutDimension == null){
            layoutDimension = new HashMap();
        }
        layoutDimension.put(key, new Integer(dimension));
    }
    
    /**
     * Assign the size of a layout dimension to the key. 
     * @param key the Layout dimension, from PercentBase.
     * @param dimension The layout length.
     */
    public void setLayoutDimension(Integer key, float dimension) {
        if (layoutDimension == null){
            layoutDimension = new HashMap();
        }
        layoutDimension.put(key, new Float(dimension));
    }
    
    /**
     * Return the size associated with the key.
     * @param key The layout dimension key.
     * @return the length.
     */
    public Number getLayoutDimension(Integer key) {
        if (layoutDimension != null) {
            Number result = (Number) layoutDimension.get(key);
            if (result != null) {
                return result;
            }
        }
        if (parent != null) {
            return ((FObj) parent).getLayoutDimension(key);
        }
        return new Integer(0);
    }

    /**
     * Get the id string for this formatting object.
     * This will be unique for the fo document.
     *
     * @return the id string or null if not set
     */
    public String getID() {
        return id;
    }

    /**
     * Check if this formatting object generates reference areas.
     *
     * @return true if generates reference areas
     */
    public boolean generatesReferenceAreas() {
        return false;
    }

    /**
     * Check if this formatting object generates inline areas.
     *
     * @return true if generates inline areas
     */
    public boolean generatesInlineAreas() {
        return true;
    }

    /**
     * Set writing mode for this FO.
     * Use that from the nearest ancestor, including self, which generates
     * reference areas, or from root FO if no ancestor found.
     */
    protected void setWritingMode() {
        FObj p = findNearestAncestorGeneratingRAs(true, true);
        this.propertyList.setWritingMode(
          p.getProperty(PR_WRITING_MODE).getEnum());
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
     * Add the marker to this formatting object.
     * If this object can contain markers it checks that the marker
     * has a unique class-name for this object and that it is
     * the first child.
     * @param marker Marker to add.
     */
    public void addMarker(Marker marker) {
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
                    }
                } else {
                    getLogger().error("fo:marker must be an initial child: " + mcname);
                    return;
                }
            }
        }
        if (markers == null) {
            markers = new HashMap();
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

    /**
     * Return a LayoutManager responsible for laying out this FObj's content.
     * Must override in subclasses if their content can be laid out.
     * @param list the list to which the layout manager(s) should be added
     */
    public void addLayoutManager(List list) {
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
        return (nsURI == FOElementMapping.URI && 
            (lName.equals("block") 
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
        return (nsURI == FOElementMapping.URI && 
            (lName.equals("bidi-override") 
            || lName.equals("character") 
            || lName.equals("external-graphic") 
            || lName.equals("instream-foreign-object")
            || lName.equals("inline") 
            || lName.equals("inline-container")
            || lName.equals("leader") 
            || lName.equals("page-number") 
            || lName.equals("page-number-citation")
            || lName.equals("basic-link")
            || lName.equals("multi-toggle")
            || (!isOutOfLineFODescendant && lName.equals("footnote"))
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
        return (nsURI == FOElementMapping.URI && 
            (lName.equals("multi-switch") 
            || lName.equals("multi-properties")
            || lName.equals("wrapper") 
            || (!isOutOfLineFODescendant && lName.equals("float"))
            || lName.equals("retrieve-marker")));
    }
    
    /**
     * Convenience method for validity checking.  Checks if the
     * current node has an ancestor of a given name.
     * @param ancestorName -- node name to check for (e.g., "fo:root")
     * @return number of levels above FO where ancestor exists, 
     *    -1 if not found
     */
    protected int findAncestor(String ancestorName) {
        int found = 1;
        FONode temp = getParent();
        while (temp != null) {
            if (temp.getName().equals(ancestorName)) {
                return found;
            }
            found += 1;
            temp = temp.getParent();
        }
        return -1;
    }

    /**
     * Returns the name of this FO (e.g., "fo:root");
     * @return the name of the FO
     */
    public String getName() {
        return null;
    }

    /**
     * Returns the Constants class integer value of this formatting object
     * @return the integer enumeration of this FO (e.g., FO_ROOT)
     */
    public int getNameId() {
        return FO_UNKNOWN;
    }
}

