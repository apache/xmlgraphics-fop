/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.pagination.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.FlowLayoutManager;

// Java
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

public class Flow extends FObj {

    /**
     * PageSequence container
     */
    private PageSequence pageSequence;

    /**
     * ArrayList to store snapshot
     */
    private ArrayList markerSnapshot;

    /**
     * flow-name attribute
     */
    private String _flowName;

    /**
     * Content-width of current column area during layout
     */
    private int contentWidth;


    public Flow(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        if (parent.getName().equals("fo:page-sequence")) {
            this.pageSequence = (PageSequence) parent;
        } else {
            throw new FOPException("flow must be child of " +
                                   "page-sequence, not " + parent.getName());
        }
        // according to communication from Paul Grosso (XSL-List,
        // 001228, Number 406), confusion in spec section 6.4.5 about
        // multiplicity of fo:flow in XSL 1.0 is cleared up - one (1)
        // fo:flow per fo:page-sequence only.

        /*        if (pageSequence.isFlowSet()) {
                    if (this.name.equals("fo:flow")) {
                        throw new FOPException("Only a single fo:flow permitted"
                                               + " per fo:page-sequence");
                    } else {
                        throw new FOPException(this.name
                                               + " not allowed after fo:flow");
                    }
                }
         */
        setFlowName(getProperty("flow-name").getString());
        // Now done in addChild of page-sequence
        //pageSequence.addFlow(this);

        structHandler.startFlow(this);
    }

    public void end() {
        structHandler.endFlow(this);
    }

    protected void setFlowName(String name) throws FOPException {
        if (name == null || name.equals("")) {
            throw new FOPException("A 'flow-name' is required for " +
                                   getName());
        } else {
            _flowName = name;
        }
    }

    public String getFlowName() {
        return _flowName;
    }

    protected void setContentWidth(int contentWidth) {
        this.contentWidth = contentWidth;
    }
    /**
     * Return the content width of this flow (really of the region
     * in which it is flowing).
     */
    public int getContentWidth() {
        return this.contentWidth;
    }

    public boolean generatesReferenceAreas() {
        return true;
    }

    public void addLayoutManager(List list) {
        FlowLayoutManager lm = new FlowLayoutManager(this);
        lm.setUserAgent(getUserAgent());
        list.add(lm);
    }

}
