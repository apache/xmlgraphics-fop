/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.svg;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import org.apache.batik.dom.svg.*;

import org.w3c.dom.svg.*;
import org.w3c.dom.*;

import java.util.*;

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public abstract class SVGObj extends FObj {

    String tagName = "";
    String[] props = {};
    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public SVGObj(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
    }

    protected static Hashtable ns = new Hashtable();

    public void addGraphic(Document doc, Element parent) {
        Element element = doc.createElementNS("http://www.w3.org/2000/svg",
                                              tagName);
        //        Element element = doc.createElement(tagName);
        for (int count = 0; count < props.length; count++) {
            if (this.properties.get(props[count]) != null) {
                String rf = this.properties.get(props[count]).getString();
                if (rf != null) {
                    if (props[count].indexOf(":") == -1) {
                        element.setAttribute(props[count], rf);
                    } else {
                        String pref = props[count].substring(0,
                                                             props[count].indexOf(":"));
                        System.out.println(pref);
                        if (pref.equals("xmlns")) {
                            ns.put(props[count].substring(
                                     props[count].indexOf(":") + 1), rf);
                            System.out.println(ns);
                        }
                        ns.put("xlink", "http://www.w3.org/1999/xlink");
                        element.setAttributeNS((String) ns.get(pref),
                                               props[count], rf);
                    }
                }
            }
        }
        parent.appendChild(element);
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            Object child = children.elementAt(i);
            if (child instanceof SVGObj) {
                ((SVGObj) child).addGraphic(doc, element);
            } else if (child instanceof String) {
                org.w3c.dom.Text text = doc.createTextNode((String) child);
                element.appendChild(text);
            }
        }
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     * @return the status of the layout
     */
    public Status layout(Area area) throws FOPException {
        /* generate a warning */
        System.err.println("WARNING: " + this.name + " outside svg:svg");

        /* return status */
        return new Status(Status.OK);
    }
}

