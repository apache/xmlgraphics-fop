/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.AreaClass;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StructureHandler;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.fo.properties.FOPropertyMapping;
import org.apache.fop.fo.flow.Marker;

// Java
import java.util.Iterator;
import org.xml.sax.Attributes;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

/**
 * base class for representation of formatting objects and their processing
 */
public class FObj extends FONode {
    private final static String FO_URI = "http://www.w3.org/1999/XSL/Format";

    /**
     * Static property list builder that converts xml attributes
     * into fo properties. This is static since the underlying
     * property mappings for fo are also static.
     */
    protected static PropertyListBuilder plb = null;

    /**
     * Structure handler used to notify structure events
     * such as start end element.
     */
    protected StructureHandler structHandler;

    /**
     * Formatting properties for this fo element.
     */
    public PropertyList properties;

    /**
     * Property manager for handler some common properties.
     */
    protected PropertyManager propMgr;

    /**
     * Id of this fo element of null if no id.
     */
    protected String id = null;

    /**
     * The children of this node.
     */
    protected ArrayList children = null;

    /**
     * Markers added to this element.
     */
    protected Map markers = null;

    /**
     * Create a new formatting object.
     * All formatting object classes extend this class.
     *
     * @param parent the parent node
     */
    public FObj(FONode parent) {
        super(parent);
    }

    /**
     * Set the name of this element.
     * The prepends "fo:" to the name to indicate it is in the fo namespace.
     *
     * @param str the xml element name
     */
    public void setName(String str) {
        name = "fo:" + str;
    }

    protected PropertyListBuilder getListBuilder() {
        if (plb == null) {
            plb = new PropertyListBuilder();
            plb.addList(FOPropertyMapping.getGenericMappings());

            for (Iterator iter =
                      FOPropertyMapping.getElementMappings().iterator();
                    iter.hasNext();) {
                String elem = (String) iter.next();
                plb.addElementList(elem,
                                   FOPropertyMapping.getElementMapping(elem));
            }
        }
        return plb;
    }

    /**
     * Handle the attributes for this element.
     * The attributes must be used immediately as the sax attributes
     * will be altered for the next element.
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        FONode par = parent;
        while (par != null && !(par instanceof FObj)) {
            par = par.parent;
        }
        PropertyList props = null;
        if (par != null) {
            props = ((FObj) par).properties;
        }
        properties = getListBuilder().makeList(FO_URI, name, attlist, props,
                                               (FObj) par);
        properties.setFObj(this);
        this.propMgr = makePropertyManager(properties);
        setWritingMode();
    }

    protected PropertyManager makePropertyManager(
      PropertyList propertyList) {
        return new PropertyManager(propertyList);
    }

    /**
     * Add the child to this object.
     *
     * @param child the child node to add
     */
    protected void addChild(FONode child) {
        if (containsMarkers() && child.isMarker()) {
            addMarker((Marker)child);
        } else {
            if (children == null) {
                children = new ArrayList();
            }
            children.add(child);
        }
    }

    /**
     * Set the structure handler for handling structure events.
     *
     * @param st the structure handler
     */
    public void setStructHandler(StructureHandler st) {
        structHandler = st;
    }

    /**
     * lets outside sources access the property list
     * first used by PageNumberCitation to find the "id" property
     * @param name - the name of the desired property to obtain
     * @return the property
     */
    public Property getProperty(String name) {
        return (properties.get(name));
    }

    /**
     * Setup the id for this formatting object.
     * Most formatting objects can have an id that can be referenced.
     * This methods checks that the id isn't already used by another
     * fo and sets the id attribute of this object.
     */
    protected void setupID() {
        Property prop = this.properties.get("id");
        if (prop != null) {
            String str = prop.getString();
            if (str != null && !str.equals("")) {
                Set idrefs = structHandler.getIDReferences();
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
     * Check if this formatting object may contain markers.
     *
     * @return true if this can contian markers
     */
    protected boolean containsMarkers() {
        return false;
    }

    /**
     * Set writing mode for this FO.
     * Find nearest ancestor, including self, which generates
     * reference areas and use the value of its writing-mode property.
     * If no such ancestor is found, use the value on the root FO.
     */
    protected void setWritingMode() {
        FObj p;
        FONode parent;
        for (p = this; !p.generatesReferenceAreas() &&
                (parent = p.getParent()) != null &&
                (parent instanceof FObj); p = (FObj) parent) {
        }
        this.properties.setWritingMode(
          p.getProperty("writing-mode").getEnum());
    }

    /**
     * Return a LayoutManager responsible for laying out this FObj's content.
     * Must override in subclasses if their content can be laid out.
     * @param list the list to add the layout manager(s) to
     */
    public void addLayoutManager(List list) {
    }

    /**
     * Return an iterator over all the children of this FObj.
     * @return A ListIterator.
     */
    public ListIterator getChildren() {
        if (children != null) {
            return children.listIterator();
        }
        return null;
    }

    /**
     * Return an iterator over the object's children starting
     * at the pased node.
     * @param childNode First node in the iterator
     * @return A ListIterator or null if childNode isn't a child of
     * this FObj.
     */
    public ListIterator getChildren(FONode childNode) {
        if (children != null) {
            int i = children.indexOf(childNode);
            if (i >= 0) {
                return children.listIterator(i);
            }
        }
        return null;
    }

    /**
     * Add the marker to this formatting object.
     * If this object can contain markers it checks that the marker
     * has a unique class-name for this object and that it is
     * the first child.
     */
    public void addMarker(Marker marker) {
        String mcname = marker.getMarkerClassName();
        if (children != null) {
            // check for empty children
            for (Iterator iter = children.iterator(); iter.hasNext();) {
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
            getLogger().error("fo:marker 'marker-class-name' must be unique for same parent: " + mcname);
        }
    }

    public boolean hasMarkers() {
        return markers != null && !markers.isEmpty();
    }

    public Map getMarkers() {
        return markers;
    }

    /**
     * lets layout managers access FO properties via PropertyManager
     * @return the property manager for this FO
     */
    public PropertyManager getPropertyManager() {
        return this.propMgr;
    }

}

