/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.apps.FOPException;

import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
 * The outline object for the pdf bookmark extension.
 * The outline element contains a label and optionally more outlines.
 */
public class Outline extends ExtensionObj {
    private Label label;
    private ArrayList outlines = new ArrayList();

    private String internalDestination;
    private String externalDestination;

    /**
     * Create a new outline object.
     *
     * @param parent the parent fo node
     */
    public Outline(FONode parent) {
        super(parent);
    }

    /**
     * The attribues on the outline object are the internal and external
     * destination. One of these is required.
     *
     * @param attlist the attribute list
     * @throws FOPException a fop exception if there is an error
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        internalDestination =
            attlist.getValue("internal-destination");
        externalDestination =
            attlist.getValue("external-destination");
        if (externalDestination != null && !externalDestination.equals("")) {
            getLogger().warn("fox:outline external-destination not supported currently.");
        }

        if (internalDestination == null || internalDestination.equals("")) {
            getLogger().warn("fox:outline requires an internal-destination.");
        }

    }

    /**
     * Add the child to this outline.
     * This checks for the type, label or outline and handles appropriately.
     *
     * @param obj the child object
     */
    protected void addChild(FONode obj) {
        if (obj instanceof Label) {
            label = (Label)obj;
        } else if (obj instanceof Outline) {
            outlines.add(obj);
        }
    }

    /**
     * Get the bookmark data for this outline.
     * This creates a bookmark data with the destination
     * and adds all the data from child outlines.
     *
     * @return the new bookmark data
     */
    public BookmarkData getData() {
        BookmarkData data = new BookmarkData(internalDestination);
        data.setLabel(getLabel());
        for (int count = 0; count < outlines.size(); count++) {
            Outline out = (Outline)outlines.get(count);
            data.addSubData(out.getData());
        }
        return data;
    }

    /**
     * Get the label string.
     * This gets the label string from the child label element.
     *
     * @return the label string or empty if not found
     */
    public String getLabel() {
        return label == null ? "" : label.toString();
    }

}

