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

package org.apache.fop.fo.pagination;

// Java
import java.util.ArrayList;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.apps.FOPException;

/**
 * Class modelling the fo:flow object. See Sec. 6.4.18 in the XSL-FO Standard.
 */
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
    private String flowName;

    /**
     * Content-width of current column area during layout
     */
    private int contentWidth;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Flow(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#handleAttrs
     * @param attlist Collection of attributes passed to us from the parser.
     * @throws FOPException if parent is not a page-sequence object
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        if (parent.getName().equals("fo:page-sequence")) {
            this.pageSequence = (PageSequence) parent;
        } else {
            throw new FOPException("flow must be child of "
                                 + "page-sequence, not " + parent.getName());
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
        setFlowName(getProperty(PR_FLOW_NAME).getString());
        // Now done in addChild of page-sequence
        //pageSequence.addFlow(this);

        getFOTreeControl().getFOInputHandler().startFlow(this);
    }

    /**
     * Tell the StructureRenderer that we are at the end of the flow.
     */
    public void end() {
        getFOTreeControl().getFOInputHandler().endFlow(this);
    }

    /**
     * @param name the name of the flow to set
     * @throws FOPException for an empty name
     */
    protected void setFlowName(String name) throws FOPException {
        if (name == null || name.equals("")) {
            throw new FOPException("A 'flow-name' is required for "
                         + getName());
        } else {
            flowName = name;
        }
    }

    /**
     * @return the name of this flow
     */
    public String getFlowName() {
        return flowName;
    }

    /**
     * @param contentWidth content width of this flow, in millipoints (??)
     */
    protected void setContentWidth(int contentWidth) {
        this.contentWidth = contentWidth;
    }
    /**
     * @return the content width of this flow (really of the region
     * in which it is flowing), in millipoints (??).
     */
    public int getContentWidth() {
        return this.contentWidth;
    }

    /**
     * @return true (Flow can generate reference areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveFlow(this);
    }
}
