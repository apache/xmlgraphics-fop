/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.ColorType;

// Java
import java.util.Enumeration;
import java.awt.Rectangle;

public class BasicLink extends FObjMixed {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new BasicLink(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new BasicLink.Maker();
    }

    public BasicLink(FObj parent,
                     PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = "fo:basic-link";

        if (parent.getName().equals("fo:flow")) {
            throw new FOPException("basic-link can't be directly"
                                   + " under flow");
        }
    }

    public Status layout(Area area) throws FOPException {
        String destination;
        int linkType;

        if (!(destination =
                this.properties.get("internal-destination").getString()).equals("")) {
            linkType = LinkSet.INTERNAL;
        } else if (!(destination =
        this.properties.get("external-destination").getString()).equals("")) {
            linkType = LinkSet.EXTERNAL;
        } else {
            throw new FOPException("internal-destination or external-destination must be specified in basic-link");
        }

        if (this.marker == START) {
            // initialize id
            String id = this.properties.get("id").getString();
            area.getIDReferences().initializeID(id, area);
            this.marker = 0;
        }

        // new LinkedArea to gather up inlines
        LinkSet ls = new LinkSet(destination, area, linkType);

        Page p = area.getPage();

        AreaContainer ac = p.getBody().getCurrentColumnArea();
        if (ac == null) {
            throw new FOPException("Couldn't get ancestor AreaContainer when processing basic-link");
        }

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.elementAt(i);
            fo.setLinkSet(ls);

            Status status;
            if ((status = fo.layout(area)).isIncomplete()) {
                this.marker = i;
                return status;
            }
        }

        ls.applyAreaContainerOffsets(ac, area);

        // pass on command line
        String mergeLinks = System.getProperty("links.merge");
        if ((null != mergeLinks) &&!mergeLinks.equalsIgnoreCase("no")) {
            ls.mergeLinks();
        }

        p.addLinkSet(ls);

        return new Status(Status.OK);
    }

}
