/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.apps.FOPException;

import java.util.*;

import org.xml.sax.Attributes;

public class Outline extends ExtensionObj {
    private Label label;
    private ArrayList outlines = new ArrayList();

    private String internalDestination;
    private String externalDestination;

    public Outline(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        internalDestination =
            attlist.getValue("internal-destination");
        externalDestination =
            attlist.getValue("external-destination");
        if (externalDestination != null &&!externalDestination.equals("")) {
            log.warn("fox:outline external-destination not supported currently.");
        }

        if (internalDestination == null || internalDestination.equals("")) {
            log.warn("fox:outline requires an internal-destination.");
        }

    }

    protected void addChild(FONode obj) {
        if (obj instanceof Label) {
            label = (Label)obj;
        } else if (obj instanceof Outline) {
            outlines.add(obj);
        }
    }

    public BookmarkData getData() {
        BookmarkData data = new BookmarkData(internalDestination);
        data.setLabel(getLabel());
        for(int count = 0; count < outlines.size(); count++) {
            Outline out = (Outline)outlines.get(count);
            data.addSubData(out.getData());
        }
        return data;
    }

    public String getLabel() {
        return label == null ? "" : label.toString();
    }

}

